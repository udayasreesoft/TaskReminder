package com.udayasreesoft.businesslibrary.utils

import android.app.ProgressDialog
import android.content.Context

class CustomProgressDialog(val context: Context) {
    private var progressDialog: ProgressDialog? = null
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

    fun setTitle(title : String) {
        progressTitle = title
        if (progressDialog != null && !progressDialog!!.isShowing) {
            progressDialog!!.setTitle(progressTitle)
        }
    }

    fun setMessage(message : String) {
        progressMessage = message
        if (progressDialog != null && !progressDialog!!.isShowing) {
            progressDialog!!.setMessage(progressMessage)
        }
    }

    fun build() {
        progressDialog = ProgressDialog(context)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setTitle(progressTitle)
        progressDialog!!.setMessage(progressMessage)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setCancelable(false)
    }

    fun show() {
        dismiss()
        if (progressDialog != null && !progressDialog!!.isShowing) {
            progressDialog!!.setMessage(progressMessage)
            progressDialog!!.show()
        }
    }

    fun dismiss() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }
}