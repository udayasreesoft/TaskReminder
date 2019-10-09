package com.udayasreesoft.adminbusinessanalysis

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast

class AppUtils {
    companion object {
        internal fun networkConnectivityCheck(context: Context): Boolean {
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