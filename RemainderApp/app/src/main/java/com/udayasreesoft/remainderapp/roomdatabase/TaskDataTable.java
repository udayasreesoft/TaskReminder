package com.udayasreesoft.remainderapp.roomdatabase;

import android.arch.persistence.room.*;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

@Entity
public class TaskDataTable {

    @PrimaryKey(autoGenerate = true)
    private int slNo;

    @ColumnInfo(name = "company_name")
    private String companyName;

    @ColumnInfo(name = "date_in_millis")
    private long date;

    @ColumnInfo(name = "amount")
    private String amount;

    @ColumnInfo(name = "cheque_number")
    private String chequeNo;

    @ColumnInfo(name = "status")
    private Boolean isTaskCompleted;

    @ColumnInfo(name = "days")
    private int days;

    public TaskDataTable(String companyName, long date,  String amount, String chequeNo, Boolean isTaskCompleted, int days) {
        this.companyName = companyName;
        this.date = date;
        this.amount = amount;
        this.chequeNo = chequeNo;
        this.isTaskCompleted = isTaskCompleted;
        this.days = days;
    }

    public int getSlNo() {
        return slNo;
    }

    public void setSlNo(int slNo) {
        this.slNo = slNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }

    public Boolean getTaskCompleted() {
        return isTaskCompleted;
    }

    public void setTaskCompleted(Boolean taskCompleted) {
        isTaskCompleted = taskCompleted;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
