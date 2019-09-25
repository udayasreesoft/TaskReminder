package com.udayasreesoft.remainderapp.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class AppUtils {
    companion object {
        @JvmField var isServiceRun = true

        @JvmField var SCREEN_WIDTH = 0
        @JvmField var SCREEN_HEIGHT = 0

        fun logMessage(message : String) {
            Log.d("CHECK_DATE", message)
        }

        fun randomNumbers() : Int {
            return Random().nextInt(60000)
        }
    }
}