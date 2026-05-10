package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.DiscussdianyingxinxiEntity;
import com.entity.view.DiscussdianyingxinxiView;
import com.service.AppMovieInteractionService;
import com.utils.DeSensUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电影评论旧接口兼容桥接，底层统一走 app_movie_comment*。
 */
@RestController
@RequestMapping("/discussdianyingxinxi")
public class DiscussdianyingxinxiController {

    @Autowired
    private AppMovieInteractionService appMovieInteractionService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,
                  DiscussdianyingxinxiEntity<?> discussdianyingxinxi,
                  HttpServletRequest request) {
        PageUtils page = appMovieInteractionService.queryLegacyComments(params, discussdianyingxinxi);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 前台列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,
                  DiscussdianyingxinxiEntity<?> discussdianyingxinxi,
                  HttpServletRequest request) {
        PageUtils page = appMovieInteractionService.queryLegacyComments(params, discussdianyingxinxi);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(DiscussdianyingxinxiEntity<?> discussdianyingxinxi) {
        return R.ok().put("data", appMovieInteractionService.selectLegacyCommentViews(discussdianyingxinxi));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DiscussdianyingxinxiEntity<?> discussdianyingxinxi) {
        DiscussdianyingxinxiView view = appMovieInteractionService.selectLegacyCommentView(discussdianyingxinxi);
        return R.ok("查询电影信息评论表成功").put("data", view);
    }

    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", desensitizeEntity(appMovieInteractionService.getLegacyCommentById(id)));
    }

    /**
     * 前台详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        return R.ok().put("data", desensitizeEntity(appMovieInteractionService.getLegacyCommentById(id)));
    }

    /**
     * 后台保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DiscussdianyingxinxiEntity<?> discussdianyingxinxi, HttpServletRequest request) {
        try {
            Long savedId = appMovieInteractionService.createLegacyComment(getLegacyUserId(request, discussdianyingxinxi), discussdianyingxinxi);
            return savedId == null ? R.error("评论保存失败") : R.ok().put("data", savedId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 前台保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DiscussdianyingxinxiEntity<?> discussdianyingxinxi, HttpServletRequest request) {
        try {
            Long savedId = appMovieInteractionService.createLegacyComment(getLegacyUserId(request, discussdianyingxinxi), discussdianyingxinxi);
            return savedId == null ? R.error("评论保存失败") : R.ok().put("data", savedId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username) {
        return R.ok().put("data", null);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    @IgnoreAuth
    public R update(@RequestBody DiscussdianyingxinxiEntity<?> discussdianyingxinxi, HttpServletRequest request) {
        try {
            return appMovieInteractionService.updateLegacyComment(discussdianyingxinxi) ? R.ok() : R.error("评论更新失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        try {
            return appMovieInteractionService.deleteLegacyComments(idList) > 0 ? R.ok() : R.error("评论删除失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 前台智能排序
     */
    @IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,
                      DiscussdianyingxinxiEntity<?> discussdianyingxinxi,
                      HttpServletRequest request,
                      String pre) {
        params.put("sort", "addtime");
        params.put("order", "desc");
        return R.ok().put("data", appMovieInteractionService.queryLegacyComments(params, discussdianyingxinxi));
    }

    private DiscussdianyingxinxiEntity<?> desensitizeEntity(DiscussdianyingxinxiEntity<?> entity) {
        DeSensUtil.desensitize(entity, new HashMap<String, String>());
        return entity;
    }

    private Long getLegacyUserId(HttpServletRequest request, DiscussdianyingxinxiEntity<?> discussdianyingxinxi) {
        Object userId = request == null || request.getSession() == null ? null : request.getSession().getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId != null) {
            try {
                return Long.valueOf(String.valueOf(userId));
            } catch (NumberFormatException e) {
                return discussdianyingxinxi == null ? null : discussdianyingxinxi.getUserid();
            }
        }
        return discussdianyingxinxi == null ? null : discussdianyingxinxi.getUserid();
    }
}
