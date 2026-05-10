package com.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.AiConversationDao;
import com.dao.AiMessageDao;
import com.dao.AiRecommendationFeedbackDao;
import com.dao.AiUserPreferenceDao;
import com.entity.AiConversationEntity;
import com.entity.AiMessageEntity;
import com.entity.AiRecommendationFeedbackEntity;
import com.entity.AiUserPreferenceEntity;
import com.entity.vo.AppMovieListVO;
import com.service.AiChatService;
import com.service.AppMovieService;
import com.utils.LlmClient;
import com.utils.PageUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI chat service implementation.
 */
@Service("aiChatService")
public class AiChatServiceImpl extends ServiceImpl<AiConversationDao, AiConversationEntity> implements AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private static final int DEFAULT_RECOMMEND_LIMIT = 3;
    private static final int RECOMMENDATION_FETCH_MULTIPLIER = 4;
    private static final int MAX_CONVERSATION_TITLE_LENGTH = 16;
    private static final String DEFAULT_CONVERSATION_TITLE = "新对话";
    private static final String DEFAULT_CONVERSATION_TITLE_EN = "New Chat";
    private static final List<String> TITLE_PREFIX_FILLERS = Arrays.asList(
            "今晚想看", "今天想看", "请帮我推荐", "帮我推荐", "给我推荐", "推荐一部",
            "请帮我", "请推荐", "帮我", "给我", "我想看", "我想找", "我想要",
            "想看", "想找", "想要", "今晚", "今天", "最近", "周末", "假期",
            "来点", "有没有", "一部", "一个"
    );
    private static final List<String> TITLE_SUFFIX_FILLERS = Arrays.asList(
            "一下", "一些"
    );
    private static final Pattern TITLE_DESCRIPTOR_CONNECTOR_PATTERN = Pattern.compile(
            "(?<=[\\u4e00-\\u9fa5A-Za-z0-9])的(?=[\\u4e00-\\u9fa5A-Za-z0-9]*(电影|影片|片子|片)$)"
    );

    private static final int ANSWER_LOG_PREVIEW_LIMIT = 300;
    private static final String LIULANGDIQIU2_POSTER_PATH = "upload/movie_liulangdiqiu2.jpg";
    private static final List<String> KNOWN_GENRES = Arrays.asList(
            "动作", "喜剧", "剧情", "悬疑", "爱情", "科幻", "惊悚", "动画",
            "纪录片", "冒险", "犯罪", "战争", "奇幻", "家庭", "青春", "武侠"
    );
    private static final List<String> KNOWN_REGIONS = Arrays.asList(
            "中国大陆", "中国香港", "中国台湾", "美国", "日本", "韩国", "英国", "法国", "欧洲"
    );
    private static final List<String> KNOWN_MOODS = Arrays.asList(
            "放松", "热血", "浪漫", "烧脑", "治愈", "怀旧", "轻松", "紧张"
    );

    private static final Pattern LOG_SECRET_PATTERN = Pattern.compile(
            "(?i)(api[-_ ]?key|token|authorization|bearer|llm[-_ ]?api[-_ ]?key|x[-_ ]?api[-_ ]?key)\\s*[:=]\\s*[^\\s,;]+"
    );
    private static final Pattern PERSON_TOKEN_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5A-Za-z0-9]{2,12})(导演|主演|演员)");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19\\d{2}|20\\d{2})");
    private static final Pattern MOVIE_DETAIL_INTENT_PATTERN = Pattern.compile(
            "(查看|看看|打开|跳转|进入|去)(.{1,24}?)(详情|详情页|介绍|电影详情)"
    );

    @Autowired
    private AiConversationDao conversationDao;

    @Autowired
    private AiMessageDao messageDao;

    @Autowired
    private AiUserPreferenceDao userPreferenceDao;

    @Autowired
    private AiRecommendationFeedbackDao feedbackDao;

    @Autowired
    private AppMovieService appMovieService;

    @Autowired
    private LlmClient llmClient;

    @Override
    public AiConversationEntity createConversation(Long userId, String title, Integer isColdStart) {
        AiConversationEntity conversation = new AiConversationEntity();
        conversation.setUserId(userId);
        conversation.setTitle(StringUtils.isBlank(title) ? DEFAULT_CONVERSATION_TITLE : title.trim());
        conversation.setStatus("active");
        conversation.setIsColdStart(isColdStart == null ? 0 : isColdStart);
        conversation.setIsDeleted(0);
        conversation.setCreatedAt(new Date());
        conversation.setUpdatedAt(new Date());
        conversationDao.insert(conversation);
        return conversation;
    }

    @Override
    public List<AiConversationEntity> listConversations(Long userId) {
        EntityWrapper<AiConversationEntity> wrapper = new EntityWrapper<AiConversationEntity>();
        wrapper.eq("user_id", userId).eq("is_deleted", 0).orderBy("updated_at", false);
        return conversationDao.selectList(wrapper);
    }

    @Override
    public List<AiMessageEntity> getMessages(Long conversationId, Long userId) {
        if (!ownsConversation(conversationId, userId)) {
            return new ArrayList<AiMessageEntity>();
        }
        EntityWrapper<AiMessageEntity> wrapper = new EntityWrapper<AiMessageEntity>();
        wrapper.eq("conversation_id", conversationId).orderBy("created_at", true).orderBy("id", true);
        List<AiMessageEntity> messages = messageDao.selectList(wrapper);
        enrichMessageMetadata(messages, userId);
        return messages;
    }

    @Override
    public Flux<String> streamChat(Long conversationId, String userMessage, Long userId) {
        AiConversationEntity conversation = conversationDao.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId()) || Integer.valueOf(1).equals(conversation.getIsDeleted())) {
            return Flux.just("无权访问该对话。");
        }

        String cleanMessage = StringUtils.defaultString(userMessage).trim();
        if (StringUtils.isBlank(cleanMessage)) {
            return Flux.just("请先告诉我你想看的电影类型、心情或关键词。");
        }

        saveMessage(conversationId, "user", cleanMessage, null, null);
        conversation.setUpdatedAt(new Date());
        conversation.setTitle(resolveConversationTitle(conversation.getTitle(), cleanMessage));
        conversationDao.updateById(conversation);

        List<Map<String, Object>> intentActions = resolveIntentActions(cleanMessage, userId);
        if (intentActions != null && !intentActions.isEmpty()) {
            String answer = buildIntentActionReply(intentActions);
            saveMessage(conversationId, "assistant", answer, null, null);
            logger.info("Short-circuited AI chat intent navigation: conversationId={}, userId={}, actionLabel={}",
                    conversationId, userId, intentActions.get(0).get("label"));
            return Flux.just(answer);
        }

        AiUserPreferenceEntity preference = getUserPreference(userId);
        List<Map<String, Object>> recommendationCandidates = getRecommendedMovieMaps(
                userId, DEFAULT_RECOMMEND_LIMIT, cleanMessage, preference
        );
        String systemPrompt = Integer.valueOf(1).equals(conversation.getIsColdStart())
                ? llmClient.buildColdStartSystemPrompt()
                : buildRecommendationSystemPrompt(preference, recommendationCandidates);
        List<JSONObject> history = buildHistory(conversationId);
        List<JSONObject> messages = llmClient.buildMessages(systemPrompt, history);
        String fallbackAnswer = buildLocalRecommendationAnswer(userId, cleanMessage, preference, recommendationCandidates);
        StringBuilder fullResponse = new StringBuilder();
        AtomicBoolean persisted = new AtomicBoolean(false);
        AtomicBoolean streamedRemoteContent = new AtomicBoolean(false);
        AtomicReference<String> answerSource = new AtomicReference<String>(llmClient.hasRemoteConfig() ? "remote_stream" : "local_fallback");

        Flux<String> responseFlux;
        if (llmClient.hasRemoteConfig()) {
            Flux<String> remoteFallbackFlux = remoteCompletionFallback(messages, fallbackAnswer, answerSource);
            responseFlux = llmClient.streamChat(messages)
                    .filter(StringUtils::isNotBlank)
                    .doOnNext(chunk -> streamedRemoteContent.set(true))
                    .onErrorResume(e -> {
                        logger.warn("Remote LLM stream failed: {}", e.getMessage());
                        return streamedRemoteContent.get() ? Flux.empty() : remoteFallbackFlux;
                    })
                    .switchIfEmpty(Mono.defer(() -> streamedRemoteContent.get() ? Mono.empty() : remoteFallbackFlux.next()));
        } else {
            logger.info("Remote LLM config is missing or local provider is selected; using local fallback.");
            responseFlux = Flux.just(fallbackAnswer);
        }

        return responseFlux.doOnNext(fullResponse::append).doFinally(signalType -> {
            if (persisted.compareAndSet(false, true)) {
                String answer = fullResponse.length() == 0 ? fallbackAnswer : fullResponse.toString();
                if (fullResponse.length() == 0) {
                    answerSource.set("local_fallback");
                }
                Map<String, Object> boundMovie = resolveBoundMovie(answer, recommendationCandidates);
                Long movieId = parseLong(boundMovie.get("id"));
                String reason = boundMovie.get("recommendationReason") == null ? null : String.valueOf(boundMovie.get("recommendationReason"));
                saveMessage(conversationId, "assistant", answer, movieId, reason);
                logger.info("Persisted AI assistant answer: conversationId={}, userId={}, source={}, answerLength={}, preview={}",
                        conversationId, userId, answerSource.get(), answer.length(), buildAnswerLogPreview(answer));
            }
        });
    }

    private Flux<String> remoteCompletionFallback(List<JSONObject> messages, String fallbackAnswer, AtomicReference<String> answerSource) {
        return llmClient.chat(messages)
                .filter(StringUtils::isNotBlank)
                .doOnNext(answer -> answerSource.set("remote_completion"))
                .flux()
                .onErrorResume(nonStreamError -> {
                    logger.warn("Remote LLM completion failed, using local fallback: {}", nonStreamError.getMessage());
                    answerSource.set("local_fallback");
                    return Flux.just(fallbackAnswer);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    answerSource.set("local_fallback");
                    return fallbackAnswer;
                }).flux());
    }

    private String resolveConversationTitle(String currentTitle, String firstUserMessage) {
        String normalizedCurrentTitle = StringUtils.trimToEmpty(currentTitle);
        if (!DEFAULT_CONVERSATION_TITLE.equals(normalizedCurrentTitle)
                && !DEFAULT_CONVERSATION_TITLE_EN.equals(normalizedCurrentTitle)) {
            return currentTitle;
        }
        return generateConversationTitle(firstUserMessage);
    }

    private String generateConversationTitle(String firstUserMessage) {
        String sanitized = sanitizeConversationTitleSource(firstUserMessage);
        if (StringUtils.isBlank(sanitized)) {
            return fallbackConversationTitle(firstUserMessage);
        }

        String normalized = normalizeConversationTitleBody(sanitized);
        normalized = sanitizeConversationTitleSource(normalized);
        if (StringUtils.isBlank(normalized)) {
            return fallbackConversationTitle(firstUserMessage);
        }

        String noun = "";
        if (normalized.endsWith("片子")) {
            normalized = StringUtils.removeEnd(normalized, "片子");
            noun = "片";
        } else if (normalized.endsWith("片儿")) {
            normalized = StringUtils.removeEnd(normalized, "片儿");
            noun = "片";
        } else if (normalized.endsWith("影片")) {
            normalized = StringUtils.removeEnd(normalized, "影片");
            noun = "电影";
        } else if (normalized.endsWith("电影")) {
            normalized = StringUtils.removeEnd(normalized, "电影");
            noun = "电影";
        } else if (normalized.endsWith("片")) {
            normalized = StringUtils.removeEnd(normalized, "片");
            noun = "片";
        } else if (containsMovieDemand(firstUserMessage)) {
            noun = "电影";
        }
        normalized = sanitizeConversationTitleSource(normalized);
        if (StringUtils.isBlank(normalized) && StringUtils.isBlank(noun)) {
            return fallbackConversationTitle(firstUserMessage);
        }

        StringBuilder title = new StringBuilder(StringUtils.defaultString(normalized));
        title.append(noun);
        if (containsExplicitRecommendationIntent(firstUserMessage)) {
            title.append("推荐");
        }
        String compactTitle = sanitizeConversationTitleSource(title.toString());
        if (StringUtils.isBlank(compactTitle)) {
            return fallbackConversationTitle(firstUserMessage);
        }
        return compactTitle.length() > MAX_CONVERSATION_TITLE_LENGTH
                ? compactTitle.substring(0, MAX_CONVERSATION_TITLE_LENGTH)
                : compactTitle;
    }

    private String sanitizeConversationTitleSource(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value.replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .replace('？', ' ')
                .replace('?', ' ')
                .replace('！', ' ')
                .replace('!', ' ')
                .replace('。', ' ')
                .replace('.', ' ')
                .replace('，', ' ')
                .replace(',', ' ')
                .replace('；', ' ')
                .replace(';', ' ')
                .replace('：', ' ')
                .replace(':', ' ')
                .replace('、', ' ')
                .replace('“', ' ')
                .replace('”', ' ')
                .replace('"', ' ')
                .replace('《', ' ')
                .replace('》', ' ')
                .replace('（', ' ')
                .replace('）', ' ')
                .replace('(', ' ')
                .replace(')', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return StringUtils.trimToNull(sanitized);
    }

    private String stripConversationPrefix(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String result = value;
        String previous;
        do {
            previous = result;
            for (String filler : TITLE_PREFIX_FILLERS) {
                result = StringUtils.removeStart(result, filler);
            }
            result = StringUtils.trimToEmpty(result);
        } while (!StringUtils.equals(previous, result));
        return result;
    }

    private String normalizeConversationTitleBody(String value) {
        String normalized = stripConversationPrefix(value);
        normalized = normalized.replace("节奏紧凑", "紧凑")
                .replace("剧情紧凑", "紧凑");
        normalized = stripTitleSuffixFiller(normalized);
        normalized = normalized.replace("那种", "")
                .replace("这种", "")
                .replace("看看", "")
                .replace(" ", "");
        normalized = TITLE_DESCRIPTOR_CONNECTOR_PATTERN.matcher(normalized).replaceAll("");
        return StringUtils.trimToEmpty(normalized);
    }

    private String stripTitleSuffixFiller(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String result = value;
        String previous;
        do {
            previous = result;
            for (String filler : TITLE_SUFFIX_FILLERS) {
                result = StringUtils.removeEnd(result, filler);
            }
            result = StringUtils.trimToEmpty(result);
        } while (!StringUtils.equals(previous, result));
        return result;
    }

    private boolean containsExplicitRecommendationIntent(String message) {
        String normalized = StringUtils.defaultString(message);
        return normalized.contains("推荐");
    }

    private boolean containsMovieDemand(String message) {
        String normalized = StringUtils.defaultString(message);
        return normalized.contains("电影") || normalized.contains("影片") || normalized.contains("片子") || normalized.contains("片");
    }

    private String fallbackConversationTitle(String originalMessage) {
        String fallback = sanitizeConversationTitleSource(originalMessage);
        if (StringUtils.isBlank(fallback)) {
            return DEFAULT_CONVERSATION_TITLE;
        }
        return fallback.length() > MAX_CONVERSATION_TITLE_LENGTH
                ? fallback.substring(0, MAX_CONVERSATION_TITLE_LENGTH)
                : fallback;
    }

    private String buildAnswerLogPreview(String answer) {
        String preview = StringUtils.defaultString(answer).replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        preview = LOG_SECRET_PATTERN.matcher(preview).replaceAll("$1=<redacted>");
        if (preview.length() <= ANSWER_LOG_PREVIEW_LIMIT) {
            return preview;
        }
        return preview.substring(0, ANSWER_LOG_PREVIEW_LIMIT) + "...<truncated>";
    }

    @Override
    public AiMessageEntity saveMessage(Long conversationId, String role, String content, Long movieId, String recommendationReason) {
        AiMessageEntity message = new AiMessageEntity();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setMovieId(movieId);
        message.setRecommendationReason(recommendationReason);
        message.setCreatedAt(new Date());
        messageDao.insert(message);
        return message;
    }

    @Override
    public AiUserPreferenceEntity getUserPreference(Long userId) {
        EntityWrapper<AiUserPreferenceEntity> wrapper = new EntityWrapper<AiUserPreferenceEntity>();
        wrapper.eq("user_id", userId);
        List<AiUserPreferenceEntity> list = userPreferenceDao.selectList(wrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        AiUserPreferenceEntity preference = new AiUserPreferenceEntity();
        preference.setUserId(userId);
        preference.setColdStartCompleted(0);
        preference.setCreatedAt(new Date());
        preference.setUpdatedAt(new Date());
        userPreferenceDao.insert(preference);
        return preference;
    }

    @Override
    public AiUserPreferenceEntity saveUserPreference(Long userId, String favoriteGenres, String favoriteYears,
                                                     String favoriteRegions, String moodPreferences,
                                                     String watchFrequency, String dislikedGenres,
                                                     boolean markColdStartCompleted) {
        AiUserPreferenceEntity preference = getUserPreference(userId);
        if (favoriteGenres != null) preference.setFavoriteGenres(favoriteGenres);
        if (favoriteYears != null) preference.setFavoriteYears(favoriteYears);
        if (favoriteRegions != null) preference.setFavoriteRegions(favoriteRegions);
        if (moodPreferences != null) preference.setMoodPreferences(moodPreferences);
        if (watchFrequency != null) preference.setWatchFrequency(watchFrequency);
        if (dislikedGenres != null) preference.setDislikedGenres(dislikedGenres);
        if (markColdStartCompleted) {
            preference.setColdStartCompleted(1);
            preference.setColdStartCompletedAt(new Date());
        }
        preference.setUpdatedAt(new Date());
        userPreferenceDao.updateById(preference);
        return preference;
    }

    @Override
    public AiRecommendationFeedbackEntity saveFeedback(Long userId, Long conversationId, Long messageId,
                                                        Long movieId, String feedbackType, String feedbackReason) {
        if (!ownsConversation(conversationId, userId)) {
            throw new IllegalArgumentException("无权访问该对话");
        }
        AiMessageEntity message = messageDao.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("反馈消息不存在");
        }
        if (!conversationId.equals(message.getConversationId())) {
            throw new IllegalArgumentException("反馈消息不属于当前对话");
        }
        if (!"assistant".equals(message.getRole())) {
            throw new IllegalArgumentException("只能对助手推荐消息提交反馈");
        }
        if (message.getMovieId() == null) {
            throw new IllegalArgumentException("该推荐消息未绑定电影，无法反馈");
        }
        if (!message.getMovieId().equals(movieId)) {
            throw new IllegalArgumentException("反馈电影与推荐消息不一致");
        }
        if (!isValidFeedbackType(feedbackType)) {
            throw new IllegalArgumentException("反馈类型无效");
        }
        EntityWrapper<AiRecommendationFeedbackEntity> wrapper = new EntityWrapper<AiRecommendationFeedbackEntity>();
        wrapper.eq("user_id", userId).eq("message_id", messageId).eq("movie_id", movieId);
        List<AiRecommendationFeedbackEntity> existing = feedbackDao.selectList(wrapper);
        AiRecommendationFeedbackEntity feedback = existing == null || existing.isEmpty()
                ? new AiRecommendationFeedbackEntity()
                : existing.get(0);
        feedback.setUserId(userId);
        feedback.setConversationId(conversationId);
        feedback.setMessageId(messageId);
        feedback.setMovieId(movieId);
        feedback.setFeedbackType(feedbackType);
        feedback.setFeedbackReason(feedbackReason);
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(new Date());
        }
        if (feedback.getId() == null) {
            feedbackDao.insert(feedback);
        } else {
            feedbackDao.updateById(feedback);
        }
        return feedback;
    }

    @Override
    public List<Map<String, Object>> getRecommendedMovies(Long userId, int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_RECOMMEND_LIMIT : limit;
        return getRecommendedMovieMaps(userId, safeLimit, null, getUserPreference(userId));
    }

    @Override
    public boolean isColdStartCompleted(Long userId) {
        AiUserPreferenceEntity preference = getUserPreference(userId);
        return preference != null && Integer.valueOf(1).equals(preference.getColdStartCompleted());
    }

    @Override
    public AiUserPreferenceEntity completeColdStart(Long userId, Map<String, Object> params) {
        return saveUserPreference(userId, stringValue(params, "favoriteGenres"), stringValue(params, "favoriteYears"),
                stringValue(params, "favoriteRegions"), stringValue(params, "moodPreferences"),
                stringValue(params, "watchFrequency"), stringValue(params, "dislikedGenres"), true);
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversationEntity conversation = conversationDao.selectById(conversationId);
        if (conversation != null && userId.equals(conversation.getUserId())) {
            conversation.setIsDeleted(1);
            conversation.setUpdatedAt(new Date());
            conversationDao.updateById(conversation);
        }
    }

    private boolean ownsConversation(Long conversationId, Long userId) {
        AiConversationEntity conversation = conversationDao.selectById(conversationId);
        return conversation != null && userId != null && userId.equals(conversation.getUserId())
                && !Integer.valueOf(1).equals(conversation.getIsDeleted());
    }

    private boolean isValidFeedbackType(String feedbackType) {
        return "like".equals(feedbackType)
                || "dislike".equals(feedbackType)
                || "watched".equals(feedbackType)
                || "want_watch".equals(feedbackType)
                || "not_interested".equals(feedbackType);
    }

    private List<JSONObject> buildHistory(Long conversationId) {
        EntityWrapper<AiMessageEntity> wrapper = new EntityWrapper<AiMessageEntity>();
        wrapper.eq("conversation_id", conversationId).orderBy("created_at", true).orderBy("id", true);
        List<AiMessageEntity> messages = messageDao.selectList(wrapper);
        List<JSONObject> history = new ArrayList<JSONObject>();
        if (messages != null) {
            int start = Math.max(0, messages.size() - 12);
            for (int i = start; i < messages.size(); i++) {
                AiMessageEntity message = messages.get(i);
                if ("system".equals(message.getRole())) {
                    continue;
                }
                history.add(llmClient.createMessage(message.getRole(), message.getContent()));
            }
        }
        return history;
    }

    private String buildRecommendationSystemPrompt(AiUserPreferenceEntity preference, List<Map<String, Object>> candidates) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(llmClient.buildSystemPrompt(
                preference == null ? null : preference.getFavoriteGenres(),
                preference == null ? null : preference.getFavoriteYears(),
                preference == null ? null : preference.getFavoriteRegions(),
                preference == null ? null : preference.getMoodPreferences()));
        prompt.append("回答时只输出纯文本，不要使用 Markdown 语法，不要写标题、加粗、分隔线、代码块。");
        prompt.append("避免使用 1.、2.、-、* 等列表标记，改用自然语句和少量换行保持可读性。\n");
        if (candidates == null || candidates.isEmpty()) {
            prompt.append("当前本地电影库没有候选影片。请说明暂时无法给出具体片名，并引导用户补充偏好。\n");
            return prompt.toString();
        }
        prompt.append("本轮只能推荐和解释以下本地候选影片，不要编造或推荐候选列表之外的电影。候选已按贴合度排序，第 1 部会绑定为推荐卡片，只有在它确实最贴合用户当前需求时才放在首位。\n");
        int index = 1;
        for (Map<String, Object> movie : candidates) {
            prompt.append(index++).append(". id=").append(movie.get("id"))
                    .append("，片名《").append(movie.get("title")).append("》")
                    .append("，类型=").append(defaultMovieText(movie.get("typeName")))
                    .append("，地区=").append(defaultMovieText(movie.get("regionName")))
                    .append("，理由=").append(movie.get("recommendationReason"))
                    .append("\n");
        }
        return prompt.toString();
    }

    private void enrichMessageMetadata(List<AiMessageEntity> messages, Long userId) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        AiUserPreferenceEntity preference = getUserPreference(userId);
        String latestUserMessage = null;
        for (AiMessageEntity message : messages) {
            if (message == null) {
                continue;
            }
            if ("user".equals(message.getRole())) {
                latestUserMessage = message.getContent();
                continue;
            }
            if ("assistant".equals(message.getRole())) {
                message.setIntentActions(resolveIntentActions(latestUserMessage, userId));
                List<Map<String, Object>> recommendationCandidates = getRecommendedMovieMaps(
                        userId, DEFAULT_RECOMMEND_LIMIT, latestUserMessage, preference
                );
                message.setRecommendedMovies(resolveRecommendedMovies(
                        latestUserMessage,
                        message.getContent(),
                        recommendationCandidates
                ));
            }
        }
    }

    private List<Map<String, Object>> resolveIntentActions(String userMessage, Long userId) {
        String normalizedMessage = StringUtils.defaultString(userMessage).trim();
        if (StringUtils.isBlank(normalizedMessage)) {
            return new ArrayList<Map<String, Object>>();
        }

        List<Map<String, Object>> actions = new ArrayList<Map<String, Object>>();
        if (containsAnyKeyword(normalizedMessage, "修改密码", "改密码", "重置密码", "修改一下密码")) {
            actions.add(buildNavigateAction("去修改密码", "/index/center", singletonQuery("section", "password")));
            return actions;
        }

        if (containsAnyKeyword(normalizedMessage,
                "修改头像", "换头像", "上传头像", "设置头像")) {
            actions.add(buildNavigateAction("去修改头像", "/index/center", singletonQuery("section", "profile")));
            return actions;
        }

        if (containsAnyKeyword(normalizedMessage,
                "修改资料", "编辑资料", "修改个人信息", "完善资料",
                "修改昵称", "修改姓名", "改手机号", "改联系方式", "改身份证", "修改个人资料")) {
            actions.add(buildNavigateAction("去个人资料", "/index/center", singletonQuery("section", "profile")));
            return actions;
        }

        if (containsAnyKeyword(normalizedMessage,
                "账号设置", "账户设置", "个人设置", "系统设置")) {
            actions.add(buildNavigateAction("去个人中心", "/index/center", singletonQuery("section", "profile")));
            return actions;
        }

        Map<String, Object> movieDetailAction = resolveMovieDetailAction(normalizedMessage, userId);
        if (!movieDetailAction.isEmpty()) {
            actions.add(movieDetailAction);
            return actions;
        }

        if (containsAnyKeyword(normalizedMessage, "首页", "回首页", "去首页", "主页")) {
            actions.add(buildNavigateAction("去首页", "/index/home", null));
        } else if (containsAnyKeyword(normalizedMessage, "电影页", "电影列表", "电影广场", "去电影", "看电影列表")) {
            actions.add(buildNavigateAction("去电影页", "/index/dianyingxinxi", null));
        } else if (containsAnyKeyword(normalizedMessage, "我的收藏", "收藏夹", "收藏页", "去收藏")) {
            actions.add(buildNavigateAction("去我的收藏", "/index/storeup", null));
        } else if (containsAnyKeyword(normalizedMessage, "个人中心", "个人主页", "我的主页", "我的资料", "账号中心")) {
            actions.add(buildNavigateAction("去个人中心", "/index/center", singletonQuery("section", "profile")));
        } else if (containsAnyKeyword(normalizedMessage, "登录", "去登录", "登录页", "重新登录")) {
            actions.add(buildNavigateAction("去登录", "/login", null));
        }
        return actions;
    }

    private String buildIntentActionReply(List<Map<String, Object>> intentActions) {
        if (intentActions == null || intentActions.isEmpty()) {
            return "可以，点击下面按钮继续。";
        }
        Object label = intentActions.get(0).get("label");
        String normalizedLabel = label == null ? "" : String.valueOf(label);
        if (normalizedLabel.contains("修改密码")) {
            return "可以，点击下面按钮进入修改密码页面。";
        }
        if (normalizedLabel.contains("修改头像")) {
            return "可以，点击下面按钮进入个人资料页面修改头像。";
        }
        if (normalizedLabel.contains("个人资料")) {
            return "可以，点击下面按钮进入个人资料页面。";
        }
        if (normalizedLabel.contains("首页")) {
            return "可以，点击下面按钮回到首页。";
        }
        if (normalizedLabel.contains("收藏")) {
            return "可以，点击下面按钮进入我的收藏。";
        }
        if (normalizedLabel.contains("登录")) {
            return "可以，点击下面按钮前往登录页。";
        }
        if (normalizedLabel.contains("个人中心")) {
            return "可以，点击下面按钮进入个人中心。";
        }
        if (normalizedLabel.contains("电影页")) {
            return "可以，点击下面按钮进入电影页。";
        }
        if (normalizedLabel.contains("详情")) {
            return "可以，点击下面按钮查看电影详情。";
        }
        return "可以，点击下面按钮继续。";
    }

    private Map<String, Object> resolveMovieDetailAction(String userMessage, Long userId) {
        String titleHint = extractMovieTitleHint(userMessage);
        if (StringUtils.isBlank(titleHint)) {
            return new HashMap<String, Object>();
        }
        List<AppMovieListVO> fuzzyMatches = queryMoviesByTitle(titleHint, false);
        List<AppMovieListVO> exactMatches = filterExactTitleMatches(fuzzyMatches, titleHint);
        if (exactMatches.size() == 1) {
            AppMovieListVO movie = exactMatches.get(0);
            return buildNavigateAction("查看《" + movie.getTitle() + "》详情",
                    "/index/dianyingxinxiDetail",
                    singletonQuery("id", movie.getId()));
        }
        if (!fuzzyMatches.isEmpty()) {
            return buildNavigateAction("去电影页", "/index/dianyingxinxi", null);
        }
        return new HashMap<String, Object>();
    }

    private List<AppMovieListVO> queryMoviesByTitle(String titleHint, boolean exactOnly) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", "1");
        params.put("limit", "10");
        params.put("title", titleHint);
        PageUtils page = appMovieService.queryFrontPage(params);
        List<?> rawList = page == null ? new ArrayList<Object>() : page.getList();
        List<AppMovieListVO> matchedMovies = new ArrayList<AppMovieListVO>();
        for (Object record : rawList) {
            if (!(record instanceof AppMovieListVO)) {
                continue;
            }
            AppMovieListVO movie = (AppMovieListVO) record;
            if (movie.getId() == null || StringUtils.isBlank(movie.getTitle())) {
                continue;
            }
            if (exactOnly) {
                String normalizedTitle = normalizeText(movie.getTitle());
                if (StringUtils.equals(normalizedTitle, normalizeText(titleHint))) {
                    matchedMovies.add(movie);
                }
            } else {
                matchedMovies.add(movie);
            }
        }
        return matchedMovies;
    }

    private List<AppMovieListVO> filterExactTitleMatches(List<AppMovieListVO> movies, String titleHint) {
        List<AppMovieListVO> exactMatches = new ArrayList<AppMovieListVO>();
        String normalizedHint = normalizeText(titleHint);
        if (movies == null) {
            return exactMatches;
        }
        for (AppMovieListVO movie : movies) {
            if (movie == null || StringUtils.isBlank(movie.getTitle())) {
                continue;
            }
            if (StringUtils.equals(normalizeText(movie.getTitle()), normalizedHint)) {
                exactMatches.add(movie);
            }
        }
        return exactMatches;
    }

    private String extractMovieTitleHint(String userMessage) {
        String normalized = StringUtils.defaultString(userMessage).trim();
        if (!containsAnyKeyword(normalized, "详情", "详情页", "介绍", "电影详情")) {
            return null;
        }
        Matcher bracketMatcher = Pattern.compile("《([^》]{1,24})》").matcher(normalized);
        if (bracketMatcher.find()) {
            return StringUtils.trimToNull(bracketMatcher.group(1));
        }
        Matcher matcher = MOVIE_DETAIL_INTENT_PATTERN.matcher(normalized);
        if (matcher.find()) {
            String hint = StringUtils.trimToNull(matcher.group(2));
            if (hint != null) {
                hint = hint.replace("这部", "").replace("那个", "").replace("一下", "").trim();
            }
            return StringUtils.trimToNull(hint);
        }
        return null;
    }

    private boolean containsAnyKeyword(String text, String... keywords) {
        if (StringUtils.isBlank(text) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.isNotBlank(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> singletonQuery(String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return null;
        }
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return query;
    }

    private Map<String, Object> buildNavigateAction(String label, String targetRoute, Map<String, Object> query) {
        Map<String, Object> action = new HashMap<String, Object>();
        action.put("actionType", "navigate");
        action.put("label", label);
        action.put("targetRoute", targetRoute);
        action.put("query", query);
        return action;
    }

    private String buildLocalRecommendationAnswer(Long userId, String userMessage, AiUserPreferenceEntity preference, int limit) {
        return buildLocalRecommendationAnswer(
                userId,
                userMessage,
                preference,
                getRecommendedMovieMaps(userId, limit, userMessage, preference)
        );
    }

    private String buildLocalRecommendationAnswer(Long userId, String userMessage, AiUserPreferenceEntity preference, List<Map<String, Object>> movies) {
        if (movies == null || movies.isEmpty()) {
            return "我已经记录了你的需求：" + userMessage + "。当前电影库暂时没有可推荐影片，你可以补充喜欢的类型、地区或年代，我会继续帮你筛选。";
        }
        StringBuilder answer = new StringBuilder();
        answer.append("结合你的历史偏好");
        if (preference != null && StringUtils.isNotBlank(preference.getFavoriteGenres())) {
            answer.append("（类型 ").append(preference.getFavoriteGenres()).append("）");
        }
        answer.append("，以及你这次提到的“").append(userMessage).append("”，我先给你 2-3 部更贴近当前诉求的候选：\n");
        for (int i = 0; i < movies.size(); i++) {
            Map<String, Object> movie = movies.get(i);
            if (i == 0) {
                answer.append("优先可以看《").append(movie.get("title")).append("》，")
                        .append(movie.get("recommendationReason")).append("\n");
            } else {
                answer.append("也可以看看《").append(movie.get("title")).append("》，")
                        .append(movie.get("recommendationReason")).append("\n");
            }
        }
        answer.append("如果你想更偏某个地区、导演、演员，或者想更轻松/更烧脑一点，继续告诉我，我会据此重排。");
        return answer.toString();
    }

    private List<Map<String, Object>> getRecommendedMovieMaps(Long userId, int limit, String userMessage, AiUserPreferenceEntity preference) {
        int safeLimit = limit <= 0 ? DEFAULT_RECOMMEND_LIMIT : limit;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", "1");
        params.put("limit", String.valueOf(Math.max(safeLimit * RECOMMENDATION_FETCH_MULTIPLIER, safeLimit)));
        params.put("sort", "totalScore");
        params.put("order", "desc");
        PageUtils page = appMovieService.queryFrontRecommendedPage(params, userId);
        List<?> records = page == null ? new ArrayList<Object>() : page.getList();
        Set<Long> positiveIds = new LinkedHashSet<Long>(feedbackDao.selectPositiveMovieIds(userId));
        Set<Long> negativeIds = new HashSet<Long>(feedbackDao.selectNegativeMovieIds(userId));
        RecommendationContext context = buildRecommendationContext(userMessage, preference);
        List<Map<String, Object>> ranked = new ArrayList<Map<String, Object>>();
        int sourceOrder = 0;
        for (Object record : records) {
            if (!(record instanceof AppMovieListVO)) {
                continue;
            }
            AppMovieListVO movie = (AppMovieListVO) record;
            if (movie.getId() == null || negativeIds.contains(movie.getId())) {
                continue;
            }
            RecommendationEvidence evidence = analyzeMovie(movie, context, positiveIds.contains(movie.getId()), sourceOrder++);
            ranked.add(mapMovie(movie, evidence));
        }
        Collections.sort(ranked, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                int scoreCompare = Double.compare(readCandidateScore(right), readCandidateScore(left));
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                int matchCompare = Integer.compare(readCandidateMatchCount(right), readCandidateMatchCount(left));
                if (matchCompare != 0) {
                    return matchCompare;
                }
                int movieScoreCompare = Double.compare(parseDouble(right.get("totalScore")), parseDouble(left.get("totalScore")));
                if (movieScoreCompare != 0) {
                    return movieScoreCompare;
                }
                return Integer.compare(readCandidateOrder(left), readCandidateOrder(right));
            }
        });
        return ranked.stream()
                .limit(safeLimit)
                .peek(this::removeCandidateSortFields)
                .collect(Collectors.toList());
    }

    private Map<String, Object> firstRecommendedMovie(List<Map<String, Object>> movies) {
        return movies == null || movies.isEmpty() ? new HashMap<String, Object>() : movies.get(0);
    }

    private Map<String, Object> resolveBoundMovie(String answer, List<Map<String, Object>> movies) {
        List<Map<String, Object>> recommendedMovies = resolveRecommendedMovies(null, answer, movies);
        if (recommendedMovies.isEmpty()) {
            return new HashMap<String, Object>();
        }
        Map<String, Object> firstMovie = recommendedMovies.get(0);
        return firstMovie == null ? new HashMap<String, Object>() : firstMovie;
    }

    private List<Map<String, Object>> resolveRecommendedMovies(String userMessage,
                                                               String answer,
                                                               List<Map<String, Object>> movies) {
        if (movies == null || movies.isEmpty()) {
            return new ArrayList<Map<String, Object>>();
        }
        if (!isRecommendationAnswer(userMessage, answer, movies)) {
            return new ArrayList<Map<String, Object>>();
        }

        String normalizedAnswer = normalizeText(answer);
        List<Map<String, Object>> orderedMovies = new ArrayList<Map<String, Object>>();
        Set<Long> seenMovieIds = new LinkedHashSet<Long>();
        for (Map<String, Object> movie : movies) {
            if (movie == null) {
                continue;
            }
            String title = String.valueOf(movie.get("title"));
            String normalizedTitle = normalizeText(title);
            if (StringUtils.isBlank(normalizedTitle)) {
                continue;
            }
            int index = normalizedAnswer.indexOf(normalizedTitle);
            if (index >= 0) {
                Map<String, Object> matchedMovie = new HashMap<String, Object>(movie);
                matchedMovie.put("_mentionIndex", index);
                orderedMovies.add(matchedMovie);
            }
        }
        Collections.sort(orderedMovies, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                Long leftIndex = parseLong(left.get("_mentionIndex"));
                Long rightIndex = parseLong(right.get("_mentionIndex"));
                return Long.compare(leftIndex == null ? Long.MAX_VALUE : leftIndex, rightIndex == null ? Long.MAX_VALUE : rightIndex);
            }
        });

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> movie : orderedMovies) {
            Long movieId = parseLong(movie.get("id"));
            if (movieId == null || seenMovieIds.contains(movieId)) {
                continue;
            }
            seenMovieIds.add(movieId);
            movie.remove("_mentionIndex");
            result.add(movie);
            if (result.size() >= DEFAULT_RECOMMEND_LIMIT) {
                return result;
            }
        }

        if (!result.isEmpty()) {
            return result;
        }

        Map<String, Object> fallbackMovie = firstRecommendedMovie(movies);
        if (!fallbackMovie.isEmpty()) {
            result.add(new HashMap<String, Object>(fallbackMovie));
        }
        return result;
    }

    private boolean isRecommendationAnswer(String userMessage,
                                           String answer,
                                           List<Map<String, Object>> movies) {
        String normalizedUserMessage = StringUtils.defaultString(userMessage).trim();
        String normalizedAnswer = StringUtils.defaultString(answer).trim();
        if (StringUtils.isBlank(normalizedAnswer)) {
            return false;
        }
        if (!resolveIntentActions(normalizedUserMessage, null).isEmpty()) {
            return false;
        }
        if (containsAnyKeyword(normalizedAnswer, "推荐", "候选", "可以看《", "也可以看看《", "优先可以看《")) {
            return true;
        }
        if (containsAnyKeyword(normalizedUserMessage, "推荐", "片单", "看什么", "候选")) {
            return true;
        }
        int matchedTitles = 0;
        String answerText = normalizeText(normalizedAnswer);
        for (Map<String, Object> movie : movies) {
            if (movie == null) {
                continue;
            }
            String title = normalizeText(String.valueOf(movie.get("title")));
            if (StringUtils.isBlank(title)) {
                continue;
            }
            if (answerText.contains(title)) {
                matchedTitles++;
                if (matchedTitles >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Object> mapMovie(AppMovieListVO movie, RecommendationEvidence evidence) {
        Map<String, Object> mapped = new HashMap<String, Object>();
        mapped.put("id", movie.getId());
        mapped.put("title", movie.getTitle());
        mapped.put("typeName", movie.getTypeName());
        mapped.put("posterUrl", selectPosterUrl(movie));
        mapped.put("posterUrls", movie.getPosterUrls());
        mapped.put("regionName", movie.getRegionName());
        mapped.put("releaseDate", movie.getReleaseDate());
        mapped.put("directorName", movie.getDirectorName());
        mapped.put("castNames", movie.getCastNames());
        mapped.put("synopsis", movie.getSynopsis());
        mapped.put("totalScore", movie.getTotalScore());
        mapped.put("recommendationReason", buildReason(movie, evidence));
        mapped.put("_candidateScore", evidence.getScore());
        mapped.put("_candidateMatchCount", evidence.getMatchCount());
        mapped.put("_candidateOrder", evidence.getSourceOrder());
        return mapped;
    }

    private String buildReason(AppMovieListVO movie, RecommendationEvidence evidence) {
        List<String> fragments = new ArrayList<String>();
        if (evidence != null) {
            fragments.addAll(evidence.getReasonParts());
        }
        if (fragments.isEmpty() && StringUtils.isNotBlank(movie.getDirectorName())) {
            fragments.add("导演是" + movie.getDirectorName());
        }
        if (fragments.isEmpty() && StringUtils.isNotBlank(movie.getCastNames())) {
            fragments.add("主演阵容包括" + shortenPeople(movie.getCastNames()));
        }
        if (fragments.isEmpty() && movie.getReleaseDate() != null) {
            fragments.add("上映时间在" + extractYear(movie.getReleaseDate()) + "年");
        }
        fragments.add("当前候选评分为 " + trimTrailingZero(movie.getTotalScore()));
        return StringUtils.join(fragments, "，") + "。";
    }

    private RecommendationContext buildRecommendationContext(String userMessage, AiUserPreferenceEntity preference) {
        RecommendationContext context = new RecommendationContext();
        context.addCurrentGenres(extractKnownTokens(userMessage, KNOWN_GENRES));
        context.addPreferenceGenres(filterKnownTokens(parseStoredPreference(preference == null ? null : preference.getFavoriteGenres()), KNOWN_GENRES));
        context.addCurrentRegions(extractRegionTokens(userMessage));
        context.addPreferenceRegions(filterKnownTokens(expandRegionAliases(parseStoredPreference(preference == null ? null : preference.getFavoriteRegions())), KNOWN_REGIONS));
        context.addCurrentMoods(extractMoodTokens(userMessage));
        context.addPreferenceMoods(filterKnownTokens(parseStoredPreference(preference == null ? null : preference.getMoodPreferences()), KNOWN_MOODS));
        context.addCurrentPersons(extractPersonTokens(userMessage));
        context.addCurrentYearHints(extractYearHints(userMessage));
        context.addPreferenceYearHints(parseStoredPreference(preference == null ? null : preference.getFavoriteYears()));
        return context;
    }

    private RecommendationEvidence analyzeMovie(AppMovieListVO movie,
                                                RecommendationContext context,
                                                boolean hasPositiveFeedback,
                                                int sourceOrder) {
        RecommendationEvidence evidence = new RecommendationEvidence();
        evidence.setSourceOrder(sourceOrder);
        double score = parseDouble(movie.getTotalScore()) * 0.32D - sourceOrder * 0.04D;
        if (hasPositiveFeedback) {
            score += 1.1D;
            evidence.addReason("你之前给过正向反馈，这次先保留在前排");
        }

        String searchableText = buildSearchableText(movie);
        Set<String> matchedCurrentGenres = matchTokens(searchableText, context.getCurrentGenres());
        if (!matchedCurrentGenres.isEmpty()) {
            score += 4.2D + matchedCurrentGenres.size() * 0.55D;
            evidence.addReason("类型上贴近你这次想看的" + StringUtils.join(matchedCurrentGenres, "、"));
            evidence.incrementMatchCount();
        }
        Set<String> matchedPreferenceGenres = subtractMatches(matchTokens(searchableText, context.getPreferenceGenres()), matchedCurrentGenres);
        if (!matchedPreferenceGenres.isEmpty()) {
            score += 0.9D + matchedPreferenceGenres.size() * 0.2D;
            evidence.addReason("也延续了你平时偏好的" + StringUtils.join(matchedPreferenceGenres, "、") + "口味");
        }

        Set<String> matchedCurrentRegions = matchTokens(searchableText, context.getCurrentRegions());
        if (!matchedCurrentRegions.isEmpty()) {
            score += 2.6D + matchedCurrentRegions.size() * 0.35D;
            evidence.addReason("地区方向贴近你提到的" + StringUtils.join(matchedCurrentRegions, "、"));
            evidence.incrementMatchCount();
        }
        Set<String> matchedPreferenceRegions = subtractMatches(matchTokens(searchableText, context.getPreferenceRegions()), matchedCurrentRegions);
        if (!matchedPreferenceRegions.isEmpty()) {
            score += 0.7D + matchedPreferenceRegions.size() * 0.15D;
            evidence.addReason("地区口味上也符合你常看的" + StringUtils.join(matchedPreferenceRegions, "、"));
        }

        Set<String> matchedPersons = matchTokens(searchableText, context.getCurrentPersons());
        if (!matchedPersons.isEmpty()) {
            score += 3.1D + matchedPersons.size() * 0.5D;
            evidence.addReason("人物偏好上命中了" + StringUtils.join(matchedPersons, "、"));
            evidence.incrementMatchCount();
        }

        Set<String> matchedCurrentMoods = matchMoodTokens(searchableText, context.getCurrentMoods());
        if (!matchedCurrentMoods.isEmpty()) {
            score += 2.1D + matchedCurrentMoods.size() * 0.3D;
            evidence.addReason("整体气质更偏" + StringUtils.join(matchedCurrentMoods, "、"));
            evidence.incrementMatchCount();
        }
        Set<String> matchedPreferenceMoods = subtractMatches(matchMoodTokens(searchableText, context.getPreferenceMoods()), matchedCurrentMoods);
        if (!matchedPreferenceMoods.isEmpty()) {
            score += 0.6D + matchedPreferenceMoods.size() * 0.12D;
            evidence.addReason("氛围上也贴着你平时偏好的" + StringUtils.join(matchedPreferenceMoods, "、"));
        }

        if (matchesYearHints(movie.getReleaseDate(), context.getCurrentYearHints())) {
            score += 1.9D;
            evidence.addReason("上映时间也落在你当前能接受的年代范围里");
            evidence.incrementMatchCount();
        } else if (matchesYearHints(movie.getReleaseDate(), context.getPreferenceYearHints())) {
            score += 0.5D;
            evidence.addReason("年代上也没有偏离你平时的选择");
        }

        if (evidence.getReasonParts().isEmpty()) {
            evidence.addReason(buildFallbackReason(movie));
        }
        evidence.setScore(score);
        return evidence;
    }

    private String buildSearchableText(AppMovieListVO movie) {
        return StringUtils.join(Arrays.asList(
                normalizeText(movie.getTitle()),
                normalizeText(movie.getTypeName()),
                normalizeText(movie.getRegionName()),
                normalizeText(movie.getDirectorName()),
                normalizeText(movie.getCastNames()),
                normalizeText(movie.getSynopsis())
        ), " ");
    }

    private String buildFallbackReason(AppMovieListVO movie) {
        List<String> fragments = new ArrayList<String>();
        if (StringUtils.isNotBlank(movie.getTypeName())) {
            fragments.add("类型覆盖" + movie.getTypeName());
        }
        if (StringUtils.isNotBlank(movie.getRegionName())) {
            fragments.add("地区是" + movie.getRegionName());
        }
        if (StringUtils.isNotBlank(movie.getDirectorName())) {
            fragments.add("由" + movie.getDirectorName() + "执导");
        }
        if (fragments.isEmpty()) {
            return "在当前片库里口碑和完成度都比较稳";
        }
        return StringUtils.join(fragments, "，");
    }

    private String selectPosterUrl(AppMovieListVO movie) {
        if (movie == null) {
            return null;
        }
        if (StringUtils.isNotBlank(movie.getPosterUrl())) {
            return movie.getPosterUrl();
        }
        if (movie.getPosterUrls() != null) {
            for (String posterUrl : movie.getPosterUrls()) {
                if (StringUtils.isNotBlank(posterUrl)) {
                    return posterUrl.trim();
                }
            }
        }
        if (StringUtils.containsIgnoreCase(StringUtils.deleteWhitespace(StringUtils.defaultString(movie.getTitle())), "流浪地球2")) {
            return LIULANGDIQIU2_POSTER_PATH;
        }
        return null;
    }

    private Set<String> extractKnownTokens(String text, List<String> candidates) {
        Set<String> tokens = new LinkedHashSet<String>();
        String normalized = normalizeText(text);
        if (StringUtils.isBlank(normalized)) {
            return tokens;
        }
        for (String candidate : candidates) {
            if (normalized.contains(candidate.toLowerCase())) {
                tokens.add(candidate);
            }
        }
        return tokens;
    }

    private Set<String> extractRegionTokens(String text) {
        Set<String> regions = extractKnownTokens(text, KNOWN_REGIONS);
        String normalized = normalizeText(text);
        if (normalized.contains("国产") || normalized.contains("大陆")) {
            regions.add("中国大陆");
        }
        if (normalized.contains("港片") || normalized.contains("香港")) {
            regions.add("中国香港");
        }
        if (normalized.contains("台片") || normalized.contains("台湾")) {
            regions.add("中国台湾");
        }
        if (normalized.contains("日影") || normalized.contains("日本")) {
            regions.add("日本");
        }
        if (normalized.contains("韩影") || normalized.contains("韩国")) {
            regions.add("韩国");
        }
        if (normalized.contains("欧美") || normalized.contains("欧洲")) {
            regions.add("欧洲");
        }
        if (normalized.contains("好莱坞") || normalized.contains("美国")) {
            regions.add("美国");
        }
        return regions;
    }

    private Set<String> extractMoodTokens(String text) {
        Set<String> moods = extractKnownTokens(text, KNOWN_MOODS);
        String normalized = normalizeText(text);
        if (normalized.contains("治愈")) {
            moods.add("治愈");
            moods.add("放松");
        }
        if (normalized.contains("轻松")) {
            moods.add("轻松");
            moods.add("放松");
        }
        if (normalized.contains("浪漫")) {
            moods.add("浪漫");
        }
        if (normalized.contains("烧脑")) {
            moods.add("烧脑");
        }
        if (normalized.contains("热血") || normalized.contains("燃")) {
            moods.add("热血");
        }
        if (normalized.contains("怀旧") || normalized.contains("经典")) {
            moods.add("怀旧");
        }
        return moods;
    }

    private Set<String> extractPersonTokens(String text) {
        Set<String> persons = new LinkedHashSet<String>();
        if (StringUtils.isBlank(text)) {
            return persons;
        }
        java.util.regex.Matcher matcher = PERSON_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            persons.add(matcher.group(1));
        }
        return persons;
    }

    private List<String> extractYearHints(String text) {
        List<String> hints = new ArrayList<String>();
        if (StringUtils.isBlank(text)) {
            return hints;
        }
        java.util.regex.Matcher matcher = YEAR_PATTERN.matcher(text);
        while (matcher.find()) {
            hints.add(matcher.group(1));
        }
        String normalized = normalizeText(text);
        if (normalized.contains("2020以后") || normalized.contains("近年") || normalized.contains("新片")) {
            hints.add("2020以后");
        }
        if (normalized.contains("2010-2019")) {
            hints.add("2010-2019");
        }
        if (normalized.contains("2000-2009")) {
            hints.add("2000-2009");
        }
        if (normalized.contains("2000以前") || normalized.contains("老电影") || normalized.contains("经典")) {
            hints.add("2000以前");
        }
        return hints;
    }

    private List<String> parseStoredPreference(String value) {
        List<String> tokens = new ArrayList<String>();
        if (StringUtils.isBlank(value)) {
            return tokens;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                JSONArray array = JSONArray.parseArray(trimmed);
                if (array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        String token = array.getString(i);
                        if (StringUtils.isNotBlank(token)) {
                            tokens.add(token.trim());
                        }
                    }
                }
                return tokens;
            } catch (Exception ignored) {
                // 继续走兜底拆分，兼容历史脏数据。
            }
        }
        String normalized = trimmed.replace("，", ",").replace("、", ",").replace("|", ",").replace(";", ",");
        for (String token : normalized.split(",")) {
            if (StringUtils.isNotBlank(token)) {
                tokens.add(token.trim());
            }
        }
        return tokens;
    }

    private List<String> filterKnownTokens(List<String> rawTokens, List<String> knownTokens) {
        List<String> result = new ArrayList<String>();
        if (rawTokens == null) {
            return result;
        }
        for (String rawToken : rawTokens) {
            if (StringUtils.isBlank(rawToken)) {
                continue;
            }
            for (String knownToken : knownTokens) {
                if (StringUtils.containsIgnoreCase(rawToken, knownToken)) {
                    result.add(knownToken);
                    break;
                }
            }
        }
        return result;
    }

    private List<String> expandRegionAliases(List<String> rawTokens) {
        List<String> result = new ArrayList<String>();
        if (rawTokens == null) {
            return result;
        }
        for (String token : rawTokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            result.add(token);
            String normalized = normalizeText(token);
            if (normalized.contains("国产") || normalized.contains("大陆")) {
                result.add("中国大陆");
            }
            if (normalized.contains("港")) {
                result.add("中国香港");
            }
            if (normalized.contains("台")) {
                result.add("中国台湾");
            }
            if (normalized.contains("日")) {
                result.add("日本");
            }
            if (normalized.contains("韩")) {
                result.add("韩国");
            }
            if (normalized.contains("欧美") || normalized.contains("欧")) {
                result.add("欧洲");
            }
            if (normalized.contains("美")) {
                result.add("美国");
            }
        }
        return result;
    }

    private Set<String> matchTokens(String searchableText, List<String> tokens) {
        Set<String> matched = new LinkedHashSet<String>();
        if (StringUtils.isBlank(searchableText) || tokens == null) {
            return matched;
        }
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && searchableText.contains(token.toLowerCase())) {
                matched.add(token);
            }
        }
        return matched;
    }

    private Set<String> matchMoodTokens(String searchableText, List<String> moods) {
        Set<String> matched = new LinkedHashSet<String>();
        if (StringUtils.isBlank(searchableText) || moods == null) {
            return matched;
        }
        for (String mood : moods) {
            if ("放松".equals(mood) && containsAny(searchableText, "喜剧", "轻松", "家庭", "温暖", "治愈", "动画")) {
                matched.add(mood);
            } else if ("热血".equals(mood) && containsAny(searchableText, "动作", "冒险", "战争", "燃", "科幻")) {
                matched.add(mood);
            } else if ("浪漫".equals(mood) && containsAny(searchableText, "爱情", "浪漫", "心动")) {
                matched.add(mood);
            } else if ("烧脑".equals(mood) && containsAny(searchableText, "悬疑", "犯罪", "推理", "科幻", "烧脑")) {
                matched.add(mood);
            } else if ("治愈".equals(mood) && containsAny(searchableText, "治愈", "温暖", "成长", "家庭", "陪伴")) {
                matched.add(mood);
            } else if ("怀旧".equals(mood) && containsAny(searchableText, "年代", "经典", "旧时", "回忆")) {
                matched.add(mood);
            } else if ("轻松".equals(mood) && containsAny(searchableText, "轻松", "喜剧", "治愈", "动画")) {
                matched.add(mood);
            } else if ("紧张".equals(mood) && containsAny(searchableText, "惊悚", "悬疑", "动作", "危机")) {
                matched.add(mood);
            }
        }
        return matched;
    }

    private Set<String> subtractMatches(Set<String> source, Set<String> exclusions) {
        Set<String> result = new LinkedHashSet<String>();
        if (source == null) {
            return result;
        }
        result.addAll(source);
        if (exclusions != null) {
            result.removeAll(exclusions);
        }
        return result;
    }

    private boolean matchesYearHints(Date releaseDate, List<String> yearHints) {
        if (releaseDate == null || yearHints == null || yearHints.isEmpty()) {
            return false;
        }
        int releaseYear = extractYear(releaseDate);
        for (String yearHint : yearHints) {
            if (StringUtils.isBlank(yearHint)) {
                continue;
            }
            String normalized = yearHint.replace(" ", "");
            if (normalized.matches("\\d{4}") && Integer.parseInt(normalized) == releaseYear) {
                return true;
            }
            if ("2020以后".equals(normalized) && releaseYear >= 2020) {
                return true;
            }
            if ("2010-2019".equals(normalized) && releaseYear >= 2010 && releaseYear <= 2019) {
                return true;
            }
            if ("2000-2009".equals(normalized) && releaseYear >= 2000 && releaseYear <= 2009) {
                return true;
            }
            if ("2000以前".equals(normalized) && releaseYear < 2000) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String searchableText, String... keywords) {
        if (StringUtils.isBlank(searchableText) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (searchableText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        return StringUtils.defaultString(value).trim().toLowerCase();
    }

    private String shortenPeople(String people) {
        List<String> names = parseStoredPreference(people);
        if (names.isEmpty()) {
            return people;
        }
        return StringUtils.join(names.stream().limit(2).collect(Collectors.toList()), "、");
    }

    private int extractYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private double readCandidateScore(Map<String, Object> candidate) {
        return parseDouble(candidate.get("_candidateScore"));
    }

    private int readCandidateMatchCount(Map<String, Object> candidate) {
        Long value = parseLong(candidate.get("_candidateMatchCount"));
        return value == null ? 0 : value.intValue();
    }

    private int readCandidateOrder(Map<String, Object> candidate) {
        Long value = parseLong(candidate.get("_candidateOrder"));
        return value == null ? Integer.MAX_VALUE : value.intValue();
    }

    private void removeCandidateSortFields(Map<String, Object> candidate) {
        candidate.remove("_candidateScore");
        candidate.remove("_candidateMatchCount");
        candidate.remove("_candidateOrder");
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    private String trimTrailingZero(Double value) {
        double numeric = value == null ? 0D : value;
        if (numeric == (long) numeric) {
            return String.valueOf((long) numeric);
        }
        return String.valueOf(numeric);
    }

    private String defaultMovieText(Object value) {
        String text = value == null ? null : String.valueOf(value);
        return StringUtils.isBlank(text) ? "未知" : text;
    }

    private String stringValue(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class RecommendationContext {
        private final List<String> currentGenres = new ArrayList<String>();
        private final List<String> preferenceGenres = new ArrayList<String>();
        private final List<String> currentRegions = new ArrayList<String>();
        private final List<String> preferenceRegions = new ArrayList<String>();
        private final List<String> currentMoods = new ArrayList<String>();
        private final List<String> preferenceMoods = new ArrayList<String>();
        private final List<String> currentPersons = new ArrayList<String>();
        private final List<String> currentYearHints = new ArrayList<String>();
        private final List<String> preferenceYearHints = new ArrayList<String>();

        private void addCurrentGenres(Iterable<String> values) { addDistinct(currentGenres, values); }
        private void addPreferenceGenres(Iterable<String> values) { addDistinct(preferenceGenres, values); }
        private void addCurrentRegions(Iterable<String> values) { addDistinct(currentRegions, values); }
        private void addPreferenceRegions(Iterable<String> values) { addDistinct(preferenceRegions, values); }
        private void addCurrentMoods(Iterable<String> values) { addDistinct(currentMoods, values); }
        private void addPreferenceMoods(Iterable<String> values) { addDistinct(preferenceMoods, values); }
        private void addCurrentPersons(Iterable<String> values) { addDistinct(currentPersons, values); }
        private void addCurrentYearHints(Iterable<String> values) { addDistinct(currentYearHints, values); }
        private void addPreferenceYearHints(Iterable<String> values) { addDistinct(preferenceYearHints, values); }

        private List<String> getCurrentGenres() { return currentGenres; }
        private List<String> getPreferenceGenres() { return preferenceGenres; }
        private List<String> getCurrentRegions() { return currentRegions; }
        private List<String> getPreferenceRegions() { return preferenceRegions; }
        private List<String> getCurrentMoods() { return currentMoods; }
        private List<String> getPreferenceMoods() { return preferenceMoods; }
        private List<String> getCurrentPersons() { return currentPersons; }
        private List<String> getCurrentYearHints() { return currentYearHints; }
        private List<String> getPreferenceYearHints() { return preferenceYearHints; }

        private void addDistinct(List<String> target, Iterable<String> values) {
            if (values == null) {
                return;
            }
            for (String value : values) {
                if (StringUtils.isBlank(value) || target.contains(value)) {
                    continue;
                }
                target.add(value);
            }
        }
    }

    private static final class RecommendationEvidence {
        private double score;
        private int matchCount;
        private int sourceOrder;
        private final List<String> reasonParts = new ArrayList<String>();

        private double getScore() { return score; }
        private void setScore(double score) { this.score = score; }
        private int getMatchCount() { return matchCount; }
        private void incrementMatchCount() { this.matchCount++; }
        private int getSourceOrder() { return sourceOrder; }
        private void setSourceOrder(int sourceOrder) { this.sourceOrder = sourceOrder; }
        private List<String> getReasonParts() { return reasonParts; }
        private void addReason(String reason) {
            if (StringUtils.isNotBlank(reason) && !reasonParts.contains(reason)) {
                reasonParts.add(reason);
            }
        }
    }
}
