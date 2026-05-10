package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dao.AppUserCompatibilityDao;
import com.dao.AppUserSessionDao;
import com.entity.YonghuEntity;
import com.entity.vo.PasswordChangeRequestVO;
import com.service.TokenService;
import com.service.YonghuService;
import com.utils.EncryptUtil;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class YonghuControllerAccountSecurityTest {

    @Mock
    private YonghuService yonghuService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AppUserSessionDao appUserSessionDao;

    @Mock
    private AppUserCompatibilityDao appUserCompatibilityDao;

    @Test
    void changePasswordShouldRejectWhenUserNotLoggedIn() {
        YonghuController controller = newController();

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), new MockHttpServletRequest());

        assertEquals(500, result.get("code"));
        assertEquals("未登录或会话已失效", result.get("msg"));
        verify(appUserCompatibilityDao, never()).selectByLegacyId(any());
    }

    @Test
    void changePasswordCaptchaShouldRejectWhenUserNotLoggedIn() {
        YonghuController controller = newController();

        R result = controller.changePasswordCaptcha(new MockHttpServletRequest());

        assertEquals(500, result.get("code"));
        assertEquals("未登录或会话已失效", result.get("msg"));
    }

    @Test
    void changePasswordCaptchaShouldReturnCaptchaAndWriteSessionWhenLoggedIn() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);

        R result = controller.changePasswordCaptcha(request);

        assertEquals(0, result.get("code"));
        Object data = result.get("data");
        assertNotNull(data);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) data;
        String captcha = String.valueOf(payload.get("captcha"));
        assertEquals(4, captcha.length());
        assertEquals(captcha, request.getSession().getAttribute("yonghu:changePassword:captcha"));
        assertNotNull(request.getSession().getAttribute("yonghu:changePassword:expireAt"));
        assertEquals("changePassword", request.getSession().getAttribute("yonghu:changePassword:purpose"));
    }

    @Test
    void changePasswordShouldRejectWhenSessionTableNameDoesNotMatch() {
        YonghuController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 7L);
        request.getSession().setAttribute("tableName", "users");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), request);

        assertEquals(500, result.get("code"));
        assertEquals("当前会话不允许修改该账户密码", result.get("msg"));
        verify(appUserCompatibilityDao, never()).selectByLegacyId(any());
    }

    @Test
    void changePasswordShouldRejectBlankPasswords() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);

        R result = controller.changePassword(passwordChange(" ", ""), request);

        assertEquals(500, result.get("code"));
        assertEquals("原密码和新密码不能为空", result.get("msg"));
        verify(appUserCompatibilityDao, never()).selectByLegacyId(any());
        verify(appUserCompatibilityDao, never()).updateByLegacyId(any(YonghuEntity.class));
        verify(appUserSessionDao, never()).upsertFromYonghu(any(YonghuEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsMissing() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码不能为空", result.get("msg"));
        verify(appUserCompatibilityDao, never()).updateByLegacyId(any(YonghuEntity.class));
        verify(appUserSessionDao, never()).upsertFromYonghu(any(YonghuEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsWrong() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass", "9999"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码错误", result.get("msg"));
        verify(appUserCompatibilityDao, never()).updateByLegacyId(any(YonghuEntity.class));
        verify(appUserSessionDao, never()).upsertFromYonghu(any(YonghuEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsExpired() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() - 1L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass", "1234"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码已过期，请刷新后重试", result.get("msg"));
        verify(appUserCompatibilityDao, never()).updateByLegacyId(any(YonghuEntity.class));
        verify(appUserSessionDao, never()).upsertFromYonghu(any(YonghuEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenOldPasswordIsWrong() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setMima("right-pass");

        when(appUserCompatibilityDao.selectByLegacyId(7L)).thenReturn(persisted);

        R result = controller.changePassword(passwordChange("wrong-pass", "new-pass", "1234"), request);

        assertEquals(500, result.get("code"));
        assertEquals("原密码错误", result.get("msg"));
        assertEquals(null, request.getSession().getAttribute("yonghu:changePassword:captcha"));
        verify(appUserCompatibilityDao, never()).updateByLegacyId(any(YonghuEntity.class));
        verify(appUserSessionDao, never()).upsertFromYonghu(any(YonghuEntity.class));
    }

    @Test
    void changePasswordShouldUpdateCompatRecordAndSessionSnapshot() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setMima("right-pass");
        persisted.setYonghuzhanghao("movie-user");

        when(appUserCompatibilityDao.selectByLegacyId(7L)).thenReturn(persisted);

        R result = controller.changePassword(
            passwordChange(EncryptUtil.aesEncrypt("right-pass"), EncryptUtil.aesEncrypt("new-pass"), "1234"),
            request
        );

        assertEquals(0, result.get("code"));
        assertEquals("密码修改成功", result.get("msg"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), persisted.getMima());
        assertNotEquals("new-pass", persisted.getMima());
        assertEquals(null, request.getSession().getAttribute("yonghu:changePassword:captcha"));
        verify(appUserCompatibilityDao).updateByLegacyId(persisted);
        verify(appUserSessionDao).upsertFromYonghu(persisted);
    }

    @Test
    void changePasswordShouldRejectCaptchaReuseAfterSuccessfulChange() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        request.getSession().setAttribute("yonghu:changePassword:captcha", "1234");
        request.getSession().setAttribute("yonghu:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("yonghu:changePassword:purpose", "changePassword");
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setMima("right-pass");
        persisted.setYonghuzhanghao("movie-user");

        when(appUserCompatibilityDao.selectByLegacyId(7L)).thenReturn(persisted);

        R success = controller.changePassword(passwordChange("right-pass", "new-pass", "1234"), request);
        R reused = controller.changePassword(passwordChange("new-pass", "final-pass", "1234"), request);

        assertEquals(0, success.get("code"));
        assertEquals(500, reused.get("code"));
        assertEquals("验证码已过期，请刷新后重试", reused.get("msg"));
    }

    @Test
    void loginShouldAcceptAesIncomingPasswordWhenStoredPasswordIsPlaintext() {
        YonghuController controller = newController();
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setYonghuzhanghao("movie-user");
        persisted.setMima("right-pass");

        when(appUserCompatibilityDao.selectByLoginName("movie-user")).thenReturn(persisted);
        when(tokenService.generateToken(7L, "movie-user", "yonghu", "用户")).thenReturn("token-plain");

        R result = controller.login("movie-user", EncryptUtil.aesEncrypt("right-pass"), null, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals("token-plain", result.get("token"));
        verify(appUserSessionDao).upsertFromYonghu(persisted);
    }

    @Test
    void loginShouldAcceptAesIncomingPasswordWhenStoredPasswordIsEncrypted() {
        YonghuController controller = newController();
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setYonghuzhanghao("movie-user");
        persisted.setMima(EncryptUtil.aesEncrypt("right-pass"));

        when(appUserCompatibilityDao.selectByLoginName("movie-user")).thenReturn(persisted);
        when(tokenService.generateToken(7L, "movie-user", "yonghu", "用户")).thenReturn("token-cipher");

        R result = controller.login("movie-user", EncryptUtil.aesEncrypt("right-pass"), null, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals("token-cipher", result.get("token"));
        verify(appUserSessionDao).upsertFromYonghu(persisted);
    }

    @Test
    void saveShouldPersistEncryptedPassword() {
        YonghuController controller = newController();
        YonghuEntity payload = new YonghuEntity();
        payload.setYonghuzhanghao("movie-user");
        payload.setMima(EncryptUtil.aesEncrypt("new-pass"));

        when(appUserCompatibilityDao.countByLoginName("movie-user", null)).thenReturn(0);
        when(appUserCompatibilityDao.selectByLoginName("movie-user")).thenReturn(null);

        R result = controller.save(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), payload.getMima());
        assertNotEquals("new-pass", payload.getMima());
        verify(appUserCompatibilityDao).insertFromCompat(payload);
        verify(appUserSessionDao).upsertFromYonghu(payload);
    }

    @Test
    void addShouldPersistEncryptedPassword() {
        YonghuController controller = newController();
        YonghuEntity payload = new YonghuEntity();
        payload.setYonghuzhanghao("movie-user");
        payload.setMima(EncryptUtil.aesEncrypt("new-pass"));

        when(appUserCompatibilityDao.countByLoginName("movie-user", null)).thenReturn(0);
        when(appUserCompatibilityDao.selectByLoginName("movie-user")).thenReturn(null);

        R result = controller.add(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), payload.getMima());
        assertNotEquals("new-pass", payload.getMima());
        verify(appUserCompatibilityDao).insertFromCompat(payload);
        verify(appUserSessionDao).upsertFromYonghu(payload);
    }

    @Test
    void resetPassShouldBeDisabled() {
        YonghuController controller = newController();

        R result = controller.resetPass("movie-user", new MockHttpServletRequest());

        assertEquals(500, result.get("code"));
        assertEquals("公开密码重置已关闭，请联系管理员处理", result.get("msg"));
        verify(appUserCompatibilityDao, never()).selectByLoginName(any());
        verify(yonghuService, never()).selectOne(any());
    }

    @Test
    void sessionShouldNotExposePassword() {
        YonghuController controller = newController();
        MockHttpServletRequest request = requestForYonghu(7L);
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setMima("server-pass");

        when(appUserCompatibilityDao.selectByLegacyId(7L)).thenReturn(persisted);

        R result = controller.getCurrUser(request);

        assertEquals(0, result.get("code"));
        assertEquals(persisted, result.get("data"));
        assertEquals(null, persisted.getMima());
    }

    @Test
    void updateShouldNotAllowDirectPasswordMutation() {
        YonghuController controller = newController();
        YonghuEntity persisted = new YonghuEntity();
        persisted.setId(7L);
        persisted.setMima("server-pass");
        persisted.setYonghuzhanghao("movie-user");

        YonghuEntity payload = new YonghuEntity();
        payload.setId(7L);
        payload.setMima("client-pass");
        payload.setYonghuzhanghao("movie-user");

        when(appUserCompatibilityDao.countByLoginName("movie-user", 7L)).thenReturn(0);
        when(appUserCompatibilityDao.selectByLegacyId(7L)).thenReturn(persisted);

        R result = controller.update(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals("server-pass", payload.getMima());
        verify(appUserCompatibilityDao).updateByLegacyId(payload);
        verify(appUserSessionDao).upsertFromYonghu(persisted);
    }

    private YonghuController newController() {
        YonghuController controller = new YonghuController();
        ReflectionTestUtils.setField(controller, "yonghuService", yonghuService);
        ReflectionTestUtils.setField(controller, "tokenService", tokenService);
        ReflectionTestUtils.setField(controller, "appUserSessionDao", appUserSessionDao);
        ReflectionTestUtils.setField(controller, "appUserCompatibilityDao", appUserCompatibilityDao);
        return controller;
    }

    private PasswordChangeRequestVO passwordChange(String oldPassword, String newPassword) {
        return passwordChange(oldPassword, newPassword, null);
    }

    private PasswordChangeRequestVO passwordChange(String oldPassword, String newPassword, String captcha) {
        PasswordChangeRequestVO request = new PasswordChangeRequestVO();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        request.setCaptcha(captcha);
        return request;
    }

    private MockHttpServletRequest requestForYonghu(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", userId);
        request.getSession().setAttribute("tableName", "yonghu");
        return request;
    }
}
