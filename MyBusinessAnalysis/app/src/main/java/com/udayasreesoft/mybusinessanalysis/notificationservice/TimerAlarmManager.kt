package com.udayasreesoft.mybusinessanalysis.notificationservice

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import com.udayasreesoft.mybusinessanalysis.utils.AppUtils
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TimeDataTable
import com.udayasreesoft.mybusinessanalysis.utils.ConstantUtils
import com.udayasreesoft.mybusinessanalysis.utils.PreferenceSharedUtils
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class TimerAlarmManager(val context: Context) {

    private var timerAlarmManager: TimerAlarmManager? = null

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    @Synchronized
    fun getInstance(): TimerAlarmManager {
        if (timerAlarmManager == null) {
            timerAlarmManager = TimerAlarmManager(context.applicationContext)
        }
        return timerAlarmManager as TimerAlarmManager
    }

    private fun requestCodeFromPreference(requestType : Boolean, ids : Int) : ArrayList<Int> {
        val requestIds = ArrayList<Int>()
        try {
            val preferenceSharedUtils = PreferenceSharedUtils(context).getInstance()
            val jsonArray : JSONArray = JSONArray(preferenceSharedUtils.getAlarmIds())

            if (requestType) {
                jsonArray.put(ids)
                preferenceSharedUtils.clearPreference()
                preferenceSharedUtils.setAlarmIds(jsonArray.toString())
            }

            for (i in 0 until jsonArray.length()) {
                requestIds.add(jsonArray.getInt(i))
            }
            if (!requestType){
                preferenceSharedUtils.clearPreference()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return requestIds
    }

    fun startAlarmManager() {
        AppUtils.logMessage("Start")
        GetTimerDateAsync().execute()
    }

    fun stopAlarmManager() {
        AppUtils.logMessage("Stop")
        val idsList = requestCodeFromPreference(false, 0)
        for (ids in idsList) {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent: Intent = Intent(context, TimerNotificationBroadCastReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(context, ids, intent, 0)
            alarmManager.cancel(pendingIntent)
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetTimerDateAsync : AsyncTask<Void, Void, List<TimeDataTable>>() {
        override fun doInBackground(vararg p0: Void?): List<TimeDataTable> {
            return TaskRepository(context.applicationContext).queryDateInMillis(false) as ArrayList<TimeDataTable>
        }

        override fun onPostExecute(result: List<TimeDataTable>?) {
            super.onPostExecute(result)
            AppUtils.logMessage("Result : ${result?.size}")
            if (result != null && result.isNotEmpty()) {
                for (resultData in result) {
                    with(resultData){
                        for (i in 0..days) {
                            val calendar = Calendar.getInstance()
                            calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
                            calendar.timeInMillis = date
                            calendar.add(Calendar.DATE, ((days - i) * -1))
                            val millis : Long = calendar.timeInMillis

                            if (millis >= Calendar.getInstance().timeInMillis) {

                                AppUtils.logMessage(calendar.timeInMillis.toString() + "  =  " +
                                        SimpleDateFormat(
                                            "MMM dd yyyy hh:mm:ss a",
                                            Locale.US
                                        ).format(calendar.timeInMillis))

                                val requestCode = AppUtils.randomNumbers()
                                requestCodeFromPreference(true, requestCode)
                                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val intent: Intent = Intent(
                                    context,
                                    TimerNotificationBroadCastReceiver::class.java
                                )
                                intent.putExtra(ConstantUtils.TASK_SLNO, slNo)
                                pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                                } else {
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}