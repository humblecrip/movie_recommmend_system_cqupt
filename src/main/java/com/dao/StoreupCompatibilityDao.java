package com.dao;

import com.entity.view.StoreupView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 旧 storeup 接口兼容查询，底层桥接 app_user_movie_action。
 */
public interface StoreupCompatibilityDao {

    int countByFilter(@Param("nameKeyword") String nameKeyword,
                      @Param("filter") StoreupView filter,
                      @Param("sessionUserId") Long sessionUserId,
                      @Param("adminView") boolean adminView);

    List<StoreupView> selectPageByFilter(@Param("nameKeyword") String nameKeyword,
                                         @Param("filter") StoreupView filter,
                                         @Param("sessionUserId") Long sessionUserId,
                                         @Param("adminView") boolean adminView,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit,
                                         @Param("sortColumn") String sortColumn,
                                         @Param("orderKeyword") String orderKeyword);

    List<StoreupView> selectListByFilter(@Param("nameKeyword") String nameKeyword,
                                         @Param("filter") StoreupView filter,
                                         @Param("sessionUserId") Long sessionUserId,
                                         @Param("adminView") boolean adminView,
                                         @Param("sortColumn") String sortColumn,
                                         @Param("orderKeyword") String orderKeyword);

    StoreupView selectById(@Param("id") Long id);

    Long selectMovieIdByLegacyRefId(@Param("legacyRefId") Long legacyRefId);

    int insertAction(@Param("legacyUserId") Long legacyUserId,
                     @Param("appUserId") Long appUserId,
                     @Param("movieId") Long movieId,
                     @Param("legacyRefId") Long legacyRefId,
                     @Param("legacyTableName") String legacyTableName,
                     @Param("actionType") String actionType,
                     @Param("name") String name,
                     @Param("picture") String picture,
                     @Param("recommendType") String recommendType,
                     @Param("remark") String remark);

    Long selectLastInsertId();

    int updateAction(@Param("id") Long id,
                     @Param("legacyUserId") Long legacyUserId,
                     @Param("adminView") boolean adminView,
                     @Param("movieId") Long movieId,
                     @Param("legacyRefId") Long legacyRefId,
                     @Param("legacyTableName") String legacyTableName,
                     @Param("actionType") String actionType,
                     @Param("name") String name,
                     @Param("picture") String picture,
                     @Param("recommendType") String recommendType,
                     @Param("remark") String remark);

    int deleteBatch(@Param("ids") List<Long> ids);
}
