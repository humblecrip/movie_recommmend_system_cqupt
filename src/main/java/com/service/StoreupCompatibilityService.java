package com.service;

import com.entity.StoreupEntity;
import com.entity.view.StoreupView;
import com.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 旧 storeup 接口到 app_user_movie_action 的兼容桥接服务。
 */
public interface StoreupCompatibilityService {

    PageUtils queryPage(Map<String, Object> params, StoreupEntity filter, Long sessionUserId, boolean adminView);

    List<StoreupView> selectList(StoreupEntity filter, Long sessionUserId, boolean adminView);

    StoreupView selectById(Long id);

    Long save(StoreupEntity storeup, Long sessionUserId);

    boolean update(StoreupEntity storeup, Long sessionUserId, boolean adminView);

    int deleteBatch(List<Long> ids);
}
