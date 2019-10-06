package com.udayasreesoft.remainderapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.Typeface
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.udayasreesoft.remainderapp.R
import com.udayasreesoft.remainderapp.roomdatabase.CompanyNamesTable
import com.udayasreesoft.remainderapp.roomdatabase.TaskDataTable
import com.udayasreesoft.remainderapp.roomdatabase.TaskRepository
import com.udayasreesoft.remainderapp.utils.AppUtils
import com.udayasreesoft.remainderapp.utils.ConstantUtils
import java.text.SimpleDateFormat
import java.util.*

class ReminderTaskActivity : AppCompatActivity(), View.OnClickListener {

    private var _modifyTaskDataTable : TaskDataTable? = null
    private lateinit var progressDialog: ProgressDialog
    private var isTaskAddedStatus = false
    private val selectDays = "Select Days"

    private var _selectedDateInMills: Long = 0
    private var _companyName: String = ""
    private var _selectedDays : String = ""
    private var _chequeNo: String = ""
    private var _payableAmount: String = ""

    private lateinit var title: TextView
    private lateinit var companyName : AutoCompleteTextView
    private lateinit var selectDate : EditText
    private lateinit var taskRemindDaySpinner : Spinner
    private lateinit var taskChequeNo: EditText
    private lateinit var taskAmount: EditText
    private lateinit var addTaskBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_task)
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

        selectDate.setOnClickListener(this)
        addTaskBtn.setOnClickListener(this)
        addTaskBtn.text = "Add Task"
        setCurrentDate()
        remindTaskDaysSpinner()

        title.typeface = Typeface.createFromAsset(assets, "fonts/seasrn.ttf")
        if (!getIntentDates()) {
            GetCompanyNameTaskAsync("NA").execute()
        }
    }

    private fun getIntentDates() : Boolean {
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ConstantUtils.TASK_SLNO)) {
                MenuTaskAsync(bundle.getInt("task_slno",-1)).execute()
            }
            return true
        }
        return false
    }

    /*TODO : Fetch Reminder Task from Database with SLNO*/
    @SuppressLint("StaticFieldLeak")
    inner class MenuTaskAsync(private val slNo: Int) : AsyncTask<Void, Void, TaskDataTable>() {
        override fun doInBackground(vararg p0: Void?): TaskDataTable {
            return TaskRepository(this@ReminderTaskActivity).queryTaskBySlNo(slNo) as TaskDataTable
        }

        override fun onPostExecute(result: TaskDataTable?) {
            super.onPostExecute(result)
            if (result != null) {
                with(result) {
                    _modifyTaskDataTable = result
                    GetCompanyNameTaskAsync(companyName).execute()
                    val simpleDateFormat = java.text.SimpleDateFormat(ConstantUtils.DATE_FORMAT, java.util.Locale.US)
                    selectDate.setText(simpleDateFormat.format(Date(date.toLong())))
                    taskChequeNo.setText(chequeNo)
                    taskAmount.setText(amount)
                    taskRemindDaySpinner.setSelection(days)
                    addTaskBtn.text = "Apply Changes"
                }
            }
        }
    }

    private fun remindTaskDaysSpinner() {
        val days = arrayOf(selectDays,"1","2","3","4","5","6", "7")

        val daysAdapter = ArrayAdapter(this@ReminderTaskActivity, android.R.layout.simple_spinner_item, days)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskRemindDaySpinner.adapter = daysAdapter
        taskRemindDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapter: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                _selectedDays  = adapter?.selectedItem.toString()
            }
        }
    }

    /*TODO : Fetch Company Names from Database*/
    @SuppressLint("StaticFieldLeak")
    inner class GetCompanyNameTaskAsync(val name: String) : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg p0: Void?): List<String> {
            return TaskRepository(this@ReminderTaskActivity).queryCompanyName() as ArrayList<String>
        }

        override fun onPostExecute(companyNames: List<String>?) {
            super.onPostExecute(companyNames)
            if (companyNames != null && companyNames.isNotEmpty()) {
                val arrayAdapter =
                    ArrayAdapter(this@ReminderTaskActivity, android.R.layout.select_dialog_item, companyNames)
                companyName.threshold = 1

                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                companyName.setAdapter(arrayAdapter)

                companyName.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        _companyName = arrayAdapter.getItem(position)!!
                    }

                if (name != "NA") {
                    for (i in companyNames.indices) {
                        if (companyNames[i] == name) {
                            companyName.setText(name)
                            _companyName = name
                            break
                        }
                    }
                }
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
                AppUtils.logMessage("Date : ${simpleDateFormat.format(Date(calendar.timeInMillis))}")
            }

        val datePicker = DatePickerDialog(
            this@ReminderTaskActivity, datePickerDialog, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    /*TODO : Add reminder task to database*/
    private fun addTaskToDB(isInsert: Boolean) {
        if (_companyName != companyName.text.toString()) {
            _companyName = ""
        }
        if (_companyName.isEmpty()) {
            _companyName = companyName.text.toString()
            TaskRepository(this).insertCompanyName(CompanyNamesTable(_companyName))
        }
        _chequeNo = taskChequeNo.text.toString()
        _payableAmount = taskAmount.text.toString()

        if (_selectedDays != selectDays && _companyName.isNotEmpty() && _selectedDateInMills != 0L &&
            _chequeNo.isNotEmpty() && _payableAmount.isNotEmpty()) {
            if (isInsert) {
                TaskRepository(this).insertTask(
                    TaskDataTable(
                        _companyName, _selectedDateInMills, _payableAmount,
                        _chequeNo, false, _selectedDays.toInt()
                    )
                )
            } else {
                _modifyTaskDataTable?.companyName = _companyName
                _modifyTaskDataTable?.date = _selectedDateInMills
                _modifyTaskDataTable?.amount = _payableAmount
                _modifyTaskDataTable?.chequeNo = _chequeNo
                _modifyTaskDataTable?.days = _selectedDays.toInt()
                TaskRepository(this@ReminderTaskActivity).updateTask(_modifyTaskDataTable)
            }
            clearInputs()
            GetCompanyNameTaskAsync("NA").execute()
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
                Toast.makeText(this@ReminderTaskActivity, "Please Select days", Toast.LENGTH_SHORT).show()
            }
        }
        _companyName = ""
        dismissProgress()
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
                showProgress()
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

    private fun showProgress() {
        progressDialog = ProgressDialog(this@ReminderTaskActivity)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
    }

    private fun dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    override fun onBackPressed() {
        setResult(
            if (isTaskAddedStatus)
            {Activity.RESULT_OK} else {Activity.RESULT_CANCELED}
        )
        super.onBackPressed()
    }
}
