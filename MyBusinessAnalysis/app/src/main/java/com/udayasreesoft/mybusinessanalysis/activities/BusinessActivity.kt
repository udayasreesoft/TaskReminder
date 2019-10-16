package com.udayasreesoft.mybusinessanalysis.activities

import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.BusinessModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BusinessActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var listLayout : LinearLayout
    private lateinit var listRecyclerView : RecyclerView
    private lateinit var listEmpty : TextView
    private lateinit var addLayout : LinearLayout
    private lateinit var listCalendar : EditText
    private lateinit var addCalendar : EditText
    private lateinit var insertBtn : ImageView
    private lateinit var includeLayout : LinearLayout
    private lateinit var saveBtn : Button
    private lateinit var addFab : FloatingActionButton

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private lateinit var progress: CustomProgressDialog

    private var isAddBusiness = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business)

        initView()
    }

    private fun initView() {
        listLayout = findViewById(R.id.business_list_layout_id)
        listRecyclerView = findViewById(R.id.business_list_recycler_id)
        listEmpty = findViewById(R.id.business_list_empty_id)
        addLayout = findViewById(R.id.business_add_layout_id)
        listCalendar = findViewById(R.id.business_list_date_id)
        addCalendar = findViewById(R.id.business_add_date_id)
        insertBtn = findViewById(R.id.business_add_include_id)
        includeLayout = findViewById(R.id.business_add_insert_id)
        saveBtn = findViewById(R.id.business_add_save_id)
        addFab = findViewById(R.id.business_add_fab_id)

        insertBtn.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        addFab.setOnClickListener(this)
        addCalendar.setOnClickListener(this)
        listCalendar.setOnClickListener(this)

        if (!AppUtils.isAdminStatus) {
            addFab.hide()
        }

        preferenceSharedUtils = PreferenceSharedUtils(this).getInstance()
        progress = CustomProgressDialog(this).getInstance()
        progress.setMessage("Please wait...")
        progress.build()

        setViewVisibility(false)
    }

    private fun setCurrentDate() {
        val simpleDateFormat = SimpleDateFormat(ConstantUtils.DATE_FORMAT, Locale.US)
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        calendar.set(Calendar.HOUR_OF_DAY, ConstantUtils.HOUR)
        calendar.set(Calendar.MINUTE, ConstantUtils.MINUTE)
        calendar.set(Calendar.SECOND, ConstantUtils.SECOND)
        if (isAddBusiness) {
            addCalendar.setText(simpleDateFormat.format(calendar.timeInMillis))
        } else {
            listCalendar.setText(simpleDateFormat.format(calendar.timeInMillis))
        }
    }

    private fun calendarViewDialog() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        val datePickerDialog: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                if (isAddBusiness) {
                    addCalendar.setText(simpleDateFormat.format(calendar.time) ?: "")
                } else {
                    listCalendar.setText(simpleDateFormat.format(calendar.time) ?: "")
                }
            }

        val datePicker = DatePickerDialog(
            this, datePickerDialog, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Date().time
        datePicker.show()
    }

    private fun readBusinessFromFireBase() {
        val outletNameDB: String = preferenceSharedUtils.getOutletName()!!
        val selectedDate : String = if (isAddBusiness) {addCalendar.text.toString()} else {listCalendar.text.toString()}
        if (AppUtils.networkConnectivityCheck(this) && selectedDate.isNotEmpty()
            && outletNameDB.isNotEmpty() && outletNameDB != "NA"
        ) {
            progress.show()
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(outletNameDB)
                .child(ConstantUtils.BUSINESS)
                .child(selectedDate)

            fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.exists()) {
                        val businessList = ArrayList<BusinessModel>()
                        for (element in dataSnapShot.children) {
                            businessList.add(BusinessModel(element.key, element.value.toString()))
                        }
                        if (businessList.isNotEmpty()) {
                            listRecyclerView.visibility = View.VISIBLE
                            listEmpty.visibility = View.GONE

                        } else {
                            listRecyclerView.visibility = View.GONE
                            listEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

    private fun setViewVisibility(isAdd : Boolean) {
        isAddBusiness = isAdd
        setCurrentDate()
        readBusinessFromFireBase()
        val animation = AnimationUtils.loadAnimation(this, R.anim.left_side)
        if (isAdd) {
            addFab.hide()
            addLayout.visibility = View.VISIBLE
            listLayout.visibility = View.GONE
            addLayout.animation = animation
        } else {
            addFab.show()
            addLayout.visibility = View.GONE
            listLayout.visibility = View.VISIBLE
            listLayout.animation = animation
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.business_add_include_id -> {

            }

            R.id.business_add_save_id -> {

            }

            R.id.business_add_fab_id -> {
                setViewVisibility(true)
            }

            R.id.business_list_date_id, R.id.business_add_date_id -> {
                calendarViewDialog()
            }
        }
    }

    override fun onBackPressed() {
        if (addLayout.visibility == View.VISIBLE) {
            setViewVisibility(false)
        } else {
            super.onBackPressed()
            finish()
        }
    }
}
