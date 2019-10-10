package com.udayasreesoft.businesslibrary.models;

public class UserSignInModel {
    private String userId, userName, userMobile, userOutlet, verificationCode;
    private boolean isCodeVerified, isAdmin;

    public UserSignInModel() {
    }

    public UserSignInModel(String userId, String userName, String userMobile, String userOutlet, String verificationCode, boolean isCodeVerified, boolean isAdmin) {
        this.userId = userId;
        this.userName = userName;
        this.userMobile = userMobile;
        this.userOutlet = userOutlet;
        this.verificationCode = verificationCode;
        this.isCodeVerified = isCodeVerified;
        this.isAdmin = isAdmin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserOutlet() {
        return userOutlet;
    }

    public void setUserOutlet(String userOutlet) {
        this.userOutlet = userOutlet;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean getCodeVerified() {
        return isCodeVerified;
    }

    public void setCodeVerified(boolean codeVerified) {
        isCodeVerified = codeVerified;
    }

    public boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}