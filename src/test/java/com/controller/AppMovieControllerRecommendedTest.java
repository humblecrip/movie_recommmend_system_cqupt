package com.controller;

import com.entity.TokenEntity;
import com.interceptor.AuthorizationInterceptor;
import com.service.AppMovieService;
import com.service.TokenService;
import com.utils.PageUtils;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppMovieControllerRecommendedTest {

    @Mock
    private AppMovieService appMovieService;

    @Mock
    private TokenService tokenService;

    @Test
    void frontRecommendedShouldResolveLegacyUserIdFromTokenHeader() {
        AppMovieController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthorizationInterceptor.LOGIN_TOKEN_KEY, "front-token");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", "6");
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUserid(42L);
        tokenEntity.setTablename("yonghu");
        PageUtils page = new PageUtils(Collections.emptyList(), 0, 6, 1);

        when(tokenService.getTokenEntity("front-token")).thenReturn(tokenEntity);
        when(appMovieService.queryFrontRecommendedPage(params, 42L)).thenReturn(page);

        R result = controller.frontRecommended(params, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(tokenService).getTokenEntity("front-token");
        verify(appMovieService).queryFrontRecommendedPage(params, 42L);
    }

    @Test
    void frontRecommendedShouldFallbackToAnonymousWhenTokenIsMissingOrInvalid() {
        AppMovieController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthorizationInterceptor.LOGIN_TOKEN_KEY, "invalid-token");
        Map<String, Object> params = new HashMap<String, Object>();
        PageUtils page = new PageUtils(Collections.emptyList(), 0, 6, 1);

        when(tokenService.getTokenEntity("invalid-token")).thenReturn(null);
        when(appMovieService.queryFrontRecommendedPage(params, null)).thenReturn(page);

        R result = controller.frontRecommended(params, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(tokenService).getTokenEntity("invalid-token");
        verify(appMovieService).queryFrontRecommendedPage(params, null);
    }

    @Test
    void frontRecommendedShouldIgnoreAdminTokenForPersonalization() {
        AppMovieController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthorizationInterceptor.LOGIN_TOKEN_KEY, "admin-token");
        Map<String, Object> params = new HashMap<String, Object>();
        PageUtils page = new PageUtils(Collections.emptyList(), 0, 6, 1);
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUserid(7L);
        tokenEntity.setTablename("users");

        when(tokenService.getTokenEntity("admin-token")).thenReturn(tokenEntity);
        when(appMovieService.queryFrontRecommendedPage(params, null)).thenReturn(page);

        R result = controller.frontRecommended(params, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(tokenService).getTokenEntity("admin-token");
        verify(appMovieService).queryFrontRecommendedPage(params, null);
    }

    private AppMovieController newController() {
        AppMovieController controller = new AppMovieController();
        ReflectionTestUtils.setField(controller, "appMovieService", appMovieService);
        ReflectionTestUtils.setField(controller, "tokenService", tokenService);
        return controller;
    }
}
