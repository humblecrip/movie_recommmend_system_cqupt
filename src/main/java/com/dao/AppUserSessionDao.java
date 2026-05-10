package com.dao;

import org.apache.ibatis.annotations.Param;

import com.entity.YonghuEntity;

/**
 * app_user 与 yonghu 兼容映射专用 Mapper。
 */
public interface AppUserSessionDao {

    Long selectIdByLegacyYonghuId(@Param("legacyYonghuId") Long legacyYonghuId);

    int upsertFromYonghu(YonghuEntity yonghu);
}
