package com.udayasreesoft.remainderapp.activities

import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.udayasreesoft.remainderapp.R
import com.udayasreesoft.remainderapp.firebasedatabase.UserSignInModel
import com.udayasreesoft.remainderapp.utils.AppUtils
import com.udayasreesoft.remainderapp.utils.PreferenceSharedUtils

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var loginLayout : LinearLayout
    private lateinit var loginUserName : EditText
    private lateinit var loginMobile : EditText
    private lateinit var loginOutletName : AutoCompleteTextView
    private lateinit var loginBtn : Button

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils

    private val outletName = "Kushika Kids Collections"
    private val outletCode = "KKC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        screenSize()
        initView()
        preferenceSharedUtils = PreferenceSharedUtils(this@LoginActivity).getInstance()
        if (preferenceSharedUtils.getUserSignInStatus()) {
            // TODO : true -> launch ReminderListActivity
            with(preferenceSharedUtils) {
                if (getUserFireBaseChildId() != "NA" && getUserName() != "NA"
                    && getOutletName() != "NA" && getSignInCode() != "NA") {
                    startActivity(Intent(this@LoginActivity, ReminderListActivity::class.java))
                }
            }
        }

        if (preferenceSharedUtils.getSignInCode() != "NA") {
            confirmationCodeAlert()
        }
    }

    private fun screenSize() {
        val size = Point()
        val w = windowManager

        w.defaultDisplay.getSize(size)
        AppUtils.SCREEN_WIDTH = size.x
        AppUtils.SCREEN_HEIGHT = size.y
    }

    private fun initView() {
        supportActionBar?.hide()
        loginLayout = findViewById(R.id.login_layout_id)
        loginUserName = findViewById(R.id.login_user_name_id)
        loginMobile = findViewById(R.id.login_mobile_id)
        loginOutletName = findViewById(R.id.login_outlet_name_id)
        loginBtn = findViewById(R.id.login_login_btn_id)
        findViewById<TextView>(R.id.login_title_id).typeface = Typeface.createFromAsset(assets, "fonts/sundaprada.ttf")

        loginLayout.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.80).toInt()
        loginLayout.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.80).toInt()

        loginBtn.setOnClickListener(this)
    }

    private fun confirmationCodeAlert() {
        if (!preferenceSharedUtils.getUserConfirmationStatus()) {
            val builder = AlertDialog.Builder(this@LoginActivity)

            val codeText = EditText(this@LoginActivity)
            val codeParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            codeText.layoutParams = codeParams
            builder.setView(codeText)
            builder.setPositiveButton("OK"
            ) { dialog, _ ->
                dialog?.dismiss()
                if (preferenceSharedUtils.getSignInCode() == codeText.text.toString()) {
                    
                }
            }
            builder.setCancelable(false)
            builder.create().show()
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.login_login_btn_id -> {

            }
        }
    }
}
