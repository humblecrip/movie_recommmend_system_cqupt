package com.service;

import com.dao.AiConversationDao;
import com.dao.AiMessageDao;
import com.dao.AiRecommendationFeedbackDao;
import com.dao.AiUserPreferenceDao;
import com.entity.AiConversationEntity;
import com.entity.AiMessageEntity;
import com.entity.AiUserPreferenceEntity;
import com.entity.vo.AppMovieListVO;
import com.service.impl.AiChatServiceImpl;
import com.utils.LlmClient;
import com.utils.PageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private AiConversationDao conversationDao;

    @Mock
    private AiMessageDao messageDao;

    @Mock
    private AiUserPreferenceDao userPreferenceDao;

    @Mock
    private AppMovieService appMovieService;

    @Mock
    private AiRecommendationFeedbackDao feedbackDao;

    @Mock
    private LlmClient llmClient;

    @Test
    void streamChatShouldShortCircuitIntentNavigationBeforeCallingLlm() {
        AiChatServiceImpl service = newService();
        Long conversationId = 100L;
        Long userId = 8L;

        AiConversationEntity conversation = new AiConversationEntity();
        conversation.setId(conversationId);
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        conversation.setIsColdStart(0);
        conversation.setIsDeleted(0);

        when(conversationDao.selectById(conversationId)).thenReturn(conversation);
        List<AiMessageEntity> storedMessages = new ArrayList<AiMessageEntity>();
        AtomicLong messageIdSequence = new AtomicLong(1L);
        when(messageDao.insert(any(AiMessageEntity.class))).thenAnswer(invocation -> {
            AiMessageEntity message = invocation.getArgument(0);
            message.setId(messageIdSequence.getAndIncrement());
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(new Date());
            }
            storedMessages.add(message);
            return 1;
        });
        when(messageDao.selectList(any())).thenAnswer(invocation -> new ArrayList<AiMessageEntity>(storedMessages));

        Flux<String> response = service.streamChat(conversationId, "我想修改密码", userId);

        List<String> chunks = response.collectList().block();
        assertEquals(Collections.singletonList("可以，点击下面按钮进入修改密码页面。"), chunks);

        verify(llmClient, never()).streamChat(any());
        verify(llmClient, never()).chat(any());

        assertEquals(2, storedMessages.size());
        AiMessageEntity assistantMessage = storedMessages.get(1);
        assertEquals("assistant", assistantMessage.getRole());
        assertEquals("可以，点击下面按钮进入修改密码页面。", assistantMessage.getContent());

        List<AiMessageEntity> messages = service.getMessages(conversationId, userId);
        assertEquals(2, messages.size());
        AiMessageEntity assistant = messages.get(1);
        assertNotNull(assistant.getIntentActions());
        assertEquals(1, assistant.getIntentActions().size());
        assertEquals("去修改密码", assistant.getIntentActions().get(0).get("label"));
        assertEquals("/index/center", assistant.getIntentActions().get(0).get("targetRoute"));
    }

    @Test
    void streamChatShouldShortCircuitAvatarIntentBeforeCallingLlm() {
        AiChatServiceImpl service = newService();
        Long conversationId = 101L;
        Long userId = 9L;

        AiConversationEntity conversation = new AiConversationEntity();
        conversation.setId(conversationId);
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        conversation.setIsColdStart(0);
        conversation.setIsDeleted(0);

        when(conversationDao.selectById(conversationId)).thenReturn(conversation);
        List<AiMessageEntity> storedMessages = new ArrayList<AiMessageEntity>();
        AtomicLong messageIdSequence = new AtomicLong(1L);
        when(messageDao.insert(any(AiMessageEntity.class))).thenAnswer(invocation -> {
            AiMessageEntity message = invocation.getArgument(0);
            message.setId(messageIdSequence.getAndIncrement());
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(new Date());
            }
            storedMessages.add(message);
            return 1;
        });
        when(messageDao.selectList(any())).thenAnswer(invocation -> new ArrayList<AiMessageEntity>(storedMessages));

        List<String> chunks = service.streamChat(conversationId, "我想修改头像", userId).collectList().block();

        assertEquals(Collections.singletonList("可以，点击下面按钮进入个人资料页面修改头像。"), chunks);
        verify(llmClient, never()).streamChat(any());
        verify(llmClient, never()).chat(any());

        List<AiMessageEntity> messages = service.getMessages(conversationId, userId);
        assertEquals(2, messages.size());
        AiMessageEntity assistant = messages.get(1);
        assertEquals("可以，点击下面按钮进入个人资料页面修改头像。", assistant.getContent());
        assertNotNull(assistant.getIntentActions());
        assertEquals(1, assistant.getIntentActions().size());
        assertEquals("去修改头像", assistant.getIntentActions().get(0).get("label"));
        assertEquals("/index/center", assistant.getIntentActions().get(0).get("targetRoute"));
        assertEquals(Collections.singletonMap("section", "profile"), assistant.getIntentActions().get(0).get("query"));
    }

    @Test
    void getRecommendedMoviesShouldPrioritizeCurrentMessageMatchOverStableTopScore() {
        AiChatServiceImpl service = newService();
        when(feedbackDao.selectPositiveMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(feedbackDao.selectNegativeMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(appMovieService.queryFrontRecommendedPage(anyMap(), anyLong())).thenReturn(new PageUtils(Arrays.asList(
                movie(1L, "流浪地球2", "科幻", "中国大陆", 9.8D, 2023, "郭帆", "吴京, 刘德华", "太空危机与热血拯救"),
                movie(2L, "隐入尘烟", "剧情", "中国大陆", 8.7D, 2022, "李睿珺", "武仁林, 海清", "细腻克制的现实主义故事"),
                movie(3L, "扬名立万", "悬疑", "中国大陆", 8.2D, 2021, "刘循子墨", "尹正, 邓家佳", "剧本杀式悬疑群像")
        ), 3, 3, 1));

        AiUserPreferenceEntity preference = new AiUserPreferenceEntity();
        preference.setFavoriteGenres("[\"科幻\"]");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "getRecommendedMovieMaps",
                8L,
                3,
                "今晚想看现实主义剧情片，最好中国大陆近年的",
                preference
        );

        assertEquals(3, result.size());
        assertEquals("隐入尘烟", result.get(0).get("title"));
        assertTrue(!"流浪地球2".equals(result.get(0).get("title")));
    }

    @Test
    void getRecommendedMoviesShouldUsePosterFallbackForLiulangdiqiu2() {
        AiChatServiceImpl service = newService();
        when(feedbackDao.selectPositiveMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(feedbackDao.selectNegativeMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(appMovieService.queryFrontRecommendedPage(anyMap(), anyLong())).thenReturn(new PageUtils(Collections.singletonList(
                movie(1L, "流浪地球2", "科幻", "中国大陆", 9.8D, 2023, "郭帆", "吴京, 刘德华", "太空危机与热血拯救")
        ), 1, 1, 1));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "getRecommendedMovieMaps",
                9L,
                1,
                "推荐一部国产科幻片",
                new AiUserPreferenceEntity()
        );

        assertEquals("upload/movie_liulangdiqiu2.jpg", result.get(0).get("posterUrl"));
    }

    @Test
    void buildLocalRecommendationAnswerShouldIncludeSpecificReasons() {
        AiChatServiceImpl service = newService();
        when(feedbackDao.selectPositiveMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(feedbackDao.selectNegativeMovieIds(anyLong())).thenReturn(Collections.<Long>emptyList());
        when(appMovieService.queryFrontRecommendedPage(anyMap(), anyLong())).thenReturn(new PageUtils(Arrays.asList(
                movie(1L, "流浪地球2", "科幻", "中国大陆", 9.8D, 2023, "郭帆", "吴京, 刘德华", "太空危机与热血拯救"),
                movie(2L, "你好，李焕英", "喜剧", "中国大陆", 8.5D, 2021, "贾玲", "贾玲, 张小斐", "温暖轻松又带亲情回响")
        ), 2, 2, 1));

        AiUserPreferenceEntity preference = new AiUserPreferenceEntity();
        preference.setFavoriteGenres("[\"喜剧\"]");

        String answer = (String) ReflectionTestUtils.invokeMethod(
                service,
                "buildLocalRecommendationAnswer",
                3L,
                "今晚想轻松一点，最好温暖治愈",
                preference,
                2
        );

        assertTrue(answer.contains("2-3 部更贴近当前诉求的候选"));
        assertTrue(answer.contains("整体气质更偏"));
        assertTrue(answer.contains("当前候选评分为"));
    }

    @Test
    void buildLocalRecommendationAnswerShouldAvoidMarkdownNumberedListMarkers() {
        AiChatServiceImpl service = newService();
        String answer = (String) ReflectionTestUtils.invokeMethod(
                service,
                "buildLocalRecommendationAnswer",
                11L,
                "想看轻松一点的国产电影",
                new AiUserPreferenceEntity(),
                Arrays.asList(
                        candidate(1L, "流浪地球2", "整体气质更偏热血科幻，当前候选评分为 9.8。"),
                        candidate(2L, "你好，李焕英", "整体气质更偏温暖轻松，当前候选评分为 8.5。")
                )
        );

        assertTrue(answer.contains("优先可以看《流浪地球2》"));
        assertTrue(answer.contains("也可以看看《你好，李焕英》"));
        assertTrue(!answer.contains("\n1."));
        assertTrue(!answer.contains("\n2."));
    }

    @Test
    void buildRecommendationSystemPromptShouldExplicitlyForbidMarkdownMarkers() {
        AiChatServiceImpl service = newService();
        ReflectionTestUtils.setField(service, "llmClient", new LlmClient());

        AiUserPreferenceEntity preference = new AiUserPreferenceEntity();
        preference.setFavoriteGenres("[\"悬疑\"]");
        @SuppressWarnings("unchecked")
        String prompt = (String) ReflectionTestUtils.invokeMethod(
                service,
                "buildRecommendationSystemPrompt",
                preference,
                Arrays.asList(
                        candidate(1L, "扬名立万", "更贴近悬疑口味"),
                        candidate(2L, "隐入尘烟", "更贴近现实主义诉求")
                )
        );

        assertTrue(prompt.contains("只输出纯文本"));
        assertTrue(prompt.contains("不要使用 Markdown 语法"));
        assertTrue(prompt.contains("避免使用 1.、2.、-、* 等列表标记"));
    }

    @Test
    void resolveBoundMovieShouldFollowMentionedMovieInsteadOfAlwaysUsingFirstCandidate() {
        AiChatServiceImpl service = newService();
        @SuppressWarnings("unchecked")
        Map<String, Object> boundMovie = (Map<String, Object>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveBoundMovie",
                "这次我更推荐《隐入尘烟》，如果你想更悬疑一点再看《扬名立万》。",
                Arrays.asList(
                        candidate(1L, "流浪地球2", "适合热血科幻"),
                        candidate(2L, "隐入尘烟", "更贴近现实主义诉求"),
                        candidate(3L, "扬名立万", "适合悬疑口味")
                )
        );

        assertEquals(2L, boundMovie.get("id"));
        assertEquals("更贴近现实主义诉求", boundMovie.get("recommendationReason"));
    }

    @Test
    void resolveBoundMovieShouldAllowNoBindingForNonRecommendationAnswer() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        Map<String, Object> boundMovie = (Map<String, Object>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveBoundMovie",
                "可以，点击下面按钮进入个人资料页面修改头像。",
                Arrays.asList(
                        candidate(1L, "流浪地球2", "适合热血科幻"),
                        candidate(2L, "隐入尘烟", "更贴近现实主义诉求")
                )
        );

        assertTrue(boundMovie.isEmpty());
    }

    @Test
    void resolveRecommendedMoviesShouldExtractUpToThreeCandidatesFromRecommendationAnswer() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> movies = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveRecommendedMovies",
                "请根据我的偏好推荐三部电影",
                "这次优先可以看《隐入尘烟》，也可以看看《扬名立万》，如果你想更热血一点再试试《流浪地球2》。",
                Arrays.asList(
                        candidate(1L, "流浪地球2", "适合热血科幻"),
                        candidate(2L, "隐入尘烟", "更贴近现实主义诉求"),
                        candidate(3L, "扬名立万", "适合悬疑口味"),
                        candidate(4L, "你好，李焕英", "适合轻松治愈")
                )
        );

        assertEquals(3, movies.size());
        assertEquals(2L, movies.get(0).get("id"));
        assertEquals(3L, movies.get(1).get("id"));
        assertEquals(1L, movies.get(2).get("id"));
    }

    @Test
    void resolveRecommendedMoviesShouldKeepSingleMovieBindingWhenOnlyOneCandidateIsExplicit() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> movies = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveRecommendedMovies",
                "推荐一部科幻电影",
                "这次我更推荐《流浪地球2》，整体气质更偏热血科幻。",
                Arrays.asList(
                        candidate(1L, "流浪地球2", "适合热血科幻"),
                        candidate(2L, "隐入尘烟", "更贴近现实主义诉求")
                )
        );

        assertEquals(1, movies.size());
        assertEquals(1L, movies.get(0).get("id"));
    }

    @Test
    void resolveIntentActionsShouldReturnPasswordActionForPasswordIntent() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "我想修改密码，带我去一下",
                Long.valueOf(1L)
        );

        assertEquals(1, actions.size());
        assertEquals("navigate", actions.get(0).get("actionType"));
        assertEquals("去修改密码", actions.get(0).get("label"));
        assertEquals("/index/center", actions.get(0).get("targetRoute"));
        assertEquals(Collections.singletonMap("section", "password"), actions.get(0).get("query"));
    }

    @Test
    void resolveIntentActionsShouldReturnProfileActionForProfileIntent() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "我想修改个人资料",
                Long.valueOf(1L)
        );

        assertEquals(1, actions.size());
        assertEquals("navigate", actions.get(0).get("actionType"));
        assertEquals("去个人资料", actions.get(0).get("label"));
        assertEquals("/index/center", actions.get(0).get("targetRoute"));
        assertEquals(Collections.singletonMap("section", "profile"), actions.get(0).get("query"));
    }

    @Test
    void resolveIntentActionsShouldKeepPasswordIntentAheadOfProfileRules() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "我想修改密码和个人资料",
                Long.valueOf(1L)
        );

        assertEquals(1, actions.size());
        assertEquals("去修改密码", actions.get(0).get("label"));
        assertEquals(Collections.singletonMap("section", "password"), actions.get(0).get("query"));
    }

    @Test
    void resolveIntentActionsShouldIgnoreOrdinaryMovieRecommendationRequest() {
        AiChatServiceImpl service = newService();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "推荐一部适合今晚看的科幻电影",
                Long.valueOf(1L)
        );

        assertTrue(actions.isEmpty());
    }

    @Test
    void resolveIntentActionsShouldPreferMovieDetailWhenTitleMatchesUniquely() {
        AiChatServiceImpl service = newService();
        when(appMovieService.queryFrontPage(anyMap())).thenReturn(new PageUtils(Collections.singletonList(
                movie(7L, "流浪地球2", "科幻", "中国大陆", 9.8D, 2023, "郭帆", "吴京, 刘德华", "太空危机与热血拯救")
        ), 1, 10, 1));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "帮我打开流浪地球2详情页",
                Long.valueOf(1L)
        );

        assertEquals(1, actions.size());
        assertEquals("查看《流浪地球2》详情", actions.get(0).get("label"));
        assertEquals("/index/dianyingxinxiDetail", actions.get(0).get("targetRoute"));
        assertEquals(Collections.singletonMap("id", 7L), actions.get(0).get("query"));
    }

    @Test
    void resolveIntentActionsShouldNotJumpToMovieDetailWhenTitleIsNotUnique() {
        AiChatServiceImpl service = newService();
        when(appMovieService.queryFrontPage(anyMap())).thenReturn(new PageUtils(Arrays.asList(
                movie(7L, "流浪地球", "科幻", "中国大陆", 9.4D, 2019, "郭帆", "吴京", "拯救地球"),
                movie(8L, "流浪地球2", "科幻", "中国大陆", 9.8D, 2023, "郭帆", "吴京, 刘德华", "太空危机与热血拯救")
        ), 2, 10, 1));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service,
                "resolveIntentActions",
                "帮我打开流浪详情页",
                Long.valueOf(1L)
        );

        assertFalse(actions.isEmpty());
        assertEquals("去电影页", actions.get(0).get("label"));
        assertEquals("/index/dianyingxinxi", actions.get(0).get("targetRoute"));
        assertNull(actions.get(0).get("query"));
    }

    @Test
    void generateConversationTitleShouldCreateConciseSummaryForRecommendationRequest() {
        AiChatServiceImpl service = newService();

        String title = (String) ReflectionTestUtils.invokeMethod(
                service,
                "generateConversationTitle",
                "请推荐一部节奏紧凑的悬疑片"
        );

        assertEquals("紧凑悬疑片推荐", title);
        assertTrue(!title.contains("请推荐"));
        assertNotEquals("请推荐一部节奏紧凑的悬疑片", title);
    }

    @Test
    void generateConversationTitleShouldSummarizeViewingIntent() {
        AiChatServiceImpl service = newService();

        String title = (String) ReflectionTestUtils.invokeMethod(
                service,
                "generateConversationTitle",
                "今晚想看轻松治愈的电影"
        );

        assertEquals("轻松治愈电影", title);
    }

    @Test
    void generateConversationTitleShouldNotStripChineseContentInsideMovieName() {
        AiChatServiceImpl service = newService();

        String title = (String) ReflectionTestUtils.invokeMethod(
                service,
                "generateConversationTitle",
                "我想看不能说的秘密"
        );

        assertEquals("不能说的秘密", title);
    }

    @Test
    void generateConversationTitleShouldFallbackSafelyForBlankInput() {
        AiChatServiceImpl service = newService();

        String title = (String) ReflectionTestUtils.invokeMethod(
                service,
                "generateConversationTitle",
                "  \n  "
        );

        assertEquals("新对话", title);
    }

    @Test
    void resolveConversationTitleShouldTreatTrimmedDefaultTitleAsUninitialized() {
        AiChatServiceImpl service = newService();

        String generated = (String) ReflectionTestUtils.invokeMethod(
                service,
                "resolveConversationTitle",
                "  New Chat  ",
                "请推荐一部节奏紧凑的悬疑片"
        );

        assertEquals("紧凑悬疑片推荐", generated);
    }

    @Test
    void resolveConversationTitleShouldOnlyUpdateDefaultTitleOnce() {
        AiChatServiceImpl service = newService();

        String generated = (String) ReflectionTestUtils.invokeMethod(
                service,
                "resolveConversationTitle",
                "新对话",
                "请推荐一部节奏紧凑的悬疑片"
        );
        String preserved = (String) ReflectionTestUtils.invokeMethod(
                service,
                "resolveConversationTitle",
                "已经命名的标题",
                "今晚想看轻松治愈的电影"
        );
        String englishDefault = (String) ReflectionTestUtils.invokeMethod(
                service,
                "resolveConversationTitle",
                "New Chat",
                "今晚想看轻松治愈的电影"
        );

        assertEquals("紧凑悬疑片推荐", generated);
        assertEquals("已经命名的标题", preserved);
        assertEquals("轻松治愈电影", englishDefault);
    }

    private AiChatServiceImpl newService() {
        AiChatServiceImpl service = new AiChatServiceImpl();
        ReflectionTestUtils.setField(service, "conversationDao", conversationDao);
        ReflectionTestUtils.setField(service, "messageDao", messageDao);
        ReflectionTestUtils.setField(service, "userPreferenceDao", userPreferenceDao);
        ReflectionTestUtils.setField(service, "appMovieService", appMovieService);
        ReflectionTestUtils.setField(service, "feedbackDao", feedbackDao);
        ReflectionTestUtils.setField(service, "llmClient", llmClient);
        return service;
    }

    private AiUserPreferenceEntity preference(Long userId) {
        AiUserPreferenceEntity preference = new AiUserPreferenceEntity();
        preference.setId(1L);
        preference.setUserId(userId);
        preference.setColdStartCompleted(1);
        return preference;
    }

    private AppMovieListVO movie(Long id,
                                 String title,
                                 String typeName,
                                 String regionName,
                                 Double totalScore,
                                 int releaseYear,
                                 String directorName,
                                 String castNames,
                                 String synopsis) {
        AppMovieListVO movie = new AppMovieListVO();
        movie.setId(id);
        movie.setTitle(title);
        movie.setTypeName(typeName);
        movie.setRegionName(regionName);
        movie.setTotalScore(totalScore);
        movie.setDirectorName(directorName);
        movie.setCastNames(castNames);
        movie.setSynopsis(synopsis);
        movie.setReleaseDate(new java.util.GregorianCalendar(releaseYear, 0, 1).getTime());
        return movie;
    }

    private Map<String, Object> candidate(Long id, String title, String reason) {
        return new java.util.HashMap<String, Object>() {{
            put("id", id);
            put("title", title);
            put("recommendationReason", reason);
        }};
    }
}
