package com.controller;

import com.entity.DianyingxinxiEntity;
import com.service.AppMovieCompatibilityService;
import com.utils.PageUtils;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DianyingxinxiControllerCompatibilityTest {

    @Mock
    private AppMovieCompatibilityService appMovieCompatibilityService;

    @Test
    void pageShouldUseCompatibilityServiceInsteadOfLegacyMovieTable() {
        DianyingxinxiController controller = newController();
        Map<String, Object> params = new HashMap<String, Object>();
        DianyingxinxiEntity<?> filter = new DianyingxinxiEntity<Object>();
        PageUtils page = new PageUtils(Collections.singletonList(new DianyingxinxiEntity<Object>()), 1, 10, 1);

        when(appMovieCompatibilityService.queryLegacyMoviePage(params, filter)).thenReturn(page);

        R result = controller.page(params, filter, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(appMovieCompatibilityService).queryLegacyMoviePage(params, filter);
    }

    @Test
    void infoShouldUseCompatibilityServiceAndKeepLegacyIdContract() {
        DianyingxinxiController controller = newController();
        DianyingxinxiEntity<Object> movie = new DianyingxinxiEntity<Object>();
        movie.setId(-15L);
        movie.setDianyingmingcheng("Compat Movie");

        doReturn(movie).when(appMovieCompatibilityService).getLegacyMovieById(-15L, true);

        R result = controller.info(-15L);

        assertEquals(0, result.get("code"));
        assertEquals(movie, result.get("data"));
        verify(appMovieCompatibilityService).getLegacyMovieById(-15L, true);
    }

    @Test
    void saveShouldUseCompatibilityServiceInsteadOfLegacyInsert() {
        DianyingxinxiController controller = newController();
        DianyingxinxiEntity<?> payload = new DianyingxinxiEntity<Object>();
        payload.setDianyingmingcheng("Compat Save");

        when(appMovieCompatibilityService.saveLegacyMovie(payload)).thenReturn(-23L);

        R result = controller.save(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(-23L, result.get("data"));
        verify(appMovieCompatibilityService).saveLegacyMovie(payload);
    }

    @Test
    void autoSort2ShouldUseCompatibilityRecommendationsInsteadOfLegacyStoreupTable() {
        DianyingxinxiController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", "6");
        DianyingxinxiEntity<?> filter = new DianyingxinxiEntity<Object>();
        PageUtils page = new PageUtils(Collections.singletonList(new DianyingxinxiEntity<Object>()), 1, 6, 1);

        when(appMovieCompatibilityService.queryLegacyRecommendedMovies(params, filter, 12L)).thenReturn(page);

        R result = controller.autoSort2(params, filter, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(appMovieCompatibilityService).queryLegacyRecommendedMovies(params, filter, 12L);
    }

    @Test
    void valueShouldUseCompatibilityStatsInsteadOfLegacyMovieDao() throws Exception {
        DianyingxinxiController controller = newController();
        List<Map<String, Object>> stats = Collections.<Map<String, Object>>singletonList(new HashMap<String, Object>() {{
            put("dianyingleixing", "科幻");
            put("total", 18);
        }});

        when(appMovieCompatibilityService.selectLegacyMovieValueStats("dianyingleixing", "clicknum", null)).thenReturn(stats);

        R result = controller.value("clicknum", "dianyingleixing", new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(stats, result.get("data"));
        verify(appMovieCompatibilityService).selectLegacyMovieValueStats("dianyingleixing", "clicknum", null);
    }

    @Test
    void valueShouldIgnoreLegacyFixtureFileAndKeepUsingCompatibilityStats() throws Exception {
        DianyingxinxiController controller = newController();
        Path fixturePath = Paths.get("value_dianyingxinxi_dianyingleixing_clicknum_timeType.json");
        List<Map<String, Object>> stats = Collections.<Map<String, Object>>singletonList(new HashMap<String, Object>() {{
            put("dianyingleixing", "悬疑");
            put("total", 6);
        }});
        Files.write(fixturePath, "[{\"dianyingleixing\":\"旧夹具\",\"total\":999}]".getBytes(StandardCharsets.UTF_8));
        when(appMovieCompatibilityService.selectLegacyMovieValueStats("dianyingleixing", "clicknum", null)).thenReturn(stats);

        try {
            R result = controller.value("clicknum", "dianyingleixing", new MockHttpServletRequest());

            assertEquals(0, result.get("code"));
            assertEquals(stats, result.get("data"));
            verify(appMovieCompatibilityService).selectLegacyMovieValueStats("dianyingleixing", "clicknum", null);
        } finally {
            Files.deleteIfExists(fixturePath);
        }
    }

    @Test
    void groupShouldUseCompatibilityStatsInsteadOfLegacyMovieDao() throws Exception {
        DianyingxinxiController controller = newController();
        List<Map<String, Object>> stats = Collections.<Map<String, Object>>singletonList(new HashMap<String, Object>() {{
            put("dianyingleixing", "剧情");
            put("total", 2);
        }});

        when(appMovieCompatibilityService.selectLegacyMovieGroupStats("dianyingleixing")).thenReturn(stats);

        R result = controller.group("dianyingleixing", new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(stats, result.get("data"));
        verify(appMovieCompatibilityService).selectLegacyMovieGroupStats("dianyingleixing");
    }

    private DianyingxinxiController newController() {
        DianyingxinxiController controller = new DianyingxinxiController();
        ReflectionTestUtils.setField(controller, "appMovieCompatibilityService", appMovieCompatibilityService);
        return controller;
    }
}
