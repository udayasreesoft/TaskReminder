package com.udayasreesoft.remainderapp.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RebootBroadCastReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, p1: Intent?) {
        TimerAlarmManager(context = context!!).getInstance().startAlarmManager()
    }
}