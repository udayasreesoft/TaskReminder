package com.udayasreesoft.businesslibrary.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceSharedUtils(val context: Context) {
    private var preferenceSharedUtils : PreferenceSharedUtils? = null

    private var sharedPreferences : SharedPreferences = context.getSharedPreferences("REMINDER_PREFERENCE", Context.MODE_PRIVATE)

    private val alarm_manager_ids = "ALARM_MANAGER_IDS"
    private val user_signin_status = "USER_SIGN_IN_STATUS"
    private val user_firebase_id = "USER_FIRE_BASE_CHILD_ID"
    private val user_outlet_name = "FIRE_BASE_USER_OUTLET"
    private val user_name_firebase = "FIRE_BASE_USER_NAME"
    private val user_mobile_number = "USER_MOBILE_NUMBER"
    private val user_signin_code_firebase = "FIRE_BASE_CODE"
    private val user_confirmation_status = "USER_CONFIRMATION_STATUS"
    private val user_admin_status = "USER_ADMIN_STATUS"
    private val outlet_logo_url = "OUTLET_LOGO_URL"
    private val outlet_banner_url = "OUTLET_BANNER_URL"
    private val pay_update_version = "PAY_UPDATE_VERSION"
    private val registered_date_in_millis = "REGISTERED_DATE"

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

    fun setUserSignInStatus(status : Boolean) {
        sharedPreferences.edit()?.putBoolean(user_signin_status, status)?.apply()
    }

    fun getUserSignInStatus() : Boolean {
        return sharedPreferences.getBoolean(user_signin_status, false)
    }

    fun setUserFireBaseChildId(childId : String) {
        sharedPreferences.edit()?.putString(user_firebase_id, childId)?.apply()
    }

    fun getUserFireBaseChildId() : String? {
        return sharedPreferences.getString(user_firebase_id, "NA")
    }

    fun setUserName(userName : String) {
        sharedPreferences.edit()?.putString( user_name_firebase, userName)?.apply()
    }

    fun getUserName() : String? {
        return sharedPreferences.getString(user_name_firebase, "NA")
    }

    fun setOutletName(outLet : String) {
        sharedPreferences.edit()?.putString(user_outlet_name, outLet)?.apply()
    }

    fun getOutletName() : String? {
        return sharedPreferences.getString(user_outlet_name, "NA")
    }

    fun setSignInCode(code : String) {
        sharedPreferences.edit()?.putString(user_signin_code_firebase, code)?.apply()
    }

    fun getSignInCode() : String? {
        return sharedPreferences.getString(user_signin_code_firebase, "NA")
    }

    fun setUserConfirmationStatus(isConfirm : Boolean) {
        sharedPreferences.edit()?.putBoolean(user_confirmation_status, isConfirm)?.apply()
    }

    fun getUserConfirmationStatus() : Boolean {
        return sharedPreferences.getBoolean(user_confirmation_status, false)
    }

    fun setMobileNumber(mobile : String) {
        sharedPreferences.edit()?.putString(user_mobile_number, mobile)?.apply()
    }

    fun getMobileNumber() : String? {
        return sharedPreferences.getString(user_mobile_number, "NA")
    }

    fun setAdminStatus(isAdmin : Boolean) {
        sharedPreferences.edit()?.putBoolean(user_admin_status, isAdmin)?.apply()
    }

    fun getAdminStatus() : Boolean {
        return sharedPreferences.getBoolean(user_admin_status, false)
    }

    fun setOutletLogoUrl(url : String) {
        sharedPreferences.edit()?.putString(outlet_logo_url, url)?.apply()
    }

    fun getOutletLogoUrl() : String? {
        return sharedPreferences.getString(outlet_logo_url, ConstantUtils.DEFAULT_LOGO)
    }

    fun setOutletBannerUrl(url : String) {
        sharedPreferences.edit()?.putString(outlet_banner_url, url)?.apply()
    }

    fun getOutletBannerUrl() : String? {
        return sharedPreferences.getString(outlet_banner_url, ConstantUtils.DEFAULT_BANNER)
    }

    fun setPayVersionUpdate(version : Float) {
        sharedPreferences.edit()?.putFloat(pay_update_version, version)?.apply()
    }

    fun getPayVersionUpdate() : Float? {
        return sharedPreferences.getFloat(pay_update_version, 0.0f)
    }

    fun setRegisteredDate(millis : Long) {
        sharedPreferences.edit()?.putLong(registered_date_in_millis, millis)?.apply()
    }

    fun getRegisteredDate() : Long {
        return sharedPreferences.getLong(registered_date_in_millis, 0L)
    }

    fun clearAlarmIDPreference() {
        setAlarmIds("[]")
    }

    fun clearPreference() {
        sharedPreferences.edit()?.clear()?.apply()
    }
}