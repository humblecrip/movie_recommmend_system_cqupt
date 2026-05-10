package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.AiConversationEntity;
import com.entity.AiRecommendationFeedbackEntity;
import com.entity.AiUserPreferenceEntity;
import com.entity.TokenEntity;
import com.service.AiChatService;
import com.service.TokenService;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI conversational movie recommendation endpoints.
 */
@RestController
@RequestMapping("/ai-chat")
public class AiChatController {

    private static final String FRONT_USER_TABLE_NAME = "yonghu";
    private static final long STREAM_TIMEOUT_MILLIS = 130000L;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/conversation/create")
    public R createConversation(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        String title = stringValue(params, "title");
        Integer isColdStart = parseInteger(stringValue(params, "isColdStart"), 0);
        AiConversationEntity conversation = aiChatService.createConversation(userId, title, isColdStart);
        return R.ok().put("data", conversation);
    }

    @GetMapping("/conversation/list")
    public R listConversations(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok().put("data", aiChatService.listConversations(userId));
    }

    @GetMapping("/messages")
    public R getMessages(@RequestParam Long conversationId, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok().put("data", aiChatService.getMessages(conversationId, userId));
    }

    @IgnoreAuth
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam Long conversationId,
                                 @RequestParam String message,
                                 HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<Disposable> subscriptionRef = new AtomicReference<Disposable>();
        registerEmitterCleanup(emitter, subscriptionRef, completed);

        Long userId = getUserId(request);
        if (userId == null) {
            sendChunk(emitter, "请先登录", completed);
            sendDoneAndComplete(emitter, completed);
            return emitter;
        }

        Disposable subscription = aiChatService.streamChat(conversationId, message, userId)
                .subscribe(
                        content -> sendChunk(emitter, content, completed),
                        error -> handleStreamError(emitter, completed, error),
                        () -> sendDoneAndComplete(emitter, completed)
                );
        subscriptionRef.set(subscription);
        if (completed.get()) {
            subscription.dispose();
        }
        return emitter;
    }

    @DeleteMapping("/conversation/{id}")
    public R deleteConversation(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        aiChatService.deleteConversation(id, userId);
        return R.ok("对话已删除");
    }

    @GetMapping("/preference")
    public R getPreference(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok().put("data", aiChatService.getUserPreference(userId));
    }

    @PostMapping("/preference/save")
    public R savePreference(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        boolean markCompleted = Boolean.parseBoolean(StringUtils.defaultString(stringValue(params, "markColdStartCompleted"), "false"));
        AiUserPreferenceEntity preference = aiChatService.saveUserPreference(userId,
                stringValue(params, "favoriteGenres"), stringValue(params, "favoriteYears"),
                stringValue(params, "favoriteRegions"), stringValue(params, "moodPreferences"),
                stringValue(params, "watchFrequency"), stringValue(params, "dislikedGenres"), markCompleted);
        return R.ok("偏好已保存").put("data", preference);
    }

    @PostMapping("/feedback")
    public R saveFeedback(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        try {
            Long conversationId = parseLongRequired(params, "conversationId");
            Long messageId = parseLongRequired(params, "messageId");
            Long movieId = parseLongRequired(params, "movieId");
            AiRecommendationFeedbackEntity feedback = aiChatService.saveFeedback(userId, conversationId, messageId,
                    movieId, stringValue(params, "feedbackType"), stringValue(params, "feedbackReason"));
            return R.ok("反馈已保存")
                    .put("data", feedback)
                    .put("recommendations", aiChatService.getRecommendedMovies(userId, 6));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/cold-start/status")
    public R getColdStartStatus(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        AiUserPreferenceEntity preference = aiChatService.getUserPreference(userId);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("completed", aiChatService.isColdStartCompleted(userId));
        result.put("preference", preference);
        return R.ok().put("data", result);
    }

    @PostMapping("/cold-start/complete")
    public R completeColdStart(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok("冷启动偏好已完成").put("data", aiChatService.completeColdStart(userId, params));
    }

    @GetMapping("/recommendations")
    @IgnoreAuth
    public R getRecommendations(@RequestParam(defaultValue = "6") int limit, HttpServletRequest request) {
        return R.ok().put("data", aiChatService.getRecommendedMovies(getUserId(request), limit));
    }

    private void registerEmitterCleanup(SseEmitter emitter, AtomicReference<Disposable> subscriptionRef, AtomicBoolean completed) {
        Runnable disposeSubscription = () -> {
            completed.set(true);
            Disposable subscription = subscriptionRef.get();
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        };
        emitter.onCompletion(disposeSubscription);
        emitter.onTimeout(() -> {
            disposeSubscription.run();
            emitter.complete();
        });
        emitter.onError(error -> disposeSubscription.run());
    }

    private void sendChunk(SseEmitter emitter, String content, AtomicBoolean completed) {
        if (completed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().data(formatSseData(content)));
        } catch (IOException | IllegalStateException e) {
            completed.set(true);
            emitter.completeWithError(e);
        }
    }

    private void sendDoneAndComplete(SseEmitter emitter, AtomicBoolean completed) {
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }
    }

    private void handleStreamError(SseEmitter emitter, AtomicBoolean completed, Throwable error) {
        if (completed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().data(formatSseData("AI推荐暂时不可用，请稍后重试")));
            emitter.send(SseEmitter.event().data("[DONE]"));
            completed.set(true);
            emitter.complete();
        } catch (IOException | IllegalStateException sendError) {
            completed.set(true);
            emitter.completeWithError(sendError);
        }
    }

    private Long getUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isNotBlank(token)) {
            TokenEntity tokenEntity = tokenService.getTokenEntity(token);
            if (tokenEntity != null && FRONT_USER_TABLE_NAME.equals(tokenEntity.getTablename())) {
                return tokenEntity.getUserid();
            }
        }
        Object userId = request.getSession().getAttribute("userId");
        if (userId == null) {
            return null;
        }
        String sessionTableName = firstSessionTableName(request, "tableName", "UserTableName", "sessionTable");
        if (StringUtils.isNotBlank(sessionTableName) && !FRONT_USER_TABLE_NAME.equals(sessionTableName)) {
            return null;
        }
        return parseLong(String.valueOf(userId));
    }

    private String firstSessionTableName(HttpServletRequest request, String... names) {
        for (String name : names) {
            Object value = request.getSession().getAttribute(name);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private String formatSseData(String content) {
        return StringUtils.defaultString(content).replace("\r", " ").replace("\n", "\\n");
    }

    private String stringValue(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        try {
            return StringUtils.isBlank(value) ? defaultValue : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Long parseLongRequired(Map<String, Object> params, String key) {
        Long value = parseLong(stringValue(params, key));
        if (value == null) {
            throw new IllegalArgumentException("参数缺失: " + key);
        }
        return value;
    }

    private Long parseLong(String value) {
        try {
            return StringUtils.isBlank(value) ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
