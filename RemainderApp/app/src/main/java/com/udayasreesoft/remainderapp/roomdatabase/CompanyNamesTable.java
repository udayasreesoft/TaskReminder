package com.udayasreesoft.remainderapp.roomdatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class CompanyNamesTable {

    @PrimaryKey(autoGenerate = true)
    private int slNo;

    @ColumnInfo(name = "company_names")
    private String companyName;

    public CompanyNamesTable(String companyName) {
        this.companyName = companyName;
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
}
