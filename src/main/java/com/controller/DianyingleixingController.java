package com.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.lang.*;
import java.math.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import com.utils.ValidatorUtils;
import com.utils.DeSensUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.annotation.IgnoreAuth;

import com.entity.DianyingleixingEntity;
import com.entity.view.DianyingleixingView;

import com.service.DianyingleixingService;
import com.service.TokenService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MPUtil;
import com.utils.MapUtils;
import com.utils.CommonUtil;
import java.io.IOException;

/**
 * 电影类型
 * 后端接口
 * @author 
 * @email 
 * @date 2025-04-12 20:00:43
 */
@RestController
@RequestMapping("/dianyingleixing")
public class DianyingleixingController {
    @Autowired
    private DianyingleixingService dianyingleixingService;






    



    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,DianyingleixingEntity dianyingleixing,
		HttpServletRequest request){
        //设置查询条件
        EntityWrapper<DianyingleixingEntity> ew = new EntityWrapper<DianyingleixingEntity>();


        //查询结果
		PageUtils page = dianyingleixingService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, dianyingleixing), params), params));
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(page,deSens);
        return R.ok().put("data", page);
    }
    
    /**
     * 前台列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,DianyingleixingEntity dianyingleixing, 
		HttpServletRequest request){
        //设置查询条件
        EntityWrapper<DianyingleixingEntity> ew = new EntityWrapper<DianyingleixingEntity>();

        //查询结果
		PageUtils page = dianyingleixingService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, dianyingleixing), params), params));
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(page,deSens);
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( DianyingleixingEntity dianyingleixing){
       	EntityWrapper<DianyingleixingEntity> ew = new EntityWrapper<DianyingleixingEntity>();
      	ew.allEq(MPUtil.allEQMapPre( dianyingleixing, "dianyingleixing")); 
        return R.ok().put("data", dianyingleixingService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DianyingleixingEntity dianyingleixing){
        EntityWrapper< DianyingleixingEntity> ew = new EntityWrapper< DianyingleixingEntity>();
 		ew.allEq(MPUtil.allEQMapPre( dianyingleixing, "dianyingleixing")); 
		DianyingleixingView dianyingleixingView =  dianyingleixingService.selectView(ew);
		return R.ok("查询电影类型成功").put("data", dianyingleixingView);
    }
	
    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        DianyingleixingEntity dianyingleixing = dianyingleixingService.selectById(id);
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(dianyingleixing,deSens);
        return R.ok().put("data", dianyingleixing);
    }

    /**
     * 前台详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        DianyingleixingEntity dianyingleixing = dianyingleixingService.selectById(id);
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(dianyingleixing,deSens);
        return R.ok().put("data", dianyingleixing);
    }
    



    /**
     * 后台保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DianyingleixingEntity dianyingleixing, HttpServletRequest request){
        //ValidatorUtils.validateEntity(dianyingleixing);
        dianyingleixingService.insert(dianyingleixing);
        return R.ok().put("data",dianyingleixing.getId());
    }
    
    /**
     * 前台保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DianyingleixingEntity dianyingleixing, HttpServletRequest request){
        //ValidatorUtils.validateEntity(dianyingleixing);
        dianyingleixingService.insert(dianyingleixing);
        return R.ok().put("data",dianyingleixing.getId());
    }





    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody DianyingleixingEntity dianyingleixing, HttpServletRequest request){
        //ValidatorUtils.validateEntity(dianyingleixing);
        //全部更新
        dianyingleixingService.updateById(dianyingleixing);
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        dianyingleixingService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    








}
