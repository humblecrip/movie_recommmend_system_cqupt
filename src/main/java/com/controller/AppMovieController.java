package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.TokenEntity;
import com.service.AppMovieService;
import com.service.TokenService;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * app_* 电影只读接口。
 */
@RestController
@RequestMapping("/appmovie")
public class AppMovieController {

    private static final String FRONT_USER_TABLE_NAME = "yonghu";

    @Autowired
    private AppMovieService appMovieService;

    @Autowired
    private TokenService tokenService;

    @IgnoreAuth
    @RequestMapping("/types")
    public R types() {
        return R.ok().put("data", appMovieService.listTypes());
    }

    @IgnoreAuth
    @RequestMapping("/front/list")
    public R frontList(@RequestParam Map<String, Object> params) {
        return R.ok().put("data", appMovieService.queryFrontPage(params));
    }

    @IgnoreAuth
    @RequestMapping("/front/recommended")
    public R frontRecommended(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        return R.ok().put("data", appMovieService.queryFrontRecommendedPage(params, resolveLegacyUserId(request)));
    }

    @IgnoreAuth
    @RequestMapping("/front/detail/{id}")
    public R frontDetail(@PathVariable("id") Long id) {
        return R.ok().put("data", appMovieService.getFrontDetail(id));
    }

    @RequestMapping("/admin/page")
    public R adminPage(@RequestParam Map<String, Object> params) {
        return R.ok().put("data", appMovieService.queryAdminPage(params));
    }

    @RequestMapping("/admin/dashboard/overview")
    public R dashboardOverview() {
        return R.ok().put("data", appMovieService.getDashboardOverview());
    }

    private Long resolveLegacyUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Token");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        TokenEntity tokenEntity = tokenService.getTokenEntity(token);
        if (tokenEntity == null || !FRONT_USER_TABLE_NAME.equals(tokenEntity.getTablename())) {
            return null;
        }
        return tokenEntity.getUserid();
    }
}
