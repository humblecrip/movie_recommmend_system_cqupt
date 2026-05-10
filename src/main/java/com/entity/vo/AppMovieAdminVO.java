package com.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台电影分页 DTO。
 */
public class AppMovieAdminVO extends AppMovieListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long legacyDianyingxinxiId;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getLegacyDianyingxinxiId() {
        return legacyDianyingxinxiId;
    }

    public void setLegacyDianyingxinxiId(Long legacyDianyingxinxiId) {
        this.legacyDianyingxinxiId = legacyDianyingxinxiId;
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
