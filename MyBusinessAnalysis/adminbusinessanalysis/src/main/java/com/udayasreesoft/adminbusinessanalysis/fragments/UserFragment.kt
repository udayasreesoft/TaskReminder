package com.udayasreesoft.adminbusinessanalysis.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.adminbusinessanalysis.R
import com.udayasreesoft.adminbusinessanalysis.retorfit.ApiClient
import com.udayasreesoft.adminbusinessanalysis.retorfit.ApiInterface
import com.udayasreesoft.adminbusinessanalysis.retorfit.model.PostOffice
import com.udayasreesoft.adminbusinessanalysis.retorfit.model.ZipcodeModel
import com.udayasreesoft.businesslibrary.models.BusinessOutletModel
import com.udayasreesoft.businesslibrary.models.UserSignInModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import retrofit2.Call
import retrofit2.Callback

/**
 * A simple [Fragment] subclass.
 */
class UserFragment : Fragment(), View.OnClickListener {
    private lateinit var userTitleText: TextView
    private lateinit var userNameText: EditText
    private lateinit var userMobileText: EditText
    private lateinit var userOutletText: AutoCompleteTextView
    private lateinit var userAddressText: EditText
    private lateinit var userPinCodeText: EditText
    private lateinit var userPinCodeBtn: ImageView
    private lateinit var userAdminCodeText: EditText
    private lateinit var userAddBtn: Button

    private lateinit var customProgressDialog: CustomProgressDialog

    private lateinit var outletNameList: ArrayList<String>
    private var isOutletSelected = false
    private var outletName = ""
    private var outletCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        outletNameList = ArrayList()

        userTitleText = view.findViewById(R.id.frag_user_title_id)
        userNameText = view.findViewById(R.id.frag_user_name_id)
        userMobileText = view.findViewById(R.id.frag_user_mobile_id)
        userOutletText = view.findViewById(R.id.frag_user_outlet_name_id)
        userAddressText = view.findViewById(R.id.frag_user_address_id)
        userPinCodeText = view.findViewById(R.id.frag_user_zipcode_id)
        userPinCodeBtn = view.findViewById(R.id.frag_search_zipcode_id)
        userAdminCodeText = view.findViewById(R.id.frag_user_admin_code_id)
        userAddBtn = view.findViewById(R.id.frag_user_add_btn)

        userAddBtn.setOnClickListener(this)
        userPinCodeBtn.setOnClickListener(this)

        customProgressDialog = CustomProgressDialog(context!!).getInstance()
        customProgressDialog.setProgressMessage("Connecting to Server. Please wait...")
        customProgressDialog.setUpProgressDialog()
        readOutletToFireBase()

    }

    private fun readOutletToFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!)) {
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
                    for (ds in dataSnapShot.children) {
                        outletList.add(ds.getValue(BusinessOutletModel::class.java)!!)
                    }
                    for (element in outletList) {
                        outletNameList.add(element.businessOutlet)
                    }
                    setupOutletTextView(outletNameList)
                    customProgressDialog.dismissProgressDialog()
                }
            })
        }
    }

    private fun setupOutletTextView(outletNames: List<String>?) {
        if (outletNames != null && outletNames.isNotEmpty()) {
            val arrayAdapter =
                ArrayAdapter(context!!, android.R.layout.select_dialog_item, outletNames)
            userOutletText.threshold = 1
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userOutletText.setAdapter(arrayAdapter)

            userOutletText.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    outletName = arrayAdapter.getItem(position)!!
                    isOutletSelected = true
                }
        }
    }

    private fun checkOutletName() {
        isOutletSelected = false
        for (element in outletNameList) {
            if (outletName == element) {
                isOutletSelected = true
                break
            }
        }

        if (outletName.isNotEmpty() && outletName.isNotBlank()) {
            val split: List<String> = outletName.split(" ")
            for (s in split) {
                outletCode += s[0]
            }
        }
    }

    private fun writeOutletToFireBase(
        businessOutlet: BusinessOutletModel,
        userSignInModel: UserSignInModel
    ) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.DETAILS)
                .child(ConstantUtils.OUTLET)
                .push()
                .setValue(businessOutlet) { error, _ ->
                    if (error == null) {
                        writeUserToFirebase(userSignInModel)
                    } else {
                        Toast.makeText(
                            context,
                            "Fail to create user. Please try again",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun writeUserToFirebase(userSignInModel: UserSignInModel) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            with(userSignInModel) {
                FirebaseDatabase.getInstance()
                    .getReference(ConstantUtils.USERS)
                    .child(outletName)
                    .child(userMobile)
                    .setValue(userSignInModel) { error, _ ->
                        if (error == null) {
                            userNameText.setText("")
                            userMobileText.setText("")
                            userOutletText.setText("")
                            userAddressText.setText("")
                            userPinCodeText.setText("")
                            userAdminCodeText.setText("")
                            customProgressDialog.dismissProgressDialog()
                            Toast.makeText(
                                context,
                                "Successfully Created Admin",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            customProgressDialog.dismissProgressDialog()
                            Toast.makeText(
                                context,
                                "Fail to create user. Please try again",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
        }
    }

    private fun getZipCodeAddress(zipcode: String) {
        if (zipcode.isNotEmpty() || zipcode.isNotBlank()) {
            val apiInterface = ApiClient.getZipCodeApiClient().create(ApiInterface::class.java)
            val call = apiInterface.getZipCodeAddress(zipcode)
            call.enqueue(object : Callback<ZipcodeModel> {
                override fun onFailure(call: Call<ZipcodeModel>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<ZipcodeModel>,
                    response: retrofit2.Response<ZipcodeModel>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val zipCodeModel: ZipcodeModel? = response.body()
                        if (zipCodeModel != null) {
                            val postOffice: List<PostOffice> = zipCodeModel.postOffice
                            if (postOffice.isNotEmpty()) {
                                val element = postOffice[0]
                                val address =
                                    "${element.name}, ${element.division}, ${element.state}, " +
                                            "${element.country} Zipcode - $zipcode"

                                userAddressText.setText(address)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.frag_search_zipcode_id -> {
                getZipCodeAddress(userPinCodeText.text.toString())
            }

            R.id.frag_user_add_btn -> {
                val userName = userNameText.text.toString()
                val mobile = userMobileText.text.toString()
                if (!isOutletSelected) {
                    outletName = userOutletText.text.toString()
                }
                checkOutletName()
                val address = userAddressText.text.toString()
                val pinCode = userPinCodeText.text.toString()
                val adminCode = userAdminCodeText.text.toString()
                if (userName.isNotEmpty() && mobile.isNotEmpty() && mobile.length == 10 && outletName.isNotEmpty()
                    && address.isNotEmpty() && pinCode.isNotEmpty() && adminCode.isNotEmpty() && adminCode.length == 6
                ) {
                    customProgressDialog.showProgressDialog()
                    val userSignInModel =
                        UserSignInModel(
                            userName,
                            mobile,
                            outletName,
                            "$outletName, \n$address \nPincode - $pinCode",
                            adminCode,
                            false,
                            true
                        )
                    if (isOutletSelected) {
                        writeUserToFirebase(userSignInModel)
                    } else {
                        writeOutletToFireBase(BusinessOutletModel(outletName), userSignInModel)
                    }

                } else {
                    if (userName.isEmpty() || userName.isBlank()) {
                        userNameText.error = "Enter User Name"
                    }

                    if (mobile.isEmpty() || mobile.isBlank() || mobile.length < 10) {
                        userMobileText.error = "Enter Valid Number"
                    }

                    if (outletName.isEmpty() || outletName.isBlank()) {
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
