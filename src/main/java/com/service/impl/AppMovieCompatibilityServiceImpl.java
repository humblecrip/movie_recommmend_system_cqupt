package com.service.impl;

import com.dao.AppMovieCompatibilityDao;
import com.entity.DianyingleixingEntity;
import com.entity.DianyingxinxiEntity;
import com.entity.StoreupEntity;
import com.entity.view.StoreupView;
import com.service.AppMovieCompatibilityService;
import com.utils.PageUtils;
import com.utils.UserBasedCollaborativeFiltering;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 电影/类型旧 URL 兼容服务，底层统一桥接 app_movie / app_movie_type。
 */
@Service("appMovieCompatibilityService")
public class AppMovieCompatibilityServiceImpl implements AppMovieCompatibilityService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final String TYPE_SOURCE_NOTE = "compat:dianyingleixing";
    private static final String POSTER_MEDIA_ROLE = "poster";
    private static final String DIRECTOR_RELATION_TYPE = "director";
    private static final String CAST_RELATION_TYPE = "cast";
    private static final String DIRECTOR_SOURCE_NOTE = "compat:dianyingxinxi.daoyan";
    private static final String CAST_SOURCE_NOTE = "compat:dianyingxinxi.zhuyan";

    private static final Map<String, String> MOVIE_SORT_COLUMNS;
    private static final Map<String, String> TYPE_SORT_COLUMNS;

    static {
        Map<String, String> movieColumns = new LinkedHashMap<String, String>();
        movieColumns.put("id", "m.id");
        movieColumns.put("dianyingmingcheng", "m.title");
        movieColumns.put("haibao", "haibao");
        movieColumns.put("dianyingleixing", "dianyingleixing");
        movieColumns.put("quyu", "quyu");
        movieColumns.put("shangyingshijian", "m.release_date");
        movieColumns.put("daoyan", "daoyan");
        movieColumns.put("zhuyan", "zhuyan");
        movieColumns.put("juqingjianjie", "m.synopsis");
        movieColumns.put("dianyingxiangqing", "m.detail_html");
        movieColumns.put("thumbsupnum", "m.like_count");
        movieColumns.put("crazilynum", "m.dislike_count");
        movieColumns.put("clicktime", "m.last_clicked_at");
        movieColumns.put("clicknum", "m.click_count");
        movieColumns.put("discussnum", "m.comment_count");
        movieColumns.put("totalscore", "m.total_score");
        movieColumns.put("storeupnum", "m.favorite_count");
        movieColumns.put("addtime", "m.created_at");
        MOVIE_SORT_COLUMNS = Collections.unmodifiableMap(movieColumns);

        Map<String, String> typeColumns = new LinkedHashMap<String, String>();
        typeColumns.put("id", "t.id");
        typeColumns.put("dianyingleixing", "t.type_name");
        typeColumns.put("addtime", "t.created_at");
        TYPE_SORT_COLUMNS = Collections.unmodifiableMap(typeColumns);
    }

    @Autowired
    private AppMovieCompatibilityDao appMovieCompatibilityDao;

    @Override
    public PageUtils queryLegacyMoviePage(Map<String, Object> params, DianyingxinxiEntity<?> filter) {
        Map<String, Object> queryParams = normalizeMovieParams(params, filter);
        Long compatId = extractMovieCompatId(params, filter);
        if (compatId != null) {
            DianyingxinxiEntity<?> movie = getLegacyMovieById(compatId, false);
            List<DianyingxinxiEntity<?>> records = new ArrayList<DianyingxinxiEntity<?>>();
            if (movie != null) {
                records.add(movie);
            }
            return new PageUtils(records, records.size(), (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
        }

        int total = appMovieCompatibilityDao.countLegacyMovies(
                (String) queryParams.get("titleKeyword"),
                (String) queryParams.get("typeKeyword"),
                (String) queryParams.get("regionKeyword"),
                (String) queryParams.get("directorKeyword"),
                (String) queryParams.get("castKeyword")
        );
        List<DianyingxinxiEntity> records = total == 0
                ? new ArrayList<DianyingxinxiEntity>()
                : appMovieCompatibilityDao.selectLegacyMoviesPage(
                        (String) queryParams.get("titleKeyword"),
                        (String) queryParams.get("typeKeyword"),
                        (String) queryParams.get("regionKeyword"),
                        (String) queryParams.get("directorKeyword"),
                        (String) queryParams.get("castKeyword"),
                        (Integer) queryParams.get("offset"),
                        (Integer) queryParams.get("limit"),
                        (String) queryParams.get("sortColumn"),
                        (String) queryParams.get("orderKeyword")
                );
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    public List<DianyingxinxiEntity> selectLegacyMovieList(DianyingxinxiEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            DianyingxinxiEntity<?> movie = getLegacyMovieById(filter.getId(), false);
            List<DianyingxinxiEntity> single = new ArrayList<DianyingxinxiEntity>();
            if (movie != null) {
                single.add((DianyingxinxiEntity) movie);
            }
            return single;
        }
        Map<String, Object> queryParams = normalizeMovieParams(null, filter);
        return appMovieCompatibilityDao.selectLegacyMovies(
                (String) queryParams.get("titleKeyword"),
                (String) queryParams.get("typeKeyword"),
                (String) queryParams.get("regionKeyword"),
                (String) queryParams.get("directorKeyword"),
                (String) queryParams.get("castKeyword"),
                (String) queryParams.get("sortColumn"),
                (String) queryParams.get("orderKeyword")
        );
    }

    @Override
    public DianyingxinxiEntity<?> selectLegacyMovie(DianyingxinxiEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            return getLegacyMovieById(filter.getId(), false);
        }
        List<DianyingxinxiEntity> movies = selectLegacyMovieList(filter);
        return movies.isEmpty() ? null : movies.get(0);
    }

    @Override
    public DianyingxinxiEntity<?> getLegacyMovieById(Long compatId, boolean incrementClick) {
        Long actualId = resolveActualMovieId(compatId);
        if (actualId == null) {
            return null;
        }
        if (incrementClick) {
            appMovieCompatibilityDao.incrementMovieClick(actualId);
        }
        return appMovieCompatibilityDao.selectLegacyMovieByActualId(actualId);
    }

    @Override
    @Transactional
    public Long saveLegacyMovie(DianyingxinxiEntity<?> movie) {
        if (movie == null) {
            return null;
        }
        String typeName = normalizeKeyword(movie.getDianyingleixing());
        Long typeId = resolveMovieTypeId(typeName);
        int inserted = appMovieCompatibilityDao.insertMovie(
                typeId,
                typeName,
                trimToNull(movie.getDianyingmingcheng()),
                trimToNull(movie.getHaibao()),
                trimToNull(movie.getQuyu()),
                movie.getShangyingshijian(),
                trimToNull(movie.getDaoyan()),
                trimToNull(movie.getZhuyan()),
                trimToNull(movie.getJuqingjianjie()),
                trimToNull(movie.getDianyingxiangqing()),
                defaultInt(movie.getThumbsupnum()),
                defaultInt(movie.getCrazilynum()),
                defaultInt(movie.getClicknum()),
                defaultInt(movie.getDiscussnum()),
                defaultInt(movie.getStoreupnum()),
                defaultDouble(movie.getTotalscore()),
                movie.getClicktime()
        );
        if (inserted <= 0) {
            return null;
        }
        Long actualId = appMovieCompatibilityDao.selectLastInsertId();
        if (actualId == null || actualId <= 0) {
            return null;
        }
        syncMoviePosters(actualId, null, movie.getHaibao());
        syncMovieTypeRelation(actualId, typeId);
        syncMovieRegion(actualId, movie.getQuyu());
        syncMoviePeople(actualId, DIRECTOR_RELATION_TYPE, movie.getDaoyan(), DIRECTOR_SOURCE_NOTE);
        syncMoviePeople(actualId, CAST_RELATION_TYPE, movie.getZhuyan(), CAST_SOURCE_NOTE);
        DianyingxinxiEntity saved = appMovieCompatibilityDao.selectLegacyMovieByActualId(actualId);
        return saved == null ? null : saved.getId();
    }

    @Override
    @Transactional
    public boolean updateLegacyMovie(DianyingxinxiEntity<?> movie) {
        if (movie == null || movie.getId() == null) {
            return false;
        }
        Long actualId = resolveActualMovieId(movie.getId());
        if (actualId == null) {
            return false;
        }
        String typeName = normalizeKeyword(movie.getDianyingleixing());
        Long typeId = resolveMovieTypeId(typeName);
        int updated = appMovieCompatibilityDao.updateMovieByActualId(
                actualId,
                typeId,
                typeName,
                trimToNull(movie.getDianyingmingcheng()),
                trimToNull(movie.getHaibao()),
                trimToNull(movie.getQuyu()),
                movie.getShangyingshijian(),
                trimToNull(movie.getDaoyan()),
                trimToNull(movie.getZhuyan()),
                trimToNull(movie.getJuqingjianjie()),
                trimToNull(movie.getDianyingxiangqing()),
                defaultInt(movie.getThumbsupnum()),
                defaultInt(movie.getCrazilynum()),
                defaultInt(movie.getClicknum()),
                defaultInt(movie.getDiscussnum()),
                defaultInt(movie.getStoreupnum()),
                defaultDouble(movie.getTotalscore()),
                movie.getClicktime()
        );
        if (updated <= 0) {
            return false;
        }
        syncMoviePosters(actualId, movie.getId(), movie.getHaibao());
        syncMovieTypeRelation(actualId, typeId);
        syncMovieRegion(actualId, movie.getQuyu());
        syncMoviePeople(actualId, DIRECTOR_RELATION_TYPE, movie.getDaoyan(), DIRECTOR_SOURCE_NOTE);
        syncMoviePeople(actualId, CAST_RELATION_TYPE, movie.getZhuyan(), CAST_SOURCE_NOTE);
        return true;
    }

    @Override
    @Transactional
    public int deleteLegacyMovies(List<Long> compatIds) {
        if (compatIds == null || compatIds.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        Set<Long> actualIds = new LinkedHashSet<Long>();
        for (Long compatId : compatIds) {
            Long actualId = resolveActualMovieId(compatId);
            if (actualId != null) {
                actualIds.add(actualId);
            }
        }
        for (Long actualId : actualIds) {
            appMovieCompatibilityDao.deleteMovieMediaByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieTypeRelationsByMovieId(actualId);
            appMovieCompatibilityDao.deleteMoviePersonRelationsByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieCommentVotesByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieCommentsByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieActionsByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieLibrariesByMovieId(actualId);
            appMovieCompatibilityDao.deleteMovieOrdersByMovieId(actualId);
            deleted += appMovieCompatibilityDao.deleteMovieByActualId(actualId);
        }
        return deleted;
    }

    @Override
    public PageUtils queryLegacyRecommendedMovies(Map<String, Object> params,
                                                  DianyingxinxiEntity<?> filter,
                                                  Long legacyUserId) {
        Map<String, Object> queryParams = normalizeMovieParams(params, filter);
        int limit = (Integer) queryParams.get("limit");
        if (legacyUserId == null) {
            legacyUserId = parseLong(valueOf(params, "userId"));
        }
        if (legacyUserId == null) {
            queryParams.put("page", 1);
            queryParams.put("offset", 0);
            return queryLegacyMoviePage(queryParams, filter);
        }

        List<StoreupView> favoriteViews = appMovieCompatibilityDao.selectLegacyFavoriteActionsForRecommendation();
        List<StoreupEntity> favoriteActions = new ArrayList<StoreupEntity>();
        if (favoriteViews != null) {
            for (StoreupView view : favoriteViews) {
                favoriteActions.add(new StoreupEntity<StoreupView>(view));
            }
        }
        List<String> recommendations = new UserBasedCollaborativeFiltering().recommendItems(
                favoriteActions,
                "userid",
                "refid",
                String.valueOf(legacyUserId),
                limit
        );

        List<Long> recommendedIds = parseLongList(recommendations);
        List<DianyingxinxiEntity> result = new ArrayList<DianyingxinxiEntity>();
        Set<Long> seenIds = new LinkedHashSet<Long>();
        if (!recommendedIds.isEmpty()) {
            List<DianyingxinxiEntity> recommendedMovies = appMovieCompatibilityDao.selectLegacyMoviesByLegacyIds(recommendedIds);
            Map<Long, DianyingxinxiEntity> movieMap = new LinkedHashMap<Long, DianyingxinxiEntity>();
            for (DianyingxinxiEntity movie : recommendedMovies) {
                movieMap.put(movie.getId(), movie);
            }
            for (Long compatId : recommendedIds) {
                DianyingxinxiEntity movie = movieMap.get(compatId);
                if (movie != null && seenIds.add(movie.getId())) {
                    result.add(movie);
                }
            }
        }

        if (result.size() < limit) {
            List<DianyingxinxiEntity> fallbackMovies = appMovieCompatibilityDao.selectLegacyMovies(
                    (String) queryParams.get("titleKeyword"),
                    (String) queryParams.get("typeKeyword"),
                    (String) queryParams.get("regionKeyword"),
                    (String) queryParams.get("directorKeyword"),
                    (String) queryParams.get("castKeyword"),
                    (String) queryParams.get("sortColumn"),
                    (String) queryParams.get("orderKeyword")
            );
            for (DianyingxinxiEntity movie : fallbackMovies) {
                if (movie != null && seenIds.add(movie.getId())) {
                    result.add(movie);
                }
                if (result.size() >= limit) {
                    break;
                }
            }
        }

        if (result.size() > limit) {
            result = new ArrayList<DianyingxinxiEntity>(result.subList(0, limit));
        }
        return new PageUtils(result, result.size(), limit, 1);
    }

    @Override
    public int countLegacyMovies(Map<String, Object> params, DianyingxinxiEntity<?> filter) {
        Map<String, Object> queryParams = normalizeMovieParams(params, filter);
        Long compatId = extractMovieCompatId(params, filter);
        if (compatId != null) {
            return getLegacyMovieById(compatId, false) == null ? 0 : 1;
        }
        return appMovieCompatibilityDao.countLegacyMovies(
                (String) queryParams.get("titleKeyword"),
                (String) queryParams.get("typeKeyword"),
                (String) queryParams.get("regionKeyword"),
                (String) queryParams.get("directorKeyword"),
                (String) queryParams.get("castKeyword")
        );
    }

    @Override
    public List<Map<String, Object>> selectLegacyMovieValueStats(String xColumnName, String yColumnName, String timeStatType) {
        String validatedXColumnName = requireSupportedLegacyMovieColumn(xColumnName);
        String validatedYColumnName = requireSupportedLegacyMovieNumericColumn(yColumnName);
        List<DianyingxinxiEntity> movies = appMovieCompatibilityDao.selectLegacyMovies(
                null,
                null,
                null,
                null,
                null,
                "m.id",
                "asc"
        );
        Map<String, Double> totals = new LinkedHashMap<String, Double>();
        for (DianyingxinxiEntity movie : movies) {
            String bucketKey = resolveLegacyMovieBucketKey(movie, validatedXColumnName, timeStatType);
            Double totalValue = extractLegacyMovieNumericValue(movie, validatedYColumnName);
            if (bucketKey == null || totalValue == null) {
                continue;
            }
            totals.put(bucketKey, totals.containsKey(bucketKey) ? totals.get(bucketKey) + totalValue : totalValue);
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put(validatedXColumnName, entry.getKey());
            row.put("total", normalizeStatNumber(entry.getValue()));
            result.add(row);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectLegacyMovieGroupStats(String columnName) {
        String validatedColumnName = requireSupportedLegacyMovieColumn(columnName);
        List<DianyingxinxiEntity> movies = appMovieCompatibilityDao.selectLegacyMovies(
                null,
                null,
                null,
                null,
                null,
                "m.id",
                "asc"
        );
        Map<String, Long> counts = new LinkedHashMap<String, Long>();
        for (DianyingxinxiEntity movie : movies) {
            String bucketKey = resolveLegacyMovieBucketKey(movie, validatedColumnName, null);
            if (bucketKey == null) {
                continue;
            }
            counts.put(bucketKey, counts.containsKey(bucketKey) ? counts.get(bucketKey) + 1L : 1L);
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put(validatedColumnName, entry.getKey());
            row.put("total", entry.getValue());
            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean incrementLegacyMovieVote(Long compatId, String type) {
        Long actualId = resolveActualMovieId(compatId);
        if (actualId == null) {
            return false;
        }
        int likeDelta = "1".equals(type) ? 1 : 0;
        int dislikeDelta = "1".equals(type) ? 0 : 1;
        return appMovieCompatibilityDao.incrementMovieVote(actualId, likeDelta, dislikeDelta) > 0;
    }

    @Override
    public PageUtils queryLegacyMovieTypePage(Map<String, Object> params, DianyingleixingEntity<?> filter) {
        Map<String, Object> queryParams = normalizeTypeParams(params, filter);
        Long compatId = extractMovieTypeCompatId(params, filter);
        if (compatId != null) {
            DianyingleixingEntity<?> movieType = getLegacyMovieTypeById(compatId);
            List<DianyingleixingEntity<?>> records = new ArrayList<DianyingleixingEntity<?>>();
            if (movieType != null) {
                records.add(movieType);
            }
            return new PageUtils(records, records.size(), (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
        }

        int total = appMovieCompatibilityDao.countLegacyMovieTypes((String) queryParams.get("typeKeyword"));
        List<DianyingleixingEntity> records = total == 0
                ? new ArrayList<DianyingleixingEntity>()
                : appMovieCompatibilityDao.selectLegacyMovieTypesPage(
                        (String) queryParams.get("typeKeyword"),
                        (Integer) queryParams.get("offset"),
                        (Integer) queryParams.get("limit"),
                        (String) queryParams.get("sortColumn"),
                        (String) queryParams.get("orderKeyword")
                );
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    public List<DianyingleixingEntity> selectLegacyMovieTypeList(DianyingleixingEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            DianyingleixingEntity<?> movieType = getLegacyMovieTypeById(filter.getId());
            List<DianyingleixingEntity> single = new ArrayList<DianyingleixingEntity>();
            if (movieType != null) {
                single.add((DianyingleixingEntity) movieType);
            }
            return single;
        }
        Map<String, Object> queryParams = normalizeTypeParams(null, filter);
        return appMovieCompatibilityDao.selectLegacyMovieTypes(
                (String) queryParams.get("typeKeyword"),
                (String) queryParams.get("sortColumn"),
                (String) queryParams.get("orderKeyword")
        );
    }

    @Override
    public DianyingleixingEntity<?> selectLegacyMovieType(DianyingleixingEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            return getLegacyMovieTypeById(filter.getId());
        }
        List<DianyingleixingEntity> movieTypes = selectLegacyMovieTypeList(filter);
        return movieTypes.isEmpty() ? null : movieTypes.get(0);
    }

    @Override
    public DianyingleixingEntity<?> getLegacyMovieTypeById(Long compatId) {
        Long actualId = resolveActualMovieTypeId(compatId);
        return actualId == null ? null : appMovieCompatibilityDao.selectLegacyMovieTypeByActualId(actualId);
    }

    @Override
    @Transactional
    public Long saveLegacyMovieType(DianyingleixingEntity<?> movieType) {
        String typeName = movieType == null ? null : normalizeKeyword(movieType.getDianyingleixing());
        if (StringUtils.isBlank(typeName)) {
            return null;
        }
        Long existingId = appMovieCompatibilityDao.selectMovieTypeIdByName(typeName);
        if (existingId != null) {
            DianyingleixingEntity existing = appMovieCompatibilityDao.selectLegacyMovieTypeByActualId(existingId);
            return existing == null ? existingId : existing.getId();
        }
        int inserted = appMovieCompatibilityDao.insertMovieType(typeName, TYPE_SOURCE_NOTE);
        if (inserted <= 0) {
            return null;
        }
        Long actualId = appMovieCompatibilityDao.selectMovieTypeIdByName(typeName);
        if (actualId == null) {
            return null;
        }
        DianyingleixingEntity saved = appMovieCompatibilityDao.selectLegacyMovieTypeByActualId(actualId);
        return saved == null ? actualId : saved.getId();
    }

    @Override
    @Transactional
    public boolean updateLegacyMovieType(DianyingleixingEntity<?> movieType) {
        if (movieType == null || movieType.getId() == null) {
            return false;
        }
        Long actualId = resolveActualMovieTypeId(movieType.getId());
        String typeName = normalizeKeyword(movieType.getDianyingleixing());
        if (actualId == null || StringUtils.isBlank(typeName)) {
            return false;
        }
        return appMovieCompatibilityDao.updateMovieTypeByActualId(actualId, typeName, TYPE_SOURCE_NOTE) > 0;
    }

    @Override
    @Transactional
    public int deleteLegacyMovieTypes(List<Long> compatIds) {
        if (compatIds == null || compatIds.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        Set<Long> actualIds = new LinkedHashSet<Long>();
        for (Long compatId : compatIds) {
            Long actualId = resolveActualMovieTypeId(compatId);
            if (actualId != null) {
                actualIds.add(actualId);
            }
        }
        for (Long actualId : actualIds) {
            DianyingleixingEntity movieType = appMovieCompatibilityDao.selectLegacyMovieTypeByActualId(actualId);
            String fallbackTypeName = movieType == null ? null : movieType.getDianyingleixing();
            List<Long> affectedMovieIds = appMovieCompatibilityDao.selectMovieIdsByTypeId(actualId);
            appMovieCompatibilityDao.detachMovieTypeReferences(actualId, fallbackTypeName);
            appMovieCompatibilityDao.deleteMovieTypeRelationsByTypeId(actualId);
            if (affectedMovieIds != null) {
                for (Long movieId : affectedMovieIds) {
                    Long fallbackTypeId = appMovieCompatibilityDao.selectFallbackMovieTypeIdByMovieId(movieId);
                    appMovieCompatibilityDao.updateMoviePrimaryTypeByMovieId(movieId, fallbackTypeId);
                }
            }
            deleted += appMovieCompatibilityDao.deleteMovieTypeByActualId(actualId);
        }
        return deleted;
    }

    @Override
    public List<String> listLegacyTypeNames() {
        List<DianyingleixingEntity> types = appMovieCompatibilityDao.selectLegacyMovieTypes(null, "t.type_name", "asc");
        List<String> names = new ArrayList<String>();
        if (types == null) {
            return names;
        }
        for (DianyingleixingEntity type : types) {
            if (type != null && StringUtils.isNotBlank(type.getDianyingleixing())) {
                names.add(type.getDianyingleixing());
            }
        }
        return names;
    }

    private String requireSupportedLegacyMovieColumn(String columnName) {
        String normalizedColumnName = trimToNull(columnName);
        if (normalizedColumnName == null || !isSupportedLegacyMovieColumn(normalizedColumnName)) {
            throw new IllegalArgumentException("不支持的电影统计字段: " + String.valueOf(columnName));
        }
        return normalizedColumnName;
    }

    private String requireSupportedLegacyMovieNumericColumn(String columnName) {
        String normalizedColumnName = requireSupportedLegacyMovieColumn(columnName);
        if (!isSupportedLegacyMovieNumericColumn(normalizedColumnName)) {
            throw new IllegalArgumentException("电影统计仅支持数值字段聚合: " + String.valueOf(columnName));
        }
        return normalizedColumnName;
    }

    private boolean isSupportedLegacyMovieColumn(String columnName) {
        return extractLegacyMovieRawValue(new DianyingxinxiEntity<Object>(), columnName) != UnsupportedLegacyMovieColumnMarker.INSTANCE;
    }

    private boolean isSupportedLegacyMovieNumericColumn(String columnName) {
        return "id".equals(columnName)
                || "thumbsupnum".equals(columnName)
                || "crazilynum".equals(columnName)
                || "clicknum".equals(columnName)
                || "discussnum".equals(columnName)
                || "totalscore".equals(columnName)
                || "storeupnum".equals(columnName);
    }

    private String resolveLegacyMovieBucketKey(DianyingxinxiEntity<?> movie, String columnName, String timeStatType) {
        Object rawValue = extractLegacyMovieRawValue(movie, columnName);
        if (rawValue == UnsupportedLegacyMovieColumnMarker.INSTANCE || rawValue == null) {
            return null;
        }
        if (rawValue instanceof Date) {
            return formatLegacyMovieTimeBucket((Date) rawValue, timeStatType);
        }
        if (StringUtils.isNotBlank(timeStatType)) {
            throw new IllegalArgumentException("时间统计仅支持日期字段: " + columnName);
        }
        if (rawValue instanceof Number) {
            return String.valueOf(normalizeStatNumber(((Number) rawValue).doubleValue()));
        }
        return trimToNull(String.valueOf(rawValue));
    }

    private String formatLegacyMovieTimeBucket(Date value, String timeStatType) {
        if (value == null) {
            return null;
        }
        String normalizedTimeStatType = trimToNull(timeStatType);
        String pattern;
        if (normalizedTimeStatType == null || "日".equals(normalizedTimeStatType)) {
            pattern = "yyyy-MM-dd";
        } else if ("月".equals(normalizedTimeStatType)) {
            pattern = "yyyy-MM";
        } else if ("年".equals(normalizedTimeStatType)) {
            pattern = "yyyy";
        } else {
            throw new IllegalArgumentException("不支持的时间统计类型: " + timeStatType);
        }
        return new SimpleDateFormat(pattern).format(value);
    }

    private Double extractLegacyMovieNumericValue(DianyingxinxiEntity<?> movie, String columnName) {
        Object rawValue = extractLegacyMovieRawValue(movie, columnName);
        if (!(rawValue instanceof Number)) {
            return null;
        }
        return ((Number) rawValue).doubleValue();
    }

    private Object extractLegacyMovieRawValue(DianyingxinxiEntity<?> movie, String columnName) {
        if (movie == null) {
            movie = new DianyingxinxiEntity<Object>();
        }
        if ("id".equals(columnName)) {
            return movie.getId();
        }
        if ("dianyingmingcheng".equals(columnName)) {
            return movie.getDianyingmingcheng();
        }
        if ("haibao".equals(columnName)) {
            return movie.getHaibao();
        }
        if ("dianyingleixing".equals(columnName)) {
            return movie.getDianyingleixing();
        }
        if ("quyu".equals(columnName)) {
            return movie.getQuyu();
        }
        if ("shangyingshijian".equals(columnName)) {
            return movie.getShangyingshijian();
        }
        if ("daoyan".equals(columnName)) {
            return movie.getDaoyan();
        }
        if ("zhuyan".equals(columnName)) {
            return movie.getZhuyan();
        }
        if ("juqingjianjie".equals(columnName)) {
            return movie.getJuqingjianjie();
        }
        if ("dianyingxiangqing".equals(columnName)) {
            return movie.getDianyingxiangqing();
        }
        if ("thumbsupnum".equals(columnName)) {
            return movie.getThumbsupnum();
        }
        if ("crazilynum".equals(columnName)) {
            return movie.getCrazilynum();
        }
        if ("clicktime".equals(columnName)) {
            return movie.getClicktime();
        }
        if ("clicknum".equals(columnName)) {
            return movie.getClicknum();
        }
        if ("discussnum".equals(columnName)) {
            return movie.getDiscussnum();
        }
        if ("totalscore".equals(columnName)) {
            return movie.getTotalscore();
        }
        if ("storeupnum".equals(columnName)) {
            return movie.getStoreupnum();
        }
        if ("addtime".equals(columnName)) {
            return movie.getAddtime();
        }
        return UnsupportedLegacyMovieColumnMarker.INSTANCE;
    }

    private Object normalizeStatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001D) {
            return Long.valueOf(Math.round(value));
        }
        return value;
    }

    private Map<String, Object> normalizeMovieParams(Map<String, Object> params, DianyingxinxiEntity<?> filter) {
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        int page = parsePositiveInt(valueOf(params, "page"), DEFAULT_PAGE);
        int limit = parsePositiveInt(valueOf(params, "limit"), DEFAULT_LIMIT);
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        queryParams.put("page", page);
        queryParams.put("limit", limit);
        queryParams.put("offset", (page - 1) * limit);
        queryParams.put("sortColumn", resolveSortColumn(MOVIE_SORT_COLUMNS, valueOf(params, "sort"), "m.id"));
        queryParams.put("orderKeyword", resolveOrder(valueOf(params, "order"), "desc"));
        queryParams.put("titleKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getDianyingmingcheng(), valueOf(params, "dianyingmingcheng"))));
        queryParams.put("typeKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getDianyingleixing(), valueOf(params, "dianyingleixing"))));
        queryParams.put("regionKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getQuyu(), valueOf(params, "quyu"))));
        queryParams.put("directorKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getDaoyan(), valueOf(params, "daoyan"))));
        queryParams.put("castKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getZhuyan(), valueOf(params, "zhuyan"))));
        return queryParams;
    }

    private Map<String, Object> normalizeTypeParams(Map<String, Object> params, DianyingleixingEntity<?> filter) {
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        int page = parsePositiveInt(valueOf(params, "page"), DEFAULT_PAGE);
        int limit = parsePositiveInt(valueOf(params, "limit"), DEFAULT_LIMIT);
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        queryParams.put("page", page);
        queryParams.put("limit", limit);
        queryParams.put("offset", (page - 1) * limit);
        queryParams.put("sortColumn", resolveSortColumn(TYPE_SORT_COLUMNS, valueOf(params, "sort"), "t.id"));
        queryParams.put("orderKeyword", resolveOrder(valueOf(params, "order"), "desc"));
        queryParams.put("typeKeyword", normalizeKeyword(firstNonBlank(filter == null ? null : filter.getDianyingleixing(), valueOf(params, "dianyingleixing"))));
        return queryParams;
    }

    private Long extractMovieCompatId(Map<String, Object> params, DianyingxinxiEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            return filter.getId();
        }
        return parseLong(valueOf(params, "id"));
    }

    private Long extractMovieTypeCompatId(Map<String, Object> params, DianyingleixingEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            return filter.getId();
        }
        return parseLong(valueOf(params, "id"));
    }

    private Long resolveActualMovieId(Long compatId) {
        if (compatId == null) {
            return null;
        }
        Long actualId = appMovieCompatibilityDao.selectActualMovieIdByLegacyId(compatId);
        if (actualId != null) {
            return actualId;
        }
        return appMovieCompatibilityDao.selectBridgeMovieIdByActualId(compatId);
    }

    private Long resolveActualMovieTypeId(Long compatId) {
        if (compatId == null) {
            return null;
        }
        Long actualId = appMovieCompatibilityDao.selectActualMovieTypeIdByLegacyId(compatId);
        if (actualId != null) {
            return actualId;
        }
        return appMovieCompatibilityDao.selectBridgeMovieTypeIdByActualId(compatId);
    }

    private Long resolveMovieTypeId(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return null;
        }
        Long typeId = appMovieCompatibilityDao.selectMovieTypeIdByName(typeName);
        if (typeId != null) {
            return typeId;
        }
        if (appMovieCompatibilityDao.insertMovieType(typeName, TYPE_SOURCE_NOTE) <= 0) {
            return null;
        }
        return appMovieCompatibilityDao.selectMovieTypeIdByName(typeName);
    }

    private Long resolveMovieRegionId(String regionName) {
        String normalizedRegionName = trimToNull(regionName);
        if (normalizedRegionName == null) {
            return null;
        }
        Long regionId = appMovieCompatibilityDao.selectMovieRegionIdByName(normalizedRegionName);
        if (regionId != null) {
            return regionId;
        }
        String normalizedKey = normalizedRegionName.toLowerCase(Locale.ROOT);
        if (appMovieCompatibilityDao.insertMovieRegion(normalizedRegionName, normalizedKey) <= 0) {
            return null;
        }
        return appMovieCompatibilityDao.selectMovieRegionIdByName(normalizedRegionName);
    }

    private Long resolveMoviePersonId(String personName, String sourceNote) {
        String normalizedPersonName = trimToNull(personName);
        if (normalizedPersonName == null) {
            return null;
        }
        Long personId = appMovieCompatibilityDao.selectMoviePersonIdByName(normalizedPersonName);
        if (personId != null) {
            return personId;
        }
        String normalizedKey = normalizedPersonName.toLowerCase(Locale.ROOT);
        if (appMovieCompatibilityDao.insertMoviePerson(normalizedPersonName, normalizedKey, sourceNote) <= 0) {
            return null;
        }
        return appMovieCompatibilityDao.selectMoviePersonIdByName(normalizedPersonName);
    }

    private void syncMoviePosters(Long actualMovieId, Long compatMovieId, String posterUrlsCsv) {
        if (actualMovieId == null) {
            return;
        }
        appMovieCompatibilityDao.deleteMovieMediaByMovieId(actualMovieId);
        List<String> posterUrls = splitCsvValues(posterUrlsCsv);
        int sortOrder = 1;
        for (String posterUrl : posterUrls) {
            appMovieCompatibilityDao.insertMovieMedia(actualMovieId, compatMovieId, sortOrder++, POSTER_MEDIA_ROLE, posterUrl);
        }
    }

    private void syncMovieTypeRelation(Long actualMovieId, Long typeId) {
        if (actualMovieId == null) {
            return;
        }
        appMovieCompatibilityDao.deleteMovieTypeRelationsByMovieId(actualMovieId);
        if (typeId != null) {
            appMovieCompatibilityDao.insertMovieTypeRelation(actualMovieId, typeId, 1, 1);
        }
    }

    private void syncMovieRegion(Long actualMovieId, String regionName) {
        if (actualMovieId == null) {
            return;
        }
        Long regionId = resolveMovieRegionId(regionName);
        appMovieCompatibilityDao.updateMovieRegionReference(actualMovieId, regionId);
    }

    private void syncMoviePeople(Long actualMovieId, String relationType, String rawNames, String sourceNote) {
        if (actualMovieId == null) {
            return;
        }
        appMovieCompatibilityDao.deleteMoviePersonRelationsByMovieIdAndType(actualMovieId, relationType);
        List<String> people = splitPersonNames(rawNames);
        int sortOrder = 1;
        for (String personName : people) {
            Long personId = resolveMoviePersonId(personName, sourceNote);
            if (personId != null) {
                appMovieCompatibilityDao.insertMoviePersonRelation(actualMovieId, personId, relationType, sortOrder++);
            }
        }
    }

    private List<String> splitCsvValues(String csv) {
        List<String> values = new ArrayList<String>();
        if (StringUtils.isBlank(csv)) {
            return values;
        }
        String[] parts = csv.split(",");
        for (String part : parts) {
            String value = trimToNull(part);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private List<String> splitPersonNames(String rawNames) {
        List<String> values = new ArrayList<String>();
        if (StringUtils.isBlank(rawNames)) {
            return values;
        }
        String normalized = rawNames
                .replace('，', ',')
                .replace('、', ',')
                .replace(';', ',')
                .replace('；', ',')
                .replace('/', ',')
                .replace('|', ',');
        Set<String> uniqueValues = new LinkedHashSet<String>();
        for (String part : normalized.split(",")) {
            String value = trimToNull(part);
            if (value != null) {
                uniqueValues.add(value);
            }
        }
        values.addAll(uniqueValues);
        return values;
    }

    private List<Long> parseLongList(List<String> values) {
        List<Long> ids = new ArrayList<Long>();
        if (values == null) {
            return ids;
        }
        for (String value : values) {
            Long id = parseLong(value);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private String resolveSortColumn(Map<String, String> sortColumns, String sort, String defaultColumn) {
        String column = sortColumns.get(sort);
        return StringUtils.isBlank(column) ? defaultColumn : column;
    }

    private String resolveOrder(String order, String defaultOrder) {
        if ("asc".equalsIgnoreCase(order)) {
            return "asc";
        }
        if ("desc".equalsIgnoreCase(order)) {
            return "desc";
        }
        return defaultOrder;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.isNotBlank(first)) {
            return first;
        }
        return second;
    }

    private String normalizeKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }
        return keyword.replace("%", "").trim();
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String valueOf(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private enum UnsupportedLegacyMovieColumnMarker {
        INSTANCE
    }
}
