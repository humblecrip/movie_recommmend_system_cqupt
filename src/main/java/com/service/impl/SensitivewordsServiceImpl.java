package com.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.utils.PageUtils;
import com.utils.Query;


import com.dao.SensitivewordsDao;
import com.entity.SensitivewordsEntity;
import com.service.SensitivewordsService;
import com.entity.vo.SensitivewordsVO;
import com.entity.view.SensitivewordsView;
import com.utils.SensitiveWordUtils;

import java.util.ArrayList;

@Service("sensitivewordsService")
public class SensitivewordsServiceImpl extends ServiceImpl<SensitivewordsDao, SensitivewordsEntity> implements SensitivewordsService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<SensitivewordsEntity> page = this.selectPage(
                new Query<SensitivewordsEntity>(params).getPage(),
                new EntityWrapper<SensitivewordsEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<SensitivewordsEntity> wrapper) {
		  Page<SensitivewordsView> page =new Query<SensitivewordsView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<SensitivewordsVO> selectListVO(Wrapper<SensitivewordsEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public SensitivewordsVO selectVO(Wrapper<SensitivewordsEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<SensitivewordsView> selectListView(Wrapper<SensitivewordsEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public SensitivewordsView selectView(Wrapper<SensitivewordsEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

	@Override
	public List<String> listNormalizedKeywords() {
		List<SensitivewordsEntity> records = this.selectList(new EntityWrapper<SensitivewordsEntity>().orderBy("created_at", false));
		List<String> rawTexts = new ArrayList<String>();
		for (SensitivewordsEntity record : records) {
			if (record != null) {
				rawTexts.add(record.getContent());
			}
		}
		return SensitiveWordUtils.normalizeKeywords(rawTexts);
	}


}
