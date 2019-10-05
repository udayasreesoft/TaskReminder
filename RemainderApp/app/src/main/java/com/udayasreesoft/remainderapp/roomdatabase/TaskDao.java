package com.udayasreesoft.remainderapp.roomdatabase;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insertTask(TaskDataTable taskDataTable);

    @Query("SELECT * FROM TaskDataTable WHERE status =:isStatus ORDER BY date_in_millis ASC")
    List<TaskDataTable> fetchTasks(boolean isStatus);

    @Query("SELECT * FROM TaskDataTable WHERE slNo =:slNo")
    TaskDataTable fetTaskBySlNo(int slNo);

    @Query("SELECT slNo, date_in_millis, days FROM TaskDataTable WHERE status =:isStatus ORDER BY date_in_millis ASC")
    List<TimeDataTable> getDateFromDB(boolean isStatus);

    @Update
    void updateTask(TaskDataTable taskDataTable);

    @Delete
    void deleteTask(TaskDataTable taskDataTable);

    @Query("SELECT company_names FROM CompanyNamesTable ORDER BY company_names ASC")
    List<String> getCompanyNames();

    @Insert
    void insertCompanyName(CompanyNamesTable companyNamesTable);
}