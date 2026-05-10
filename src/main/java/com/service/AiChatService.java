package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.entity.AiConversationEntity;
import com.entity.AiMessageEntity;
import com.entity.AiRecommendationFeedbackEntity;
import com.entity.AiUserPreferenceEntity;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI chat service for conversational movie recommendation.
 */
public interface AiChatService extends IService<AiConversationEntity> {

    AiConversationEntity createConversation(Long userId, String title, Integer isColdStart);

    List<AiConversationEntity> listConversations(Long userId);

    List<AiMessageEntity> getMessages(Long conversationId, Long userId);

    Flux<String> streamChat(Long conversationId, String userMessage, Long userId);

    AiMessageEntity saveMessage(Long conversationId, String role, String content, Long movieId, String recommendationReason);

    AiUserPreferenceEntity getUserPreference(Long userId);

    AiUserPreferenceEntity saveUserPreference(Long userId, String favoriteGenres, String favoriteYears,
                                               String favoriteRegions, String moodPreferences,
                                               String watchFrequency, String dislikedGenres,
                                               boolean markColdStartCompleted);

    AiRecommendationFeedbackEntity saveFeedback(Long userId, Long conversationId, Long messageId,
                                                 Long movieId, String feedbackType, String feedbackReason);

    List<Map<String, Object>> getRecommendedMovies(Long userId, int limit);

    boolean isColdStartCompleted(Long userId);

    AiUserPreferenceEntity completeColdStart(Long userId, Map<String, Object> params);

    void deleteConversation(Long conversationId, Long userId);
}
