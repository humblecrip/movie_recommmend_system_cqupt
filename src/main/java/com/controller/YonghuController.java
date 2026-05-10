package com.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.lang.*;
import java.math.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import com.dao.AppUserCompatibilityDao;
import com.dao.AppUserSessionDao;
import com.utils.ValidatorUtils;
import com.utils.DeSensUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

import com.entity.YonghuEntity;
import com.entity.vo.PasswordChangeRequestVO;
import com.entity.view.YonghuView;

import com.service.YonghuService;
import com.service.TokenService;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import com.utils.MPUtil;
import com.utils.MapUtils;
import com.utils.CommonUtil;
import com.utils.EncryptUtil;
import java.io.IOException;

/**
 * 用户
 * 后端接口
 * @author 
 * @email 
 * @date 2025-04-12 20:00:43
 */
@RestController
@RequestMapping("/yonghu")
public class YonghuController {
    private static final String CHANGE_PASSWORD_CAPTCHA_KEY = "yonghu:changePassword:captcha";
    private static final String CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY = "yonghu:changePassword:expireAt";
    private static final String CHANGE_PASSWORD_CAPTCHA_PURPOSE_KEY = "yonghu:changePassword:purpose";
    private static final String CHANGE_PASSWORD_CAPTCHA_PURPOSE = "changePassword";
    private static final int CHANGE_PASSWORD_CAPTCHA_EXPIRES_IN_SECONDS = 300;

    @Autowired
    private YonghuService yonghuService;

	@Autowired
	private TokenService tokenService;

    @Autowired
    private AppUserSessionDao appUserSessionDao;

    @Autowired
    private AppUserCompatibilityDao appUserCompatibilityDao;

    private Long resolveSessionUserId(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return null;
        }
        Object userId = request.getSession().getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(userId));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isCurrentYonghuSession(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return false;
        }
        Object tableName = request.getSession().getAttribute("tableName");
        return "yonghu".equals(tableName);
    }

    private void storeChangePasswordCaptcha(HttpServletRequest request, String captcha) {
        long expireAt = System.currentTimeMillis() + CHANGE_PASSWORD_CAPTCHA_EXPIRES_IN_SECONDS * 1000L;
        request.getSession().setAttribute(CHANGE_PASSWORD_CAPTCHA_KEY, captcha);
        request.getSession().setAttribute(CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY, expireAt);
        request.getSession().setAttribute(CHANGE_PASSWORD_CAPTCHA_PURPOSE_KEY, CHANGE_PASSWORD_CAPTCHA_PURPOSE);
    }

    private void clearChangePasswordCaptcha(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return;
        }
        request.getSession().removeAttribute(CHANGE_PASSWORD_CAPTCHA_KEY);
        request.getSession().removeAttribute(CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY);
        request.getSession().removeAttribute(CHANGE_PASSWORD_CAPTCHA_PURPOSE_KEY);
    }

    private Long readSessionLong(HttpServletRequest request, String key) {
        if (request == null || request.getSession() == null || StringUtils.isBlank(key)) {
            return null;
        }
        Object value = request.getSession().getAttribute(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private R validateAndConsumeChangePasswordCaptcha(HttpServletRequest request, String captcha) {
        if (StringUtils.isBlank(captcha)) {
            return R.error("验证码不能为空");
        }
        String sessionCaptcha = request == null || request.getSession() == null
            ? null
            : (String) request.getSession().getAttribute(CHANGE_PASSWORD_CAPTCHA_KEY);
        Long expireAt = readSessionLong(request, CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY);
        String purpose = request == null || request.getSession() == null
            ? null
            : (String) request.getSession().getAttribute(CHANGE_PASSWORD_CAPTCHA_PURPOSE_KEY);
        if (StringUtils.isBlank(sessionCaptcha)
            || expireAt == null
            || !CHANGE_PASSWORD_CAPTCHA_PURPOSE.equals(purpose)
            || expireAt.longValue() < System.currentTimeMillis()) {
            clearChangePasswordCaptcha(request);
            return R.error("验证码已过期，请刷新后重试");
        }
        if (!StringUtils.equals(sessionCaptcha, captcha.trim())) {
            return R.error("验证码错误");
        }
        clearChangePasswordCaptcha(request);
        return null;
    }

    private String resolveCompatSortColumn(Map<String, Object> params) {
        Object sort = params.get("sort");
        String sortValue = sort == null ? "" : String.valueOf(sort);
        if ("yonghuzhanghao".equals(sortValue) || "yonghuming".equals(sortValue)) {
            return "yonghuzhanghao";
        }
        if ("yonghuxingming".equals(sortValue) || "xingming".equals(sortValue)) {
            return "yonghuxingming";
        }
        if ("mima".equals(sortValue)) {
            return "mima";
        }
        if ("xingbie".equals(sortValue)) {
            return "xingbie";
        }
        if ("lianxidianhua".equals(sortValue) || "shoujihao".equals(sortValue)) {
            return "lianxidianhua";
        }
        if ("shenfenzheng".equals(sortValue)) {
            return "shenfenzheng";
        }
        if ("addtime".equals(sortValue)) {
            return "addtime";
        }
        return "id";
    }

    private String resolveCompatSortOrder(Map<String, Object> params) {
        Object order = params.get("order");
        return order != null && "asc".equalsIgnoreCase(String.valueOf(order)) ? "ASC" : "DESC";
    }

    private YonghuEntity findByLoginName(String loginName) {
        if (appUserCompatibilityDao != null) {
            return appUserCompatibilityDao.selectByLoginName(loginName);
        }
        return yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("yonghuzhanghao", loginName));
    }

    private YonghuEntity findByLegacyId(Long legacyId) {
        if (appUserCompatibilityDao != null) {
            return appUserCompatibilityDao.selectByLegacyId(legacyId);
        }
        return yonghuService.selectById(legacyId);
    }

    private int countByLoginName(String loginName, Long excludeLegacyId) {
        if (appUserCompatibilityDao != null) {
            return appUserCompatibilityDao.countByLoginName(loginName, excludeLegacyId);
        }
        EntityWrapper<YonghuEntity> wrapper = new EntityWrapper<YonghuEntity>();
        wrapper.eq("yonghuzhanghao", loginName);
        if (excludeLegacyId != null) {
            wrapper.ne("id", excludeLegacyId);
        }
        return yonghuService.selectCount(wrapper);
    }

    private void insertCompat(YonghuEntity yonghu) {
        if (appUserCompatibilityDao != null) {
            appUserCompatibilityDao.insertFromCompat(yonghu);
            return;
        }
        yonghuService.insert(yonghu);
    }

    private void updateCompat(YonghuEntity yonghu) {
        if (appUserCompatibilityDao != null) {
            appUserCompatibilityDao.updateByLegacyId(yonghu);
            return;
        }
        yonghuService.updateById(yonghu);
    }

    private void deleteCompatBatch(List<Long> legacyIds) {
        if (appUserCompatibilityDao != null) {
            appUserCompatibilityDao.deleteBatchByLegacyIds(legacyIds);
            return;
        }
        yonghuService.deleteBatchIds(legacyIds);
    }

    private List<YonghuEntity> listCompat(YonghuEntity yonghu) {
        if (appUserCompatibilityDao != null) {
            return appUserCompatibilityDao.selectCompatList(yonghu, "id", "DESC");
        }
        EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
        ew.allEq(MPUtil.allEQMapPre(yonghu, "yonghu"));
        return yonghuService.selectList(ew);
    }

    private PageUtils buildCompatPage(Map<String, Object> params, YonghuEntity yonghu) {
        if (appUserCompatibilityDao == null) {
            EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
            return yonghuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, yonghu), params), params));
        }
        Query query = new Query(params);
        long offset = ((Number)query.get("offset")).longValue();
        long total = appUserCompatibilityDao.countCompat(yonghu);
        List<YonghuEntity> records = appUserCompatibilityDao.selectCompatPage(
            yonghu,
            offset,
            query.getLimit(),
            resolveCompatSortColumn(params),
            resolveCompatSortOrder(params)
        );
        return new PageUtils(records, (int) total, query.getLimit(), query.getCurrPage());
    }

    /**
     * 兼容历史库字段：旧库使用 yonghuming/xingming/shoujihao，
     * 当前前台使用 yonghuzhanghao/yonghuxingming/lianxidianhua。
     */
    private void normalizeLegacyFields(YonghuEntity yonghu) {
        if (yonghu == null) {
            return;
        }
        if (StringUtils.isBlank(yonghu.getYonghuzhanghao()) && StringUtils.isNotBlank(yonghu.getYonghuming())) {
            yonghu.setYonghuzhanghao(yonghu.getYonghuming());
        }
        if (StringUtils.isBlank(yonghu.getYonghuming()) && StringUtils.isNotBlank(yonghu.getYonghuzhanghao())) {
            yonghu.setYonghuming(yonghu.getYonghuzhanghao());
        }
        if (StringUtils.isBlank(yonghu.getYonghuxingming()) && StringUtils.isNotBlank(yonghu.getXingming())) {
            yonghu.setYonghuxingming(yonghu.getXingming());
        }
        if (StringUtils.isBlank(yonghu.getXingming()) && StringUtils.isNotBlank(yonghu.getYonghuxingming())) {
            yonghu.setXingming(yonghu.getYonghuxingming());
        }
        if (StringUtils.isBlank(yonghu.getLianxidianhua()) && StringUtils.isNotBlank(yonghu.getShoujihao())) {
            yonghu.setLianxidianhua(yonghu.getShoujihao());
        }
        if (StringUtils.isBlank(yonghu.getShoujihao()) && StringUtils.isNotBlank(yonghu.getLianxidianhua())) {
            yonghu.setShoujihao(yonghu.getLianxidianhua());
        }
    }

    private void normalizePasswordForPersistence(YonghuEntity yonghu) {
        if (yonghu == null) {
            return;
        }
        yonghu.setMima(EncryptUtil.encryptPasswordForStorage(yonghu.getMima()));
    }
	
	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		// 根据登录查询用户信息
        YonghuEntity u = findByLoginName(username);
        // 当用户不存在或验证密码不通过时
		if(u==null || !EncryptUtil.passwordMatches(password, u.getMima())) {
            //账号或密码不正确提示
			return R.error("账号或密码不正确");
		}
		
        normalizeLegacyFields(u);
        appUserSessionDao.upsertFromYonghu(u);
        // 获取登录token
		String token;
		try {
			token = tokenService.generateToken(u.getId(), username,"yonghu",  "用户" );
		} catch (IllegalStateException exception) {
			return R.error(exception.getMessage());
		}
        //返回token
		return R.ok().put("token", token);
	}


	
	/**
     * 注册
     */
	@IgnoreAuth
    @RequestMapping("/register")
    @Transactional
    public R register(@RequestBody YonghuEntity yonghu){
    	//ValidatorUtils.validateEntity(yonghu);
        normalizeLegacyFields(yonghu);
		//根据登录账号获取用户信息判断是否存在该用户，否则返回错误信息
		YonghuEntity u = findByLoginName(yonghu.getYonghuzhanghao());
		if(u!=null) {
			return R.error("注册用户已存在");
		}
        //判断是否存在相同用户账号，否则返回错误信息
        if(countByLoginName(yonghu.getYonghuzhanghao(), null)>0) {
            return R.error("用户账号已存在");
        }
		Long uId = new Date().getTime();
		yonghu.setId(uId);
        normalizePasswordForPersistence(yonghu);
        //保存用户
        insertCompat(yonghu);
        appUserSessionDao.upsertFromYonghu(yonghu);
        return R.ok();
    }

	
	/**
	 * 退出
	 */
	@RequestMapping("/logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}
	
	/**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
    	Long id = (Long)request.getSession().getAttribute("userId");
        YonghuEntity u = findByLegacyId(id);
        if (u != null) {
            u.setMima(null);
        }
        return R.ok().put("data", u);
    }
    
    /**
     * 密码重置
     */
    @IgnoreAuth
	@RequestMapping(value = "/resetPass")
    @Transactional
    public R resetPass(String username, HttpServletRequest request){
        return R.error("公开密码重置已关闭，请联系管理员处理");
    }

    @GetMapping("/changePasswordCaptcha")
    public R changePasswordCaptcha(HttpServletRequest request) {
        Long userId = resolveSessionUserId(request);
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        if (!isCurrentYonghuSession(request)) {
            return R.error("当前会话不允许获取该账户验证码");
        }
        String captcha = CommonUtil.getRandomNumber(4);
        storeChangePasswordCaptcha(request, captcha);
        return R.ok().put("data", new HashMap<String, Object>() {{
            put("captcha", captcha);
            put("expiresInSeconds", CHANGE_PASSWORD_CAPTCHA_EXPIRES_IN_SECONDS);
            put("expireAt", readSessionLong(request, CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY));
        }});
    }

    /**
     * 当前登录用户修改密码
     */
    @PostMapping("/changePassword")
    @Transactional
    public R changePassword(@RequestBody PasswordChangeRequestVO requestBody, HttpServletRequest request) {
        Long userId = resolveSessionUserId(request);
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        if (!isCurrentYonghuSession(request)) {
            return R.error("当前会话不允许修改该账户密码");
        }
        if (requestBody == null || StringUtils.isBlank(requestBody.getOldPassword())
                || StringUtils.isBlank(requestBody.getNewPassword())) {
            return R.error("原密码和新密码不能为空");
        }
        R captchaValidationResult = validateAndConsumeChangePasswordCaptcha(request, requestBody.getCaptcha());
        if (captchaValidationResult != null) {
            return captchaValidationResult;
        }
        YonghuEntity persisted = findByLegacyId(userId);
        if (persisted == null) {
            return R.error("账号不存在");
        }
        normalizeLegacyFields(persisted);
        if (!EncryptUtil.passwordMatches(requestBody.getOldPassword(), persisted.getMima())) {
            return R.error("原密码错误");
        }
        persisted.setMima(EncryptUtil.encryptPasswordForStorage(requestBody.getNewPassword()));
        normalizeLegacyFields(persisted);
        updateCompat(persisted);
        appUserSessionDao.upsertFromYonghu(persisted);
        return R.ok("密码修改成功");
    }



    /**
     * 后台列表
     */
    @RequestMapping("/page")
	public R page(@RequestParam Map<String, Object> params,YonghuEntity yonghu,
		HttpServletRequest request){
		PageUtils page = buildCompatPage(params, yonghu);
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
	public R list(@RequestParam Map<String, Object> params,YonghuEntity yonghu,
		HttpServletRequest request){
		PageUtils page = buildCompatPage(params, yonghu);
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(page,deSens);
        return R.ok().put("data", page);
    }



	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( YonghuEntity yonghu){
        List<YonghuEntity> data = listCompat(yonghu);
        return R.ok().put("data", data);
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(YonghuEntity yonghu){
        List<YonghuEntity> data = listCompat(yonghu);
		return R.ok("查询用户成功").put("data", data.isEmpty() ? null : data.get(0));
    }
	
    /**
     * 后台详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        YonghuEntity yonghu = findByLegacyId(id);
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(yonghu,deSens);
        return R.ok().put("data", yonghu);
    }

    /**
     * 前台详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        YonghuEntity yonghu = findByLegacyId(id);
        Map<String, String> deSens = new HashMap<>();
        //给需要脱敏的字段脱敏
        DeSensUtil.desensitize(yonghu,deSens);
        return R.ok().put("data", yonghu);
    }
    



    /**
     * 后台保存
     */
    @RequestMapping("/save")
    @Transactional
    public R save(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        normalizeLegacyFields(yonghu);
        //验证字段唯一性，否则返回错误信息
        if(countByLoginName(yonghu.getYonghuzhanghao(), null)>0) {
            return R.error("用户账号已存在");
        }
        //ValidatorUtils.validateEntity(yonghu);
        //验证账号唯一性，否则返回错误信息
        YonghuEntity u = findByLoginName(yonghu.getYonghuzhanghao());
        if(u!=null) {
            return R.error("用户已存在");
        }
        yonghu.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
		yonghu.setId(new Date().getTime());
        normalizePasswordForPersistence(yonghu);
        insertCompat(yonghu);
        appUserSessionDao.upsertFromYonghu(yonghu);
        return R.ok().put("data",yonghu.getId());
    }
    
    /**
     * 前台保存
     */
    @RequestMapping("/add")
    @Transactional
    public R add(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        normalizeLegacyFields(yonghu);
        //验证字段唯一性，否则返回错误信息
        if(countByLoginName(yonghu.getYonghuzhanghao(), null)>0) {
            return R.error("用户账号已存在");
        }
        //ValidatorUtils.validateEntity(yonghu);
        //验证账号唯一性，否则返回错误信息
        YonghuEntity u = findByLoginName(yonghu.getYonghuzhanghao());
        if(u!=null) {
            return R.error("用户已存在");
        }
        yonghu.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
		yonghu.setId(new Date().getTime());
        normalizePasswordForPersistence(yonghu);
        insertCompat(yonghu);
        appUserSessionDao.upsertFromYonghu(yonghu);
        return R.ok().put("data",yonghu.getId());
    }





    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        //ValidatorUtils.validateEntity(yonghu);
        normalizeLegacyFields(yonghu);
        //验证字段唯一性，否则返回错误信息
        if(countByLoginName(yonghu.getYonghuzhanghao(), yonghu.getId())>0) {
            return R.error("用户账号已存在");
        }
        YonghuEntity persisted = findByLegacyId(yonghu.getId());
        if (persisted != null) {
            // 密码必须通过专用改密接口修改，update 只允许改资料字段。
            yonghu.setMima(persisted.getMima());
        }
        //全部更新
        updateCompat(yonghu);
        persisted = findByLegacyId(yonghu.getId());
        if (persisted != null) {
            normalizeLegacyFields(persisted);
            appUserSessionDao.upsertFromYonghu(persisted);
            // 同步会话快照，保持拦截器写入 session 的 username 与最新账号一致。
            tokenService.updateSubjectLoginName(persisted.getId(), "yonghu", persisted.getYonghuzhanghao());
        }
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        deleteCompatBatch(Arrays.asList(ids));
        return R.ok();
    }
    








}
