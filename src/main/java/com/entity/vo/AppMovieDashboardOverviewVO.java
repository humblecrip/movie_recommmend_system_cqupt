package com.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台电影首页聚合 DTO。
 */
public class AppMovieDashboardOverviewVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long movieTotal = 0L;
    private Long totalClickCount = 0L;
    private Long totalLikeCount = 0L;
    private Long totalDislikeCount = 0L;
    private Long totalFavoriteCount = 0L;
    private Long totalCommentCount = 0L;
    private Double totalScore = 0D;
    private List<AppMovieListVO> topClickedMovies = new ArrayList<AppMovieListVO>();
    private List<AppMovieListVO> topLikedMovies = new ArrayList<AppMovieListVO>();
    private List<AppMovieListVO> topFavoritedMovies = new ArrayList<AppMovieListVO>();
    private List<AppMovieMetricVO> typeDistribution = new ArrayList<AppMovieMetricVO>();
    private List<AppMovieMetricVO> releaseYearDistribution = new ArrayList<AppMovieMetricVO>();

    public Long getMovieTotal() {
        return movieTotal;
    }

    public void setMovieTotal(Long movieTotal) {
        this.movieTotal = movieTotal;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public Long getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(Long totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }

    public Long getTotalDislikeCount() {
        return totalDislikeCount;
    }

    public void setTotalDislikeCount(Long totalDislikeCount) {
        this.totalDislikeCount = totalDislikeCount;
    }

    public Long getTotalFavoriteCount() {
        return totalFavoriteCount;
    }

    public void setTotalFavoriteCount(Long totalFavoriteCount) {
        this.totalFavoriteCount = totalFavoriteCount;
    }

    public Long getTotalCommentCount() {
        return totalCommentCount;
    }

    public void setTotalCommentCount(Long totalCommentCount) {
        this.totalCommentCount = totalCommentCount;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public List<AppMovieListVO> getTopClickedMovies() {
        return topClickedMovies;
    }

    public void setTopClickedMovies(List<AppMovieListVO> topClickedMovies) {
        this.topClickedMovies = topClickedMovies;
    }

    public List<AppMovieListVO> getTopLikedMovies() {
        return topLikedMovies;
    }

    public void setTopLikedMovies(List<AppMovieListVO> topLikedMovies) {
        this.topLikedMovies = topLikedMovies;
    }

    public List<AppMovieListVO> getTopFavoritedMovies() {
        return topFavoritedMovies;
    }

    public void setTopFavoritedMovies(List<AppMovieListVO> topFavoritedMovies) {
        this.topFavoritedMovies = topFavoritedMovies;
    }

    public List<AppMovieMetricVO> getTypeDistribution() {
        return typeDistribution;
    }

    public void setTypeDistribution(List<AppMovieMetricVO> typeDistribution) {
        this.typeDistribution = typeDistribution;
    }

    public List<AppMovieMetricVO> getReleaseYearDistribution() {
        return releaseYearDistribution;
    }

    public void setReleaseYearDistribution(List<AppMovieMetricVO> releaseYearDistribution) {
        this.releaseYearDistribution = releaseYearDistribution;
    }
}
