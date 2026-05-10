package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.DianyingxinxiEntity;
import com.entity.view.DianyingxinxiView;
import com.service.AppMovieCompatibilityService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电影信息
 * 后端接口
 *
 * 兼容要求：旧 URL 继续保留，但底层真值统一桥接 app_movie*。
 */
@RestController
@RequestMapping("/dianyingxinxi")
public class DianyingxinxiController {

    @Autowired
    private AppMovieCompatibilityService appMovieCompatibilityService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,
                  DianyingxinxiEntity<?> dianyingxinxi,
                  HttpServletRequest request) {
        PageUtils page = appMovieCompatibilityService.queryLegacyMoviePage(params, dianyingxinxi);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 前台列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,
                  DianyingxinxiEntity<?> dianyingxinxi,
                  HttpServletRequest request) {
        PageUtils page = appMovieCompatibilityService.queryLegacyMoviePage(params, dianyingxinxi);
        DeSensUtil.desensitize(page, new HashMap<String, String>());
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(DianyingxinxiEntity<?> dianyingxinxi) {
        return R.ok().put("data", appMovieCompatibilityService.selectLegacyMovieList(dianyingxinxi));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DianyingxinxiEntity<?> dianyingxinxi) {
        DianyingxinxiEntity<?> movie = appMovieCompatibilityService.selectLegacyMovie(dianyingxinxi);
        DianyingxinxiView view = movie == null ? null : new DianyingxinxiView((DianyingxinxiEntity) movie);
        return R.ok("查询电影信息成功").put("data", view);
    }

    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        DianyingxinxiEntity<?> movie = appMovieCompatibilityService.getLegacyMovieById(id, true);
        if (movie != null) {
            DeSensUtil.desensitize(movie, new HashMap<String, String>());
        }
        return R.ok().put("data", movie);
    }

    /**
     * 前台详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        DianyingxinxiEntity<?> movie = appMovieCompatibilityService.getLegacyMovieById(id, true);
        if (movie != null) {
            DeSensUtil.desensitize(movie, new HashMap<String, String>());
        }
        return R.ok().put("data", movie);
    }

    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") Long id, String type) {
        return appMovieCompatibilityService.incrementLegacyMovieVote(id, type) ? R.ok("投票成功") : R.error("投票失败");
    }

    /**
     * 后台保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DianyingxinxiEntity<?> dianyingxinxi, HttpServletRequest request) {
        Long savedId = appMovieCompatibilityService.saveLegacyMovie(dianyingxinxi);
        return savedId == null ? R.error("保存电影信息失败") : R.ok().put("data", savedId);
    }

    /**
     * 前台保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DianyingxinxiEntity<?> dianyingxinxi, HttpServletRequest request) {
        Long savedId = appMovieCompatibilityService.saveLegacyMovie(dianyingxinxi);
        return savedId == null ? R.error("保存电影信息失败") : R.ok().put("data", savedId);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody DianyingxinxiEntity<?> dianyingxinxi, HttpServletRequest request) {
        return appMovieCompatibilityService.updateLegacyMovie(dianyingxinxi) ? R.ok() : R.error("更新电影信息失败");
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        int deleted = appMovieCompatibilityService.deleteLegacyMovies(Arrays.asList(ids));
        return deleted > 0 || ids.length == 0 ? R.ok() : R.error("删除电影信息失败");
    }

    /**
     * 前台智能排序
     */
    @IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,
                      DianyingxinxiEntity<?> dianyingxinxi,
                      HttpServletRequest request,
                      String pre) {
        params.put("sort", "clicknum");
        params.put("order", "desc");
        return R.ok().put("data", appMovieCompatibilityService.queryLegacyMoviePage(params, dianyingxinxi));
    }

    /**
     * 协同算法（基于用户收藏的协同算法）
     */
    @RequestMapping("/autoSort2")
    public R autoSort2(@RequestParam Map<String, Object> params,
                       DianyingxinxiEntity<?> dianyingxinxi,
                       HttpServletRequest request) {
        return R.ok().put("data", appMovieCompatibilityService.queryLegacyRecommendedMovies(
                params,
                dianyingxinxi,
                getLegacyUserId(request)
        ));
    }

    /**
     * （按值统计）
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName,
                   @PathVariable("xColumnName") String xColumnName,
                   HttpServletRequest request) {
        try {
            List<Map<String, Object>> result = appMovieCompatibilityService.selectLegacyMovieValueStats(xColumnName, yColumnName, null);
            normalizeDateValues(result);
            sortValueResult(result);
            return R.ok().put("data", result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * （按值统计(多)）
     */
    @RequestMapping("/valueMul/{xColumnName}")
    public R valueMul(@PathVariable("xColumnName") String xColumnName,
                      @RequestParam String yColumnNameMul,
                      HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        List<List<Map<String, Object>>> result = new ArrayList<List<Map<String, Object>>>();
        try {
            for (String yColumnName : yColumnNames) {
                List<Map<String, Object>> values = appMovieCompatibilityService.selectLegacyMovieValueStats(xColumnName, yColumnName, null);
                normalizeDateValues(values);
                sortValueResult(values);
                result.add(values);
            }
            return R.ok().put("data", result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * （按值统计）时间统计类型
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
    public R valueDay(@PathVariable("yColumnName") String yColumnName,
                      @PathVariable("xColumnName") String xColumnName,
                      @PathVariable("timeStatType") String timeStatType,
                      HttpServletRequest request) {
        try {
            List<Map<String, Object>> result = appMovieCompatibilityService.selectLegacyMovieValueStats(xColumnName, yColumnName, timeStatType);
            normalizeDateValues(result);
            return R.ok().put("data", result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * （按值统计）时间统计类型(多)
     */
    @RequestMapping("/valueMul/{xColumnName}/{timeStatType}")
    public R valueMulDay(@PathVariable("xColumnName") String xColumnName,
                         @PathVariable("timeStatType") String timeStatType,
                         @RequestParam String yColumnNameMul,
                         HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        List<List<Map<String, Object>>> result = new ArrayList<List<Map<String, Object>>>();
        try {
            for (String yColumnName : yColumnNames) {
                List<Map<String, Object>> values = appMovieCompatibilityService.selectLegacyMovieValueStats(xColumnName, yColumnName, timeStatType);
                normalizeDateValues(values);
                result.add(values);
            }
            return R.ok().put("data", result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 分组统计
     */
    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName, HttpServletRequest request) {
        try {
            List<Map<String, Object>> result = appMovieCompatibilityService.selectLegacyMovieGroupStats(columnName);
            normalizeDateValues(result);
            return R.ok().put("data", result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,
                   DianyingxinxiEntity<?> dianyingxinxi,
                   HttpServletRequest request) {
        return R.ok().put("data", appMovieCompatibilityService.countLegacyMovies(params, dianyingxinxi));
    }

    private void normalizeDateValues(List<Map<String, Object>> result) {
        if (result == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> record : result) {
            for (String key : new ArrayList<String>(record.keySet())) {
                if (record.get(key) instanceof Date) {
                    record.put(key, sdf.format((Date) record.get(key)));
                }
            }
        }
    }

    private void sortValueResult(List<Map<String, Object>> result) {
        Collections.sort(result, (map1, map2) -> {
            Number total1 = (Number) map1.get("total");
            Number total2 = (Number) map2.get("total");
            return Double.compare(total2 == null ? 0D : total2.doubleValue(), total1 == null ? 0D : total1.doubleValue());
        });
    }

    private Long getLegacyUserId(HttpServletRequest request) {
        Object userId = request == null || request.getSession() == null ? null : request.getSession().getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
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
