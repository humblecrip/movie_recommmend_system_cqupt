package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.DianyingleixingEntity;
import com.entity.view.DianyingleixingView;
import com.service.AppMovieCompatibilityService;
import com.service.DianyingleixingService;
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

/**
 * 电影类型
 * 后端接口
 *
 * 兼容要求：旧 URL 继续保留，但底层真值统一桥接 app_movie_type。
 */
@RestController
@RequestMapping("/dianyingleixing")
public class DianyingleixingController {

    @Autowired
    private AppMovieCompatibilityService appMovieCompatibilityService;

    @Autowired
    private DianyingleixingService dianyingleixingService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam java.util.Map<String, Object> params,
                  DianyingleixingEntity<?> dianyingleixing,
                  HttpServletRequest request) {
        PageUtils page = appMovieCompatibilityService.queryLegacyMovieTypePage(params, dianyingleixing);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 前台列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam java.util.Map<String, Object> params,
                  DianyingleixingEntity<?> dianyingleixing,
                  HttpServletRequest request) {
        PageUtils page = appMovieCompatibilityService.queryLegacyMovieTypePage(params, dianyingleixing);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(DianyingleixingEntity<?> dianyingleixing) {
        return R.ok().put("data", appMovieCompatibilityService.selectLegacyMovieTypeList(dianyingleixing));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DianyingleixingEntity<?> dianyingleixing) {
        DianyingleixingEntity<?> movieType = appMovieCompatibilityService.selectLegacyMovieType(dianyingleixing);
        DianyingleixingView view = movieType == null ? null : new DianyingleixingView((DianyingleixingEntity) movieType);
        return R.ok("查询电影类型成功").put("data", view);
    }

    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        DianyingleixingEntity<?> movieType = appMovieCompatibilityService.getLegacyMovieTypeById(id);
        if (movieType != null) {
            DeSensUtil.desensitize(movieType, new HashMap<String, String>());
        }
        return R.ok().put("data", movieType);
    }

    /**
     * 前台详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        DianyingleixingEntity<?> movieType = appMovieCompatibilityService.getLegacyMovieTypeById(id);
        if (movieType != null) {
            DeSensUtil.desensitize(movieType, new HashMap<String, String>());
        }
        return R.ok().put("data", movieType);
    }

    /**
     * 后台保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DianyingleixingEntity<?> dianyingleixing, HttpServletRequest request) {
        Long savedId = appMovieCompatibilityService.saveLegacyMovieType(dianyingleixing);
        return savedId == null ? R.error("保存电影类型失败") : R.ok().put("data", savedId);
    }

    /**
     * 前台保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DianyingleixingEntity<?> dianyingleixing, HttpServletRequest request) {
        Long savedId = appMovieCompatibilityService.saveLegacyMovieType(dianyingleixing);
        return savedId == null ? R.error("保存电影类型失败") : R.ok().put("data", savedId);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody DianyingleixingEntity<?> dianyingleixing, HttpServletRequest request) {
        return appMovieCompatibilityService.updateLegacyMovieType(dianyingleixing) ? R.ok() : R.error("更新电影类型失败");
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        int deleted = appMovieCompatibilityService.deleteLegacyMovieTypes(Arrays.asList(ids));
        return deleted > 0 || ids.length == 0 ? R.ok() : R.error("删除电影类型失败");
    }
}
