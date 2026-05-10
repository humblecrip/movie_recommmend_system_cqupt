package com.service.impl;

import com.dao.AppMovieInteractionDao;
import com.dao.StoreupCompatibilityDao;
import com.entity.StoreupEntity;
import com.entity.view.StoreupView;
import com.service.StoreupCompatibilityService;
import com.utils.PageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 旧 storeup 接口兼容服务，底层桥接 app_user_movie_action。
 */
@Service("storeupCompatibilityService")
public class StoreupCompatibilityServiceImpl implements StoreupCompatibilityService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private static final Map<String, String> SORT_COLUMNS;

    static {
        Map<String, String> columns = new LinkedHashMap<String, String>();
        columns.put("id", "a.id");
        columns.put("addtime", "a.created_at");
        columns.put("createdAt", "a.created_at");
        columns.put("name", "a.target_name_snapshot");
        columns.put("userid", "a.legacy_userid");
        columns.put("refid", "a.legacy_refid");
        columns.put("inteltype", "a.recommend_type");
        columns.put("type", "a.action_type");
        SORT_COLUMNS = Collections.unmodifiableMap(columns);
    }

    @Autowired
    private StoreupCompatibilityDao storeupCompatibilityDao;

    @Autowired
    private AppMovieInteractionDao appMovieInteractionDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params, StoreupEntity filter, Long sessionUserId, boolean adminView) {
        StoreupView normalizedFilter = normalizeFilter(filter);
        String nameKeyword = normalizeKeyword(normalizedFilter.getName());
        normalizedFilter.setName(null);
        int page = parsePositiveInt(valueOf(params, "page"), DEFAULT_PAGE);
        int limit = parsePositiveInt(valueOf(params, "limit"), DEFAULT_LIMIT);
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        String sortColumn = resolveSortColumn(valueOf(params, "sort"), "a.id");
        String orderKeyword = resolveOrder(valueOf(params, "order"), "desc");
        int total = storeupCompatibilityDao.countByFilter(nameKeyword, normalizedFilter, sessionUserId, adminView);
        List<StoreupView> records = total == 0
                ? new ArrayList<StoreupView>()
                : storeupCompatibilityDao.selectPageByFilter(
                        nameKeyword,
                        normalizedFilter,
                        sessionUserId,
                        adminView,
                        (page - 1) * limit,
                        limit,
                        sortColumn,
                        orderKeyword
                );
        return new PageUtils(records, total, limit, page);
    }

    @Override
    public List<StoreupView> selectList(StoreupEntity filter, Long sessionUserId, boolean adminView) {
        StoreupView normalizedFilter = normalizeFilter(filter);
        String nameKeyword = normalizeKeyword(normalizedFilter.getName());
        normalizedFilter.setName(null);
        return storeupCompatibilityDao.selectListByFilter(
                nameKeyword,
                normalizedFilter,
                sessionUserId,
                adminView,
                "a.id",
                "desc"
        );
    }

    @Override
    public StoreupView selectById(Long id) {
        return id == null ? null : storeupCompatibilityDao.selectById(id);
    }

    @Override
    @Transactional
    public Long save(StoreupEntity storeup, Long sessionUserId) {
        StoreupView normalized = normalizeFilter(storeup);
        Long legacyUserId = normalized.getUserid() == null ? sessionUserId : normalized.getUserid();
        if (legacyUserId == null) {
            return null;
        }
        String actionType = normalizeActionType(normalized.getType());
        String legacyTableName = normalizeLegacyTableName(normalized.getTablename());
        if (!supportsLegacyMovieTarget(legacyTableName, normalized.getRefid())) {
            return null;
        }
        Long movieId = resolveMovieId(legacyTableName, normalized.getRefid());
        if (movieId == null) {
            return null;
        }
        Long appUserId = appMovieInteractionDao.selectAppUserIdByLegacyUserId(legacyUserId);
        int inserted = storeupCompatibilityDao.insertAction(
                legacyUserId,
                appUserId,
                movieId,
                normalized.getRefid(),
                legacyTableName,
                actionType,
                normalized.getName(),
                normalized.getPicture(),
                normalized.getInteltype(),
                normalized.getRemark()
        );
        if (inserted <= 0) {
            return null;
        }
        return storeupCompatibilityDao.selectLastInsertId();
    }

    @Override
    @Transactional
    public boolean update(StoreupEntity storeup, Long sessionUserId, boolean adminView) {
        StoreupView normalized = normalizeFilter(storeup);
        if (normalized.getId() == null) {
            return false;
        }
        if (!adminView && sessionUserId == null) {
            return false;
        }
        String actionType = normalizeActionType(normalized.getType());
        String legacyTableName = normalizeLegacyTableName(normalized.getTablename());
        if (!supportsLegacyMovieTarget(legacyTableName, normalized.getRefid())) {
            return false;
        }
        Long movieId = resolveMovieId(legacyTableName, normalized.getRefid());
        if (movieId == null) {
            return false;
        }
        return storeupCompatibilityDao.updateAction(
                normalized.getId(),
                sessionUserId,
                adminView,
                movieId,
                normalized.getRefid(),
                legacyTableName,
                actionType,
                normalized.getName(),
                normalized.getPicture(),
                normalized.getInteltype(),
                normalized.getRemark()
        ) > 0;
    }

    @Override
    @Transactional
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return storeupCompatibilityDao.deleteBatch(ids);
    }

    private StoreupView normalizeFilter(StoreupEntity filter) {
        StoreupView view = new StoreupView();
        if (filter != null) {
            BeanUtils.copyProperties(filter, view);
        }
        if (StringUtils.isBlank(view.getTablename())) {
            view.setTablename("dianyingxinxi");
        }
        view.setType(normalizeActionType(view.getType()));
        return view;
    }

    private String normalizeActionType(String type) {
        if (StringUtils.isBlank(type) || "1".equals(type) || "favorite".equalsIgnoreCase(type)) {
            return "favorite";
        }
        if ("21".equals(type) || "like".equalsIgnoreCase(type)) {
            return "like";
        }
        if ("22".equals(type) || "dislike".equalsIgnoreCase(type)) {
            return "dislike";
        }
        return type.startsWith("legacy_unknown:") ? type : "legacy_unknown:" + type;
    }

    private String normalizeLegacyTableName(String tableName) {
        return StringUtils.isBlank(tableName) ? "dianyingxinxi" : tableName.trim();
    }

    private Long resolveMovieId(String legacyTableName, Long legacyRefId) {
        if (!"dianyingxinxi".equals(legacyTableName) || legacyRefId == null) {
            return null;
        }
        return storeupCompatibilityDao.selectMovieIdByLegacyRefId(legacyRefId);
    }

    private boolean supportsLegacyMovieTarget(String legacyTableName, Long legacyRefId) {
        return "dianyingxinxi".equals(legacyTableName) && legacyRefId != null;
    }

    private String normalizeKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }
        return keyword.replace("%", "").trim();
    }

    private String resolveSortColumn(String sort, String defaultColumn) {
        String column = SORT_COLUMNS.get(sort);
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

    private String valueOf(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
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
}
