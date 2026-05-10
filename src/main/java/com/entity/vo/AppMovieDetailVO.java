package com.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 前台电影详情 DTO，包含基础信息与相似电影。
 */
public class AppMovieDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long legacyDianyingxinxiId;
    private Long typeId;
    private String title;
    private String typeName;
    private String posterUrl;
    private List<String> posterUrls = new ArrayList<String>();
    private String regionName;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date releaseDate;
    private String directorName;
    private String castNames;
    private String synopsis;
    private String detailHtml;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer clickCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Double totalScore;
    private List<AppMovieListVO> similarMovies = new ArrayList<AppMovieListVO>();
    private String posterUrlsCsv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLegacyDianyingxinxiId() {
        return legacyDianyingxinxiId;
    }

    public void setLegacyDianyingxinxiId(Long legacyDianyingxinxiId) {
        this.legacyDianyingxinxiId = legacyDianyingxinxiId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<String> getPosterUrls() {
        return posterUrls;
    }

    public void setPosterUrls(List<String> posterUrls) {
        this.posterUrls = posterUrls;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public String getCastNames() {
        return castNames;
    }

    public void setCastNames(String castNames) {
        this.castNames = castNames;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getDetailHtml() {
        return detailHtml;
    }

    public void setDetailHtml(String detailHtml) {
        this.detailHtml = detailHtml;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public List<AppMovieListVO> getSimilarMovies() {
        return similarMovies;
    }

    public void setSimilarMovies(List<AppMovieListVO> similarMovies) {
        this.similarMovies = similarMovies;
    }

    @JsonIgnore
    public String getPosterUrlsCsv() {
        return posterUrlsCsv;
    }

    public void setPosterUrlsCsv(String posterUrlsCsv) {
        this.posterUrlsCsv = posterUrlsCsv;
    }
}
