package com.udayasreesoft.businesslibrary.models;

public class CompanyModel {
    private String outletName, outletAddress, outletContact, outletLogo, outletBanner;

    public CompanyModel() {
    }

    public CompanyModel(String outletName, String outletAddress, String outletContact, String outletLogo, String outletBanner) {
        this.outletName = outletName;
        this.outletAddress = outletAddress;
        this.outletContact = outletContact;
        this.outletLogo = outletLogo;
        this.outletBanner = outletBanner;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getOutletAddress() {
        return outletAddress;
    }

    public void setOutletAddress(String outletAddress) {
        this.outletAddress = outletAddress;
    }

    public String getOutletContact() {
        return outletContact;
    }

    public void setOutletContact(String outletContact) {
        this.outletContact = outletContact;
    }

    public String getOutletLogo() {
        return outletLogo;
    }

    public void setOutletLogo(String outletLogo) {
        this.outletLogo = outletLogo;
    }

    public String getOutletBanner() {
        return outletBanner;
    }

    public void setOutletBanner(String outletBanner) {
        this.outletBanner = outletBanner;
    }
}
