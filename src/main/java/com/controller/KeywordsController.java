package com.controller;

import com.annotation.IgnoreAuth;
import com.service.SensitivewordsService;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台只读关键词接口。
 */
@RestController
@RequestMapping("/keywords")
public class KeywordsController {

    @Autowired
    private SensitivewordsService sensitivewordsService;

    @IgnoreAuth
    @RequestMapping("/list")
    public R list() {
        return R.ok().put("data", sensitivewordsService.listNormalizedKeywords());
    }
}
