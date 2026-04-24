package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.entity.YonghuEntity;
import com.service.YonghuService;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class YonghuControllerRegisterCompatibilityTest {

    @Mock
    private YonghuService yonghuService;

    @Test
    void registerShouldBackfillLegacyFieldsFromCurrentFormValues() {
        YonghuController controller = new YonghuController();
        ReflectionTestUtils.setField(controller, "yonghuService", yonghuService);

        when(yonghuService.selectOne(any())).thenReturn(null);
        when(yonghuService.selectCount(any())).thenReturn(0);
        when(yonghuService.insert(any(YonghuEntity.class))).thenReturn(true);

        YonghuEntity entity = new YonghuEntity();
        entity.setYonghuzhanghao("user_001");
        entity.setMima("123456");
        entity.setYonghuxingming("测试用户");
        entity.setLianxidianhua("13800000000");

        R result = controller.register(entity);

        ArgumentCaptor<YonghuEntity> captor = ArgumentCaptor.forClass(YonghuEntity.class);
        verify(yonghuService).insert(captor.capture());
        Object legacyUsername = ReflectionTestUtils.getField(captor.getValue(), "yonghuming");
        Object legacyName = ReflectionTestUtils.getField(captor.getValue(), "xingming");
        Object legacyMobile = ReflectionTestUtils.getField(captor.getValue(), "shoujihao");

        assertEquals(0, result.get("code"));
        assertEquals("user_001", legacyUsername);
        assertEquals("测试用户", legacyName);
        assertEquals("13800000000", legacyMobile);
    }
}
