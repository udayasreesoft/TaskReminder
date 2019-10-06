package com.udayasreesoft.remainderapp.firebasedatabase;


public class UserSignInModel {
    private String userId, userName, mobileNumber, outletName, confirmationID;
    private boolean registerStatus;

    public UserSignInModel() { }

    public UserSignInModel(String userId, String userName, String mobileNumber, String outletName, String confirmationID, boolean registerStatus) {
        this.userId = userId;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
        this.outletName = outletName;
        this.confirmationID = confirmationID;
        this.registerStatus = registerStatus;
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

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getConfirmationID() {
        return confirmationID;
    }

    public void setConfirmationID(String confirmationID) {
        this.confirmationID = confirmationID;
    }

    public boolean isRegisterStatus() {
        return registerStatus;
    }

    public void setRegisterStatus(boolean registerStatus) {
        this.registerStatus = registerStatus;
    }
}
