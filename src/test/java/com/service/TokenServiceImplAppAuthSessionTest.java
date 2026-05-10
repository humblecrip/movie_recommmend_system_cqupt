package com.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dao.AppAuthSessionDao;
import com.dao.AppUserSessionDao;
import com.entity.AppAuthSessionEntity;
import com.entity.TokenEntity;
import com.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplAppAuthSessionTest {

    @Mock
    private AppAuthSessionDao appAuthSessionDao;

    @Mock
    private AppUserSessionDao appUserSessionDao;

    @Test
    void generateTokenShouldWriteFrontUserSessionIntoAppAuthSession() {
        TokenServiceImpl service = new TokenServiceImpl();
        ReflectionTestUtils.setField(service, "appAuthSessionDao", appAuthSessionDao);
        ReflectionTestUtils.setField(service, "appUserSessionDao", appUserSessionDao);

        when(appUserSessionDao.selectIdByLegacyYonghuId(1001L)).thenReturn(501L);
        when(appAuthSessionDao.updateActiveSession(any(AppAuthSessionEntity.class))).thenReturn(0);
        when(appAuthSessionDao.insertSession(any(AppAuthSessionEntity.class))).thenReturn(1);

        String token = service.generateToken(1001L, "front_user_01", "yonghu", "用户");

        ArgumentCaptor<AppAuthSessionEntity> captor = ArgumentCaptor.forClass(AppAuthSessionEntity.class);
        verify(appAuthSessionDao).insertSession(captor.capture());
        AppAuthSessionEntity session = captor.getValue();

        assertNotNull(token);
        assertEquals(token, session.getSessionToken());
        assertEquals(501L, session.getAppUserId());
        assertEquals(1001L, session.getLegacySubjectId());
        assertEquals("yonghu", session.getSubjectTableName());
        assertEquals("用户", session.getSubjectRoleName());
        assertEquals("front_user", session.getSubjectKind());
        assertEquals("front_user_01", session.getSubjectLoginName());
    }

    @Test
    void getTokenEntityShouldReturnLegacyCompatibleTokenShapeFromAppAuthSession() {
        TokenServiceImpl service = new TokenServiceImpl();
        ReflectionTestUtils.setField(service, "appAuthSessionDao", appAuthSessionDao);

        TokenEntity entity = new TokenEntity();
        entity.setUserid(2L);
        entity.setUsername("admin");
        entity.setTablename("users");
        entity.setRole("管理员");
        entity.setToken("abc123");
        when(appAuthSessionDao.selectTokenEntityByToken("abc123")).thenReturn(entity);

        TokenEntity result = service.getTokenEntity("abc123");

        assertEquals(2L, result.getUserid());
        assertEquals("admin", result.getUsername());
        assertEquals("users", result.getTablename());
        assertEquals("管理员", result.getRole());
        assertEquals("abc123", result.getToken());
    }

    @Test
    void generateTokenShouldRejectFrontUserSessionWhenAppUserMappingMissing() {
        TokenServiceImpl service = new TokenServiceImpl();
        ReflectionTestUtils.setField(service, "appAuthSessionDao", appAuthSessionDao);
        ReflectionTestUtils.setField(service, "appUserSessionDao", appUserSessionDao);

        when(appUserSessionDao.selectIdByLegacyYonghuId(1001L)).thenReturn(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.generateToken(1001L, "front_user_01", "yonghu", "用户")
        );

        assertEquals("未找到对应的 app_user 映射，无法创建前台用户会话", exception.getMessage());
        verify(appAuthSessionDao, never()).updateActiveSession(any(AppAuthSessionEntity.class));
        verify(appAuthSessionDao, never()).insertSession(any(AppAuthSessionEntity.class));
    }
}
