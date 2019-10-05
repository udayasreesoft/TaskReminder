package com.udayasreesoft.remainderapp.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceSharedUtils(val context: Context) {

    private var preferenceSharedUtils : PreferenceSharedUtils? = null

    private var sharedPreferences : SharedPreferences = context.getSharedPreferences("REMINDER_PREFERENCE", Context.MODE_PRIVATE)

    private val alarm_manager_ids = "ALARM_MANAGER_IDS"

    @Synchronized
    fun getInstance() : PreferenceSharedUtils{
        if (preferenceSharedUtils == null){
            preferenceSharedUtils = PreferenceSharedUtils(context.applicationContext)
        }
        return preferenceSharedUtils as PreferenceSharedUtils
    }

    fun setAlarmIds(ids : String){
        sharedPreferences.edit()?.putString(alarm_manager_ids, ids)?.apply()
    }

    fun getAlarmIds() : String? {
        return sharedPreferences.getString(alarm_manager_ids, "[]")
    }

    fun clearPreference() {
        sharedPreferences.edit()?.clear()?.apply()
    }
}