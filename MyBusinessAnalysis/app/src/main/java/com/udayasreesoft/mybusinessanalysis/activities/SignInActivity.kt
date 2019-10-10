package com.udayasreesoft.mybusinessanalysis.activities

import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.BusinessOutletModel
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.businesslibrary.models.UserSignInModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var loginLayout: LinearLayout
    private lateinit var loginUserName: EditText
    private lateinit var loginMobile: EditText
    private lateinit var loginOutletName: AutoCompleteTextView
    private lateinit var loginBtn: Button

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private lateinit var customProgressDialog : CustomProgressDialog

    private var outletName = ""
    private var outletCode = ""

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
                    && getOutletName() != "NA" && getSignInCode() != "NA"
                ) {
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
        findViewById<TextView>(R.id.login_title_id).typeface =
            Typeface.createFromAsset(assets, "fonts/sundaprada.ttf")

        loginLayout.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.80).toInt()
        loginLayout.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.80).toInt()

        loginBtn.setOnClickListener(this)

        customProgressDialog = CustomProgressDialog(this).getInstance()
        customProgressDialog.setProgressMessage("Connecting to Server. Please wait...")
        customProgressDialog.setUpProgressDialog()

        readOutletToFireBase()
    }

    private fun readFromFireBase(userSignInModel: UserSignInModel) {
        if (AppUtils.networkConnectivityCheck(this)) {
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.USERS)
                .child(userSignInModel.userOutlet)
                .child(userSignInModel.userMobile)

            fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    customProgressDialog.dismissProgressDialog()
                }

                override fun onDataChange(snapShot: DataSnapshot) {
                    if (snapShot.exists()) {
                        val model = snapShot.getValue(UserSignInModel::class.java)
                        if (model != null) {
                            val preferenceSharedUtils =
                                PreferenceSharedUtils(this@SignInActivity).getInstance()
                            with(model) {
                                preferenceSharedUtils.setUserName(userName)
                                preferenceSharedUtils.setMobileNumber(userMobile)
                                preferenceSharedUtils.setOutletName(userOutlet)
                                preferenceSharedUtils.setSignInCode(verificationCode)
                                preferenceSharedUtils.setUserConfirmationStatus(codeVerified)
                                preferenceSharedUtils.setUserFireBaseChildId(userId)
                                preferenceSharedUtils.setAdminStatus(admin)

                                loginUserName.setText("")
                                loginMobile.setText("")
                                loginOutletName.setText("")
                                customProgressDialog.dismissProgressDialog()
                                confirmationCodeAlert()
                            }
                        }
                    } else {
                        writeToFireBase(userSignInModel)
                    }
                }
            })
        }
    }

    private fun writeToFireBase(userSignInModel: UserSignInModel) {
        if (AppUtils.networkConnectivityCheck(this)) {
            with(userSignInModel) {
                FirebaseDatabase.getInstance()
                    .getReference(ConstantUtils.USERS)
                    .child(userOutlet)
                    .child(userMobile)
                    .setValue(userSignInModel) { error, _ ->
                        if (error == null) {
                            readFromFireBase(userSignInModel)
                        } else {
                            customProgressDialog.dismissProgressDialog()
                            Toast.makeText(
                                this@SignInActivity,
                                "Fail to create user. Please try again",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
        }
    }

    private fun readOutletToFireBase() {
        if (AppUtils.networkConnectivityCheck(this)) {
            customProgressDialog.showProgressDialog()
            val firebaseReference = FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.DETAILS)
                .child(ConstantUtils.OUTLET)

            firebaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    customProgressDialog.dismissProgressDialog()
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    val outletList = ArrayList<BusinessOutletModel>()
                    for(ds in dataSnapShot.children) {
                        outletList.add(ds.getValue(BusinessOutletModel::class.java)!!)
                    }
                    val outletName = ArrayList<String>()
                    for (element in outletList) {
                        outletName.add(element.businessOutlet)
                    }
                    setupOutletTextView(outletName)
                    customProgressDialog.dismissProgressDialog()
                }
            })
        }
    }

    private fun setupOutletTextView(outletNames : List<String>?) {
        if (outletNames != null && outletNames.isNotEmpty()) {
            val arrayAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, outletNames)
            loginOutletName.threshold = 1
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            loginOutletName.setAdapter(arrayAdapter)

            loginOutletName.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    outletName = arrayAdapter.getItem(position)!!
                    if (outletName.isNotEmpty() && outletName.isNotBlank()) {
                        val split : List<String> = outletName.split(" ")
                        for (s in split) {
                            outletCode += s[0]
                        }
                    }
                }
        }
    }

    private fun confirmationCodeAlert() {
        if (!preferenceSharedUtils.getUserConfirmationStatus()) {
            val builder = AlertDialog.Builder(this@SignInActivity)

            val title = SpannableString("Account Verification")
            title.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), 0,
                title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val message =
                SpannableString("Please enter verification code. \nContact ${preferenceSharedUtils.getOutletName()} Admin")
            message.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.white)), 0,
                message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            builder.setTitle(title)
            builder.setMessage(message)
            val codeText = EditText(this@SignInActivity)
            val codeParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            codeParams.gravity = Gravity.CENTER
            codeText.layoutParams = codeParams
            codeText.gravity = Gravity.CENTER
            codeText.hint = "Enter Verification Code"
            codeText.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_NUMBER
            codeText.background =
                ContextCompat.getDrawable(this, android.R.drawable.alert_light_frame)
            builder.setView(codeText)
            builder.setPositiveButton(
                "Verify"
            ) { dialog, _ ->
                if (preferenceSharedUtils.getSignInCode() == codeText.text.toString()) {
                    preferenceSharedUtils.setUserSignInStatus(true)
                    dialog?.dismiss()
                    startActivity(
                        android.content.Intent(
                            this@SignInActivity,
                            HomeActivity::class.java
                        )
                    )
                }
            }
            builder.setNeutralButton("Exit") { dialog, _ ->
                dialog?.dismiss()
                finishAffinity()
            }
            builder.setCancelable(false)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alertDialog.show()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.login_login_btn_id -> {
                if (AppUtils.networkConnectivityCheck(this)) {
                    val userName = loginUserName.text.toString()
                    val userMobile = loginMobile.text.toString()
                    val userId = AppUtils.fireBaseChildId(outletCode)
                    val verificationCode = AppUtils.randomNumbers().toString()
                    if (userName.isNotEmpty() && userMobile.isNotEmpty() && userMobile.length == 10
                        && outletName.isNotEmpty() && outletCode.isNotEmpty() && userId.isNotEmpty() && verificationCode.isNotEmpty()
                    ) {
                        customProgressDialog.showProgressDialog()
                        val userSignInModel =
                            UserSignInModel(
                                userId,
                                userName,
                                userMobile,
                                outletName,
                                verificationCode,
                                false, false
                            )
                        readFromFireBase(userSignInModel)
                    } else {
                        if (userName.isEmpty()) {
                            loginUserName.error = "Enter User Name"
                        }
                        if (userMobile.isEmpty() || userMobile.length < 10) {
                            loginMobile.error = "Enter Valid Number"
                        }
                        if (outletName.isEmpty()) {
                            loginOutletName.error = "Select valid Outlet"
                        }
                    }
                }
            }
        }
    }
}
