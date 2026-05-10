package com.dao;

import com.entity.DiscussdianyingxinxiEntity;
import com.entity.vo.AppMovieActionStatusVO;
import com.entity.vo.AppMovieCommentVO;
import com.entity.vo.AppMovieFavoriteVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * app_* 电影互动 DAO。
 */
public interface AppMovieInteractionDao {

    Long selectAppUserIdByLegacyUserId(@Param("legacyUserId") Long legacyUserId);

    AppMovieActionStatusVO selectMovieActionStatus(@Param("movieId") Long movieId, @Param("legacyUserId") Long legacyUserId);

    int countMovieAction(@Param("movieId") Long movieId, @Param("legacyUserId") Long legacyUserId, @Param("actionType") String actionType);

    int insertMovieAction(@Param("movieId") Long movieId,
                          @Param("appUserId") Long appUserId,
                          @Param("legacyUserId") Long legacyUserId,
                          @Param("actionType") String actionType,
                          @Param("targetName") String targetName,
                          @Param("targetPicture") String targetPicture);

    int deleteMovieAction(@Param("movieId") Long movieId, @Param("legacyUserId") Long legacyUserId, @Param("actionType") String actionType);

    int refreshMovieActionCounts(@Param("movieId") Long movieId);

    int countFavorites(@Param("params") Map<String, Object> params);

    List<AppMovieFavoriteVO> selectFavorites(@Param("params") Map<String, Object> params);

    int countComments(@Param("movieId") Long movieId);

    List<AppMovieCommentVO> selectComments(@Param("params") Map<String, Object> params);

    AppMovieCommentVO selectCommentById(@Param("commentId") Long commentId, @Param("legacyUserId") Long legacyUserId);

    int countUserComment(@Param("movieId") Long movieId, @Param("legacyUserId") Long legacyUserId);

    int insertComment(@Param("movieId") Long movieId,
                      @Param("appUserId") Long appUserId,
                      @Param("legacyUserId") Long legacyUserId,
                      @Param("authorAvatar") String authorAvatar,
                      @Param("authorName") String authorName,
                      @Param("contentHtml") String contentHtml,
                      @Param("rating") Double rating);

    Long selectLastInsertId();

    int deleteOwnComment(@Param("commentId") Long commentId, @Param("legacyUserId") Long legacyUserId);

    int refreshMovieCommentStats(@Param("movieId") Long movieId);

    Long selectMovieIdByCommentId(@Param("commentId") Long commentId);

    int deleteCommentVotesByCommentId(@Param("commentId") Long commentId);

    int countCommentVote(@Param("commentId") Long commentId, @Param("legacyUserId") Long legacyUserId, @Param("voteType") String voteType);

    int deleteCommentVote(@Param("commentId") Long commentId, @Param("legacyUserId") Long legacyUserId, @Param("voteType") String voteType);

    int insertCommentVote(@Param("commentId") Long commentId,
                          @Param("appUserId") Long appUserId,
                          @Param("legacyUserId") Long legacyUserId,
                          @Param("voteType") String voteType);

    int deleteCommentVotesByType(@Param("commentId") Long commentId, @Param("voteType") String voteType);

    int refreshCommentVoteCounts(@Param("commentId") Long commentId);

    int countLegacyComments(@Param("params") Map<String, Object> params);

    List<DiscussdianyingxinxiEntity> selectLegacyComments(@Param("params") Map<String, Object> params);

    Long selectMovieIdByLegacyRefId(@Param("legacyRefId") Long legacyRefId);

    Long selectActualCommentIdByLegacyCommentId(@Param("commentId") Long commentId);

    Long selectBridgeCommentIdByActualId(@Param("commentId") Long commentId);

    int updateLegacyCommentById(@Param("commentId") Long commentId,
                                @Param("legacyUserId") Long legacyUserId,
                                @Param("avatarUrl") String avatarUrl,
                                @Param("nickname") String nickname,
                                @Param("content") String content,
                                @Param("score") Double score,
                                @Param("reply") String reply,
                                @Param("istop") Integer istop);

    int deleteCommentById(@Param("commentId") Long commentId);
}
