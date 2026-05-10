package com.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * phase-1 会话表实体，仅用于登录鉴权运行链路。
 */
public class AppAuthSessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long legacyTokenId;
    private Long appUserId;
    private Long appAdminUserId;
    private Long legacySubjectId;
    private String subjectTableName;
    private String subjectRoleName;
    private String subjectKind;
    private String subjectLoginName;
    private String sessionToken;
    private Date issuedAt;
    private Date expiresAt;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLegacyTokenId() {
        return legacyTokenId;
    }

    public void setLegacyTokenId(Long legacyTokenId) {
        this.legacyTokenId = legacyTokenId;
    }

    public Long getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(Long appUserId) {
        this.appUserId = appUserId;
    }

    public Long getAppAdminUserId() {
        return appAdminUserId;
    }

    public void setAppAdminUserId(Long appAdminUserId) {
        this.appAdminUserId = appAdminUserId;
    }

    public Long getLegacySubjectId() {
        return legacySubjectId;
    }

    public void setLegacySubjectId(Long legacySubjectId) {
        this.legacySubjectId = legacySubjectId;
    }

    public String getSubjectTableName() {
        return subjectTableName;
    }

    public void setSubjectTableName(String subjectTableName) {
        this.subjectTableName = subjectTableName;
    }

    public String getSubjectRoleName() {
        return subjectRoleName;
    }

    public void setSubjectRoleName(String subjectRoleName) {
        this.subjectRoleName = subjectRoleName;
    }

    public String getSubjectKind() {
        return subjectKind;
    }

    public void setSubjectKind(String subjectKind) {
        this.subjectKind = subjectKind;
    }

    public String getSubjectLoginName() {
        return subjectLoginName;
    }

    public void setSubjectLoginName(String subjectLoginName) {
        this.subjectLoginName = subjectLoginName;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
