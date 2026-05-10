package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * User movie preference entity for AI cold start.
 */
@TableName("app_user_preference")
public class AiUserPreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String favoriteGenres;
    private String favoriteYears;
    private String favoriteRegions;
    private String moodPreferences;
    private String watchFrequency;
    private String dislikedGenres;
    private Integer coldStartCompleted;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date coldStartCompletedAt;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createdAt;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }
    public String getFavoriteYears() { return favoriteYears; }
    public void setFavoriteYears(String favoriteYears) { this.favoriteYears = favoriteYears; }
    public String getFavoriteRegions() { return favoriteRegions; }
    public void setFavoriteRegions(String favoriteRegions) { this.favoriteRegions = favoriteRegions; }
    public String getMoodPreferences() { return moodPreferences; }
    public void setMoodPreferences(String moodPreferences) { this.moodPreferences = moodPreferences; }
    public String getWatchFrequency() { return watchFrequency; }
    public void setWatchFrequency(String watchFrequency) { this.watchFrequency = watchFrequency; }
    public String getDislikedGenres() { return dislikedGenres; }
    public void setDislikedGenres(String dislikedGenres) { this.dislikedGenres = dislikedGenres; }
    public Integer getColdStartCompleted() { return coldStartCompleted; }
    public void setColdStartCompleted(Integer coldStartCompleted) { this.coldStartCompleted = coldStartCompleted; }
    public Date getColdStartCompletedAt() { return coldStartCompletedAt; }
    public void setColdStartCompletedAt(Date coldStartCompletedAt) { this.coldStartCompletedAt = coldStartCompletedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
