package com.service;

import com.dao.AppMovieInteractionDao;
import com.entity.DiscussdianyingxinxiEntity;
import com.entity.vo.AppMovieCommentCreateRequestVO;
import com.service.SensitivewordsService;
import com.service.impl.AppMovieInteractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppMovieInteractionServiceImplTest {

    @Mock
    private AppMovieInteractionDao appMovieInteractionDao;

    @Mock
    private SensitivewordsService sensitivewordsService;

    @Test
    void addCommentShouldMaskSensitiveWordsBeforeInsert() {
        AppMovieInteractionServiceImpl service = newService();
        AppMovieCommentCreateRequestVO payload = new AppMovieCommentCreateRequestVO();
        payload.setMovieId(5L);
        payload.setContentHtml("<p>这是违禁词内容</p>");
        payload.setAuthorAvatar("/upload/avatar.png");
        payload.setRating(4D);

        when(sensitivewordsService.listNormalizedKeywords()).thenReturn(java.util.Arrays.asList("违禁词"));
        when(appMovieInteractionDao.countUserComment(5L, 12L)).thenReturn(0);
        when(appMovieInteractionDao.selectAppUserIdByLegacyUserId(12L)).thenReturn(112L);
        when(appMovieInteractionDao.insertComment(
                eq(5L),
                eq(112L),
                eq(12L),
                eq("/upload/avatar.png"),
                eq(null),
                eq("<p>这是**内容</p>"),
                eq(4D)
        )).thenReturn(1);
        when(appMovieInteractionDao.selectLastInsertId()).thenReturn(66L);

        service.addComment(12L, payload);

        verify(appMovieInteractionDao).insertComment(
                5L,
                112L,
                12L,
                "/upload/avatar.png",
                null,
                "<p>这是**内容</p>",
                4D
        );
        verify(appMovieInteractionDao).refreshMovieCommentStats(5L);
        verify(appMovieInteractionDao).selectCommentById(66L, 12L);
    }

    @Test
    void createLegacyCommentShouldRejectUnsafeMovieIdFallbackWhenLegacyMappingMissing() {
        AppMovieInteractionServiceImpl service = newService();
        DiscussdianyingxinxiEntity<?> payload = new DiscussdianyingxinxiEntity<Object>();
        payload.setRefid(3L);
        payload.setContent("<p>桥接评论</p>");

        when(appMovieInteractionDao.selectMovieIdByLegacyRefId(3L)).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.createLegacyComment(12L, payload)
        );

        assertEquals("旧电影ID未映射到 app_movie，拒绝直连旧表", exception.getMessage());
        verify(appMovieInteractionDao).selectMovieIdByLegacyRefId(3L);
        verify(appMovieInteractionDao, never()).insertComment(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateLegacyCommentShouldSyncLegacyVoteListsIntoCommentVoteBridge() {
        AppMovieInteractionServiceImpl service = newService();
        DiscussdianyingxinxiEntity<?> payload = new DiscussdianyingxinxiEntity<Object>();
        payload.setId(18L);
        payload.setUserid(12L);
        payload.setReply("<p>后台回复</p>");
        payload.setTuserids("12,15");
        payload.setCuserids("21");

        when(appMovieInteractionDao.selectActualCommentIdByLegacyCommentId(18L)).thenReturn(42L);
        when(appMovieInteractionDao.updateLegacyCommentById(42L, 12L, null, null, null, null, "<p>后台回复</p>", null))
                .thenReturn(1);
        when(appMovieInteractionDao.selectAppUserIdByLegacyUserId(12L)).thenReturn(112L);
        when(appMovieInteractionDao.selectAppUserIdByLegacyUserId(15L)).thenReturn(115L);
        when(appMovieInteractionDao.selectAppUserIdByLegacyUserId(21L)).thenReturn(121L);
        when(appMovieInteractionDao.selectMovieIdByCommentId(42L)).thenReturn(9L);

        boolean updated = service.updateLegacyComment(payload);

        assertEquals(true, updated);
        verify(appMovieInteractionDao).deleteCommentVotesByType(42L, "like");
        verify(appMovieInteractionDao).deleteCommentVotesByType(42L, "dislike");
        verify(appMovieInteractionDao).insertCommentVote(42L, 112L, 12L, "like");
        verify(appMovieInteractionDao).insertCommentVote(42L, 115L, 15L, "like");
        verify(appMovieInteractionDao).insertCommentVote(42L, 121L, 21L, "dislike");
        verify(appMovieInteractionDao, times(1)).refreshCommentVoteCounts(42L);
        verify(appMovieInteractionDao).refreshMovieCommentStats(9L);
    }

    private AppMovieInteractionServiceImpl newService() {
        AppMovieInteractionServiceImpl service = new AppMovieInteractionServiceImpl();
        ReflectionTestUtils.setField(service, "appMovieInteractionDao", appMovieInteractionDao);
        ReflectionTestUtils.setField(service, "sensitivewordsService", sensitivewordsService);
        return service;
    }
}
