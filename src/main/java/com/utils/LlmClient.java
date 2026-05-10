package com.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * OpenAI-compatible LLM client with deterministic local fallback support.
 */
@Component
public class LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(LlmClient.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(90);
    private static final Duration SSL_HANDSHAKE_TIMEOUT = Duration.ofSeconds(20);
    private static final int MAX_TRANSIENT_RETRIES = 1;

    @Value("${llm.api.url:https://api.openai.com/v1}")
    private String apiUrl;

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.model:gpt-4o-mini}")
    private String model;

    @Value("${llm.api.provider:openai}")
    private String provider;

    @Value("${llm.api.anthropic-version:2023-06-01}")
    private String anthropicVersion;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .build();
    }

    private HttpClient buildHttpClient() {
        return HttpClient.create()
                .tcpConfiguration(tcpClient -> tcpClient.option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Long.valueOf(CONNECT_TIMEOUT.toMillis()).intValue())
                        .doOnConnected(connection -> connection
                                .addHandlerLast(new ReadTimeoutHandler(Long.valueOf(RESPONSE_TIMEOUT.getSeconds()).intValue()))
                                .addHandlerLast(new WriteTimeoutHandler(Long.valueOf(RESPONSE_TIMEOUT.getSeconds()).intValue()))))
                .secure(ssl -> ssl.sslContext(io.netty.handler.ssl.SslContextBuilder.forClient())
                        .defaultConfiguration(reactor.netty.tcp.SslProvider.DefaultConfigurationType.TCP)
                        .handshakeTimeout(SSL_HANDSHAKE_TIMEOUT));
    }

    public boolean hasRemoteConfig() {
        return StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(apiUrl)
                && !"local".equalsIgnoreCase(provider);
    }

    public String buildSystemPrompt(String favoriteGenres, String favoriteYears,
                                    String favoriteRegions, String moodPreferences) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是电影推荐助手 The Curator。请用中文回答，结合用户偏好给出电影推荐、推荐理由和可继续追问的问题。\n");
        sb.append("只输出适合聊天气泡展示的纯文本，不要使用 Markdown 语法，不要写标题、加粗、分隔线、代码块或项目符号。\n");
        sb.append("避免使用 1.、2.、-、* 等列表标记，改用自然语句和分段换行表达。\n");
        if (StringUtils.isNotBlank(favoriteGenres)) {
            sb.append("用户喜欢的类型: ").append(favoriteGenres).append("\n");
        }
        if (StringUtils.isNotBlank(favoriteYears)) {
            sb.append("用户偏好的年代: ").append(favoriteYears).append("\n");
        }
        if (StringUtils.isNotBlank(favoriteRegions)) {
            sb.append("用户偏好的地区: ").append(favoriteRegions).append("\n");
        }
        if (StringUtils.isNotBlank(moodPreferences)) {
            sb.append("用户观影心情: ").append(moodPreferences).append("\n");
        }
        return sb.toString();
    }

    public String buildColdStartSystemPrompt() {
        return "你是电影推荐助手 The Curator。请用中文逐步收集用户喜欢的电影类型、年代、地区、观影心情和观影频率。"
                + "只输出纯文本，不要使用 Markdown、标题、加粗、分隔线、代码块或列表标记。";
    }

    public Flux<String> streamChat(List<JSONObject> messages) {
        if (!hasRemoteConfig()) {
            return Flux.empty();
        }
        logger.info("Starting remote LLM stream provider={}, model={}, baseUrl={}", provider, model, apiUrl);
        if ("anthropic".equalsIgnoreCase(provider)) {
            return streamAnthropicChat(messages);
        }
        return streamOpenAiChat(messages);
    }

    public Mono<String> chat(List<JSONObject> messages) {
        if (!hasRemoteConfig()) {
            return Mono.empty();
        }
        logger.info("Starting remote LLM completion provider={}, model={}, baseUrl={}", provider, model, apiUrl);
        if ("anthropic".equalsIgnoreCase(provider)) {
            return anthropicChat(messages);
        }
        return openAiChat(messages);
    }

    private Flux<String> streamOpenAiChat(List<JSONObject> messages) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("max_tokens", 1000);

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(buildRemoteErrorMessage(
                                        "openai-compatible", "/chat/completions", response.rawStatusCode(), body))))
                .bodyToFlux(String.class)
                .retryWhen(errors -> retryTransientErrors(errors, "openai-compatible", "/chat/completions"))
                .flatMap(this::parseOpenAiChunk)
                .timeout(Duration.ofMinutes(2));
    }

    private Mono<String> openAiChat(List<JSONObject> messages) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("max_tokens", 1000);

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(buildRemoteErrorMessage(
                                        "openai-compatible", "/chat/completions", response.rawStatusCode(), body))))
                .bodyToMono(String.class)
                .flatMap(this::parseOpenAiResponse)
                .timeout(Duration.ofMinutes(2));
    }

    private Flux<String> streamAnthropicChat(List<JSONObject> messages) {
        JSONObject requestBody = buildAnthropicRequestBody(messages, true);
        return webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey.trim())
                .header("anthropic-version", StringUtils.defaultIfBlank(anthropicVersion, "2023-06-01"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(buildRemoteErrorMessage(
                                        "anthropic", "/v1/messages", response.rawStatusCode(), body))))
                .bodyToFlux(String.class)
                .retryWhen(errors -> retryTransientErrors(errors, "anthropic", "/v1/messages"))
                .flatMap(this::parseAnthropicChunk)
                .timeout(Duration.ofMinutes(2));
    }

    private Mono<String> anthropicChat(List<JSONObject> messages) {
        JSONObject requestBody = buildAnthropicRequestBody(messages, false);
        return webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey.trim())
                .header("anthropic-version", StringUtils.defaultIfBlank(anthropicVersion, "2023-06-01"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(buildRemoteErrorMessage(
                                        "anthropic", "/v1/messages", response.rawStatusCode(), body))))
                .bodyToMono(String.class)
                .flatMap(this::parseAnthropicResponse)
                .timeout(Duration.ofMinutes(2));
    }

    private JSONObject buildAnthropicRequestBody(List<JSONObject> messages, boolean stream) {
        String systemPrompt = "";
        JSONArray anthropicMessages = new JSONArray();
        if (messages != null) {
            for (JSONObject message : messages) {
                if (message == null) {
                    continue;
                }
                String role = message.getString("role");
                String content = message.getString("content");
                if ("system".equalsIgnoreCase(role)) {
                    if (StringUtils.isBlank(systemPrompt)) {
                        systemPrompt = content == null ? "" : content;
                    }
                    continue;
                }
                if ("user".equalsIgnoreCase(role) || "assistant".equalsIgnoreCase(role)) {
                    anthropicMessages.add(createMessage(role.toLowerCase(), content));
                }
            }
        }
        if (anthropicMessages.isEmpty()) {
            anthropicMessages.add(createMessage("user", "请根据用户偏好推荐电影。"));
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1000);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", anthropicMessages);
        requestBody.put("stream", stream);
        return requestBody;
    }

    private String buildRemoteErrorMessage(String providerName, String path, int statusCode, String responseBody) {
        return "Remote LLM request failed provider=" + providerName
                + " path=" + path
                + " status=" + statusCode
                + " responseBody=" + sanitizeResponseBody(responseBody);
    }

    private Flux<Long> retryTransientErrors(Flux<Throwable> errors, String providerName, String path) {
        return errors.zipWith(Flux.range(1, MAX_TRANSIENT_RETRIES + 1), (error, attempt) -> {
                    if (attempt <= MAX_TRANSIENT_RETRIES && isTransientNetworkError(error)) {
                        logger.warn("Retrying remote LLM stream after transient network error provider={} path={} attempt={} error={}",
                                providerName, path, attempt, error.toString());
                        return attempt;
                    }
                    throw propagate(error);
                })
                .flatMap(attempt -> Mono.delay(Duration.ofSeconds(1)));
    }

    private RuntimeException propagate(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

    private boolean isTransientNetworkError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TimeoutException
                    || current instanceof ConnectException
                    || current instanceof SSLException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String sanitizeResponseBody(String responseBody) {
        if (StringUtils.isBlank(responseBody)) {
            return "<empty>";
        }
        String sanitized = responseBody.replaceAll("(?i)(Bearer\\s+)[^\\s\"']+", "$1<redacted>")
                .replaceAll("(?i)(api[_-]?key[\\s:=\"']+)[^\\s\"',}]+", "$1<redacted>")
                .replaceAll("[\\r\\n\\t]+", " ")
                .trim();
        if (sanitized.length() > 500) {
            return sanitized.substring(0, 500) + "...<truncated>";
        }
        return sanitized;
    }

    private Mono<String> parseOpenAiResponse(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Mono.empty();
        }
        try {
            JSONObject json = JSONObject.parseObject(raw);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return Mono.empty();
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null || StringUtils.isBlank(message.getString("content"))) {
                return Mono.empty();
            }
            return Mono.just(message.getString("content"));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<String> parseAnthropicResponse(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Mono.empty();
        }
        try {
            JSONObject json = JSONObject.parseObject(raw);
            JSONArray content = json.getJSONArray("content");
            if (content == null || content.isEmpty()) {
                return Mono.empty();
            }
            for (int i = 0; i < content.size(); i++) {
                JSONObject item = content.getJSONObject(i);
                if (item != null && "text".equals(item.getString("type"))
                        && StringUtils.isNotBlank(item.getString("text"))) {
                    return Mono.just(item.getString("text"));
                }
            }
            return Mono.empty();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Flux<String> parseOpenAiChunk(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Flux.empty();
        }
        List<String> parts = new ArrayList<String>();
        int reasoningOnlyChunks = 0;
        int malformedChunks = 0;
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            String jsonStr = extractSseJsonPayload(line);
            if (StringUtils.isBlank(jsonStr)) {
                continue;
            }
            try {
                JSONObject json = JSONObject.parseObject(jsonStr);
                JSONArray choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                    if (delta != null && StringUtils.isNotBlank(delta.getString("content"))) {
                        parts.add(delta.getString("content"));
                    } else if (delta != null && StringUtils.isNotBlank(delta.getString("reasoning_content"))) {
                        reasoningOnlyChunks++;
                    }
                }
            } catch (Exception ignored) {
                malformedChunks++;
                // Ignore malformed provider chunks and continue streaming later chunks.
            }
        }
        if (logger.isDebugEnabled() && parts.isEmpty() && reasoningOnlyChunks > 0) {
            logger.debug("OpenAI-compatible stream chunk contained reasoning-only deltas count={} malformed={}",
                    reasoningOnlyChunks, malformedChunks);
        }
        return Flux.fromIterable(parts);
    }

    private Flux<String> parseAnthropicChunk(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Flux.empty();
        }
        List<String> parts = new ArrayList<String>();
        int malformedChunks = 0;
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            String jsonStr = extractSseJsonPayload(line);
            if (StringUtils.isBlank(jsonStr)) {
                continue;
            }
            try {
                JSONObject json = JSONObject.parseObject(jsonStr);
                if (!"content_block_delta".equals(json.getString("type"))) {
                    continue;
                }
                JSONObject delta = json.getJSONObject("delta");
                if (delta != null && "text_delta".equals(delta.getString("type"))
                        && StringUtils.isNotBlank(delta.getString("text"))) {
                    parts.add(delta.getString("text"));
                }
            } catch (Exception ignored) {
                malformedChunks++;
                // Ignore malformed provider chunks and continue streaming later chunks.
            }
        }
        if (logger.isDebugEnabled() && malformedChunks > 0) {
            logger.debug("Anthropic stream chunk contained malformed deltas count={}", malformedChunks);
        }
        return Flux.fromIterable(parts);
    }

    static String extractSseJsonPayload(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (StringUtils.isBlank(trimmed)) {
            return "";
        }
        String jsonStr;
        if (trimmed.startsWith("data:")) {
            jsonStr = trimmed.substring(5).trim();
        } else if (trimmed.startsWith("{")) {
            jsonStr = trimmed;
        } else {
            return "";
        }
        if ("[DONE]".equals(jsonStr)) {
            return "";
        }
        return jsonStr;
    }

    public JSONObject createMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    public List<JSONObject> buildMessages(String systemPrompt, List<JSONObject> conversationHistory) {
        List<JSONObject> messages = new ArrayList<JSONObject>();
        messages.add(createMessage("system", systemPrompt));
        if (conversationHistory != null) {
            messages.addAll(conversationHistory);
        }
        return messages;
    }
}
