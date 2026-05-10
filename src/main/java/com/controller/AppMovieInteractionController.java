package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.vo.AppMovieActionStatusVO;
import com.entity.vo.AppMovieActionToggleRequestVO;
import com.entity.vo.AppMovieCommentCreateRequestVO;
import com.entity.vo.AppMovieCommentVO;
import com.entity.vo.AppMovieCommentVoteRequestVO;
import com.service.AppMovieInteractionService;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * app_* 电影互动接口，替代前台电影域旧 storeup/discuss 写链路。
 */
@RestController
@RequestMapping("/appmovie")
public class AppMovieInteractionController {

    @Autowired
    private AppMovieInteractionService appMovieInteractionService;

    @RequestMapping("/actions/status")
    public R movieActionStatus(@RequestParam("movieId") Long movieId, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        return R.ok().put("data", appMovieInteractionService.getMovieActionStatus(legacyUserId, movieId));
    }

    @RequestMapping("/actions/toggle")
    public R toggleMovieAction(@RequestBody AppMovieActionToggleRequestVO actionRequest, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        AppMovieActionStatusVO actionStatus = appMovieInteractionService.toggleMovieAction(legacyUserId, actionRequest);
        return actionStatus == null ? R.error("电影互动操作失败") : R.ok().put("data", actionStatus);
    }

    @RequestMapping("/favorites/page")
    public R favoritePage(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        return R.ok().put("data", appMovieInteractionService.queryFavorites(legacyUserId, params));
    }

    @RequestMapping("/favorites/cancel")
    public R cancelFavorite(@RequestBody AppMovieActionToggleRequestVO actionRequest, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        if (actionRequest != null) {
            actionRequest.setActionType("favorite");
        }
        AppMovieActionStatusVO actionStatus = appMovieInteractionService.cancelFavorite(legacyUserId, actionRequest);
        return actionStatus == null ? R.error("取消收藏失败") : R.ok().put("data", actionStatus);
    }

    @IgnoreAuth
    @RequestMapping("/comments/page")
    public R commentPage(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        return R.ok().put("data", appMovieInteractionService.queryComments(getLegacyUserId(request, params), params));
    }

    @RequestMapping("/comments/add")
    public R addComment(@RequestBody AppMovieCommentCreateRequestVO commentRequest, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        AppMovieCommentVO comment = appMovieInteractionService.addComment(legacyUserId, commentRequest);
        return comment == null ? R.error("评论发布失败") : R.ok().put("data", comment);
    }

    @RequestMapping("/comments/delete/{id}")
    public R deleteComment(@PathVariable("id") Long id, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        return appMovieInteractionService.deleteComment(legacyUserId, id) ? R.ok() : R.error("评论不存在或无权删除");
    }

    @RequestMapping("/comments/delete")
    public R deleteCommentByBody(@RequestBody AppMovieCommentVoteRequestVO requestBody, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        Long commentId = requestBody == null ? null : requestBody.getCommentId();
        return appMovieInteractionService.deleteComment(legacyUserId, commentId) ? R.ok() : R.error("评论不存在或无权删除");
    }

    @RequestMapping("/comments/vote")
    public R toggleCommentVote(@RequestBody AppMovieCommentVoteRequestVO voteRequest, HttpServletRequest request) {
        Long legacyUserId = getLegacyUserId(request);
        if (legacyUserId == null) {
            return R.error(401, "请先登录");
        }
        AppMovieCommentVO comment = appMovieInteractionService.toggleCommentVote(legacyUserId, voteRequest);
        return comment == null ? R.error("评论互动操作失败") : R.ok().put("data", comment);
    }

    private Long getLegacyUserId(HttpServletRequest request) {
        return getLegacyUserId(request, null);
    }

    private Long getLegacyUserId(HttpServletRequest request, Map<String, Object> params) {
        Object userId = request == null || request.getSession() == null ? null : request.getSession().getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId == null && params != null) {
            userId = params.get("legacyUserId");
        }
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
