package com.udayasreesoft.mybusinessanalysis.roomdatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

@Entity
public class TimeDataTable {


    private int slNo;

    @ColumnInfo(name = "date_in_millis")
    private long date;

    @ColumnInfo(name = "days")
    private int days;

    public TimeDataTable(int slNo, long date, int days) {
        this.slNo = slNo;
        this.date = date;
        this.days = days;
    }

    public int getSlNo() {
        return slNo;
    }

    public void setSlNo(int slNo) {
        this.slNo = slNo;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
