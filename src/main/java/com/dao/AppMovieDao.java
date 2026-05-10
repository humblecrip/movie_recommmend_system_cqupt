package com.dao;

import com.entity.vo.AppMovieAdminVO;
import com.entity.vo.AppMovieDashboardOverviewVO;
import com.entity.vo.AppMovieDetailVO;
import com.entity.vo.AppMovieListVO;
import com.entity.vo.AppMovieMetricVO;
import com.entity.vo.AppMovieRecommendationActionVO;
import com.entity.vo.AppMovieTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * app_* 电影只读查询 DAO。
 */
public interface AppMovieDao {

    List<AppMovieTypeVO> selectTypes();

    int countMovies(@Param("params") Map<String, Object> params);

    List<AppMovieListVO> selectFrontList(@Param("params") Map<String, Object> params);

    List<AppMovieRecommendationActionVO> selectPositiveRecommendationActions();

    List<AppMovieListVO> selectMoviesByIds(@Param("ids") List<Long> ids);

    List<AppMovieAdminVO> selectAdminPage(@Param("params") Map<String, Object> params);

    AppMovieDetailVO selectDetail(@Param("id") Long id);

    List<AppMovieListVO> selectSimilarMovies(@Param("id") Long id, @Param("typeId") Long typeId, @Param("limit") Integer limit);

    AppMovieDashboardOverviewVO selectDashboardOverview();

    List<AppMovieListVO> selectTopClickedMovies(@Param("limit") Integer limit);

    List<AppMovieListVO> selectTopLikedMovies(@Param("limit") Integer limit);

    List<AppMovieListVO> selectTopFavoritedMovies(@Param("limit") Integer limit);

    List<AppMovieMetricVO> selectTypeDistribution(@Param("limit") Integer limit);

    List<AppMovieMetricVO> selectReleaseYearDistribution(@Param("limit") Integer limit);
}
