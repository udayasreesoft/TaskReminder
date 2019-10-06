package com.udayasreesoft.remainderapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Point
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.os.ConfigurationCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.udayasreesoft.remainderapp.R
import com.udayasreesoft.remainderapp.adapters.TaskAdapter
import com.udayasreesoft.remainderapp.roomdatabase.TaskDataTable
import java.text.SimpleDateFormat
import java.util.*
import com.udayasreesoft.remainderapp.roomdatabase.TaskRepository
import com.udayasreesoft.remainderapp.utils.AppUtils
import com.udayasreesoft.remainderapp.utils.ConstantUtils
import java.text.NumberFormat
import kotlin.collections.ArrayList


class ReminderListActivity : AppCompatActivity(), View.OnClickListener,
    TaskAdapter.TaskInterface {

    private lateinit var remainderRecyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var paymentTotalText : TextView
    private lateinit var progressBar : ProgressBar
    private var clickStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remainder_list)
        initView()
    }

    /*TODO : init view*/
    private fun initView() {
        remainderRecyclerView = findViewById(R.id.remainder_list_recycler_id)
        emptyTextView = findViewById(R.id.remainder_lsit_empty_id)
        fabAddTask = findViewById(R.id.remainder_fab_id)
        fabAddTask.setOnClickListener(this)
        paymentTotalText = findViewById(R.id.remainder_total_text_id)
        progressBar = findViewById(R.id.remainder_progressbar_id)
        progressBar.visibility = View.GONE
        remainderRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 /*|| dy < 0*/ && fabAddTask.isShown) {
                    fabAddTask.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    fabAddTask.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        GetTaskListAsync(false).execute()
    }

    override fun menuTaskAction(slNo: Int, status: Int) {
        when (status) {
            1 -> {
                startActivityForResult(
                    Intent(this, ReminderTaskActivity::class.java)
                        .putExtra(ConstantUtils.TASK_SLNO, slNo),
                    ConstantUtils.REMINDER_LIST_CODE
                )
            }
            else -> {
                MenuTaskAsync(slNo, status).execute()
            }
        }
    }

    /*TODO : Fetch Reminder Task from Database with SLNO*/
    @SuppressLint("StaticFieldLeak")
    inner class MenuTaskAsync(private val slNo: Int, private val status: Int) : AsyncTask<Void, Void, TaskDataTable>() {
        override fun doInBackground(vararg p0: Void?): TaskDataTable {
            return TaskRepository(this@ReminderListActivity).queryTaskBySlNo(slNo) as TaskDataTable
        }

        override fun onPostExecute(result: TaskDataTable?) {
            super.onPostExecute(result)
            if (result != null) {
                when (status) {
                    0 -> {
                        // payed
                        calendarViewDialog(result)
                    }

                    2 -> {
                        // delete
                        TaskRepository(this@ReminderListActivity).deleteTask(result)
                    }
                }
            }
        }
    }

    private fun sumOfTotal(result : List<TaskDataTable>?, status : Boolean) {
        if (result != null) {
            var totalSum : Int = 0
            for (table in result.iterator()) {
                with(table) {
                    totalSum += amount.toInt()
                }
            }
            if (status) {
                paymentTotalText.text = "Payed Amount of Rs. ${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(resources.configuration)[0]).format(totalSum)} /-"
            } else {
                paymentTotalText.text = "Payable Amount of Rs. ${NumberFormat.getNumberInstance(
                    ConfigurationCompat.getLocales(resources.configuration)[0]).format(totalSum)} /-"
            }
        }
    }

    /*TODO : Get Reminder Task from Database*/
    @SuppressLint("StaticFieldLeak")
    inner class GetTaskListAsync(private val status: Boolean) : AsyncTask<Void, Void, List<TaskDataTable>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            clickStatus = status
            progressBar.visibility = View.VISIBLE
            if (status) {
                supportActionBar?.title = "Payed List"
            } else {
                supportActionBar?.title = "Payable List"
            }
        }

        override fun doInBackground(vararg p0: Void?): List<TaskDataTable> {
            return TaskRepository(this@ReminderListActivity).queryTask(status) as ArrayList<TaskDataTable>
        }

        override fun onPostExecute(result: List<TaskDataTable>?) {
            super.onPostExecute(result)
            if (result != null && result.isNotEmpty()) {
                remainderRecyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
                val layoutManager = LinearLayoutManager(this@ReminderListActivity, LinearLayoutManager.VERTICAL, false)
                remainderRecyclerView.layoutManager = layoutManager
                sumOfTotal(result, status)
                val taskInterface: TaskAdapter.TaskInterface = this@ReminderListActivity
                val adapter = TaskAdapter(
                    this@ReminderListActivity,
                    result as ArrayList<TaskDataTable>,
                    taskInterface
                )
                remainderRecyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                emptyTextView.visibility = View.VISIBLE
                remainderRecyclerView.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
        }
    }

    /*TODO : Dialog for calendar view*/
    private fun calendarViewDialog(_modifyTaskDataTable: TaskDataTable?) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        val datePickerDialog: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val payedDate = simpleDateFormat.format(calendar.time)
                _modifyTaskDataTable?.taskCompleted = true
                _modifyTaskDataTable?.companyName = _modifyTaskDataTable?.companyName.plus(" [$payedDate]")
                TaskRepository(this@ReminderListActivity).updateTask(_modifyTaskDataTable)
                GetTaskListAsync(false).execute()
            }

        val datePicker = DatePickerDialog(
            this@ReminderListActivity, datePickerDialog, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Date().time
        datePicker.show()
    }

    /*TODO : Option menu created*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflaters = menuInflater
        inflaters.inflate(R.menu.list_task_menu, menu)
        return true
    }

    /*TODO : Option menu listener*/
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.task_list_payable_id -> {
                if (clickStatus) {
                    GetTaskListAsync(false).execute()
                }
            }

            R.id.task_list_payed_id -> {
                if (!clickStatus) {
                    GetTaskListAsync(true).execute()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*TODO : OnClick Views listener*/
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.remainder_fab_id -> {
                // show add task layout
                startActivityForResult(
                    Intent(this@ReminderListActivity, ReminderTaskActivity::class.java),
                    ConstantUtils.REMINDER_LIST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantUtils.REMINDER_LIST_CODE && resultCode == Activity.RESULT_OK) {
            GetTaskListAsync(false).execute()
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Do you want to Exit ?")
        builder.setPositiveButton("Exit") { dialog, _ ->
            dialog?.dismiss()
            super.onBackPressed()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> {dialog?.dismiss()}}
        builder.create().show()
    }
}
