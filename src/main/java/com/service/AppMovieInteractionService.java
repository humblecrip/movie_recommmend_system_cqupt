package com.service;

import com.entity.DiscussdianyingxinxiEntity;
import com.entity.view.DiscussdianyingxinxiView;
import com.entity.vo.AppMovieActionStatusVO;
import com.entity.vo.AppMovieActionToggleRequestVO;
import com.entity.vo.AppMovieCommentCreateRequestVO;
import com.entity.vo.AppMovieCommentVO;
import com.entity.vo.AppMovieCommentVoteRequestVO;
import com.utils.PageUtils;

import java.util.Map;
import java.util.List;

/**
 * app_* 电影互动服务。
 */
public interface AppMovieInteractionService {

    AppMovieActionStatusVO getMovieActionStatus(Long legacyUserId, Long movieId);

    AppMovieActionStatusVO toggleMovieAction(Long legacyUserId, AppMovieActionToggleRequestVO request);

    PageUtils queryFavorites(Long legacyUserId, Map<String, Object> params);

    AppMovieActionStatusVO cancelFavorite(Long legacyUserId, AppMovieActionToggleRequestVO request);

    PageUtils queryComments(Long legacyUserId, Map<String, Object> params);

    AppMovieCommentVO addComment(Long legacyUserId, AppMovieCommentCreateRequestVO request);

    boolean deleteComment(Long legacyUserId, Long commentId);

    AppMovieCommentVO toggleCommentVote(Long legacyUserId, AppMovieCommentVoteRequestVO request);

    PageUtils queryLegacyComments(Map<String, Object> params, DiscussdianyingxinxiEntity<?> filter);

    List<DiscussdianyingxinxiView> selectLegacyCommentViews(DiscussdianyingxinxiEntity<?> filter);

    DiscussdianyingxinxiView selectLegacyCommentView(DiscussdianyingxinxiEntity<?> filter);

    DiscussdianyingxinxiEntity<?> getLegacyCommentById(Long commentId);

    Long createLegacyComment(Long sessionLegacyUserId, DiscussdianyingxinxiEntity<?> legacyComment);

    boolean updateLegacyComment(DiscussdianyingxinxiEntity<?> legacyComment);

    int deleteLegacyComments(List<Long> ids);
}
