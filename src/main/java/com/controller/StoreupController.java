package com.controller;

import com.annotation.IgnoreAuth;
import com.entity.StoreupEntity;
import com.entity.view.StoreupView;
import com.service.StoreupCompatibilityService;
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
 * 收藏表
 * 后端接口
 * @author 
 * @email 
 * @date 2025-04-12 20:00:43
 */
@RestController
@RequestMapping("/storeup")
public class StoreupController {
    private static final String UPDATE_FAILURE_MESSAGE = "收藏更新失败：仅支持已映射到 app_movie 的电影收藏记录";

    @Autowired
    private StoreupCompatibilityService storeupCompatibilityService;





    



    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,StoreupEntity storeup,
		HttpServletRequest request){
        boolean adminView = isAdmin(request);
        if(!adminView) {
            storeup.setUserid(getSessionUserId(request));
        }
		PageUtils page = storeupCompatibilityService.queryPage(params, storeup, getSessionUserId(request), adminView);
        Map<String, String> deSens = new HashMap<>();
        DeSensUtil.desensitize(page,deSens);
        return R.ok().put("data", page);
    }
    
    /**
     * 前台列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,StoreupEntity storeup, 
		HttpServletRequest request){
		PageUtils page = storeupCompatibilityService.queryPage(params, storeup, getSessionUserId(request), isAdmin(request));
        Map<String, String> deSens = new HashMap<>();
        DeSensUtil.desensitize(page,deSens);
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( StoreupEntity storeup){
        return R.ok().put("data", storeupCompatibilityService.selectList(storeup, null, true));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(StoreupEntity storeup){
		List<StoreupView> storeupViews = storeupCompatibilityService.selectList(storeup, null, true);
        StoreupView storeupView = storeupViews.isEmpty() ? null : storeupViews.get(0);
		return R.ok("查询收藏表成功").put("data", storeupView);
    }
	
    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        StoreupView storeup = storeupCompatibilityService.selectById(id);
        Map<String, String> deSens = new HashMap<>();
        DeSensUtil.desensitize(storeup,deSens);
        return R.ok().put("data", storeup);
    }

    /**
     * 前台详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        StoreupView storeup = storeupCompatibilityService.selectById(id);
        Map<String, String> deSens = new HashMap<>();
        DeSensUtil.desensitize(storeup,deSens);
        return R.ok().put("data", storeup);
    }
    



    /**
     * 后台保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody StoreupEntity storeup, HttpServletRequest request){
        storeup.setUserid(getSessionUserId(request));
        Long id = storeupCompatibilityService.save(storeup, getSessionUserId(request));
        return id == null ? R.error("收藏保存失败") : R.ok().put("data",id);
    }
    
    /**
     * 前台保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody StoreupEntity storeup, HttpServletRequest request){
        if (storeup.getUserid() == null) {
            storeup.setUserid(getSessionUserId(request));
        }
        Long id = storeupCompatibilityService.save(storeup, getSessionUserId(request));
        return id == null ? R.error("收藏保存失败") : R.ok().put("data",id);
    }



     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        return R.error("收藏兼容桥接未提供该能力");
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody StoreupEntity storeup, HttpServletRequest request){
        boolean updated = storeupCompatibilityService.update(storeup, getSessionUserId(request), isAdmin(request));
        return updated ? R.ok() : R.error(UPDATE_FAILURE_MESSAGE);
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        storeupCompatibilityService.deleteBatch(Arrays.asList(ids));
        return R.ok();
    }
    
	/**
     * 前台智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,StoreupEntity storeup, HttpServletRequest request,String pre){
		params.put("sort", "addtime");
        params.put("order", "desc");
		PageUtils page = storeupCompatibilityService.queryPage(params, storeup, getSessionUserId(request), isAdmin(request));
        return R.ok().put("data", page);
    }

    private Long getSessionUserId(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return null;
        }
        Object userId = request.getSession().getAttribute("userId");
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return false;
        }
        Object role = request.getSession().getAttribute("role");
        return role != null && "管理员".equals(String.valueOf(role));
    }








}
