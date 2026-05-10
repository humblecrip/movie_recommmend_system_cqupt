package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.entity.DiscussdianyingxinxiEntity;
import com.service.AppMovieInteractionService;
import com.utils.PageUtils;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DiscussdianyingxinxiControllerCompatibilityTest {

    @Mock
    private AppMovieInteractionService appMovieInteractionService;

    @Test
    void pageShouldUseAppMovieInteractionCompatibilityQuery() {
        DiscussdianyingxinxiController controller = newController();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", "1");
        params.put("limit", "10");
        DiscussdianyingxinxiEntity<?> filter = new DiscussdianyingxinxiEntity<Object>();
        filter.setRefid(9L);

        PageUtils page = new PageUtils(new ArrayList<Object>(), 0, 10, 1);
        when(appMovieInteractionService.queryLegacyComments(params, filter)).thenReturn(page);

        R result = controller.page(params, filter, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(appMovieInteractionService).queryLegacyComments(params, filter);
    }

    @Test
    void addShouldBridgeToAppMovieCommentUsingSessionUserId() {
        DiscussdianyingxinxiController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);

        DiscussdianyingxinxiEntity<?> payload = new DiscussdianyingxinxiEntity<Object>();
        payload.setRefid(3L);
        payload.setContent("<p>桥接评论</p>");
        payload.setScore(4.0);

        when(appMovieInteractionService.createLegacyComment(12L, payload)).thenReturn(88L);

        R result = controller.add(payload, request);

        assertEquals(0, result.get("code"));
        assertEquals(88L, result.get("data"));
        verify(appMovieInteractionService).createLegacyComment(12L, payload);
    }

    @Test
    void addShouldReturnExplicitFailureWhenLegacyMovieMappingMissing() {
        DiscussdianyingxinxiController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);

        DiscussdianyingxinxiEntity<?> payload = new DiscussdianyingxinxiEntity<Object>();
        payload.setRefid(3L);
        payload.setContent("<p>桥接评论</p>");

        when(appMovieInteractionService.createLegacyComment(12L, payload))
                .thenThrow(new IllegalStateException("旧电影ID未映射到 app_movie，拒绝直连旧表"));

        R result = controller.add(payload, request);

        assertEquals(500, result.get("code"));
        assertEquals("旧电影ID未映射到 app_movie，拒绝直连旧表", result.get("msg"));
        verify(appMovieInteractionService).createLegacyComment(12L, payload);
    }

    @Test
    void updateShouldUseCompatibilityBridge() {
        DiscussdianyingxinxiController controller = newController();
        DiscussdianyingxinxiEntity<?> payload = new DiscussdianyingxinxiEntity<Object>();
        payload.setId(18L);
        payload.setReply("<p>后台回复</p>");
        payload.setIstop(1);

        when(appMovieInteractionService.updateLegacyComment(payload)).thenReturn(true);

        R result = controller.update(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        verify(appMovieInteractionService).updateLegacyComment(payload);
    }

    private DiscussdianyingxinxiController newController() {
        DiscussdianyingxinxiController controller = new DiscussdianyingxinxiController();
        ReflectionTestUtils.setField(controller, "appMovieInteractionService", appMovieInteractionService);
        return controller;
    }
}
