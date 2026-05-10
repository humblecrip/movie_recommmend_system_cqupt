
package com.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.ConfigEntity;

/**
 * 配置
 */
public interface ConfigDao extends BaseMapper<ConfigEntity> {

    List<ConfigEntity> selectListView(@Param("ew") Wrapper<ConfigEntity> wrapper);

    List<ConfigEntity> selectListView(Pagination page, @Param("ew") Wrapper<ConfigEntity> wrapper);
}
