package com.controller;

import com.entity.StoreupEntity;
import com.entity.view.StoreupView;
import com.service.StoreupCompatibilityService;
import com.utils.PageUtils;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreupControllerCompatibilityTest {

    @Mock
    private StoreupCompatibilityService storeupCompatibilityService;

    @Test
    void pageShouldUseCompatibilityServiceInsteadOfLegacyStoreupTable() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("role", "用户");
        request.getSession().setAttribute("userId", 12L);
        Map<String, Object> params = new HashMap<String, Object>();
        StoreupEntity filter = new StoreupEntity();
        PageUtils page = new PageUtils(Collections.singletonList(new StoreupView()), 1, 10, 1);

        when(storeupCompatibilityService.queryPage(params, filter, 12L, false)).thenReturn(page);

        R result = controller.page(params, filter, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(storeupCompatibilityService).queryPage(params, filter, 12L, false);
    }

    @Test
    void infoShouldUseCompatibilityServiceInsteadOfLegacyStoreupTable() {
        StoreupController controller = newController();
        StoreupView view = new StoreupView();
        view.setId(33L);

        when(storeupCompatibilityService.selectById(33L)).thenReturn(view);

        R result = controller.info(33L);

        assertEquals(0, result.get("code"));
        assertEquals(view, result.get("data"));
        verify(storeupCompatibilityService).selectById(33L);
    }

    @Test
    void listShouldUseCompatibilityServiceInsteadOfLegacyStoreupTable() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        Map<String, Object> params = new HashMap<String, Object>();
        StoreupEntity filter = new StoreupEntity();
        PageUtils page = new PageUtils(Collections.singletonList(new StoreupView()), 1, 10, 1);

        when(storeupCompatibilityService.queryPage(params, filter, 12L, false)).thenReturn(page);

        R result = controller.list(params, filter, request);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(storeupCompatibilityService).queryPage(params, filter, 12L, false);
    }

    @Test
    void detailShouldUseCompatibilityServiceInsteadOfLegacyStoreupTable() {
        StoreupController controller = newController();
        StoreupView view = new StoreupView();
        view.setId(44L);

        when(storeupCompatibilityService.selectById(44L)).thenReturn(view);

        R result = controller.detail(44L);

        assertEquals(0, result.get("code"));
        assertEquals(view, result.get("data"));
        verify(storeupCompatibilityService).selectById(44L);
    }

    @Test
    void saveShouldUseCompatibilityServiceInsteadOfLegacyInsert() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        StoreupEntity body = new StoreupEntity();

        when(storeupCompatibilityService.save(body, 12L)).thenReturn(88L);

        R result = controller.save(body, request);

        assertEquals(0, result.get("code"));
        assertEquals(88L, result.get("data"));
        verify(storeupCompatibilityService).save(body, 12L);
    }

    @Test
    void addShouldUseCompatibilityServiceInsteadOfLegacyInsert() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        StoreupEntity body = new StoreupEntity();

        when(storeupCompatibilityService.save(body, 12L)).thenReturn(66L);

        R result = controller.add(body, request);

        assertEquals(0, result.get("code"));
        assertEquals(66L, result.get("data"));
        verify(storeupCompatibilityService).save(body, 12L);
    }

    @Test
    void updateShouldReturnExplicitCompatibilityFailure() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        StoreupEntity body = new StoreupEntity();
        body.setId(18L);

        when(storeupCompatibilityService.update(body, 12L, false)).thenReturn(false);

        R result = controller.update(body, request);

        assertEquals(500, result.get("code"));
        assertEquals("收藏更新失败：仅支持已映射到 app_movie 的电影收藏记录", result.get("msg"));
        verify(storeupCompatibilityService).update(body, 12L, false);
    }

    @Test
    void deleteShouldUseCompatibilityServiceInsteadOfLegacyDeleteBatch() {
        StoreupController controller = newController();
        Long[] ids = new Long[]{3L, 4L};

        when(storeupCompatibilityService.deleteBatch(Arrays.asList(ids))).thenReturn(2);

        R result = controller.delete(ids);

        assertEquals(0, result.get("code"));
        verify(storeupCompatibilityService).deleteBatch(Arrays.asList(ids));
    }

    @Test
    void autoSortShouldUseCompatibilityServiceInsteadOfLegacyStoreupTable() {
        StoreupController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 12L);
        Map<String, Object> params = new HashMap<String, Object>();
        StoreupEntity filter = new StoreupEntity();
        PageUtils page = new PageUtils(Collections.singletonList(new StoreupView()), 1, 10, 1);

        when(storeupCompatibilityService.queryPage(anyMap(), any(StoreupEntity.class), anyLong(), anyBoolean()))
                .thenReturn(page);

        R result = controller.autoSort(params, filter, request, null);

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        assertEquals("addtime", params.get("sort"));
        assertEquals("desc", params.get("order"));
        verify(storeupCompatibilityService).queryPage(params, filter, 12L, false);
    }

    private StoreupController newController() {
        StoreupController controller = new StoreupController();
        ReflectionTestUtils.setField(controller, "storeupCompatibilityService", storeupCompatibilityService);
        return controller;
    }
}
