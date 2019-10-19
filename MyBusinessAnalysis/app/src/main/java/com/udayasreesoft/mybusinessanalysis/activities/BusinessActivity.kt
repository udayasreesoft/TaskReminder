package com.udayasreesoft.mybusinessanalysis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v4.os.ConfigurationCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.AmountViewModel
import com.udayasreesoft.businesslibrary.models.BusinessViewIds
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.AmountViewAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
@SuppressLint("SetTextI18n")
class BusinessActivity : AppCompatActivity(), View.OnClickListener, AmountViewAdapter.HomeInterface {

    private lateinit var listLayout: RelativeLayout
    private lateinit var listRecyclerView: RecyclerView
    private lateinit var listEmpty: TextView
    private lateinit var addLayout: RelativeLayout
    private lateinit var listCalendar: EditText
    private lateinit var addCalendar: EditText
    private lateinit var insertBtn: ImageView
    private lateinit var includeLayout: LinearLayout
    private lateinit var saveBtn: Button
    private lateinit var addFab: FloatingActionButton

    private lateinit var netAmountText: TextView
    private lateinit var expensesAmountText: TextView
    private lateinit var grossAmountText: TextView

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private lateinit var progress: CustomProgressDialog

    private var isAddBusiness = false
    private lateinit var businessLayoutIDs: ArrayList<BusinessViewIds>

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

        netAmountText = findViewById(R.id.business_net_amount_id)
        expensesAmountText = findViewById(R.id.business_expenses_amount_id)
        grossAmountText = findViewById(R.id.business_gross_amount_id)

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

    private fun calendarViewDialog() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        val datePickerDialog: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val simpleDateFormat = SimpleDateFormat(ConstantUtils.DATE_FORMAT, Locale.US)
                if (isAddBusiness) {
                    removeAllLayouts()
                    addCalendar.setText(simpleDateFormat.format(calendar.time) ?: "")
                } else {
                    listCalendar.setText(simpleDateFormat.format(calendar.time) ?: "")
                }
                readBusinessFromFireBase()
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
        val selectedDate: String = if (isAddBusiness) {
            addCalendar.text.toString()
        } else {
            listCalendar.text.toString()
        }
        if (AppUtils.networkConnectivityCheck(this) && selectedDate.isNotEmpty()
            && AppUtils.OUTLET_NAME.isNotEmpty() && AppUtils.OUTLET_NAME != "NA"
        ) {
            progress.show()
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(AppUtils.OUTLET_NAME)
                .child(ConstantUtils.BUSINESS)
                .child(selectedDate)

            fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                    netAmountText.text = "Rs. 0/-"
                    expensesAmountText.text = "Rs. 0/-"
                    grossAmountText.text = "Rs. 0/-"
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.exists()) {
                        val businessList = ArrayList<AmountViewModel>()
                        for (element in dataSnapShot.children) {
                            businessList.add(
                                AmountViewModel(
                                    element.key ?: "", element.value.toString().toInt() ?: 0
                                )
                            )
                        }
                        if (businessList.isNotEmpty()) {
                            if (isAddBusiness) {
                                editBusinessLayoutWithList(businessList)
                            } else {
                                calculatePriceDetails(businessList)
                                listRecyclerView.visibility = View.VISIBLE
                                listEmpty.visibility = View.GONE
                                val layoutManager = GridLayoutManager(
                                    this@BusinessActivity, 2,
                                    GridLayoutManager.VERTICAL, false
                                )
                                val adapter =
                                    AmountViewAdapter(this@BusinessActivity, businessList, this@BusinessActivity)
                                listRecyclerView.layoutManager = layoutManager
                                listRecyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            if (isAddBusiness) {
                                businessLayoutIDs.clear()
                                createBusinessLayout(true)
                            } else {
                                netAmountText.text = "Rs. 0/-"
                                expensesAmountText.text = "Rs. 0/-"
                                grossAmountText.text = "Rs. 0/-"
                                listRecyclerView.visibility = View.GONE
                                listEmpty.visibility = View.VISIBLE
                            }
                        }
                        progress.dismiss()
                    } else {
                        netAmountText.text = "Rs. 0/-"
                        expensesAmountText.text = "Rs. 0/-"
                        grossAmountText.text = "Rs. 0/-"
                        listRecyclerView.visibility = View.GONE
                        listEmpty.visibility = View.VISIBLE
                        progress.dismiss()
                    }
                }
            })
        }
    }

    private fun editBusinessLayoutWithList(businessList : ArrayList<AmountViewModel>) {
        if (businessList.isNotEmpty()) {
            val model1 = businessList[0]
            for (i in 0 until businessList.size) {
                val element = businessList[i]
                if (element.title == "Expenses") {
                    businessList[0] = element
                    businessList[i] = model1
                    break
                }
            }

            for (i in 0 until businessList.size) {
                val element = businessList[i]
                if (i == 0) {
                    if (businessLayoutIDs.isNotEmpty()) {
                        val ids = businessLayoutIDs[i]
                        findViewById<EditText>(ids.amountId).setText(element.total.toString())
                    }
                } else {
                    createBusinessLayout(false)
                    if (businessLayoutIDs.isNotEmpty()) {
                        val ids = businessLayoutIDs[i]
                        findViewById<EditText>(ids.nameId).setText(element.title)
                        findViewById<EditText>(ids.amountId).setText(element.total.toString())
                    }
                }
            }
        }
    }

    private fun calculatePriceDetails(businessList: ArrayList<AmountViewModel>) {
        if (businessList.isNotEmpty()) {
            var netAmount = 0
            var expensesAmount = 0
            var grossAmount = 0
            for (element in businessList) {
                if (element.title.equals("Expenses")) {
                    expensesAmount += element.total.toInt()
                } else {
                    netAmount += element.total.toInt()
                }
            }
            grossAmount = (netAmount - expensesAmount)

            netAmountText.text =
                "Rs. ${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(resources.configuration)[0])
                    .format(netAmount)}/-"
            expensesAmountText.text =
                "Rs. ${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(resources.configuration)[0])
                    .format(expensesAmount)}/-"
            grossAmountText.text =
                "Rs. ${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(resources.configuration)[0])
                    .format(grossAmount)}/-"
        } else {
            netAmountText.text = "Rs. 0/-"
            expensesAmountText.text = "Rs. 0/-"
            grossAmountText.text = "Rs. 0/-"
        }
    }

    private fun calculateExpensesAndBusiness(businessModelList: ArrayList<AmountViewModel>) {
        if (AppUtils.networkConnectivityCheck(this) && AppUtils.OUTLET_NAME.isNotEmpty() && AppUtils.OUTLET_NAME != "NA"
        ) {
            progress.show()
            var expensesAmount = 0
            var grossAmount = 0
            var totalAmount = 0

            val fireBaseRead = FirebaseDatabase.getInstance()
                .getReference(AppUtils.OUTLET_NAME)
                .child(ConstantUtils.TOTAL_AMOUNT)

            fireBaseRead.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    grossAmount = 0
                    expensesAmount = 0
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.exists()) {
                        grossAmount = dataSnapShot.child(ConstantUtils.GROSS_AMOUNT).getValue(Int::class.java)!!
                        expensesAmount = dataSnapShot.child(ConstantUtils.EXPENSES_AMOUNT).getValue(Int::class.java)!!

                        writeBusinessFromFireBase(businessModelList, expensesAmount, grossAmount)
                    }
                }
            })
        }
    }

    private fun writeBusinessFromFireBase(businessModelList: ArrayList<AmountViewModel>, expensesAmount : Int,
                                          grossAmount : Int) {
        val selectedDate: String = addCalendar.text.toString() ?: ""
        if (AppUtils.networkConnectivityCheck(this) && selectedDate.isNotEmpty()
            && AppUtils.OUTLET_NAME.isNotEmpty() && AppUtils.OUTLET_NAME != "NA"
        ) {
            progress.show()

            for (element in businessModelList) {
                when (element.title) {
                    "Expenses" -> {
                        val totalAmount = expensesAmount + element.total
                        FirebaseDatabase.getInstance()
                            .getReference(AppUtils.OUTLET_NAME)
                            .child(ConstantUtils.TOTAL_AMOUNT)
                            .child(ConstantUtils.EXPENSES_AMOUNT)
                            .setValue(totalAmount)
                    }

                    else -> {
                        val totalAmount = grossAmount + element.total
                        FirebaseDatabase.getInstance()
                            .getReference(AppUtils.OUTLET_NAME)
                            .child(ConstantUtils.TOTAL_AMOUNT)
                            .child(ConstantUtils.GROSS_AMOUNT)
                            .setValue(totalAmount)
                    }
                }
            }

            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(AppUtils.OUTLET_NAME)
                .child(ConstantUtils.BUSINESS)
                .child(selectedDate)
            for (i in 0 until businessModelList.size) {
                val model = businessModelList[i]
                fireBaseReference
                    .child(model.title)
                    .setValue(model.total) { _, _ ->
                        if (i == (businessModelList.size - 1)) {
                            progress.dismiss()
                        }
                    }
            }
        }
    }

    private fun setViewVisibility(isAdd: Boolean) {
        isAddBusiness = isAdd
        if (isAdd) {
            addCalendar.setText(AppUtils.getCurrentDate())
        } else {
            listCalendar.setText(AppUtils.getCurrentDate())
        }
        readBusinessFromFireBase()
        val animation = AnimationUtils.loadAnimation(this, R.anim.left_side)
        if (isAdd) {
            addFab.hide()
            supportActionBar?.title = "Add Business"
            addLayout.visibility = View.VISIBLE
            listLayout.visibility = View.GONE
            addLayout.animation = animation
        } else {
            addFab.show()
            if (!AppUtils.isAdminStatus) {
                addFab.hide()
            }
            supportActionBar?.title = "Business List"
            addLayout.visibility = View.GONE
            listLayout.visibility = View.VISIBLE
            listLayout.animation = animation
        }
    }

    private fun createBusinessLayout(isFirst: Boolean) {
        val parentId = View.generateViewId()
        val deleteId = View.generateViewId()
        val nameId = View.generateViewId()
        val amountId = View.generateViewId()
        val parentChildId = View.generateViewId()
        businessLayoutIDs.add(BusinessViewIds(parentId, deleteId, nameId, amountId))


        val parentLayout = RelativeLayout(this)
        parentLayout.layoutParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        parentLayout.id = parentId

        val deleteRow = ImageView(this)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_END)
        deleteRow.layoutParams = params
        deleteRow.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_delete))
        deleteRow.setBackgroundColor(Color.BLACK)
        deleteRow.id = deleteId

        val parentChildLayout = LinearLayout(this)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.BELOW, deleteId)
        layoutParams.setMargins(0, 5, 0, 0)
        parentChildLayout.layoutParams = layoutParams
        parentChildLayout.orientation = LinearLayout.HORIZONTAL
        parentChildLayout.weightSum = 2f
        parentChildLayout.id = parentChildId


        val nameTextLayout = TextInputLayout(this)
        nameTextLayout.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        val nameEditText = EditText(this)
        nameEditText.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (isFirst) {
            with(nameEditText) {
                setText("Expenses")
                setTextColor(ContextCompat.getColor(this@BusinessActivity, android.R.color.holo_red_light))
                isClickable = false
                isCursorVisible = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
        } else {
            nameEditText.inputType =
                InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_FLAG_CAP_WORDS + InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            nameEditText.hint = "Business Name"
        }
        nameEditText.id = nameId
        nameTextLayout.addView(nameEditText)

        val amountTextLayout = TextInputLayout(this)
        amountTextLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        val amountEditText = EditText(this)
        amountEditText.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        amountEditText.inputType = InputType.TYPE_CLASS_NUMBER
        amountEditText.hint = "Total Amount"
        amountEditText.id = amountId
        amountTextLayout.addView(amountEditText)

        val divider = View(this)
        val dividerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 3)
        dividerParams.addRule(RelativeLayout.BELOW, parentChildId)
        divider.layoutParams = dividerParams
        divider.setBackgroundColor(Color.BLACK)
        divider.setPadding(0, 2, 0, 2)

        parentChildLayout.addView(nameTextLayout)
        parentChildLayout.addView(amountTextLayout)

        parentLayout.addView(deleteRow)
        parentLayout.addView(parentChildLayout)
        parentLayout.addView(divider)


        includeLayout.addView(parentLayout)

        deleteRow.setOnClickListener { view ->
            if (businessLayoutIDs.size > 1) {
                for (i in 0 until businessLayoutIDs.size) {
                    val ids = businessLayoutIDs[i]
                    if (i > 0) {
                        if (view.id == ids.deleteId) {
                            includeLayout.removeView(findViewById(ids.parentId))
                            businessLayoutIDs.removeAt(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun checkForDataInView(): Boolean {
        if (businessLayoutIDs.isNotEmpty()) {
            val ids = businessLayoutIDs[businessLayoutIDs.size - 1]
            val name = findViewById<EditText>(ids.nameId).text.toString()
            val amount = findViewById<EditText>(ids.amountId).text.toString()
            if (name.isNotEmpty() && amount.isNotEmpty()) {
                return true
            }
            return false
        }
        return true
    }

    private fun saveDetailsToServer() {
        if (businessLayoutIDs.size > 0) {
            val businessModelList = ArrayList<AmountViewModel>()
            for (ids in businessLayoutIDs) {
                val name = findViewById<EditText>(ids.nameId).text.toString() ?: ""
                val amount = findViewById<EditText>(ids.amountId).text.toString() ?: ""
                if (name.isNotEmpty() && amount.isNotEmpty()) {
                    businessModelList.add(AmountViewModel(name, amount.toInt()))
                }
            }

            removeAllLayouts()

            calculateExpensesAndBusiness(businessModelList)
        }
    }

    private fun removeAllLayouts() {
        if (businessLayoutIDs.isNotEmpty()) {
            for (ids in businessLayoutIDs) {
                includeLayout.removeView(findViewById(ids.parentId))
            }
            businessLayoutIDs.clear()
            createBusinessLayout(true)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.business_add_include_id -> {
                if (checkForDataInView()) {
                    createBusinessLayout(false)
                }
            }

            R.id.business_add_save_id -> {
                saveDetailsToServer()
            }

            R.id.business_add_fab_id -> {
                businessLayoutIDs = ArrayList()
                createBusinessLayout(true)
                setViewVisibility(true)
            }

            R.id.business_list_date_id, R.id.business_add_date_id -> {
                calendarViewDialog()
            }
        }
    }

    override fun homeListener(position: Int) {

    }

    override fun onBackPressed() {
        if (addLayout.visibility == View.VISIBLE) {
            for (ids in businessLayoutIDs) {
                includeLayout.removeView(findViewById(ids.parentId))
            }
            setViewVisibility(false)
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
            super.onBackPressed()
        }
    }
}
