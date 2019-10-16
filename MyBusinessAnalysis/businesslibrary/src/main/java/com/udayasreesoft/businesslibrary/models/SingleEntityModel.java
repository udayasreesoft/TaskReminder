package com.udayasreesoft.businesslibrary.models;

public class SingleEntityModel {
    private String businessOutlet;

    public SingleEntityModel() {
    }

    public SingleEntityModel(String businessOutlet) {
        this.businessOutlet = businessOutlet;
    }

    public String getBusinessOutlet() {
        return businessOutlet;
    }

    public void setBusinessOutlet(String businessOutlet) {
        this.businessOutlet = businessOutlet;
    }
}
