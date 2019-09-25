package com.udayasreesoft.remainderapp.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.udayasreesoft.remainderapp.utils.AppUtils

class ShortRunService : Service() {
    override fun onBind(p0: Intent?): IBinder? { return null}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        AppUtils.isServiceRun = false
        AppUtils.logMessage("ShortRunService")
        TimerAlarmManager(context = applicationContext).getInstance().startAlarmManager()
        stopSelf()
    }

    override fun onDestroy() { super.onDestroy() }
}