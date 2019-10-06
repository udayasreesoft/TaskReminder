package com.udayasreesoft.remainderapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.contracts.Returns

class AppUtils {
    companion object {
        @JvmField var isServiceRun = true

        @JvmField var SCREEN_WIDTH = 0
        @JvmField var SCREEN_HEIGHT = 0

        fun logMessage(message : String) {
            Log.d("CHECK_DATE", message)
        }

        fun randomNumbers() : Int {
            return Random().nextInt(999999)
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

        internal fun networkConnectivityCheck(context: Context): Boolean {
            val connectivityManager: ConnectivityManager? =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

    }
}