package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.SensitivewordsEntity;
import com.service.SensitivewordsService;
import com.utils.DeSensUtil;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;

/**
 * 敏感词后台管理接口。
 */
@RestController
@RequestMapping("/sensitivewords")
public class SensitivewordsController {

    private static final String FRONT_ENDPOINT_DISABLED_MESSAGE = "前台敏感词 CRUD/详情接口已停用，请改用 /keywords/list";

    @Autowired
    private SensitivewordsService sensitivewordsService;

    /**
     * 后台列表。
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,
                  SensitivewordsEntity sensitivewords,
                  HttpServletRequest request) {
        EntityWrapper<SensitivewordsEntity> wrapper = new EntityWrapper<SensitivewordsEntity>();
        PageUtils page = sensitivewordsService.queryPage(
                params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(wrapper, sensitivewords), params), params)
        );
        Map<String, String> deSens = new HashMap<String, String>();
        DeSensUtil.desensitize(page, deSens);
        return R.ok().put("data", page);
    }

    /**
     * 后台详情。
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SensitivewordsEntity sensitivewords = sensitivewordsService.selectById(id);
        Map<String, String> deSens = new HashMap<String, String>();
        DeSensUtil.desensitize(sensitivewords, deSens);
        return R.ok().put("data", sensitivewords);
    }

    /**
     * 后台保存。
     */
    @RequestMapping("/save")
    public R save(@RequestBody SensitivewordsEntity sensitivewords, HttpServletRequest request) {
        String validationMessage = validateContent(sensitivewords);
        if (validationMessage != null) {
            return R.error(validationMessage);
        }
        if (sensitivewords.getId() == null) {
            sensitivewords.setId(System.currentTimeMillis() + (long) Math.floor(Math.random() * 1000));
        }
        sensitivewordsService.insert(sensitivewords);
        return R.ok().put("data", sensitivewords.getId());
    }

    /**
     * 后台修改。
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody SensitivewordsEntity sensitivewords, HttpServletRequest request) {
        if (sensitivewords == null || sensitivewords.getId() == null) {
            return R.error("缺少敏感词ID");
        }
        String validationMessage = validateContent(sensitivewords);
        if (validationMessage != null) {
            return R.error(validationMessage);
        }
        sensitivewordsService.updateById(sensitivewords);
        return R.ok();
    }

    /**
     * 后台删除。
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        sensitivewordsService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 禁用前台模板残留接口。
     */
    @IgnoreAuth
    @RequestMapping({"/list", "/lists", "/query", "/detail/{id}", "/add", "/security", "/autoSort"})
    public R disabledFrontEndpoint() {
        return R.error(410, FRONT_ENDPOINT_DISABLED_MESSAGE);
    }

    private String validateContent(SensitivewordsEntity sensitivewords) {
        if (sensitivewords == null) {
            return "敏感词内容不能为空";
        }
        sensitivewords.setContent(StringUtils.trimToEmpty(sensitivewords.getContent()));
        return StringUtils.isBlank(sensitivewords.getContent()) ? "敏感词内容不能为空" : null;
    }
}
