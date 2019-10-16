package com.udayasreesoft.businesslibrary.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BusinessModel implements Parcelable {
    private String businessName, totalAmount;

    public BusinessModel() {
    }

    public BusinessModel(String businessName, String totalAmount) {
        this.businessName = businessName;
        this.totalAmount = totalAmount;
    }

    protected BusinessModel(Parcel in) {
        businessName = in.readString();
        totalAmount = in.readString();
    }

    public static final Creator<BusinessModel> CREATOR = new Creator<BusinessModel>() {
        @Override
        public BusinessModel createFromParcel(Parcel in) {
            return new BusinessModel(in);
        }

        @Override
        public BusinessModel[] newArray(int size) {
            return new BusinessModel[size];
        }
    };

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(businessName);
        parcel.writeString(totalAmount);
    }
}
