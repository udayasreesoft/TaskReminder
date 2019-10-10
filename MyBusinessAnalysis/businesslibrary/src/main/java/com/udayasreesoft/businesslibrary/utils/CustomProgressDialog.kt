package com.udayasreesoft.businesslibrary.utils

import android.app.ProgressDialog
import android.content.Context

class CustomProgressDialog(val context: Context) {
    private lateinit var progressDialog: ProgressDialog
    private var customProgressDialog : CustomProgressDialog? = null
    private var progressTitle : String = ""
    private var progressMessage : String = ""

    @Synchronized
    fun getInstance() : CustomProgressDialog{
        if (customProgressDialog == null) {
            customProgressDialog = CustomProgressDialog(context)
        }
        return customProgressDialog as CustomProgressDialog
    }

    fun setProgressTitle(title : String) {
        progressTitle = title
    }

    fun setProgressMessage(message : String) {
        progressMessage = message
    }

    fun setUpProgressDialog() {
        progressDialog = ProgressDialog(context)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setTitle(progressTitle)
        progressDialog.setMessage(progressMessage)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)
    }

    fun showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}