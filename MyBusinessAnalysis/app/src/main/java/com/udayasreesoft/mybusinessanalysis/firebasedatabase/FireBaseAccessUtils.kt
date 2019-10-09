package com.udayasreesoft.mybusinessanalysis.firebasedatabase

import android.content.Context
import com.google.firebase.database.*
import com.udayasreesoft.mybusinessanalysis.utils.PreferenceSharedUtils

class FireBaseAccessUtils(val context: Context) {

    private var fireBaseAccessUtils : FireBaseAccessUtils? = null

    @Synchronized
    fun getInstance() : FireBaseAccessUtils{
        if (fireBaseAccessUtils == null) {
            fireBaseAccessUtils = FireBaseAccessUtils(context)
        }
        return fireBaseAccessUtils as FireBaseAccessUtils
    }
}