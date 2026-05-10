package com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import com.entity.UsersEntity;
import com.entity.vo.PasswordChangeRequestVO;
import com.service.TokenService;
import com.service.UsersService;
import com.utils.EncryptUtil;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UsersControllerAccountSecurityTest {

    @Mock
    private UsersService usersService;

    @Mock
    private TokenService tokenService;

    @Test
    void changePasswordShouldRejectWhenUserNotLoggedIn() {
        UsersController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), request);

        assertEquals(500, result.get("code"));
        assertEquals("未登录或会话已失效", result.get("msg"));
        verify(usersService, never()).selectById(any());
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordCaptchaShouldRejectWhenUserNotLoggedIn() {
        UsersController controller = newController();

        R result = controller.changePasswordCaptcha(new MockHttpServletRequest());

        assertEquals(500, result.get("code"));
        assertEquals("未登录或会话已失效", result.get("msg"));
    }

    @Test
    void changePasswordCaptchaShouldReturnCaptchaAndWriteSessionWhenLoggedIn() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);

        R result = controller.changePasswordCaptcha(request);

        assertEquals(0, result.get("code"));
        Object data = result.get("data");
        assertNotNull(data);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) data;
        String captcha = String.valueOf(payload.get("captcha"));
        assertEquals(4, captcha.length());
        assertEquals(captcha, request.getSession().getAttribute("users:changePassword:captcha"));
        assertNotNull(request.getSession().getAttribute("users:changePassword:expireAt"));
        assertEquals("changePassword", request.getSession().getAttribute("users:changePassword:purpose"));
    }

    @Test
    void changePasswordShouldRejectWhenSessionTableNameDoesNotMatch() {
        UsersController controller = newController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", 9L);
        request.getSession().setAttribute("tableName", "yonghu");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), request);

        assertEquals(500, result.get("code"));
        assertEquals("当前会话不允许修改该账户密码", result.get("msg"));
        verify(usersService, never()).selectById(any());
    }

    @Test
    void changePasswordShouldRejectBlankPasswords() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);

        R result = controller.changePassword(passwordChange(" ", ""), request);

        assertEquals(500, result.get("code"));
        assertEquals("原密码和新密码不能为空", result.get("msg"));
        verify(usersService, never()).selectById(any());
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsMissing() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码不能为空", result.get("msg"));
        verify(usersService, never()).selectById(any());
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsWrong() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass", "9999"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码错误", result.get("msg"));
        verify(usersService, never()).selectById(any());
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenCaptchaIsExpired() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() - 1L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");

        R result = controller.changePassword(passwordChange("old-pass", "new-pass", "1234"), request);

        assertEquals(500, result.get("code"));
        assertEquals("验证码已过期，请刷新后重试", result.get("msg"));
        verify(usersService, never()).selectById(any());
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordShouldRejectWhenOldPasswordIsWrong() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setPassword("right-pass");

        when(usersService.selectById(9L)).thenReturn(persisted);

        R result = controller.changePassword(passwordChange("wrong-pass", "new-pass", "1234"), request);

        assertEquals(500, result.get("code"));
        assertEquals("原密码错误", result.get("msg"));
        assertEquals(null, request.getSession().getAttribute("users:changePassword:captcha"));
        verify(usersService, never()).updateById(any(UsersEntity.class));
    }

    @Test
    void changePasswordShouldUpdatePersistedPasswordWhenOldPasswordMatches() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setPassword("right-pass");

        when(usersService.selectById(9L)).thenReturn(persisted);

        R result = controller.changePassword(passwordChange(EncryptUtil.aesEncrypt("right-pass"), EncryptUtil.aesEncrypt("new-pass"), "1234"), request);

        assertEquals(0, result.get("code"));
        assertEquals("密码修改成功", result.get("msg"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), persisted.getPassword());
        assertNotEquals("new-pass", persisted.getPassword());
        assertEquals(null, request.getSession().getAttribute("users:changePassword:captcha"));
        verify(usersService).updateById(persisted);
    }

    @Test
    void changePasswordShouldRejectCaptchaReuseAfterSuccessfulChange() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        request.getSession().setAttribute("users:changePassword:captcha", "1234");
        request.getSession().setAttribute("users:changePassword:expireAt", System.currentTimeMillis() + 60_000L);
        request.getSession().setAttribute("users:changePassword:purpose", "changePassword");
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setPassword("right-pass");

        when(usersService.selectById(9L)).thenReturn(persisted);

        R success = controller.changePassword(passwordChange("right-pass", "new-pass", "1234"), request);
        R reused = controller.changePassword(passwordChange("new-pass", "final-pass", "1234"), request);

        assertEquals(0, success.get("code"));
        assertEquals(500, reused.get("code"));
        assertEquals("验证码已过期，请刷新后重试", reused.get("msg"));
    }

    @Test
    void loginShouldAcceptAesIncomingPasswordWhenStoredPasswordIsPlaintext() {
        UsersController controller = newController();
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setUsername("admin");
        persisted.setPassword("right-pass");
        persisted.setRole("管理员");

        when(usersService.selectOne(any())).thenReturn(persisted);
        when(tokenService.generateToken(9L, "admin", "users", "管理员")).thenReturn("token-plain");

        R result = controller.login("admin", EncryptUtil.aesEncrypt("right-pass"), null, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals("token-plain", result.get("token"));
    }

    @Test
    void loginShouldAcceptAesIncomingPasswordWhenStoredPasswordIsEncrypted() {
        UsersController controller = newController();
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setUsername("admin");
        persisted.setPassword(EncryptUtil.aesEncrypt("right-pass"));
        persisted.setRole("管理员");

        when(usersService.selectOne(any())).thenReturn(persisted);
        when(tokenService.generateToken(9L, "admin", "users", "管理员")).thenReturn("token-cipher");

        R result = controller.login("admin", EncryptUtil.aesEncrypt("right-pass"), null, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals("token-cipher", result.get("token"));
    }

    @Test
    void registerShouldPersistEncryptedPassword() {
        UsersController controller = newController();
        UsersEntity payload = new UsersEntity();
        payload.setUsername("admin");
        payload.setPassword(EncryptUtil.aesEncrypt("new-pass"));

        when(usersService.selectOne(any())).thenReturn(null);

        R result = controller.register(payload);

        assertEquals(0, result.get("code"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), payload.getPassword());
        assertNotEquals("new-pass", payload.getPassword());
        verify(usersService).insert(payload);
    }

    @Test
    void saveShouldPersistEncryptedPassword() {
        UsersController controller = newController();
        UsersEntity payload = new UsersEntity();
        payload.setUsername("admin");
        payload.setPassword(EncryptUtil.aesEncrypt("new-pass"));

        when(usersService.selectOne(any())).thenReturn(null);

        R result = controller.save(payload);

        assertEquals(0, result.get("code"));
        assertEquals(EncryptUtil.aesEncrypt("new-pass"), payload.getPassword());
        assertNotEquals("new-pass", payload.getPassword());
        verify(usersService).insert(payload);
    }

    @Test
    void resetPassShouldBeDisabled() {
        UsersController controller = newController();

        R result = controller.resetPass("admin", new MockHttpServletRequest());

        assertEquals(500, result.get("code"));
        assertEquals("公开密码重置已关闭，请联系管理员处理", result.get("msg"));
        verify(usersService, never()).selectOne(any());
    }

    @Test
    void sessionShouldNotExposePassword() {
        UsersController controller = newController();
        MockHttpServletRequest request = requestForUsers(9L);
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setPassword("server-pass");

        when(usersService.selectById(9L)).thenReturn(persisted);

        R result = controller.getCurrUser(request);

        assertEquals(0, result.get("code"));
        assertEquals(persisted, result.get("data"));
        assertEquals(null, persisted.getPassword());
    }

    @Test
    void updateShouldNotAllowDirectPasswordMutation() {
        UsersController controller = newController();
        UsersEntity persisted = new UsersEntity();
        persisted.setId(9L);
        persisted.setPassword("server-pass");

        UsersEntity payload = new UsersEntity();
        payload.setId(9L);
        payload.setUsername("admin");
        payload.setPassword("client-pass");

        when(usersService.selectOne(any())).thenReturn(null);
        when(usersService.selectById(9L)).thenReturn(persisted);

        R result = controller.update(payload);

        assertEquals(0, result.get("code"));
        assertEquals("server-pass", payload.getPassword());
        verify(usersService).updateById(payload);
    }

    private UsersController newController() {
        UsersController controller = new UsersController();
        ReflectionTestUtils.setField(controller, "userService", usersService);
        ReflectionTestUtils.setField(controller, "tokenService", tokenService);
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

    private MockHttpServletRequest requestForUsers(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("userId", userId);
        request.getSession().setAttribute("tableName", "users");
        return request;
    }
}
