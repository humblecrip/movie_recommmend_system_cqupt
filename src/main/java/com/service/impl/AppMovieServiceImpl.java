package com.service.impl;

import com.dao.AppMovieDao;
import com.entity.vo.AppMovieDashboardOverviewVO;
import com.entity.vo.AppMovieDetailVO;
import com.entity.vo.AppMovieListVO;
import com.entity.vo.AppMovieMetricVO;
import com.entity.vo.AppMovieRecommendationActionVO;
import com.entity.vo.AppMovieTypeVO;
import com.service.AppMovieService;
import com.utils.PageUtils;
import com.utils.UserBasedCollaborativeFiltering;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * app_* 电影只读服务实现。
 */
@Service("appMovieService")
public class AppMovieServiceImpl implements AppMovieService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int RELATED_MOVIE_LIMIT = 6;
    private static final int DASHBOARD_LIST_LIMIT = 8;
    private static final String FALLBACK_SORT_COLUMN = "m.total_score";
    private static final String FALLBACK_SORT_ORDER = "desc";
    private static final int RECOMMENDATION_FETCH_MULTIPLIER = 3;

    private static final Map<String, String> SORT_COLUMNS;

    static {
        Map<String, String> columns = new LinkedHashMap<String, String>();
        columns.put("id", "m.id");
        columns.put("title", "m.title");
        columns.put("typeName", "typeName");
        columns.put("regionName", "regionName");
        columns.put("releaseDate", "m.release_date");
        columns.put("directorName", "directorName");
        columns.put("likeCount", "m.like_count");
        columns.put("dislikeCount", "m.dislike_count");
        columns.put("clickCount", "m.click_count");
        columns.put("commentCount", "m.comment_count");
        columns.put("favoriteCount", "m.favorite_count");
        columns.put("totalScore", "m.total_score");
        columns.put("createdAt", "m.created_at");
        columns.put("updatedAt", "m.updated_at");
        SORT_COLUMNS = Collections.unmodifiableMap(columns);
    }

    @Autowired
    private AppMovieDao appMovieDao;

    @Override
    public List<AppMovieTypeVO> listTypes() {
        List<AppMovieTypeVO> types = appMovieDao.selectTypes();
        return types == null ? new ArrayList<AppMovieTypeVO>() : types;
    }

    @Override
    public PageUtils queryFrontPage(Map<String, Object> params) {
        Map<String, Object> queryParams = normalizePageParams(params, "m.click_count", "desc");
        int total = appMovieDao.countMovies(queryParams);
        List<AppMovieListVO> records = total == 0
                ? new ArrayList<AppMovieListVO>()
                : appMovieDao.selectFrontList(queryParams);
        hydrateMovieCards(records);
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    public PageUtils queryFrontRecommendedPage(Map<String, Object> params, Long legacyUserId) {
        Map<String, Object> queryParams = normalizePageParams(params, FALLBACK_SORT_COLUMN, FALLBACK_SORT_ORDER);
        int limit = (Integer) queryParams.get("limit");

        if (legacyUserId == null) {
            return queryStableRecommendedFallback(queryParams);
        }

        Map<String, Map<String, Double>> userRatings = buildRecommendationMatrix(appMovieDao.selectPositiveRecommendationActions());
        String targetUserKey = String.valueOf(legacyUserId);
        if (!userRatings.containsKey(targetUserKey) || userRatings.get(targetUserKey).isEmpty()) {
            return queryStableRecommendedFallback(queryParams);
        }

        List<Long> recommendedIds = parseRecommendedIds(
                new UserBasedCollaborativeFiltering(userRatings).recommendItems(targetUserKey, limit)
        );
        if (recommendedIds.isEmpty()) {
            return queryStableRecommendedFallback(queryParams);
        }

        List<AppMovieListVO> recommendedMovies = orderMoviesByRecommendation(
                recommendedIds,
                appMovieDao.selectMoviesByIds(recommendedIds),
                queryParams
        );
        if (recommendedMovies.isEmpty()) {
            return queryStableRecommendedFallback(queryParams);
        }

        Set<Long> seenIds = new LinkedHashSet<Long>();
        List<AppMovieListVO> finalMovies = new ArrayList<AppMovieListVO>();
        appendUniqueMovies(finalMovies, recommendedMovies, seenIds, limit);
        if (finalMovies.size() < limit) {
            appendUniqueMovies(finalMovies, selectFallbackMovies(queryParams, limit), seenIds, limit);
        }
        hydrateMovieCards(finalMovies);
        return new PageUtils(finalMovies, finalMovies.size(), limit, (Integer) queryParams.get("page"));
    }

    @Override
    public AppMovieDetailVO getFrontDetail(Long id) {
        if (id == null) {
            return null;
        }
        AppMovieDetailVO detail = appMovieDao.selectDetail(id);
        if (detail == null) {
            return null;
        }
        hydrateDetail(detail);
        List<AppMovieListVO> similarMovies = appMovieDao.selectSimilarMovies(detail.getId(), detail.getTypeId(), RELATED_MOVIE_LIMIT);
        hydrateMovieCards(similarMovies);
        detail.setSimilarMovies(similarMovies == null ? new ArrayList<AppMovieListVO>() : similarMovies);
        return detail;
    }

    @Override
    public PageUtils queryAdminPage(Map<String, Object> params) {
        Map<String, Object> queryParams = normalizePageParams(params, "m.updated_at", "desc");
        int total = appMovieDao.countMovies(queryParams);
        List<? extends AppMovieListVO> records = total == 0
                ? new ArrayList<AppMovieListVO>()
                : appMovieDao.selectAdminPage(queryParams);
        hydrateMovieCards(records);
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    public AppMovieDashboardOverviewVO getDashboardOverview() {
        AppMovieDashboardOverviewVO overview = appMovieDao.selectDashboardOverview();
        if (overview == null) {
            overview = new AppMovieDashboardOverviewVO();
        }
        overview.setTopClickedMovies(safeMovieList(appMovieDao.selectTopClickedMovies(DASHBOARD_LIST_LIMIT)));
        overview.setTopLikedMovies(safeMovieList(appMovieDao.selectTopLikedMovies(DASHBOARD_LIST_LIMIT)));
        overview.setTopFavoritedMovies(safeMovieList(appMovieDao.selectTopFavoritedMovies(DASHBOARD_LIST_LIMIT)));
        overview.setTypeDistribution(safeMetricList(appMovieDao.selectTypeDistribution(DASHBOARD_LIST_LIMIT)));
        overview.setReleaseYearDistribution(safeMetricList(appMovieDao.selectReleaseYearDistribution(DASHBOARD_LIST_LIMIT)));
        return overview;
    }

    private Map<String, Object> normalizePageParams(Map<String, Object> params, String defaultSortColumn, String defaultOrder) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        int page = parsePositiveInt(valueOf(params, "page"), DEFAULT_PAGE);
        int limit = parsePositiveInt(valueOf(params, "limit"), DEFAULT_LIMIT);
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        String sort = valueOf(params, "sort");
        String sortColumn = SORT_COLUMNS.get(sort);
        if (StringUtils.isBlank(sortColumn)) {
            sortColumn = defaultSortColumn;
        }

        String order = valueOf(params, "order");
        String orderKeyword = "asc".equalsIgnoreCase(order) ? "asc" : defaultOrder;
        if (!"asc".equalsIgnoreCase(orderKeyword) && !"desc".equalsIgnoreCase(orderKeyword)) {
            orderKeyword = "desc";
        }

        queryParams.put("page", page);
        queryParams.put("limit", limit);
        queryParams.put("offset", (page - 1) * limit);
        queryParams.put("sortColumn", sortColumn);
        queryParams.put("orderKeyword", orderKeyword);
        queryParams.put("title", blankToNull(valueOf(params, "title")));
        queryParams.put("typeName", blankToNull(valueOf(params, "typeName")));
        return queryParams;
    }

    private List<AppMovieListVO> safeMovieList(List<AppMovieListVO> movies) {
        if (movies == null) {
            return new ArrayList<AppMovieListVO>();
        }
        hydrateMovieCards(movies);
        return movies;
    }

    private List<AppMovieMetricVO> safeMetricList(List<AppMovieMetricVO> metrics) {
        return metrics == null ? new ArrayList<AppMovieMetricVO>() : metrics;
    }

    private PageUtils queryStableRecommendedFallback(Map<String, Object> queryParams) {
        int total = appMovieDao.countMovies(queryParams);
        List<AppMovieListVO> movies = total == 0
                ? new ArrayList<AppMovieListVO>()
                : appMovieDao.selectFrontList(queryParams);
        hydrateMovieCards(movies);
        return new PageUtils(movies, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    private Map<String, Map<String, Double>> buildRecommendationMatrix(List<AppMovieRecommendationActionVO> actions) {
        Map<String, Map<String, Double>> userRatings = new LinkedHashMap<String, Map<String, Double>>();
        if (actions == null) {
            return userRatings;
        }
        for (AppMovieRecommendationActionVO action : actions) {
            if (action == null || action.getLegacyUserId() == null || action.getMovieId() == null) {
                continue;
            }
            String userKey = String.valueOf(action.getLegacyUserId());
            String movieKey = String.valueOf(action.getMovieId());
            Map<String, Double> ratings = userRatings.get(userKey);
            if (ratings == null) {
                ratings = new LinkedHashMap<String, Double>();
                userRatings.put(userKey, ratings);
            }
            Double score = action.getPreferenceScore() == null ? 0D : action.getPreferenceScore();
            if (!ratings.containsKey(movieKey) || ratings.get(movieKey) < score) {
                ratings.put(movieKey, score);
            }
        }
        return userRatings;
    }

    private List<Long> parseRecommendedIds(List<String> recommendationKeys) {
        List<Long> ids = new ArrayList<Long>();
        if (recommendationKeys == null) {
            return ids;
        }
        Set<Long> seenIds = new HashSet<Long>();
        for (String recommendationKey : recommendationKeys) {
            if (StringUtils.isBlank(recommendationKey)) {
                continue;
            }
            try {
                Long movieId = Long.valueOf(recommendationKey.trim());
                if (seenIds.add(movieId)) {
                    ids.add(movieId);
                }
            } catch (NumberFormatException ignored) {
                // 跳过无法解析的历史脏样本，避免污染首页推荐结果。
            }
        }
        return ids;
    }

    private List<AppMovieListVO> orderMoviesByRecommendation(List<Long> recommendedIds,
                                                             List<AppMovieListVO> recommendedMovies,
                                                             Map<String, Object> queryParams) {
        List<AppMovieListVO> orderedMovies = new ArrayList<AppMovieListVO>();
        if (recommendedIds == null || recommendedIds.isEmpty() || recommendedMovies == null || recommendedMovies.isEmpty()) {
            return orderedMovies;
        }
        Map<Long, AppMovieListVO> movieMap = new LinkedHashMap<Long, AppMovieListVO>();
        for (AppMovieListVO movie : recommendedMovies) {
            if (movie != null && movie.getId() != null) {
                movieMap.put(movie.getId(), movie);
            }
        }
        for (Long recommendedId : recommendedIds) {
            AppMovieListVO movie = movieMap.get(recommendedId);
            if (movie != null && matchesRecommendationFilters(movie, queryParams)) {
                orderedMovies.add(movie);
            }
        }
        return orderedMovies;
    }

    private List<AppMovieListVO> selectFallbackMovies(Map<String, Object> queryParams, int limit) {
        Map<String, Object> fallbackParams = new HashMap<String, Object>(queryParams);
        fallbackParams.put("page", 1);
        fallbackParams.put("offset", 0);
        fallbackParams.put("limit", Math.min(MAX_LIMIT, Math.max(limit, limit * RECOMMENDATION_FETCH_MULTIPLIER)));
        List<AppMovieListVO> fallbackMovies = appMovieDao.selectFrontList(fallbackParams);
        return fallbackMovies == null ? new ArrayList<AppMovieListVO>() : fallbackMovies;
    }

    private void appendUniqueMovies(List<AppMovieListVO> target,
                                    List<AppMovieListVO> source,
                                    Set<Long> seenIds,
                                    int limit) {
        if (source == null) {
            return;
        }
        for (AppMovieListVO movie : source) {
            if (movie == null || movie.getId() == null || !seenIds.add(movie.getId())) {
                continue;
            }
            target.add(movie);
            if (target.size() >= limit) {
                return;
            }
        }
    }

    private boolean matchesRecommendationFilters(AppMovieListVO movie, Map<String, Object> queryParams) {
        if (movie == null) {
            return false;
        }
        return matchesKeyword(movie.getTitle(), (String) queryParams.get("title"))
                && matchesKeyword(movie.getTypeName(), (String) queryParams.get("typeName"));
    }

    private boolean matchesKeyword(String value, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        return StringUtils.containsIgnoreCase(StringUtils.defaultString(value), keyword);
    }

    private void hydrateDetail(AppMovieDetailVO detail) {
        List<String> posterUrls = splitPosterUrls(detail.getPosterUrlsCsv());
        detail.setPosterUrls(posterUrls);
        detail.setPosterUrl(posterUrls.isEmpty() ? null : posterUrls.get(0));
        detail.setLikeCount(defaultInt(detail.getLikeCount()));
        detail.setDislikeCount(defaultInt(detail.getDislikeCount()));
        detail.setClickCount(defaultInt(detail.getClickCount()));
        detail.setCommentCount(defaultInt(detail.getCommentCount()));
        detail.setFavoriteCount(defaultInt(detail.getFavoriteCount()));
        detail.setTotalScore(defaultDouble(detail.getTotalScore()));
    }

    private void hydrateMovieCards(List<? extends AppMovieListVO> movies) {
        if (movies == null) {
            return;
        }
        for (AppMovieListVO movie : movies) {
            List<String> posterUrls = splitPosterUrls(movie.getPosterUrlsCsv());
            movie.setPosterUrls(posterUrls);
            movie.setPosterUrl(posterUrls.isEmpty() ? null : posterUrls.get(0));
            movie.setLikeCount(defaultInt(movie.getLikeCount()));
            movie.setDislikeCount(defaultInt(movie.getDislikeCount()));
            movie.setClickCount(defaultInt(movie.getClickCount()));
            movie.setCommentCount(defaultInt(movie.getCommentCount()));
            movie.setFavoriteCount(defaultInt(movie.getFavoriteCount()));
            movie.setTotalScore(defaultDouble(movie.getTotalScore()));
        }
    }

    private List<String> splitPosterUrls(String posterUrlsCsv) {
        List<String> posterUrls = new ArrayList<String>();
        if (StringUtils.isBlank(posterUrlsCsv)) {
            return posterUrls;
        }
        String[] parts = posterUrlsCsv.split(",");
        for (String part : parts) {
            String url = part == null ? null : part.trim();
            if (StringUtils.isNotBlank(url)) {
                posterUrls.add(url);
            }
        }
        return posterUrls;
    }

    private String valueOf(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Double defaultDouble(Double value) {
        return value == null ? 0D : value;
    }
}
