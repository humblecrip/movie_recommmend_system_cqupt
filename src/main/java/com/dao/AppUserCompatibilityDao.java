package com.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.entity.YonghuEntity;

/**
 * yonghu 兼容读写层，底层真值为 app_user。
 */
public interface AppUserCompatibilityDao {

    YonghuEntity selectByLegacyId(@Param("legacyYonghuId") Long legacyYonghuId);

    YonghuEntity selectByLoginName(@Param("loginName") String loginName);

    int countByLoginName(@Param("loginName") String loginName,
                         @Param("excludeLegacyYonghuId") Long excludeLegacyYonghuId);

    long countCompat(@Param("entity") YonghuEntity entity);

    List<YonghuEntity> selectCompatList(@Param("entity") YonghuEntity entity,
                                        @Param("sortColumn") String sortColumn,
                                        @Param("sortOrder") String sortOrder);

    List<YonghuEntity> selectCompatPage(@Param("entity") YonghuEntity entity,
                                        @Param("offset") long offset,
                                        @Param("limit") int limit,
                                        @Param("sortColumn") String sortColumn,
                                        @Param("sortOrder") String sortOrder);

    int insertFromCompat(YonghuEntity yonghu);

    int updateByLegacyId(YonghuEntity yonghu);

    int deleteBatchByLegacyIds(@Param("legacyIds") List<Long> legacyIds);
}
