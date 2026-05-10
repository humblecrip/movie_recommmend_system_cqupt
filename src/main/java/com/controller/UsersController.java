
package com.controller;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.TokenEntity;
import com.entity.UsersEntity;
import com.entity.vo.PasswordChangeRequestVO;
import com.service.TokenService;
import com.service.UsersService;
import com.utils.CommonUtil;
import com.utils.EncryptUtil;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 登录相关
 */
@RequestMapping("users")
@RestController
public class UsersController{
	private static final String CHANGE_PASSWORD_CAPTCHA_KEY = "users:changePassword:captcha";
	private static final String CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY = "users:changePassword:expireAt";
	private static final String CHANGE_PASSWORD_CAPTCHA_PURPOSE_KEY = "users:changePassword:purpose";
	private static final String CHANGE_PASSWORD_CAPTCHA_PURPOSE = "changePassword";
	private static final int CHANGE_PASSWORD_CAPTCHA_EXPIRES_IN_SECONDS = 300;
	
	@Autowired
	private UsersService userService;
	
	@Autowired
	private TokenService tokenService;

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

    private boolean isCurrentUsersSession(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return false;
        }
        Object tableName = request.getSession().getAttribute("tableName");
        return "users".equals(tableName);
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

	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		UsersEntity user = userService.selectOne(new EntityWrapper<UsersEntity>().eq("username", username));
		if(user==null || !EncryptUtil.passwordMatches(password, user.getPassword())) {
			return R.error("账号或密码不正确");
		}
		String token = tokenService.generateToken(user.getId(),username, "users", user.getRole());
		return R.ok().put("token", token);
	}
	
	/**
	 * 注册
	 */
	@IgnoreAuth
	@PostMapping(value = "/register")
	public R register(@RequestBody UsersEntity user){
//    	ValidatorUtils.validateEntity(user);
    	if(userService.selectOne(new EntityWrapper<UsersEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
        if(user.getId() == null) {
            user.setId(new Date().getTime() + (long)Math.floor(Math.random() * 1000));
        }
        user.setPassword(EncryptUtil.encryptPasswordForStorage(user.getPassword()));
        userService.insert(user);
        return R.ok();
    }

	/**
	 * 退出
	 */
	@RequestMapping(value = "logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}
	
	/**
     * 密码重置
     */
    @IgnoreAuth
	@RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request){
        return R.error("公开密码重置已关闭，请联系管理员处理");
    }

    @GetMapping("/changePasswordCaptcha")
    public R changePasswordCaptcha(HttpServletRequest request) {
        Long userId = resolveSessionUserId(request);
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        if (!isCurrentUsersSession(request)) {
            return R.error("当前会话不允许获取该账户验证码");
        }
        String captcha = CommonUtil.getRandomNumber(4);
        storeChangePasswordCaptcha(request, captcha);
        return R.ok().put("data", new java.util.HashMap<String, Object>() {{
            put("captcha", captcha);
            put("expiresInSeconds", CHANGE_PASSWORD_CAPTCHA_EXPIRES_IN_SECONDS);
            put("expireAt", readSessionLong(request, CHANGE_PASSWORD_CAPTCHA_EXPIRE_AT_KEY));
        }});
    }

    /**
     * 当前登录用户修改密码
     */
    @PostMapping("/changePassword")
    public R changePassword(@RequestBody PasswordChangeRequestVO requestBody, HttpServletRequest request) {
        Long userId = resolveSessionUserId(request);
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        if (!isCurrentUsersSession(request)) {
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
        UsersEntity persisted = userService.selectById(userId);
        if (persisted == null) {
            return R.error("账号不存在");
        }
        if (!EncryptUtil.passwordMatches(requestBody.getOldPassword(), persisted.getPassword())) {
            return R.error("原密码错误");
        }
        persisted.setPassword(EncryptUtil.encryptPasswordForStorage(requestBody.getNewPassword()));
        userService.updateById(persisted);
        return R.ok("密码修改成功");
    }
	
	/**
     * 列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,UsersEntity user){
        EntityWrapper<UsersEntity> ew = new EntityWrapper<UsersEntity>();
    	PageUtils page = userService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.allLike(ew, user), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/list")
    public R list( UsersEntity user){
       	EntityWrapper<UsersEntity> ew = new EntityWrapper<UsersEntity>();
      	ew.allEq(MPUtil.allEQMapPre( user, "user")); 
        return R.ok().put("data", userService.selectListView(ew));
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") String id){
        UsersEntity user = userService.selectById(id);
        return R.ok().put("data", user);
    }
    
    /**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
    	Long id = (Long)request.getSession().getAttribute("userId");
        UsersEntity user = userService.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok().put("data", user);
    }

    /**
     * 保存
     */
	@PostMapping("/save")
    public R save(@RequestBody UsersEntity user){
//    	ValidatorUtils.validateEntity(user);
    	if(userService.selectOne(new EntityWrapper<UsersEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
        if(user.getId() == null) {
            user.setId(new Date().getTime() + (long)Math.floor(Math.random() * 1000));
        }
        user.setPassword(EncryptUtil.encryptPasswordForStorage(user.getPassword()));
        userService.insert(user);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody UsersEntity user){
//        ValidatorUtils.validateEntity(user);
    	UsersEntity u = userService.selectOne(new EntityWrapper<UsersEntity>().eq("username", user.getUsername()));
    	if(u!=null && u.getId()!=user.getId() && u.getUsername().equals(user.getUsername())) {
    		return R.error("用户名已存在。");
    	}
        UsersEntity persisted = user.getId() == null ? null : userService.selectById(user.getId());
        if (persisted != null) {
            // 密码必须通过专用改密接口修改，update 只允许改资料字段。
            user.setPassword(persisted.getPassword());
        }
        userService.updateById(user);//全部更新
        if(user.getUsername()!=null) {
            tokenService.updateSubjectLoginName(user.getId(), "users", user.getUsername());
        }
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        userService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
