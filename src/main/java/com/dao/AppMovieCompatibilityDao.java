package com.dao;

import com.entity.DianyingleixingEntity;
import com.entity.DianyingxinxiEntity;
import com.entity.view.StoreupView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电影/类型旧接口兼容查询 DAO，底层桥接 app_movie / app_movie_type。
 */
public interface AppMovieCompatibilityDao {

    int countLegacyMovies(@Param("titleKeyword") String titleKeyword,
                          @Param("typeKeyword") String typeKeyword,
                          @Param("regionKeyword") String regionKeyword,
                          @Param("directorKeyword") String directorKeyword,
                          @Param("castKeyword") String castKeyword);

    List<DianyingxinxiEntity> selectLegacyMoviesPage(@Param("titleKeyword") String titleKeyword,
                                                     @Param("typeKeyword") String typeKeyword,
                                                     @Param("regionKeyword") String regionKeyword,
                                                     @Param("directorKeyword") String directorKeyword,
                                                     @Param("castKeyword") String castKeyword,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit,
                                                     @Param("sortColumn") String sortColumn,
                                                     @Param("orderKeyword") String orderKeyword);

    List<DianyingxinxiEntity> selectLegacyMovies(@Param("titleKeyword") String titleKeyword,
                                                 @Param("typeKeyword") String typeKeyword,
                                                 @Param("regionKeyword") String regionKeyword,
                                                 @Param("directorKeyword") String directorKeyword,
                                                 @Param("castKeyword") String castKeyword,
                                                 @Param("sortColumn") String sortColumn,
                                                 @Param("orderKeyword") String orderKeyword);

    Long selectActualMovieIdByLegacyId(@Param("legacyId") Long legacyId);

    Long selectBridgeMovieIdByActualId(@Param("actualId") Long actualId);

    DianyingxinxiEntity selectLegacyMovieByActualId(@Param("actualId") Long actualId);

    int insertMovie(@Param("typeId") Long typeId,
                    @Param("legacyTypeName") String legacyTypeName,
                    @Param("title") String title,
                    @Param("posterUrlsCsv") String posterUrlsCsv,
                    @Param("regionName") String regionName,
                    @Param("releaseDate") java.util.Date releaseDate,
                    @Param("directorName") String directorName,
                    @Param("castNames") String castNames,
                    @Param("synopsis") String synopsis,
                    @Param("detailHtml") String detailHtml,
                    @Param("likeCount") Integer likeCount,
                    @Param("dislikeCount") Integer dislikeCount,
                    @Param("clickCount") Integer clickCount,
                    @Param("commentCount") Integer commentCount,
                    @Param("favoriteCount") Integer favoriteCount,
                    @Param("totalScore") Double totalScore,
                    @Param("lastClickedAt") java.util.Date lastClickedAt);

    Long selectLastInsertId();

    int updateMovieByActualId(@Param("actualId") Long actualId,
                              @Param("typeId") Long typeId,
                              @Param("legacyTypeName") String legacyTypeName,
                              @Param("title") String title,
                              @Param("posterUrlsCsv") String posterUrlsCsv,
                              @Param("regionName") String regionName,
                              @Param("releaseDate") java.util.Date releaseDate,
                              @Param("directorName") String directorName,
                              @Param("castNames") String castNames,
                              @Param("synopsis") String synopsis,
                              @Param("detailHtml") String detailHtml,
                              @Param("likeCount") Integer likeCount,
                              @Param("dislikeCount") Integer dislikeCount,
                              @Param("clickCount") Integer clickCount,
                              @Param("commentCount") Integer commentCount,
                              @Param("favoriteCount") Integer favoriteCount,
                              @Param("totalScore") Double totalScore,
                              @Param("lastClickedAt") java.util.Date lastClickedAt);

    int incrementMovieClick(@Param("actualId") Long actualId);

    int incrementMovieVote(@Param("actualId") Long actualId,
                           @Param("likeDelta") int likeDelta,
                           @Param("dislikeDelta") int dislikeDelta);

    int deleteMovieMediaByMovieId(@Param("movieId") Long movieId);

    int insertMovieMedia(@Param("movieId") Long movieId,
                         @Param("legacyMovieId") Long legacyMovieId,
                         @Param("sortOrder") Integer sortOrder,
                         @Param("mediaRole") String mediaRole,
                         @Param("mediaUrl") String mediaUrl);

    int deleteMovieTypeRelationsByMovieId(@Param("movieId") Long movieId);

    int insertMovieTypeRelation(@Param("movieId") Long movieId,
                                @Param("typeId") Long typeId,
                                @Param("isPrimary") Integer isPrimary,
                                @Param("sortOrder") Integer sortOrder);

    Long selectMovieRegionIdByName(@Param("regionName") String regionName);

    int insertMovieRegion(@Param("regionName") String regionName,
                          @Param("normalizedName") String normalizedName);

    int updateMovieRegionReference(@Param("movieId") Long movieId,
                                   @Param("regionId") Long regionId);

    int deleteMoviePersonRelationsByMovieId(@Param("movieId") Long movieId);

    int deleteMoviePersonRelationsByMovieIdAndType(@Param("movieId") Long movieId,
                                                   @Param("relationType") String relationType);

    Long selectMoviePersonIdByName(@Param("personName") String personName);

    int insertMoviePerson(@Param("personName") String personName,
                          @Param("normalizedName") String normalizedName,
                          @Param("sourceNote") String sourceNote);

    int insertMoviePersonRelation(@Param("movieId") Long movieId,
                                  @Param("personId") Long personId,
                                  @Param("relationType") String relationType,
                                  @Param("sortOrder") Integer sortOrder);

    int deleteMovieCommentVotesByMovieId(@Param("movieId") Long movieId);

    int deleteMovieCommentsByMovieId(@Param("movieId") Long movieId);

    int deleteMovieActionsByMovieId(@Param("movieId") Long movieId);

    int deleteMovieLibrariesByMovieId(@Param("movieId") Long movieId);

    int deleteMovieOrdersByMovieId(@Param("movieId") Long movieId);

    int deleteMovieByActualId(@Param("movieId") Long movieId);

    List<DianyingxinxiEntity> selectLegacyMoviesByLegacyIds(@Param("legacyIds") List<Long> legacyIds);

    List<StoreupView> selectLegacyFavoriteActionsForRecommendation();

    int countLegacyMovieTypes(@Param("typeKeyword") String typeKeyword);

    List<DianyingleixingEntity> selectLegacyMovieTypesPage(@Param("typeKeyword") String typeKeyword,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit,
                                                           @Param("sortColumn") String sortColumn,
                                                           @Param("orderKeyword") String orderKeyword);

    List<DianyingleixingEntity> selectLegacyMovieTypes(@Param("typeKeyword") String typeKeyword,
                                                       @Param("sortColumn") String sortColumn,
                                                       @Param("orderKeyword") String orderKeyword);

    Long selectActualMovieTypeIdByLegacyId(@Param("legacyId") Long legacyId);

    Long selectBridgeMovieTypeIdByActualId(@Param("actualId") Long actualId);

    DianyingleixingEntity selectLegacyMovieTypeByActualId(@Param("actualId") Long actualId);

    Long selectMovieTypeIdByName(@Param("typeName") String typeName);

    int insertMovieType(@Param("typeName") String typeName,
                        @Param("sourceNote") String sourceNote);

    int updateMovieTypeByActualId(@Param("actualId") Long actualId,
                                  @Param("typeName") String typeName,
                                  @Param("sourceNote") String sourceNote);

    int detachMovieTypeReferences(@Param("typeId") Long typeId,
                                  @Param("fallbackTypeName") String fallbackTypeName);

    List<Long> selectMovieIdsByTypeId(@Param("typeId") Long typeId);

    int deleteMovieTypeRelationsByTypeId(@Param("typeId") Long typeId);

    Long selectFallbackMovieTypeIdByMovieId(@Param("movieId") Long movieId);

    int updateMoviePrimaryTypeByMovieId(@Param("movieId") Long movieId,
                                        @Param("typeId") Long typeId);

    int deleteMovieTypeByActualId(@Param("typeId") Long typeId);
}
