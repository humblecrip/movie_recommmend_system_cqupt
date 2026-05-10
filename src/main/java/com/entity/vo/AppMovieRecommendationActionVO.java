package com.entity.vo;

import java.io.Serializable;

/**
 * 协同过滤训练样本：用户对电影的正向偏好。
 */
public class AppMovieRecommendationActionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long legacyUserId;
    private Long movieId;
    private Double preferenceScore;

    public Long getLegacyUserId() {
        return legacyUserId;
    }

    public void setLegacyUserId(Long legacyUserId) {
        this.legacyUserId = legacyUserId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Double getPreferenceScore() {
        return preferenceScore;
    }

    public void setPreferenceScore(Double preferenceScore) {
        this.preferenceScore = preferenceScore;
    }
}
