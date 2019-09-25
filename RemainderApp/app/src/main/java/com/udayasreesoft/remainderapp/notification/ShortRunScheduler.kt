package com.udayasreesoft.remainderapp.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ShortRunScheduler : JobService() {
    override fun onStopJob(p0: JobParameters?): Boolean {
        startService(Intent(this, ShortRunService::class.java))
        return true
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        return true
    }
}