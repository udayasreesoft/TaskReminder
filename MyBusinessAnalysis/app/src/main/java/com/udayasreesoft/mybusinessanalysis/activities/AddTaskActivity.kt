package com.udayasreesoft.mybusinessanalysis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.SingleEntityModel
import com.udayasreesoft.businesslibrary.models.PaymentModel
import com.udayasreesoft.businesslibrary.models.PaymentModelMain
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("StaticFieldLeak")
class AddTaskActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private var _modifyTaskDataTable: TaskDataTable? = null
    private lateinit var progress: CustomProgressDialog
    private var isTaskAddedStatus = false
    private val selectDays = "Select Days"

    private var _selectedDateInMills: Long = 0
    private var _companyName: String = ""
    private var _selectedDays: String = ""
    private var _chequeNo: String = ""
    private var _payableAmount: String = ""
    private var isModify = false
    private var _uniqueKeys = ""
    private lateinit var clientsName: ArrayList<String>

    private lateinit var title: TextView
    private lateinit var companyName: AutoCompleteTextView
    private lateinit var selectDate: EditText
    private lateinit var taskRemindDaySpinner: Spinner
    private lateinit var taskChequeNo: EditText
    private lateinit var taskAmount: EditText
    private lateinit var addTaskBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        supportActionBar?.title = "ADD TASK"
        initView()
    }

    private fun initView() {
        title = findViewById(R.id.remainder_task_title_id)
        companyName = findViewById(R.id.remainder_task_company_id)
        taskRemindDaySpinner = findViewById(R.id.remainder_task_remind_id)
        selectDate = findViewById(R.id.remainder_task_date_id)
        taskChequeNo = findViewById(R.id.remainder_task_cheque_id)
        taskAmount = findViewById(R.id.remainder_task_amount_id)
        addTaskBtn = findViewById(R.id.remainder_task_btn_id)
        preferenceSharedUtils = PreferenceSharedUtils(this).getInstance()

        progress = CustomProgressDialog(this@AddTaskActivity).getInstance()
        progress.setMessage("Connecting to server. Please wait until process finish...")
        progress.build()

        selectDate.setOnClickListener(this)
        addTaskBtn.setOnClickListener(this)
        addTaskBtn.text = "Add Task"
        setCurrentDate()
        remindTaskDaysSpinner()

        title.typeface = Typeface.createFromAsset(assets, "fonts/seasrn.ttf")

        if (!getIntentDates()) {
            isModify = false
            readClientsFromFireBase("NA")
        }
    }

    private fun getIntentDates(): Boolean {
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ConstantUtils.TASK_SLNO)) {
                isModify = true
                MenuTaskAsync(bundle.getInt("task_slno", -1)).execute()
            }
            return true
        }
        return false
    }

    /*TODO : Fetch Reminder Task from Database with SLNO*/
    inner class MenuTaskAsync(private val slNo: Int) : AsyncTask<Void, Void, TaskDataTable>() {
        override fun doInBackground(vararg p0: Void?): TaskDataTable {
            return TaskRepository(this@AddTaskActivity).queryTaskBySlNo(slNo) as TaskDataTable
        }

        override fun onPostExecute(result: TaskDataTable?) {
            super.onPostExecute(result)
            if (result != null) {
                with(result) {
                    _modifyTaskDataTable = result
                    readClientsFromFireBase(companyName)
                    val simpleDateFormat =
                        java.text.SimpleDateFormat(ConstantUtils.DATE_FORMAT, java.util.Locale.US)
                    selectDate.setText(simpleDateFormat.format(Date(date.toLong())))
                    taskChequeNo.setText(chequeNo)
                    taskAmount.setText(amount)
                    taskRemindDaySpinner.setSelection(days)
                    addTaskBtn.text = "Apply Changes"
                    _uniqueKeys = uniqueKey
                }
            }
        }
    }

    private fun remindTaskDaysSpinner() {
        val days = arrayOf(selectDays, "1", "2", "3", "4", "5", "6", "7")

        val daysAdapter =
            ArrayAdapter(this@AddTaskActivity, android.R.layout.simple_spinner_item, days)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskRemindDaySpinner.adapter = daysAdapter
        taskRemindDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapter: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                _selectedDays = adapter?.selectedItem.toString()
            }
        }
    }

    /*TODO : Dialog for calendar view*/
    private fun calendarViewDialog() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        val datePickerDialog: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, ConstantUtils.HOUR)
                calendar.set(Calendar.MINUTE, ConstantUtils.MINUTE)
                calendar.set(Calendar.SECOND, ConstantUtils.SECOND)

                val simpleDateFormat = SimpleDateFormat(ConstantUtils.DATE_FORMAT, Locale.US)
                _selectedDateInMills = calendar.timeInMillis
                selectDate.setText(simpleDateFormat.format(Date(calendar.timeInMillis)))
            }

        val datePicker = DatePickerDialog(
            this@AddTaskActivity, datePickerDialog, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun readClientsFromFireBase(name: String) {
        if (AppUtils.networkConnectivityCheck(this@AddTaskActivity)) {
            clientsName = ArrayList()
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {
                progress.show()
                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.CLIENT)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            val clientList = ArrayList<SingleEntityModel>()
                            for (ds in dataSnapShot.children) {
                                clientList.add(ds.getValue(SingleEntityModel::class.java)!!)
                            }
                            for (element in clientList) {
                                clientsName.add(element.businessOutlet)
                            }
                            setupClientTextView(name)
                            progress.dismiss()
                        } else {
                            progress.dismiss()
                        }
                    }
                })
            }
        }
    }

    private fun setupClientTextView(name: String) {
        if (clientsName.isNotEmpty()) {
            val arrayAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, clientsName)
            companyName.threshold = 1
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            companyName.setAdapter(arrayAdapter)
            companyName.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    _companyName = arrayAdapter.getItem(position)!!
                }
            arrayAdapter.notifyDataSetChanged()
            if (name != "NA") {
                for (i in clientsName.indices) {
                    if (clientsName[i] == name) {
                        companyName.setText(name)
                        _companyName = name
                        break
                    }
                }
            }
        }
    }

    private fun writeClientToFireBase(client: String) {
        if (client.isNotEmpty() && AppUtils.networkConnectivityCheck(this@AddTaskActivity)) {
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {
                val model = SingleEntityModel(client)
                FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.CLIENT)
                    .push()
                    .setValue(model)

                clientsName.clear()
            }
        }
    }

    private fun writePaymentToFireBase(paymentModelMain: PaymentModelMain) {
        if (AppUtils.networkConnectivityCheck(this@AddTaskActivity)) {
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {
                FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT)
                    .child(paymentModelMain.uniqueKey)
                    .setValue(paymentModelMain)
                if (!isModify) {
                    writePaymentVersionToFireBase(outletNameForDB)
                }
            }
        }
    }

    private fun writePaymentVersionToFireBase(outletNameForDB: String) {
        if (AppUtils.networkConnectivityCheck(this@AddTaskActivity) && outletNameForDB.isNotEmpty()) {
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {

                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT_VERSION)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            var version : Double = dataSnapShot.getValue(Double::class.java)!!
                            version += 0.01
                            val bigDecimal = BigDecimal(version).setScale(2, BigDecimal.ROUND_HALF_UP)
                            fireBaseReference.setValue(bigDecimal.toDouble())
                        }
                        progress.dismiss()
                    }
                })
            }
        }
    }

    /*TODO : Add reminder task to database*/
    private fun addTaskToDB(isInsert: Boolean) {
        if (_companyName != companyName.text.toString()) {
            _companyName = ""
        }
        if (_companyName.isEmpty()) {
            _companyName = companyName.text.toString()
            if (_companyName.isNotEmpty()) {
                var isFound = false
                for (element in clientsName) {
                    if (_companyName.toLowerCase() == element.toLowerCase()) {
                        isFound = true
                        break
                    }
                }
                if (!isFound) {
                    writeClientToFireBase(_companyName)
                }
            }
        }
        _chequeNo = taskChequeNo.text.toString()
        _payableAmount = taskAmount.text.toString()

        if (_selectedDays != selectDays && _companyName.isNotEmpty() && _selectedDateInMills != 0L &&
            _chequeNo.isNotEmpty() && _payableAmount.isNotEmpty()
        ) {
            writePaymentToFireBase(
                PaymentModelMain(
                    if (_uniqueKeys.isNotEmpty()) {
                        _uniqueKeys
                    } else {
                        AppUtils.uniqueKey()
                    }, PaymentModel(
                        _companyName,
                        _payableAmount, _chequeNo, _selectedDateInMills, false, _selectedDays.toInt()
                    )
                )
            )
            clearInputs()

            if (clientsName.isEmpty()) {
                isModify = false
                readClientsFromFireBase("NA")
            }
        } else {
            if (_chequeNo.isEmpty() || _chequeNo.isBlank()) {
                taskChequeNo.error = "Enter Valid Cheque Number"
            }
            if (_selectedDateInMills == 0L) {
                selectDate.error = "Select Valid Date"
            }
            if (_payableAmount.isEmpty() || _payableAmount.isBlank()) {
                taskAmount.error = "Enter Valid Amount"
            }
            if (_companyName.isBlank() || _companyName.isEmpty()) {
                companyName.error = "Select Company Name"
            }
            if (_selectedDays.isBlank() || _selectedDays.isEmpty() || _selectedDays == selectDays) {
                Toast.makeText(this@AddTaskActivity, "Please Select days", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        _companyName = ""
        progress.dismiss()
        if (!isInsert) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    /*TODO : Get current time*/
    private fun setCurrentDate() {
        val simpleDateFormat = SimpleDateFormat(ConstantUtils.DATE_FORMAT, Locale.US)
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        calendar.set(Calendar.HOUR_OF_DAY, ConstantUtils.HOUR)
        calendar.set(Calendar.MINUTE, ConstantUtils.MINUTE)
        calendar.set(Calendar.SECOND, ConstantUtils.SECOND)
        _selectedDateInMills = calendar.timeInMillis
        selectDate.setText(simpleDateFormat.format(calendar.timeInMillis))
    }

    /*TODO : Clear inputs after adding the task*/
    private fun clearInputs() {
        taskChequeNo.setText("")
        taskAmount.setText("")
        companyName.setText("")
        taskRemindDaySpinner.setSelection(0)
        setCurrentDate()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.remainder_task_date_id -> {
                // calendar action
                calendarViewDialog()
            }

            R.id.remainder_task_btn_id -> {
                // add/modify task to database
                isTaskAddedStatus = true
                if (AppUtils.networkConnectivityCheck(this@AddTaskActivity)) {
                    progress.show()
                    when (addTaskBtn.text) {

                        "Apply Changes" -> {
                            if (_modifyTaskDataTable != null) {
                                addTaskToDB(false)
                            }
                        }

                        else -> {
                            addTaskToDB(true)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(
            if (isTaskAddedStatus) {
                Activity.RESULT_OK
            } else {
                Activity.RESULT_CANCELED
            }
        )
        super.onBackPressed()
    }
}
