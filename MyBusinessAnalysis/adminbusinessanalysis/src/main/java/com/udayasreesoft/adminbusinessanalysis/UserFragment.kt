package com.udayasreesoft.adminbusinessanalysis


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 */
class UserFragment : Fragment(), View.OnClickListener {

    private lateinit var userTitleText : TextView
    private lateinit var userNameText : EditText
    private lateinit var userMobileText : EditText
    private lateinit var userOutletText : EditText
    private lateinit var userAddressText : EditText
    private lateinit var userPinCodeText: EditText
    private lateinit var userAdminCodeText : EditText
    private lateinit var userAddBtn : Button

    private lateinit var customProgressDialog : CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        userTitleText = view.findViewById(R.id.frag_user_title_id)
        userNameText = view.findViewById(R.id.frag_user_name_id)
        userMobileText = view.findViewById(R.id.frag_user_mobile_id)
        userOutletText = view.findViewById(R.id.frag_user_outlet_name_id)
        userAddressText = view.findViewById(R.id.frag_user_address_id)
        userPinCodeText = view.findViewById(R.id.frag_user_zipcode_id)
        userAdminCodeText = view.findViewById(R.id.frag_user_admin_code_id)
        userAddBtn = view.findViewById(R.id.frag_user_add_btn)

        userAddBtn.setOnClickListener(this)

        customProgressDialog = CustomProgressDialog(context!!).getInstance()
        customProgressDialog.setProgressMessage("Connecting to Server. Please wait...")
        customProgressDialog.setUpProgressDialog()
    }

    private fun writeToFireBase(adminRegisterModel: AdminRegisterModel) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            with(adminRegisterModel) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference(ConstantUtils.USERS)
                    .child(adminOutlet)
                    .child(adminMobile)
                    .setValue(adminRegisterModel) { error, _ ->
                        if (error == null) {
                            userNameText.setText("")
                            userMobileText.setText("")
                            userOutletText.setText("")
                            userAddressText.setText("")
                            userPinCodeText.setText("")
                            userAdminCodeText.setText("")

                            android.widget.Toast.makeText(
                                context,
                                "Successfully Created Admin",
                                android.widget.Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            customProgressDialog.dismissProgressDialog()
                            android.widget.Toast.makeText(
                                context,
                                "Fail to create user. Please try again",
                                android.widget.Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.frag_user_add_btn -> {
                val userName = userNameText.text.toString()
                val mobile = userMobileText.text.toString()
                val outLet = userOutletText.text.toString()
                val address = userAddressText.text.toString()
                val pinCode = userPinCodeText.text.toString()
                val adminCode = userAdminCodeText.text.toString()
                if (userName.isNotEmpty() && mobile.isNotEmpty() && mobile.length == 10 && outLet.isNotEmpty()
                    && address.isNotEmpty() && pinCode.isNotEmpty() && adminCode.isNotEmpty() && adminCode.length == 6) {



                } else {
                    if (userName.isEmpty() || userName.isBlank()) {
                        userNameText.error = "Enter User Name"
                    }

                    if (mobile.isEmpty() || mobile.isBlank() || mobile.length < 10) {
                        userMobileText.error = "Enter Valid Number"
                    }

                    if (outLet.isEmpty() || outLet.isBlank()) {
                        userOutletText.error = "Enter Outlet Name"
                    }

                    if (address.isEmpty() || address.isBlank()) {
                        userAddressText.error = "Enter Address"
                    }

                    if (pinCode.isEmpty() || pinCode.isBlank()) {
                        userPinCodeText.error = "Enter Pincode"
                    }

                    if (adminCode.isEmpty() && adminCode.isBlank() && adminCode.length < 6) {
                        userAdminCodeText.error = "Enter Valid 6-digit Code"
                    }
                }
            }
        }
    }
}
