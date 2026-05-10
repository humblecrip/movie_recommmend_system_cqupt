package com.service;

import com.entity.vo.AppMovieDashboardOverviewVO;
import com.entity.vo.AppMovieDetailVO;
import com.entity.vo.AppMovieTypeVO;
import com.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * app_* 电影只读服务。
 */
public interface AppMovieService {

    List<AppMovieTypeVO> listTypes();

    PageUtils queryFrontPage(Map<String, Object> params);

    PageUtils queryFrontRecommendedPage(Map<String, Object> params, Long legacyUserId);

    AppMovieDetailVO getFrontDetail(Long id);

    PageUtils queryAdminPage(Map<String, Object> params);

    AppMovieDashboardOverviewVO getDashboardOverview();
}
