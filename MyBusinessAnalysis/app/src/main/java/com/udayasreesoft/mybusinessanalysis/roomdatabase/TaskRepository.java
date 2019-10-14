package com.udayasreesoft.mybusinessanalysis.roomdatabase;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

@SuppressLint("StaticFieldLeak")
public class TaskRepository {
    private String DB_NAME = "reminder_tasks";

    private TaskDatabasePersistence databasePersistence;

    public TaskRepository(Context context) {
        databasePersistence = Room.databaseBuilder(context, TaskDatabasePersistence.class, DB_NAME)
                .build();
    }

    public void insertTask(final TaskDataTable taskDataTable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                databasePersistence.daoAccess().insertTask(taskDataTable);
                return null;
            }
        }.execute();
    }

    public void updateTask(final TaskDataTable taskDataTable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                databasePersistence.daoAccess().updateTask(taskDataTable);
                return null;
            }
        }.execute();
    }

    public void deleteTask(final TaskDataTable taskDataTable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                databasePersistence.daoAccess().deleteTask(taskDataTable);
                return null;
            }
        }.execute();
    }

    public void insertCompanyName(final CompanyNamesTable companyNamesTable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                databasePersistence.daoAccess().insertCompanyName(companyNamesTable);
                return null;
            }
        }.execute();
    }

    public List<TaskDataTable> queryAllTask() {
        return databasePersistence.daoAccess().fetchAllTasks();
    }

    public List<TaskDataTable> queryTask(final boolean isStatus) {
        return databasePersistence.daoAccess().fetchTasks(isStatus);
    }

    public TaskDataTable queryTaskBySlNo(final int slNo) {
        return databasePersistence.daoAccess().fetTaskBySlNo(slNo);
    }

    public List<String> queryCompanyName() {
        return databasePersistence.daoAccess().getCompanyNames();
    }

    public List<TimeDataTable> queryDateInMillis(boolean isStatus) {
        return databasePersistence.daoAccess().getDateFromDB(isStatus);
    }

    public void clearDataBase() {
        databasePersistence.isOpen();
        databasePersistence.clearAllTables();
        databasePersistence.close();
    }
}
