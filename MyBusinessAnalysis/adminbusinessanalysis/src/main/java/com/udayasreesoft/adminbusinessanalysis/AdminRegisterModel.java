package com.udayasreesoft.adminbusinessanalysis;

public class AdminRegisterModel {

    private String adminName, adminMobile, adminOutlet, adminAddress, verificationCode;
    private boolean isCodeVerified, isAdmin;

    public AdminRegisterModel() {
    }

    public AdminRegisterModel(String adminName, String adminMobile, String adminOutlet, String adminAddress, String verificationCode, boolean isCodeVerified, boolean isAdmin) {
        this.adminName = adminName;
        this.adminMobile = adminMobile;
        this.adminOutlet = adminOutlet;
        this.adminAddress = adminAddress;
        this.verificationCode = verificationCode;
        this.isCodeVerified = isCodeVerified;
        this.isAdmin = isAdmin;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminMobile() {
        return adminMobile;
    }

    public void setAdminMobile(String adminMobile) {
        this.adminMobile = adminMobile;
    }

    public String getAdminOutlet() {
        return adminOutlet;
    }

    public void setAdminOutlet(String adminOutlet) {
        this.adminOutlet = adminOutlet;
    }

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isCodeVerified() {
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
