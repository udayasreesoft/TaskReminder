package com.udayasreesoft.mybusinessanalysis.activities

import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.firebasedatabase.FireBaseAccessUtils
import com.udayasreesoft.mybusinessanalysis.firebasedatabase.UserSignInModel
import com.udayasreesoft.mybusinessanalysis.utils.AppUtils
import com.udayasreesoft.mybusinessanalysis.utils.PreferenceSharedUtils

class SignInActivity : AppCompatActivity(), View.OnClickListener {

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
        setContentView(R.layout.activity_signin)
        screenSize()
        initView()
        preferenceSharedUtils = PreferenceSharedUtils(this@SignInActivity).getInstance()
        if (preferenceSharedUtils.getUserSignInStatus()) {
            // TODO : true -> launch ReminderListActivity
            with(preferenceSharedUtils) {
                if (getUserFireBaseChildId() != "NA" && getUserName() != "NA"
                    && getOutletName() != "NA" && getSignInCode() != "NA") {
                    startActivity(
                        android.content.Intent(
                            this@SignInActivity,
                            HomeActivity::class.java
                        )
                    )
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
            val builder = AlertDialog.Builder(this@SignInActivity)
            builder.setTitle("Verification")
            builder.setMessage("Please enter verification code. \nCode is sent to ${preferenceSharedUtils.getOutletName()} app admin")
            val codeText = EditText(this@SignInActivity)
            val codeParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
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
                if (AppUtils.networkConnectivityCheck(this)) {
                    val userName = loginUserName.text.toString()
                    val userMobile = loginMobile.text.toString()
                    val userOutlet = loginOutletName.text.toString()
                    val userId = AppUtils.fireBaseChildId(outletCode)
                    val verificationCode = AppUtils.randomNumbers().toString()
                    if (userName.isNotEmpty() && userMobile.isNotEmpty() && userMobile.length == 10 && userOutlet.isNotEmpty()
                        && userId.isNotEmpty() && verificationCode.isNotEmpty()) {
                        val userSignInModel = UserSignInModel(userId, userName, userMobile, userOutlet, verificationCode, false)
                        val fireBaseAccessUtils = FireBaseAccessUtils(this).getInstance()
                        if (AppUtils.networkConnectivityCheck(this)) {
                            when(fireBaseAccessUtils.readUserFromFireBase(userSignInModel)) {
                                "NA" -> {
                                    /*TODO: Create New User*/
                                    if (AppUtils.networkConnectivityCheck(this)) {
                                        when(fireBaseAccessUtils.writeUserToDataBase(userSignInModel)) {
                                            true -> {
                                                /*TODO: Successfully User Created*/
                                                with(preferenceSharedUtils){
                                                    setUserName(userSignInModel.userName)
                                                    setMobileNumber(userSignInModel.userMobile)
                                                    setOutletName(userSignInModel.userOutlet)
                                                    setSignInCode(userSignInModel.verificationCode)
                                                    setUserConfirmationStatus(userSignInModel.codeVerified)
                                                    setUserFireBaseChildId(userSignInModel.userId)
                                                    setUserSignInStatus(true)
                                                }
                                                confirmationCodeAlert()
                                            }

                                            false -> {
                                                /*TODO: Fail to create*/
                                                Toast.makeText(this, "Unable to create account. Please try again...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }

                                "SUCCESS" -> {
                                    /*TODO: Confirmation code*/
                                    confirmationCodeAlert()
                                }

                                else -> {
                                    /*TODO: Fail*/
                                    Toast.makeText(this, "Fail to connect with server. Pleas try again...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    } else {
                        if (userName.isEmpty()) {
                            loginUserName.error = "Enter User Name"
                        }
                        if (userMobile.isEmpty() || userMobile.length < 10) {
                            loginMobile.error = "Enter Valid Number"
                        }
                        if (userOutlet.isEmpty()) {
                            loginOutletName.error = "Select valid Outlet"
                        }
                    }
                }
            }
        }
    }
}
