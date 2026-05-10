package com.dao;

import org.apache.ibatis.annotations.Param;

/**
 * app_admin_user 与 users 兼容映射专用 Mapper。
 */
public interface AppAdminUserSessionDao {

    Long selectIdByLegacyUsersId(@Param("legacyUsersId") Long legacyUsersId);
}
