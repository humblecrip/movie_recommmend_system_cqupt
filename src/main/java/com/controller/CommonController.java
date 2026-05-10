package com.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annotation.IgnoreAuth;
import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.util.Base64Util;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.ConfigEntity;
import com.entity.DianyingleixingEntity;
import com.service.AppMovieCompatibilityService;
import com.service.CommonService;
import com.service.ConfigService;
import com.utils.BaiduUtil;
import com.utils.FileUtil;
import com.utils.MapUtils;
import com.utils.R;
import com.utils.CommonUtil;
/**
 * 通用接口
 */
@RestController
public class CommonController{
	private static final Set<String> LEGACY_DYNAMIC_TABLES = new HashSet<String>(Arrays.asList(
		"dianyingxinxi",
		"dianyingleixing",
		"dianyingdingdan",
		"dianyingfenlei",
		"discussfufeidianying",
		"storeup",
		"discussdianyingxinxi",
		"discussmianfeidianying",
		"forum",
		"fufeidianying",
		"mianfeidianying",
		"news",
		"wodedianying",
		"yonghu",
		"users",
		"config",
		"schema_refactor_migration_log",
		"sensitivewords",
		"token"
	));
	private static final Set<String> ALLOWED_TIME_STAT_TYPES = new HashSet<String>(Arrays.asList(
		"日",
		"月",
		"年"
	));
	private static final Pattern STRICT_DYNAMIC_IDENTIFIER = Pattern.compile("^[a-z][a-z0-9_]*$");

	@Autowired
	private CommonService commonService;

	@Autowired
	private AppMovieCompatibilityService appMovieCompatibilityService;

    private static AipFace client = null;
    
    @Autowired
    private ConfigService configService;


	/**
	 * 获取table表中的column列表(联动接口)
	 * @param table
	 * @param column
	 * @return
	 */
	@IgnoreAuth
	@RequestMapping("/option/{tableName}/{columnName}")
	public R getOption(@PathVariable("tableName") String tableName, @PathVariable("columnName") String columnName,@RequestParam(required = false) String conditionColumn,@RequestParam(required = false) String conditionValue,String level,String parent) {
		if(isLegacyMovieTypeOption(tableName, columnName)) {
			return R.ok().put("data", appMovieCompatibilityService.listLegacyTypeNames());
		}
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedTableName = resolveRuntimeTableName(tableName);
		String validatedColumnName = requireSafeDynamicIdentifier("columnName", columnName);
		if(validatedColumnName == null) {
			return rejectUnsafeDynamicIdentifier("columnName", columnName);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", validatedTableName);
		params.put("validatedColumnName", validatedColumnName);
		if(StringUtils.isNotBlank(level)) {
			params.put("level", level);
		}
		if(StringUtils.isNotBlank(parent)) {
			params.put("parent", parent);
		}
        if(StringUtils.isNotBlank(conditionColumn)) {
			String validatedConditionColumnName = requireSafeDynamicIdentifier("conditionColumn", conditionColumn);
			if(validatedConditionColumnName == null) {
				return rejectUnsafeDynamicIdentifier("conditionColumn", conditionColumn);
			}
            params.put("validatedConditionColumnName", validatedConditionColumnName);
        }
        if(StringUtils.isNotBlank(conditionValue)) {
            params.put("conditionValue", conditionValue);
        }
		List<String> data = commonService.getOption(params);
		return R.ok().put("data", data);
	}
	
	/**
	 * 根据table中的column获取单条记录
	 * @param table
	 * @param column
	 * @return
	 */
	@IgnoreAuth
	@RequestMapping("/follow/{tableName}/{columnName}")
	public R getFollowByOption(@PathVariable("tableName") String tableName, @PathVariable("columnName") String columnName, @RequestParam String columnValue) {
		if(isLegacyMovieTypeOption(tableName, columnName)) {
			DianyingleixingEntity<Object> filter = new DianyingleixingEntity<Object>();
			filter.setDianyingleixing(columnValue);
			return R.ok().put("data", appMovieCompatibilityService.selectLegacyMovieType(filter));
		}
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedTableName = resolveRuntimeTableName(tableName);
		String validatedColumnName = requireSafeDynamicIdentifier("columnName", columnName);
		if(validatedColumnName == null) {
			return rejectUnsafeDynamicIdentifier("columnName", columnName);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", validatedTableName);
		params.put("validatedColumnName", validatedColumnName);
		params.put("columnValue", columnValue);
		Map<String, Object> result = commonService.getFollowByOption(params);
        Object o = null;
        try {
            Class<?> c1 = Class.forName("com.entity."+validatedTableName.substring(0, 1).toUpperCase()+validatedTableName.substring(1)+"Entity");
            o = MapUtils.mapToObject(result, c1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return R.ok().put("data", o);
	}

	private boolean isLegacyMovieTypeOption(String tableName, String columnName) {
		return "dianyingleixing".equals(tableName)
			&& "dianyingleixing".equals(columnName);
	}

	private String resolveRuntimeTableName(String tableName) {
		return normalizeDynamicIdentifier(tableName);
	}

	private R rejectLegacyOrUnsafeDynamicTable(String tableName) {
		String normalizedTableName = normalizeDynamicIdentifier(tableName);
		if(!LEGACY_DYNAMIC_TABLES.contains(normalizedTableName)) {
			if(isStrictDynamicIdentifier(tableName)) {
				return null;
			}
			return rejectUnsafeDynamicIdentifier("tableName", tableName);
		}
		return R.error("旧表动态入口已封禁: " + normalizedTableName + "，请改用兼容控制器或新接口");
	}

	private String normalizeDynamicIdentifier(String identifier) {
		return StringUtils.trimToEmpty(identifier).toLowerCase(Locale.ROOT);
	}

	private boolean isStrictDynamicIdentifier(String identifier) {
		if(StringUtils.isBlank(identifier)) {
			return false;
		}
		String trimmedIdentifier = StringUtils.trim(identifier);
		if(!trimmedIdentifier.equals(identifier)) {
			return false;
		}
		if(!trimmedIdentifier.equals(trimmedIdentifier.toLowerCase(Locale.ROOT))) {
			return false;
		}
		return STRICT_DYNAMIC_IDENTIFIER.matcher(trimmedIdentifier).matches();
	}

	private String requireSafeDynamicIdentifier(String identifierLabel, String identifier) {
		if(!isStrictDynamicIdentifier(identifier)) {
			return null;
		}
		return identifier;
	}

	private R rejectUnsafeDynamicIdentifier(String identifierLabel, String identifier) {
		return R.error("动态入口仅允许非 legacy 且符合小写下划线格式的标识: "
			+ identifierLabel + "=" + StringUtils.trimToEmpty(identifier));
	}
	
	/**
	 * 修改table表的sfsh状态
	 * @param table
	 * @param map
	 * @return
	 */
	@RequestMapping("/sh/{tableName}")
	public R sh(@PathVariable("tableName") String tableName, @RequestBody Map<String, Object> map) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		map.put("validatedTableName", resolveRuntimeTableName(tableName));
		commonService.sh(map);
		return R.ok();
	}
	
	/**
	 * 获取需要提醒的记录数
	 * @param tableName
	 * @param columnName
	 * @param type 1:数字 2:日期
	 * @param map
	 * @return
	 */
	@IgnoreAuth
	@RequestMapping("/remind/{tableName}/{columnName}/{type}")
	public R remindCount(@PathVariable("tableName") String tableName, @PathVariable("columnName") String columnName, 
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedColumnName = requireSafeDynamicIdentifier("columnName", columnName);
		if(validatedColumnName == null) {
			return rejectUnsafeDynamicIdentifier("columnName", columnName);
		}
		map.put("validatedTableName", resolveRuntimeTableName(tableName));
		map.put("validatedColumnName", validatedColumnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		int count = commonService.remindCount(map);
		return R.ok().put("count", count);
	}
	
	/**
	 * 单列求和
	 */
	@IgnoreAuth
	@RequestMapping("/cal/{tableName}/{columnName}")
	public R cal(@PathVariable("tableName") String tableName, @PathVariable("columnName") String columnName) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedColumnName = requireSafeDynamicIdentifier("columnName", columnName);
		if(validatedColumnName == null) {
			return rejectUnsafeDynamicIdentifier("columnName", columnName);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", resolveRuntimeTableName(tableName));
		params.put("validatedColumnName", validatedColumnName);
		Map<String, Object> result = commonService.selectCal(params);
		return R.ok().put("data", result);
	}
	
	/**
	 * 分组统计
	 */
	@IgnoreAuth
	@RequestMapping("/group/{tableName}/{columnName}")
	public R group(@PathVariable("tableName") String tableName, @PathVariable("columnName") String columnName) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedColumnName = requireSafeDynamicIdentifier("columnName", columnName);
		if(validatedColumnName == null) {
			return rejectUnsafeDynamicIdentifier("columnName", columnName);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", resolveRuntimeTableName(tableName));
		params.put("validatedColumnName", validatedColumnName);
		List<Map<String, Object>> result = commonService.selectGroup(params);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(Map<String, Object> m : result) {
			for(String k : m.keySet()) {
				if(m.get(k) instanceof Date) {
					m.put(k, sdf.format((Date)m.get(k)));
				}
			}
		}
		return R.ok().put("data", result);
	}
	
	/**
	 * （按值统计）
	 */
	@IgnoreAuth
	@RequestMapping("/value/{tableName}/{xColumnName}/{yColumnName}")
	public R value(@PathVariable("tableName") String tableName, @PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedXColumnName = requireSafeDynamicIdentifier("xColumnName", xColumnName);
		if(validatedXColumnName == null) {
			return rejectUnsafeDynamicIdentifier("xColumnName", xColumnName);
		}
		String validatedYColumnName = requireSafeDynamicIdentifier("yColumnName", yColumnName);
		if(validatedYColumnName == null) {
			return rejectUnsafeDynamicIdentifier("yColumnName", yColumnName);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", resolveRuntimeTableName(tableName));
		params.put("validatedXColumnName", validatedXColumnName);
		params.put("validatedYColumnName", validatedYColumnName);
		List<Map<String, Object>> result = commonService.selectValue(params);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(Map<String, Object> m : result) {
			for(String k : m.keySet()) {
				if(m.get(k) instanceof Date) {
					m.put(k, sdf.format((Date)m.get(k)));
				}
			}
		}
		return R.ok().put("data", result);
	}

	/**
 	 * （按值统计）时间统计类型
	 */
	@IgnoreAuth
	@RequestMapping("/value/{tableName}/{xColumnName}/{yColumnName}/{timeStatType}")
	public R valueDay(@PathVariable("tableName") String tableName, @PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType) {
		R blocked = rejectLegacyOrUnsafeDynamicTable(tableName);
		if(blocked != null) {
			return blocked;
		}
		String validatedXColumnName = requireSafeDynamicIdentifier("xColumnName", xColumnName);
		if(validatedXColumnName == null) {
			return rejectUnsafeDynamicIdentifier("xColumnName", xColumnName);
		}
		String validatedYColumnName = requireSafeDynamicIdentifier("yColumnName", yColumnName);
		if(validatedYColumnName == null) {
			return rejectUnsafeDynamicIdentifier("yColumnName", yColumnName);
		}
		if(!ALLOWED_TIME_STAT_TYPES.contains(timeStatType)) {
			return R.error("动态时间统计类型不在允许范围: " + StringUtils.trimToEmpty(timeStatType));
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("validatedTableName", resolveRuntimeTableName(tableName));
		params.put("validatedXColumnName", validatedXColumnName);
		params.put("validatedYColumnName", validatedYColumnName);
		params.put("timeStatType", timeStatType);
		List<Map<String, Object>> result = commonService.selectTimeStatValue(params);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(Map<String, Object> m : result) {
			for(String k : m.keySet()) {
				if(m.get(k) instanceof Date) {
					m.put(k, sdf.format((Date)m.get(k)));
				}
			}
		}
		return R.ok().put("data", result);
	}
	




}
