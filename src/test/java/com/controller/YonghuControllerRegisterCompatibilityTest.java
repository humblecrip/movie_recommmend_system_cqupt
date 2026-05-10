package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dao.AppUserSessionDao;
import com.entity.YonghuEntity;
import com.service.TokenService;
import com.service.YonghuService;
import com.utils.EncryptUtil;
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

    @Mock
    private AppUserSessionDao appUserSessionDao;

    @Mock
    private TokenService tokenService;

    @Test
    void registerShouldBackfillLegacyFieldsFromCurrentFormValues() {
        YonghuController controller = new YonghuController();
        ReflectionTestUtils.setField(controller, "yonghuService", yonghuService);
        ReflectionTestUtils.setField(controller, "appUserSessionDao", appUserSessionDao);

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
        assertEquals(EncryptUtil.aesEncrypt("123456"), captor.getValue().getMima());
        verify(appUserSessionDao).upsertFromYonghu(captor.getValue());
    }

    @Test
    void loginShouldReturnExplicitErrorWhenAppUserSessionBridgeIsMissing() {
        YonghuController controller = new YonghuController();
        ReflectionTestUtils.setField(controller, "yonghuService", yonghuService);
        ReflectionTestUtils.setField(controller, "appUserSessionDao", appUserSessionDao);
        ReflectionTestUtils.setField(controller, "tokenService", tokenService);

        YonghuEntity entity = new YonghuEntity();
        entity.setId(1001L);
        entity.setYonghuzhanghao("user_001");
        entity.setMima("123456");

        when(yonghuService.selectOne(any())).thenReturn(entity);
        when(tokenService.generateToken(1001L, "user_001", "yonghu", "用户"))
            .thenThrow(new IllegalStateException("未找到对应的 app_user 映射，无法创建前台用户会话"));

        R result = controller.login("user_001", "123456", null, null);

        assertEquals(500, result.get("code"));
        assertEquals("未找到对应的 app_user 映射，无法创建前台用户会话", result.get("msg"));
        verify(appUserSessionDao).upsertFromYonghu(entity);
    }
}
