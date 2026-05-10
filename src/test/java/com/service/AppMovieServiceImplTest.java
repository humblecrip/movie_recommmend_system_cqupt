package com.service;

import com.dao.AppMovieDao;
import com.entity.vo.AppMovieListVO;
import com.entity.vo.AppMovieRecommendationActionVO;
import com.service.impl.AppMovieServiceImpl;
import com.utils.PageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppMovieServiceImplTest {

    @Mock
    private AppMovieDao appMovieDao;

    @Test
    void queryFrontRecommendedPageShouldFallbackToTopRatedWhenLegacyUserMissing() {
        AppMovieServiceImpl service = newService();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", "2");
        List<AppMovieListVO> fallbackMovies = Arrays.asList(
                movie(20L, "Fallback A", 9.6D),
                movie(21L, "Fallback B", 9.4D)
        );

        when(appMovieDao.countMovies(anyMap())).thenReturn(2);
        when(appMovieDao.selectFrontList(anyMap())).thenReturn(fallbackMovies);

        PageUtils result = service.queryFrontRecommendedPage(params, null);

        assertEquals(2, result.getList().size());
        verify(appMovieDao, never()).selectPositiveRecommendationActions();
        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(appMovieDao).selectFrontList(paramsCaptor.capture());
        assertEquals("m.total_score", paramsCaptor.getValue().get("sortColumn"));
        assertEquals("desc", paramsCaptor.getValue().get("orderKeyword"));
    }

    @Test
    void queryFrontRecommendedPageShouldFallbackWhenCurrentUserHasNoPositiveActions() {
        AppMovieServiceImpl service = newService();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", "2");
        List<AppMovieListVO> fallbackMovies = Arrays.asList(
                movie(31L, "Fallback C", 9.5D),
                movie(32L, "Fallback D", 9.3D)
        );

        when(appMovieDao.selectPositiveRecommendationActions()).thenReturn(Collections.singletonList(
                action(7L, 1001L, 1.0D)
        ));
        when(appMovieDao.countMovies(anyMap())).thenReturn(2);
        when(appMovieDao.selectFrontList(anyMap())).thenReturn(fallbackMovies);

        PageUtils result = service.queryFrontRecommendedPage(params, 99L);

        assertEquals(2, result.getList().size());
        verify(appMovieDao).selectPositiveRecommendationActions();
        verify(appMovieDao, never()).selectMoviesByIds(Collections.singletonList(1001L));
    }

    @Test
    void queryFrontRecommendedPageShouldReturnCollaborativeFilteringMoviesInRecommendationOrder() {
        AppMovieServiceImpl service = newService();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", "2");
        List<AppMovieRecommendationActionVO> actions = Arrays.asList(
                action(1L, 101L, 1.0D),
                action(1L, 102L, 0.8D),
                action(2L, 101L, 1.0D),
                action(2L, 103L, 1.0D),
                action(3L, 101L, 1.0D),
                action(3L, 102L, 0.8D),
                action(3L, 104L, 1.0D)
        );
        List<AppMovieListVO> recommendedMovies = Arrays.asList(
                movie(104L, "Movie 104", 7.8D),
                movie(103L, "Movie 103", 9.9D)
        );

        when(appMovieDao.selectPositiveRecommendationActions()).thenReturn(actions);
        when(appMovieDao.selectMoviesByIds(Arrays.asList(103L, 104L))).thenReturn(recommendedMovies);

        PageUtils result = service.queryFrontRecommendedPage(params, 1L);

        List<?> movieList = result.getList();
        assertEquals(2, movieList.size());
        assertEquals(103L, ((AppMovieListVO) movieList.get(0)).getId());
        assertEquals(104L, ((AppMovieListVO) movieList.get(1)).getId());
        verify(appMovieDao).selectPositiveRecommendationActions();
        verify(appMovieDao).selectMoviesByIds(Arrays.asList(103L, 104L));
    }

    private AppMovieServiceImpl newService() {
        AppMovieServiceImpl service = new AppMovieServiceImpl();
        ReflectionTestUtils.setField(service, "appMovieDao", appMovieDao);
        return service;
    }

    private AppMovieListVO movie(Long id, String title, Double totalScore) {
        AppMovieListVO movie = new AppMovieListVO();
        movie.setId(id);
        movie.setTitle(title);
        movie.setTotalScore(totalScore);
        return movie;
    }

    private AppMovieRecommendationActionVO action(Long legacyUserId, Long movieId, Double preferenceScore) {
        AppMovieRecommendationActionVO action = new AppMovieRecommendationActionVO();
        action.setLegacyUserId(legacyUserId);
        action.setMovieId(movieId);
        action.setPreferenceScore(preferenceScore);
        return action;
    }
}
