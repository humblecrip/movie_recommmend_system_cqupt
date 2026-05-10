
package com.service.impl;


import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.AppAdminUserSessionDao;
import com.dao.AppAuthSessionDao;
import com.dao.AppUserSessionDao;
import com.dao.TokenDao;
import com.entity.AppAuthSessionEntity;
import com.entity.TokenEntity;
import com.service.TokenService;
import com.utils.CommonUtil;
import com.utils.PageUtils;
import com.utils.Query;


/**
 * token
 */
@Service("tokenService")
public class TokenServiceImpl extends ServiceImpl<TokenDao, TokenEntity> implements TokenService {

	@Autowired
	private AppAuthSessionDao appAuthSessionDao;

	@Autowired
	private AppUserSessionDao appUserSessionDao;

	@Autowired
	private AppAdminUserSessionDao appAdminUserSessionDao;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<TokenEntity> page = this.selectPage(
                new Query<TokenEntity>(params).getPage(),
                new EntityWrapper<TokenEntity>()
        );
        return new PageUtils(page);
	}

	@Override
	public List<TokenEntity> selectListView(Wrapper<TokenEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params,
			Wrapper<TokenEntity> wrapper) {
		 Page<TokenEntity> page =new Query<TokenEntity>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
	}

	@Override
	public String generateToken(Long userid,String username, String tableName, String role) {
		String token = CommonUtil.getRandomString(32);
		Calendar cal = Calendar.getInstance();   
		Date issuedAt = new Date();
        cal.setTime(issuedAt);
    	cal.add(Calendar.HOUR_OF_DAY, 1);
		Long appUserId = resolveAppUserId(userid, tableName);
		Long appAdminUserId = resolveAppAdminUserId(userid, tableName);
		if("yonghu".equals(tableName) && appUserId == null) {
			throw new IllegalStateException("未找到对应的 app_user 映射，无法创建前台用户会话");
		}
		if("users".equals(tableName) && appAdminUserId == null) {
			throw new IllegalStateException("未找到对应的 app_admin_user 映射，无法创建后台用户会话");
		}
		AppAuthSessionEntity session = new AppAuthSessionEntity();
		session.setAppUserId(appUserId);
		session.setAppAdminUserId(appAdminUserId);
		session.setLegacySubjectId(userid);
		session.setSubjectTableName(tableName);
		session.setSubjectRoleName(role);
		session.setSubjectKind(resolveSubjectKind(tableName));
		session.setSubjectLoginName(username);
		session.setSessionToken(token);
		session.setIssuedAt(issuedAt);
		session.setExpiresAt(cal.getTime());
		if(appAuthSessionDao.updateActiveSession(session) == 0) {
			appAuthSessionDao.insertSession(session);
		}
		return token;
	}

	@Override
	public TokenEntity getTokenEntity(String token) {
		return appAuthSessionDao.selectTokenEntityByToken(token);
	}

	@Override
	public void updateSubjectLoginName(Long userid, String tableName, String username) {
		if(userid == null || tableName == null || username == null) {
			return;
		}
		appAuthSessionDao.updateSubjectLoginName(userid, tableName, username);
	}

	private Long resolveAppUserId(Long userid, String tableName) {
		if(userid == null || !"yonghu".equals(tableName)) {
			return null;
		}
		return appUserSessionDao.selectIdByLegacyYonghuId(userid);
	}

	private Long resolveAppAdminUserId(Long userid, String tableName) {
		if(userid == null || !"users".equals(tableName)) {
			return null;
		}
		return appAdminUserSessionDao.selectIdByLegacyUsersId(userid);
	}

	private String resolveSubjectKind(String tableName) {
		if("yonghu".equals(tableName)) {
			return "front_user";
		}
		if("users".equals(tableName)) {
			return "admin_user";
		}
		return "legacy:" + (tableName == null ? "unknown" : tableName);
	}
}
