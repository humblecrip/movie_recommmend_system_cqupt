package com.entity.vo;

import java.io.Serializable;

/**
 * 改密请求
 */
public class PasswordChangeRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String oldPassword;

    private String newPassword;

    private String captcha;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
