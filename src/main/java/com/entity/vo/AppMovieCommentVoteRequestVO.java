package com.entity.vo;

import java.io.Serializable;

/**
 * 评论点赞/点踩请求。
 */
public class AppMovieCommentVoteRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long commentId;
    private String voteType;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getVoteType() {
        return voteType;
    }

    public void setVoteType(String voteType) {
        this.voteType = voteType;
    }
}
