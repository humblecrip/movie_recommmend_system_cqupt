package com.dao;

import org.apache.ibatis.annotations.Param;

import com.entity.AppAuthSessionEntity;
import com.entity.TokenEntity;

/**
 * app_auth_session 会话读写专用 Mapper。
 */
public interface AppAuthSessionDao {

    int updateActiveSession(AppAuthSessionEntity session);

    int insertSession(AppAuthSessionEntity session);

    TokenEntity selectTokenEntityByToken(@Param("token") String token);

    int updateSubjectLoginName(@Param("legacySubjectId") Long legacySubjectId,
                               @Param("subjectTableName") String subjectTableName,
                               @Param("subjectLoginName") String subjectLoginName);
}
