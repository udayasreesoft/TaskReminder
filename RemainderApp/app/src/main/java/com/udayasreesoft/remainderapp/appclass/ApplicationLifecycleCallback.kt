package com.udayasreesoft.remainderapp.appclass

import android.app.Activity
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.udayasreesoft.remainderapp.utils.AppUtils
import com.udayasreesoft.remainderapp.notification.ShortRunScheduler
import com.udayasreesoft.remainderapp.notification.ShortRunService
import com.udayasreesoft.remainderapp.notification.TimerAlarmManager

class ApplicationLifecycleCallback : Application.ActivityLifecycleCallbacks {
    private var numStarted: Int = 0

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity?) {
        AppUtils.isServiceRun = true
        AppUtils.logMessage("Resume : $numStarted")

        activity?.applicationContext?.let { TimerAlarmManager(it).stopAlarmManager() }
        numStarted++
    }

    override fun onActivityStopped(activity: Activity?) {
        numStarted--
        schedulerService(activity)
    }

    override fun onActivityDestroyed(activity: Activity?) {
        Handler().postDelayed(Runnable {
            schedulerService(activity)
        }, 7000)
    }

    private fun schedulerService(activity: Activity?) {
        AppUtils.logMessage("$numStarted")
        if (AppUtils.isServiceRun && numStarted == 0) {
            AppUtils.logMessage("zero")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val jobScheduler: JobScheduler =
                    activity?.applicationContext?.getSystemService(Context.JOB_SCHEDULER_SERVICE)
                            as JobScheduler

                val jobBuilder = JobInfo.Builder(18, ComponentName(activity!!, ShortRunScheduler::class.java))
                    .setRequiresBatteryNotLow(true)
                    .setMinimumLatency(1000)
                    .setOverrideDeadline(3000)

                jobScheduler.schedule(jobBuilder.build())
            } else {
                AppUtils.logMessage("Service")
                val intent = Intent(activity, ShortRunService::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity?.startService(intent)
            }
        }
    }
}