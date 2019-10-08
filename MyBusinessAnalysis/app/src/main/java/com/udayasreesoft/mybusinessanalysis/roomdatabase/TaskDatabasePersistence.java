package com.udayasreesoft.mybusinessanalysis.roomdatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {TaskDataTable.class, CompanyNamesTable.class}, version = 1, exportSchema = false)
public abstract class TaskDatabasePersistence extends RoomDatabase {
    public abstract TaskDao daoAccess();
}
