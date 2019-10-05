package com.udayasreesoft.remainderapp.appclass

import android.app.Application

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ApplicationLifecycleCallback())
    }
}