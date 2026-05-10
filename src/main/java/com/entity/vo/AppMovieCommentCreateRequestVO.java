package com.entity.vo;

import java.io.Serializable;

/**
 * 新增电影评论请求。
 */
public class AppMovieCommentCreateRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long movieId;
    private String contentHtml;
    private Double rating;
    private String authorAvatar;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }
}
