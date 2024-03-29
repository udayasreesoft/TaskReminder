package com.udayasreesoft.businesslibrary.utils

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class AppUtils {
    companion object {
        @JvmField var isAdminStatus = false
        @JvmField var isServiceRun = true
        @JvmField var OUTLET_NAME = ""

        @JvmField var SCREEN_WIDTH = 0
        @JvmField var SCREEN_HEIGHT = 0

        fun logMessage(message : String) {
            Log.d("CHECK_DATE", message)
        }

        fun randomNumbers() : Int {
            return Random().nextInt(999999)
        }

        fun uniqueKey() : String {
            val characters : String = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
            var key = ""
            for (c in 0 until 11) {
                key += characters[Random().nextInt(characters.length)]
            }
            return key
        }

        fun fireBaseChildId(outletCode : String) : String {
            val characters : String = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
            var userId = outletCode
            for (c in 0 until 11) {
                userId += characters[Random().nextInt(characters.length)]
            }
            return userId
        }

        fun alertDialogReference(context: Context, title : String, message : String, positive : String, negative : String) {
            try {
                if (message != "NA" && positive != "NA") {
                    val builder = AlertDialog.Builder(context)
                    if (title != "NA") {
                        builder.setTitle(title)
                    }
                    builder.setMessage(message)
                    builder.setPositiveButton(positive
                    ) { dialog, _ -> dialog?.dismiss() }
                    if (negative != "NA") {
                        builder.setNegativeButton(negative
                        ) { dialog, _ -> dialog?.dismiss() }
                    }
                    builder.setCancelable(false)
                    builder.create().show()
                }
            } catch (e : Exception) {e.printStackTrace()}
        }

        fun getCurrentDate() :String{
            val simpleDateFormat = SimpleDateFormat(ConstantUtils.DATE_FORMAT, Locale.US)
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            calendar.set(Calendar.HOUR_OF_DAY, ConstantUtils.HOUR)
            calendar.set(Calendar.MINUTE, ConstantUtils.MINUTE)
            calendar.set(Calendar.SECOND, ConstantUtils.SECOND)
            return simpleDateFormat.format(calendar.timeInMillis)
        }

        fun networkConnectivityCheck(context: Context): Boolean {
            val connectivityManager: ConnectivityManager? =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isConnected) {
                    return true
                }
            }
            Toast.makeText(context, "Required Internet Connection", Toast.LENGTH_SHORT).show()
            return false
        }

    }
}