package com.udayasreesoft.mybusinessanalysis.appclass

import android.app.Application

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ApplicationLifecycleCallback())
    }
}