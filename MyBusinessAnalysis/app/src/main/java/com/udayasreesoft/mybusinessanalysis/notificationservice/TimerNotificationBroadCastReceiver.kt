package com.udayasreesoft.mybusinessanalysis.notificationservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.activities.HomeActivity
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import java.text.SimpleDateFormat
import java.util.*



class TimerNotificationBroadCastReceiver : BroadcastReceiver() {

    private var context: Context? = null
    private lateinit var notificationManagerNotify: NotificationManager
    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        AppUtils.logMessage("Receiver Broadcast")
        val bundle = intent?.extras
         if (bundle != null) {
             if (bundle.containsKey(ConstantUtils.TASK_SLNO)) {
                 GetTodayNotificationData(bundle.getInt(ConstantUtils.TASK_SLNO)).execute()
             }
         }
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetTodayNotificationData(private val slNo : Int) : AsyncTask<Void, Void, TaskDataTable>() {
        override fun doInBackground(vararg p0: Void?): TaskDataTable {
            return TaskRepository(context).queryTaskBySlNo(slNo) as TaskDataTable
        }

        override fun onPostExecute(result: TaskDataTable?) {
            super.onPostExecute(result)
            if (result != null) {
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
                val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.US)
                val currentDate = dateFormat.format(calendar.timeInMillis)
                with(result) {
                    for (i in 0..days) {
                        calendar.timeInMillis = date
                        calendar.add(Calendar.DATE, ((days - i) * -1))
                        val receiverDate = dateFormat.format(calendar.timeInMillis)
                        if (currentDate == receiverDate) {
                            AppUtils.logMessage("Date : $currentDate && Date : $receiverDate")
                            simpleOfflineNotification(
                                "Pay to $companyName",
                                "Payable amount of Rs.$amount",
                                "Payable amount of Rs.$amount/- is to pay on $currentDate with Cheque $chequeNo."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun simpleOfflineNotification(title: String, amount : String, description: String) {
        val notificationId: Int = Random().nextInt(60000)

        val intent = Intent(context?.applicationContext, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val bitmapIcon = BitmapFactory.decodeResource(context?.resources, R.drawable.ic_notify_badges)
        val builder = NotificationCompat.Builder(context!!, ADMIN_CHANNEL_ID)
        builder
            .setAutoCancel(true)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_notify_badges)
            .setLargeIcon(bitmapIcon)
            .setContentTitle(title)
            .setContentText(amount)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setNumber(notificationId)

        notificationManagerNotify = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannel()
        }

        notificationManagerNotify.notify(notificationId, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannel() {
        val adminChannelName = context?.getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription = context?.getString(R.string.notifications_admin_channel_description)

        val adminChannel: NotificationChannel =
            NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.GREEN
        adminChannel.enableVibration(true)

        notificationManagerNotify.createNotificationChannel(adminChannel)
    }

}