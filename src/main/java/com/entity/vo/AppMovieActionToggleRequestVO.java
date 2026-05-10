package com.entity.vo;

import java.io.Serializable;

/**
 * 电影互动行为切换请求。
 */
public class AppMovieActionToggleRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long movieId;
    private String actionType;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
