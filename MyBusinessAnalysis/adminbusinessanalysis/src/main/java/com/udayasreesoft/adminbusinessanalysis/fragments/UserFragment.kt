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
import com.udayasreesoft.businesslibrary.models.SingleEntityModel
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
    private lateinit var userAddressText: AutoCompleteTextView
    private lateinit var userPinCodeBtn: ImageView
    private lateinit var userAdminCodeText: EditText
    private lateinit var userAddBtn: Button

    private lateinit var progress: CustomProgressDialog

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
        userAddressText = view.findViewById(R.id.frag_user_zipcode_id)
        userPinCodeBtn = view.findViewById(R.id.frag_search_zipcode_id)
        userAdminCodeText = view.findViewById(R.id.frag_user_admin_code_id)
        userAddBtn = view.findViewById(R.id.frag_user_add_btn)

        userAddBtn.setOnClickListener(this)
        userPinCodeBtn.setOnClickListener(this)

        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Connection to server. Please wait until process finish...")
        progress.build()
        readOutletToFireBase()

    }

    private fun readOutletToFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            progress.show()
            val firebaseReference = FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.ADMIN)
                .child(ConstantUtils.OUTLET)

            firebaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    val outletList = ArrayList<SingleEntityModel>()
                    for (ds in dataSnapShot.children) {
                        outletList.add(ds.getValue(SingleEntityModel::class.java)!!)
                    }
                    for (element in outletList) {
                        outletNameList.add(element.inputData)
                    }
                    setupOutletTextView(outletNameList)
                    progress.dismiss()
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
        singleEntity: SingleEntityModel,
        userSignInModel: UserSignInModel
    ) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.ADMIN)
                .child(ConstantUtils.OUTLET)
                .push()
                .setValue(singleEntity) { error, _ ->
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

    private fun readFromFireBase(userSignInModel: UserSignInModel) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(userSignInModel.userOutlet)
                .child(ConstantUtils.USERS)
                .child(userSignInModel.userMobile)

            fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                }

                override fun onDataChange(snapShot: DataSnapshot) {
                    if (snapShot.exists()) {
                        val model = snapShot.getValue(UserSignInModel::class.java)
                        if (model != null) {
                            userNameText.setText("")
                            userMobileText.setText("")
                            userOutletText.setText("")
                            userAddressText.setText("")
                            userAdminCodeText.setText("")
                            progress.dismiss()
                            Toast.makeText(
                                context,
                                "Admin details already exist",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        writeUserToFirebase(userSignInModel)
                    }
                }
            })
        }
    }

    private fun writeUserToFirebase(userSignInModel: UserSignInModel) {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            with(userSignInModel) {
                FirebaseDatabase.getInstance()
                    .getReference(userOutlet)
                    .child(ConstantUtils.USERS)
                    .child(userMobile)
                    .setValue(userSignInModel) { error, _ ->
                        if (error == null) {
                            FirebaseDatabase.getInstance()
                                .getReference(userSignInModel.userOutlet)
                                .child(ConstantUtils.TOTAL_AMOUNT)
                                .child(ConstantUtils.PAYMENT_VERSION)
                                .setValue(0.0)

                            FirebaseDatabase.getInstance()
                                .getReference(userSignInModel.userOutlet)
                                .child(ConstantUtils.TOTAL_AMOUNT)
                                .child(ConstantUtils.PAYABLE_AMOUNT)
                                .setValue(0)

                            FirebaseDatabase.getInstance()
                                .getReference(userSignInModel.userOutlet)
                                .child(ConstantUtils.TOTAL_AMOUNT)
                                .child(ConstantUtils.PAID_AMOUNT)
                                .setValue(0)

                            FirebaseDatabase.getInstance()
                                .getReference(userSignInModel.userOutlet)
                                .child(ConstantUtils.TOTAL_AMOUNT)
                                .child(ConstantUtils.EXPENSES_AMOUNT)
                                .setValue(0)

                            FirebaseDatabase.getInstance()
                                .getReference(userSignInModel.userOutlet)
                                .child(ConstantUtils.TOTAL_AMOUNT)
                                .child(ConstantUtils.GROSS_AMOUNT)
                                .setValue(0)

                            userNameText.setText("")
                            userMobileText.setText("")
                            userOutletText.setText("")
                            userAddressText.setText("")
                            userAdminCodeText.setText("")
                            progress.dismiss()
                            Toast.makeText(
                                context,
                                "Successfully Created Admin",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            progress.dismiss()
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
        if (AppUtils.networkConnectivityCheck(context!!) && zipcode.isNotEmpty() || zipcode.isNotBlank()) {
            progress.show()
            val apiInterface = ApiClient.getZipCodeApiClient().create(ApiInterface::class.java)
            val call = apiInterface.getZipCodeAddress(zipcode)
            call.enqueue(object : Callback<ZipcodeModel> {
                override fun onFailure(call: Call<ZipcodeModel>, t: Throwable) {
                    addressFunc()
                }

                override fun onResponse(
                    call: Call<ZipcodeModel>,
                    response: retrofit2.Response<ZipcodeModel>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val zipCodeModel: ZipcodeModel? = response.body()
                        if (zipCodeModel != null && zipCodeModel.status == "Success") {
                            val postOffice: List<PostOffice> = zipCodeModel.postOffice
                            if (postOffice.isNotEmpty()) {
                                addressFunc()
                                val addressList = ArrayList<String>()
                                for (element in postOffice) {
                                    with(element) {
                                        addressList.add("$name, $division,\n$state, $country,\npincode - $zipcode")
                                    }
                                }

                                if (addressList.isNotEmpty()) {
                                    setupAddressTextView(addressList)
                                }
                            }
                        } else {
                            addressFunc()
                        }
                    }
                }
            })
        }
    }

    private fun setupAddressTextView(addressList : List<String>?) {
        if (addressList != null && addressList.isNotEmpty()) {
            val arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, android.R.id.text1, addressList)
            userAddressText.threshold = 0
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            userAddressText.setAdapter(arrayAdapter)
            userAddressText.showDropDown()
            userAddressText.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    userAddressText.setText(arrayAdapter.getItem(position)!!)
                }
        }
    }

    private fun addressFunc() {
        with(userAddressText) {
            setSingleLine(false)
//            imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            maxLines = 5
            isVerticalScrollBarEnabled = true
            movementMethod = android.text.method.ScrollingMovementMethod.getInstance()
            scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_INSET
            gravity = android.view.Gravity.TOP + android.view.Gravity.START
            inputType = android.text.InputType.TYPE_CLASS_TEXT + android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS + android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText("")
            hint = "Outlet Address"
            isFocusable = true
        }
        userPinCodeBtn.visibility = View.GONE
        progress.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.frag_search_zipcode_id -> {
                if (userPinCodeBtn.visibility == View.VISIBLE) {
                    getZipCodeAddress(userAddressText.text.toString())
                }
            }

            R.id.frag_user_add_btn -> {
                val userName = userNameText.text.toString()
                val mobile = userMobileText.text.toString()
                if (!isOutletSelected) {
                    outletName = userOutletText.text.toString()
                }
                checkOutletName()
                val address = userAddressText.text.toString()
                val adminCode = userAdminCodeText.text.toString()
                if (userName.isNotEmpty() && mobile.isNotEmpty() && mobile.length == 10 && outletName.isNotEmpty()
                    && address.isNotEmpty() && adminCode.isNotEmpty() && adminCode.length == 6
                ) {
                    progress.show()
                    val userSignInModel =
                        UserSignInModel(
                            AppUtils.fireBaseChildId(outletCode),
                            userName,
                            mobile,
                            outletName,
                            adminCode,
                            false,
                            true
                        )
                    if (isOutletSelected) {
                        writeUserToFirebase(userSignInModel)
                    } else {
                        writeOutletToFireBase(
                            SingleEntityModel(
                                outletName
                            ), userSignInModel)
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

                    if (adminCode.isEmpty() && adminCode.isBlank() && adminCode.length < 6) {
                        userAdminCodeText.error = "Enter Valid 6-digit Code"
                    }
                }
            }
        }
    }
}
