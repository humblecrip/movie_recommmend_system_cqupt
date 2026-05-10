package com.entity.vo;

import java.io.Serializable;

/**
 * app_movie_type 只读输出。
 */
public class AppMovieTypeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String typeName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
