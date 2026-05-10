package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.entity.vo.AppMovieActionToggleRequestVO;
import com.entity.vo.AppMovieCommentCreateRequestVO;
import com.entity.vo.AppMovieCommentVoteRequestVO;
import com.service.AppMovieInteractionService;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AppMovieInteractionControllerTest {

    @Mock
    private AppMovieInteractionService appMovieInteractionService;

    @Test
    void toggleMovieActionShouldReturnErrorWhenServiceReturnsNull() {
        AppMovieInteractionController controller = newController();
        MockHttpServletRequest request = loggedInRequest();
        AppMovieActionToggleRequestVO body = new AppMovieActionToggleRequestVO();
        body.setMovieId(2L);
        body.setActionType("favorite");

        when(appMovieInteractionService.toggleMovieAction(12L, body)).thenReturn(null);

        R result = controller.toggleMovieAction(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("电影互动操作失败", result.get("msg"));
        verify(appMovieInteractionService).toggleMovieAction(12L, body);
    }

    @Test
    void cancelFavoriteShouldReturnErrorWhenServiceReturnsNull() {
        AppMovieInteractionController controller = newController();
        MockHttpServletRequest request = loggedInRequest();
        AppMovieActionToggleRequestVO body = new AppMovieActionToggleRequestVO();
        body.setMovieId(2L);

        when(appMovieInteractionService.cancelFavorite(12L, body)).thenReturn(null);

        R result = controller.cancelFavorite(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("取消收藏失败", result.get("msg"));
        assertEquals("favorite", body.getActionType());
        verify(appMovieInteractionService).cancelFavorite(12L, body);
    }

    @Test
    void addCommentShouldReturnErrorWhenServiceReturnsNull() {
        AppMovieInteractionController controller = newController();
        MockHttpServletRequest request = loggedInRequest();
        AppMovieCommentCreateRequestVO body = new AppMovieCommentCreateRequestVO();
        body.setMovieId(2L);
        body.setContentHtml("<p>不错</p>");
        body.setRating(4D);

        when(appMovieInteractionService.addComment(12L, body)).thenReturn(null);

        R result = controller.addComment(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("评论发布失败", result.get("msg"));
        verify(appMovieInteractionService).addComment(12L, body);
    }

    @Test
    void deleteCommentShouldReturnErrorWhenServiceReturnsFalse() {
        AppMovieInteractionController controller = newController();
        MockHttpServletRequest request = loggedInRequest();
        AppMovieCommentVoteRequestVO body = new AppMovieCommentVoteRequestVO();
        body.setCommentId(5L);

        when(appMovieInteractionService.deleteComment(12L, 5L)).thenReturn(false);

        R result = controller.deleteCommentByBody(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("评论不存在或无权删除", result.get("msg"));
        verify(appMovieInteractionService).deleteComment(12L, 5L);
    }

    @Test
    void toggleCommentVoteShouldReturnErrorWhenServiceReturnsNull() {
        AppMovieInteractionController controller = newController();
        MockHttpServletRequest request = loggedInRequest();
        AppMovieCommentVoteRequestVO body = new AppMovieCommentVoteRequestVO();
        body.setCommentId(5L);
        body.setVoteType("like");

        when(appMovieInteractionService.toggleCommentVote(12L, body)).thenReturn(null);

        R result = controller.toggleCommentVote(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("评论互动操作失败", result.get("msg"));
        verify(appMovieInteractionService).toggleCommentVote(12L, body);
    }

    @Test
    void commentPageShouldUseLegacyUserIdParamForPublicVoteState() {
        AppMovieInteractionController controller = new AppMovieInteractionController();
        ReflectionTestUtils.setField(controller, "appMovieInteractionService", appMovieInteractionService);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("movieId", 2L);
        params.put("legacyUserId", "12");

        when(appMovieInteractionService.queryComments(12L, params)).thenReturn(null);

        R result = controller.commentPage(params, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        verify(appMovieInteractionService).queryComments(12L, params);
    }

    private AppMovieInteractionController newController() {
        AppMovieInteractionController controller = new AppMovieInteractionController();
        ReflectionTestUtils.setField(controller, "appMovieInteractionService", appMovieInteractionService);
        return controller;
    }

    private MockHttpServletRequest loggedInRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        return request;
    }
}
