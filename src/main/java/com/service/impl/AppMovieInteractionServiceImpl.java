package com.service.impl;

import com.dao.AppMovieInteractionDao;
import com.entity.DiscussdianyingxinxiEntity;
import com.entity.view.DiscussdianyingxinxiView;
import com.entity.vo.AppMovieActionStatusVO;
import com.entity.vo.AppMovieActionToggleRequestVO;
import com.entity.vo.AppMovieCommentCreateRequestVO;
import com.entity.vo.AppMovieCommentVO;
import com.entity.vo.AppMovieCommentVoteRequestVO;
import com.entity.vo.AppMovieFavoriteVO;
import com.service.AppMovieInteractionService;
import com.service.SensitivewordsService;
import com.utils.PageUtils;
import com.utils.SensitiveWordUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * app_* 电影互动服务实现。
 */
@Service("appMovieInteractionService")
public class AppMovieInteractionServiceImpl implements AppMovieInteractionService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private static final String ACTION_FAVORITE = "favorite";
    private static final String ACTION_LIKE = "like";
    private static final String ACTION_DISLIKE = "dislike";
    private static final String VOTE_LIKE = "like";
    private static final String VOTE_DISLIKE = "dislike";

    private static final Map<String, String> FAVORITE_SORT_COLUMNS;
    private static final Map<String, String> COMMENT_SORT_COLUMNS;
    private static final Map<String, String> LEGACY_COMMENT_SORT_COLUMNS;

    static {
        Map<String, String> favoriteColumns = new LinkedHashMap<String, String>();
        favoriteColumns.put("createdAt", "a.created_at");
        favoriteColumns.put("addtime", "a.created_at");
        favoriteColumns.put("title", "m.title");
        favoriteColumns.put("name", "m.title");
        favoriteColumns.put("totalScore", "m.total_score");
        favoriteColumns.put("clickCount", "m.click_count");
        FAVORITE_SORT_COLUMNS = Collections.unmodifiableMap(favoriteColumns);

        Map<String, String> commentColumns = new LinkedHashMap<String, String>();
        commentColumns.put("createdAt", "c.created_at");
        commentColumns.put("addtime", "c.created_at");
        commentColumns.put("pinned", "c.is_pinned");
        commentColumns.put("istop", "c.is_pinned");
        COMMENT_SORT_COLUMNS = Collections.unmodifiableMap(commentColumns);

        Map<String, String> legacyCommentColumns = new LinkedHashMap<String, String>();
        legacyCommentColumns.put("id", "c.id");
        legacyCommentColumns.put("addtime", "c.created_at");
        legacyCommentColumns.put("score", "c.rating");
        legacyCommentColumns.put("istop", "c.is_pinned");
        legacyCommentColumns.put("thumbsupnum", "c.like_count");
        legacyCommentColumns.put("crazilynum", "c.dislike_count");
        LEGACY_COMMENT_SORT_COLUMNS = Collections.unmodifiableMap(legacyCommentColumns);
    }

    @Autowired
    private AppMovieInteractionDao appMovieInteractionDao;

    @Autowired
    private SensitivewordsService sensitivewordsService;

    @Override
    public AppMovieActionStatusVO getMovieActionStatus(Long legacyUserId, Long movieId) {
        AppMovieActionStatusVO status = appMovieInteractionDao.selectMovieActionStatus(movieId, legacyUserId);
        if (status == null) {
            status = new AppMovieActionStatusVO();
            status.setMovieId(movieId);
        }
        fillActionDefaults(status);
        return status;
    }

    @Override
    @Transactional
    public AppMovieActionStatusVO toggleMovieAction(Long legacyUserId, AppMovieActionToggleRequestVO request) {
        Long movieId = request == null ? null : request.getMovieId();
        String actionType = normalizeActionType(request == null ? null : request.getActionType());
        if (movieId == null || StringUtils.isBlank(actionType)) {
            return null;
        }
        AppMovieActionStatusVO currentStatus = selectExistingMovieActionStatus(movieId, legacyUserId);
        if (currentStatus == null) {
            return null;
        }

        int exists = appMovieInteractionDao.countMovieAction(movieId, legacyUserId, actionType);
        int changedRows;
        if (exists > 0) {
            changedRows = appMovieInteractionDao.deleteMovieAction(movieId, legacyUserId, actionType);
        } else {
            if (ACTION_LIKE.equals(actionType)) {
                appMovieInteractionDao.deleteMovieAction(movieId, legacyUserId, ACTION_DISLIKE);
            } else if (ACTION_DISLIKE.equals(actionType)) {
                appMovieInteractionDao.deleteMovieAction(movieId, legacyUserId, ACTION_LIKE);
            }
            changedRows = appMovieInteractionDao.insertMovieAction(movieId, selectAppUserId(legacyUserId), legacyUserId, actionType, null, null);
        }
        if (changedRows <= 0) {
            return null;
        }
        if (appMovieInteractionDao.refreshMovieActionCounts(movieId) <= 0) {
            return null;
        }
        return getMovieActionStatus(legacyUserId, movieId);
    }

    @Override
    public PageUtils queryFavorites(Long legacyUserId, Map<String, Object> params) {
        Map<String, Object> queryParams = normalizePageParams(params, FAVORITE_SORT_COLUMNS, "a.created_at", "desc");
        queryParams.put("legacyUserId", legacyUserId == null ? parseLong(valueOf(params, "legacyUserId")) : legacyUserId);
        queryParams.put("title", firstNonBlank(valueOf(params, "title"), valueOf(params, "name")));
        int total = appMovieInteractionDao.countFavorites(queryParams);
        List<AppMovieFavoriteVO> records = total == 0
                ? new ArrayList<AppMovieFavoriteVO>()
                : appMovieInteractionDao.selectFavorites(queryParams);
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    @Transactional
    public AppMovieActionStatusVO cancelFavorite(Long legacyUserId, AppMovieActionToggleRequestVO request) {
        Long movieId = request == null ? null : request.getMovieId();
        if (movieId == null) {
            return null;
        }
        AppMovieActionStatusVO currentStatus = selectExistingMovieActionStatus(movieId, legacyUserId);
        if (currentStatus == null) {
            return null;
        }
        if (appMovieInteractionDao.deleteMovieAction(movieId, legacyUserId, ACTION_FAVORITE) > 0) {
            if (appMovieInteractionDao.refreshMovieActionCounts(movieId) <= 0) {
                return null;
            }
        }
        return getMovieActionStatus(legacyUserId, movieId);
    }

    @Override
    public PageUtils queryComments(Long legacyUserId, Map<String, Object> params) {
        Map<String, Object> queryParams = normalizePageParams(params, COMMENT_SORT_COLUMNS, "c.is_pinned", "desc");
        queryParams.put("movieId", parseLong(valueOf(params, "movieId")));
        queryParams.put("legacyUserId", legacyUserId);
        Long movieId = (Long) queryParams.get("movieId");
        int total = movieId == null ? 0 : appMovieInteractionDao.countComments(movieId);
        List<AppMovieCommentVO> records = total == 0
                ? new ArrayList<AppMovieCommentVO>()
                : appMovieInteractionDao.selectComments(queryParams);
        fillCommentDefaults(records);
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    @Transactional
    public AppMovieCommentVO addComment(Long legacyUserId, AppMovieCommentCreateRequestVO request) {
        Long movieId = request == null ? null : request.getMovieId();
        String contentHtml = request == null ? null : request.getContentHtml();
        if (movieId == null || StringUtils.isBlank(contentHtml)) {
            return null;
        }
        String sanitizedContent = maskSensitiveContent(contentHtml);
        if (appMovieInteractionDao.countUserComment(movieId, legacyUserId) > 0) {
            return null;
        }
        int inserted = appMovieInteractionDao.insertComment(
                movieId,
                selectAppUserId(legacyUserId),
                legacyUserId,
                request.getAuthorAvatar(),
                null,
                sanitizedContent,
                request.getRating()
        );
        if (inserted <= 0) {
            return null;
        }
        Long commentId = appMovieInteractionDao.selectLastInsertId();
        if (commentId == null || commentId <= 0) {
            return null;
        }
        appMovieInteractionDao.refreshMovieCommentStats(movieId);
        return appMovieInteractionDao.selectCommentById(commentId, legacyUserId);
    }

    @Override
    @Transactional
    public boolean deleteComment(Long legacyUserId, Long commentId) {
        if (commentId == null) {
            return false;
        }
        AppMovieCommentVO existing = appMovieInteractionDao.selectCommentById(commentId, legacyUserId);
        if (existing == null || !Boolean.TRUE.equals(existing.getOwnedByCurrentUser())) {
            return false;
        }
        Long movieId = appMovieInteractionDao.selectMovieIdByCommentId(commentId);
        appMovieInteractionDao.deleteCommentVotesByCommentId(commentId);
        boolean deleted = appMovieInteractionDao.deleteOwnComment(commentId, legacyUserId) > 0;
        if (deleted && movieId != null) {
            appMovieInteractionDao.refreshMovieCommentStats(movieId);
        }
        return deleted;
    }

    @Override
    @Transactional
    public AppMovieCommentVO toggleCommentVote(Long legacyUserId, AppMovieCommentVoteRequestVO request) {
        Long commentId = request == null ? null : request.getCommentId();
        String voteType = normalizeVoteType(request == null ? null : request.getVoteType());
        if (commentId == null || StringUtils.isBlank(voteType)) {
            return null;
        }
        int exists = appMovieInteractionDao.countCommentVote(commentId, legacyUserId, voteType);
        int changedRows;
        if (exists > 0) {
            changedRows = appMovieInteractionDao.deleteCommentVote(commentId, legacyUserId, voteType);
        } else {
            if (VOTE_LIKE.equals(voteType)) {
                appMovieInteractionDao.deleteCommentVote(commentId, legacyUserId, VOTE_DISLIKE);
            } else if (VOTE_DISLIKE.equals(voteType)) {
                appMovieInteractionDao.deleteCommentVote(commentId, legacyUserId, VOTE_LIKE);
            }
            changedRows = appMovieInteractionDao.insertCommentVote(commentId, selectAppUserId(legacyUserId), legacyUserId, voteType);
        }
        if (changedRows <= 0) {
            return null;
        }
        if (appMovieInteractionDao.refreshCommentVoteCounts(commentId) <= 0) {
            return null;
        }
        return appMovieInteractionDao.selectCommentById(commentId, legacyUserId);
    }

    @Override
    public PageUtils queryLegacyComments(Map<String, Object> params, DiscussdianyingxinxiEntity<?> filter) {
        Map<String, Object> queryParams = normalizeLegacyCommentParams(params, filter, true);
        if (Boolean.TRUE.equals(queryParams.get("forceEmpty"))) {
            return new PageUtils(new ArrayList<DiscussdianyingxinxiEntity>(), 0, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
        }
        int total = appMovieInteractionDao.countLegacyComments(queryParams);
        List<DiscussdianyingxinxiEntity> records = total == 0
                ? new ArrayList<DiscussdianyingxinxiEntity>()
                : appMovieInteractionDao.selectLegacyComments(queryParams);
        return new PageUtils(records, total, (Integer) queryParams.get("limit"), (Integer) queryParams.get("page"));
    }

    @Override
    public List<DiscussdianyingxinxiView> selectLegacyCommentViews(DiscussdianyingxinxiEntity<?> filter) {
        Map<String, Object> queryParams = normalizeLegacyCommentParams(null, filter, false);
        if (Boolean.TRUE.equals(queryParams.get("forceEmpty"))) {
            return new ArrayList<DiscussdianyingxinxiView>();
        }
        List<DiscussdianyingxinxiEntity> records = appMovieInteractionDao.selectLegacyComments(queryParams);
        List<DiscussdianyingxinxiView> views = new ArrayList<DiscussdianyingxinxiView>();
        for (DiscussdianyingxinxiEntity record : records) {
            views.add(new DiscussdianyingxinxiView(record));
        }
        return views;
    }

    @Override
    public DiscussdianyingxinxiView selectLegacyCommentView(DiscussdianyingxinxiEntity<?> filter) {
        List<DiscussdianyingxinxiView> views = selectLegacyCommentViews(filter);
        return views.isEmpty() ? null : views.get(0);
    }

    @Override
    public DiscussdianyingxinxiEntity<?> getLegacyCommentById(Long commentId) {
        if (commentId == null) {
            return null;
        }
        Long actualCommentId = resolveActualCommentId(commentId);
        if (actualCommentId == null) {
            return null;
        }
        Map<String, Object> queryParams = normalizeLegacyCommentParams(null, null, false);
        queryParams.put("actualCommentId", actualCommentId);
        List<DiscussdianyingxinxiEntity> records = appMovieInteractionDao.selectLegacyComments(queryParams);
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    @Transactional
    public Long createLegacyComment(Long sessionLegacyUserId, DiscussdianyingxinxiEntity<?> legacyComment) {
        if (legacyComment == null) {
            throw new IllegalArgumentException("缺少评论内容");
        }
        if (legacyComment.getRefid() == null) {
            throw new IllegalArgumentException("缺少旧电影ID refid");
        }
        if (StringUtils.isBlank(legacyComment.getContent())) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        Long legacyUserId = sessionLegacyUserId != null ? sessionLegacyUserId : legacyComment.getUserid();
        Long movieId = appMovieInteractionDao.selectMovieIdByLegacyRefId(legacyComment.getRefid());
        if (movieId == null) {
            throw new IllegalStateException("旧电影ID未映射到 app_movie，拒绝直连旧表");
        }
        int inserted = appMovieInteractionDao.insertComment(
                movieId,
                selectAppUserId(legacyUserId),
                legacyUserId,
                legacyComment.getAvatarurl(),
                legacyComment.getNickname(),
                maskSensitiveContent(legacyComment.getContent()),
                legacyComment.getScore()
        );
        if (inserted <= 0) {
            return null;
        }
        Long commentId = appMovieInteractionDao.selectLastInsertId();
        if (commentId == null || commentId <= 0) {
            return null;
        }
        appMovieInteractionDao.updateLegacyCommentById(
                commentId,
                legacyUserId,
                legacyComment.getAvatarurl(),
                legacyComment.getNickname(),
                maskSensitiveContent(legacyComment.getContent()),
                legacyComment.getScore(),
                legacyComment.getReply(),
                legacyComment.getIstop()
        );
        syncLegacyCommentVotes(commentId, legacyComment.getTuserids(), legacyComment.getCuserids());
        appMovieInteractionDao.refreshMovieCommentStats(movieId);
        DiscussdianyingxinxiEntity<?> saved = getLegacyCommentById(commentId);
        return saved == null ? null : saved.getId();
    }

    @Override
    @Transactional
    public boolean updateLegacyComment(DiscussdianyingxinxiEntity<?> legacyComment) {
        if (legacyComment == null || legacyComment.getId() == null) {
            throw new IllegalArgumentException("缺少旧评论ID");
        }
        Long actualCommentId = resolveActualCommentId(legacyComment.getId());
        if (actualCommentId == null) {
            throw new IllegalStateException("旧评论ID未映射到 app_movie_comment，拒绝直连旧表");
        }
        int updated = appMovieInteractionDao.updateLegacyCommentById(
                actualCommentId,
                legacyComment.getUserid(),
                legacyComment.getAvatarurl(),
                legacyComment.getNickname(),
                maskSensitiveContent(legacyComment.getContent()),
                legacyComment.getScore(),
                legacyComment.getReply(),
                legacyComment.getIstop()
        );
        if (updated <= 0) {
            return false;
        }
        syncLegacyCommentVotes(actualCommentId, legacyComment.getTuserids(), legacyComment.getCuserids());
        Long movieId = appMovieInteractionDao.selectMovieIdByCommentId(actualCommentId);
        if (movieId != null) {
            appMovieInteractionDao.refreshMovieCommentStats(movieId);
        }
        return true;
    }

    @Override
    @Transactional
    public int deleteLegacyComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int deletedCount = 0;
        for (Long compatId : ids) {
            Long actualCommentId = resolveActualCommentId(compatId);
            if (actualCommentId == null) {
                throw new IllegalStateException("旧评论ID未映射到 app_movie_comment，拒绝直连旧表");
            }
            Long movieId = appMovieInteractionDao.selectMovieIdByCommentId(actualCommentId);
            appMovieInteractionDao.deleteCommentVotesByCommentId(actualCommentId);
            if (appMovieInteractionDao.deleteCommentById(actualCommentId) > 0) {
                deletedCount++;
                if (movieId != null) {
                    appMovieInteractionDao.refreshMovieCommentStats(movieId);
                }
            }
        }
        return deletedCount;
    }

    private Long selectAppUserId(Long legacyUserId) {
        return legacyUserId == null ? null : appMovieInteractionDao.selectAppUserIdByLegacyUserId(legacyUserId);
    }

    private AppMovieActionStatusVO selectExistingMovieActionStatus(Long movieId, Long legacyUserId) {
        AppMovieActionStatusVO status = appMovieInteractionDao.selectMovieActionStatus(movieId, legacyUserId);
        if (status != null) {
            fillActionDefaults(status);
        }
        return status;
    }

    private void fillActionDefaults(AppMovieActionStatusVO status) {
        status.setFavorite(Boolean.TRUE.equals(status.getFavorite()));
        status.setLiked(Boolean.TRUE.equals(status.getLiked()));
        status.setDisliked(Boolean.TRUE.equals(status.getDisliked()));
        status.setFavoriteCount(defaultInt(status.getFavoriteCount()));
        status.setLikeCount(defaultInt(status.getLikeCount()));
        status.setDislikeCount(defaultInt(status.getDislikeCount()));
    }

    private void fillCommentDefaults(List<AppMovieCommentVO> comments) {
        if (comments == null) {
            return;
        }
        for (AppMovieCommentVO comment : comments) {
            comment.setLikeCount(defaultInt(comment.getLikeCount()));
            comment.setDislikeCount(defaultInt(comment.getDislikeCount()));
            comment.setPinned(Boolean.TRUE.equals(comment.getPinned()));
            comment.setOwnedByCurrentUser(Boolean.TRUE.equals(comment.getOwnedByCurrentUser()));
        }
    }

    private Map<String, Object> normalizePageParams(Map<String, Object> params,
                                                    Map<String, String> sortColumns,
                                                    String defaultSortColumn,
                                                    String defaultOrder) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        int page = parsePositiveInt(valueOf(params, "page"), DEFAULT_PAGE);
        int limit = parsePositiveInt(valueOf(params, "limit"), DEFAULT_LIMIT);
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        String sortColumn = sortColumns.get(valueOf(params, "sort"));
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
        return queryParams;
    }

    private String normalizeActionType(String actionType) {
        if (ACTION_FAVORITE.equals(actionType) || ACTION_LIKE.equals(actionType) || ACTION_DISLIKE.equals(actionType)) {
            return actionType;
        }
        return null;
    }

    private String normalizeVoteType(String voteType) {
        if (VOTE_LIKE.equals(voteType) || VOTE_DISLIKE.equals(voteType)) {
            return voteType;
        }
        return null;
    }

    private String valueOf(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.isNotBlank(first)) {
            return first.replace("%", "").trim();
        }
        return StringUtils.isNotBlank(second) ? second.replace("%", "").trim() : null;
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

    private String maskSensitiveContent(String content) {
        List<String> keywords = sensitivewordsService == null
                ? Collections.<String>emptyList()
                : sensitivewordsService.listNormalizedKeywords();
        return SensitiveWordUtils.maskContent(content, keywords);
    }

    private Map<String, Object> normalizeLegacyCommentParams(Map<String, Object> params,
                                                             DiscussdianyingxinxiEntity<?> filter,
                                                             boolean paged) {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        if (paged) {
            queryParams.putAll(normalizePageParams(params, LEGACY_COMMENT_SORT_COLUMNS, "c.id", "desc"));
        } else {
            queryParams.put("page", 1);
            queryParams.put("limit", MAX_LIMIT);
            queryParams.put("offset", 0);
            queryParams.put("sortColumn", "c.id");
            queryParams.put("orderKeyword", "desc");
        }
        Long compatId = extractLegacyCompatId(params, filter);
        if (compatId != null) {
            Long actualCommentId = resolveActualCommentId(compatId);
            if (actualCommentId == null) {
                queryParams.put("forceEmpty", true);
                return queryParams;
            }
            queryParams.put("actualCommentId", actualCommentId);
        }
        mergeLegacyCommentFilters(queryParams, params, filter);
        return queryParams;
    }

    private void mergeLegacyCommentFilters(Map<String, Object> queryParams,
                                           Map<String, Object> params,
                                           DiscussdianyingxinxiEntity<?> filter) {
        Long legacyRefId = filter != null && filter.getRefid() != null ? filter.getRefid() : parseLong(valueOf(params, "refid"));
        if (legacyRefId != null) {
            queryParams.put("refid", legacyRefId);
        }
        Long legacyUserId = filter != null && filter.getUserid() != null ? filter.getUserid() : parseLong(valueOf(params, "userid"));
        if (legacyUserId != null) {
            queryParams.put("userid", legacyUserId);
        }
        String nickname = firstNonBlank(
                filter == null ? null : filter.getNickname(),
                valueOf(params, "nickname")
        );
        if (StringUtils.isNotBlank(nickname)) {
            queryParams.put("nickname", nickname);
        }
        String content = firstNonBlank(
                filter == null ? null : filter.getContent(),
                valueOf(params, "content")
        );
        if (StringUtils.isNotBlank(content)) {
            queryParams.put("content", content);
        }
        String reply = firstNonBlank(
                filter == null ? null : filter.getReply(),
                valueOf(params, "reply")
        );
        if (StringUtils.isNotBlank(reply)) {
            queryParams.put("reply", reply);
        }
        Integer istop = filter != null && filter.getIstop() != null ? filter.getIstop() : parseInteger(valueOf(params, "istop"));
        if (istop != null) {
            queryParams.put("istop", istop);
        }
    }

    private Integer parseInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long extractLegacyCompatId(Map<String, Object> params, DiscussdianyingxinxiEntity<?> filter) {
        if (filter != null && filter.getId() != null) {
            return filter.getId();
        }
        return parseLong(valueOf(params, "id"));
    }

    /**
     * 旧评论 ID 先按 legacy id 映射；只有桥接层新写入且没有 legacy id 的记录，
     * 才允许用 app_movie_comment.id 继续访问，避免把旧接口参数误当新主键。
     */
    private Long resolveActualCommentId(Long compatId) {
        if (compatId == null) {
            return null;
        }
        Long actualCommentId = appMovieInteractionDao.selectActualCommentIdByLegacyCommentId(compatId);
        if (actualCommentId != null) {
            return actualCommentId;
        }
        return appMovieInteractionDao.selectBridgeCommentIdByActualId(compatId);
    }

    private void syncLegacyCommentVotes(Long commentId, String likeUserIds, String dislikeUserIds) {
        if (commentId == null || (likeUserIds == null && dislikeUserIds == null)) {
            return;
        }
        Set<Long> likeVoteUsers = parseLegacyUserIds(likeUserIds);
        Set<Long> dislikeVoteUsers = parseLegacyUserIds(dislikeUserIds);
        dislikeVoteUsers.removeAll(likeVoteUsers);

        appMovieInteractionDao.deleteCommentVotesByType(commentId, VOTE_LIKE);
        for (Long legacyUserId : likeVoteUsers) {
            appMovieInteractionDao.insertCommentVote(commentId, selectAppUserId(legacyUserId), legacyUserId, VOTE_LIKE);
        }

        appMovieInteractionDao.deleteCommentVotesByType(commentId, VOTE_DISLIKE);
        for (Long legacyUserId : dislikeVoteUsers) {
            appMovieInteractionDao.insertCommentVote(commentId, selectAppUserId(legacyUserId), legacyUserId, VOTE_DISLIKE);
        }

        appMovieInteractionDao.refreshCommentVoteCounts(commentId);
    }

    private Set<Long> parseLegacyUserIds(String legacyUserIds) {
        Set<Long> parsed = new LinkedHashSet<Long>();
        if (StringUtils.isBlank(legacyUserIds)) {
            return parsed;
        }
        String[] segments = legacyUserIds.split(",");
        for (String segment : segments) {
            if (StringUtils.isBlank(segment)) {
                continue;
            }
            Long value = parseLong(segment.trim());
            if (value != null) {
                parsed.add(value);
            }
        }
        return parsed;
    }
}
