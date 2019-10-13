package com.udayasreesoft.mybusinessanalysis.fragments.payableui


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog

import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.PayTaskAdapter
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
@SuppressLint("StaticFieldLeak")
class PayFragment : Fragment(), View.OnClickListener, PayTaskAdapter.TaskInterface {

    private lateinit var payRecyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var fabAddTask : FloatingActionButton
    private lateinit var progress: CustomProgressDialog

    private lateinit var payInterface: PayInterface
    private var clickStatus = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            payInterface = context as PayInterface
        } catch (e : ClassCastException){
            throw ClassCastException(context.toString().plus(" must implement PayFragment"))
        }
    }

    companion object {
        fun newInstance(isPayable: Boolean): Fragment {
            val fragment = PayFragment()
            val args = Bundle()
            args.putBoolean("payable_key", isPayable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pay, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        payRecyclerView = view.findViewById(R.id.frag_pay_list_recycler_id)
        emptyTextView = view.findViewById(R.id.frag_pay_list_empty_id)
        fabAddTask = view.findViewById(R.id.frag_pay_fab_id)

        fabAddTask.setOnClickListener(this)

        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Please wait...")
        progress.build()

        payRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 /*|| dy < 0*/ && fabAddTask.isShown) {
                    fabAddTask.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    fabAddTask.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        val args: Bundle? = arguments
        if (args != null) {
            if (args.containsKey("payable_key")) {
                GetTaskListAsync(args.getBoolean("payable_key")).execute()
            }
        }
    }

    override fun menuTaskAction(slNo: Int, status: Int) {
        when (status) {
            1 -> {
                payInterface.payActionListener(slNo)
            }
            else -> {
                MenuTaskAsync(slNo, status).execute()
            }
        }
    }

    inner class MenuTaskAsync(private val slNo: Int, private val status: Int) :
        AsyncTask<Void, Void, TaskDataTable>() {
        override fun doInBackground(vararg p0: Void?): TaskDataTable {
            return TaskRepository(activity).queryTaskBySlNo(slNo) as TaskDataTable
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
                        TaskRepository(activity).deleteTask(result)
                    }
                }
            }
        }
    }

    inner class GetTaskListAsync(private val status: Boolean) :
        AsyncTask<Void, Void, List<TaskDataTable>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            clickStatus = status
            progress.show()
        }

        override fun doInBackground(vararg p0: Void?): List<TaskDataTable> {
            return TaskRepository(activity).queryTask(status) as ArrayList<TaskDataTable>
        }

        override fun onPostExecute(result: List<TaskDataTable>?) {
            super.onPostExecute(result)
            if (result != null && result.isNotEmpty()) {
                payRecyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                payRecyclerView.layoutManager = layoutManager

                val adapter = PayTaskAdapter(
                    context!!,
                    result as ArrayList<TaskDataTable>,
                    this@PayFragment
                )
                payRecyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                emptyTextView.visibility = View.VISIBLE
                payRecyclerView.visibility = View.GONE
            }
            progress.dismiss()
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
                _modifyTaskDataTable?.companyName =
                    _modifyTaskDataTable?.companyName.plus(" [$payedDate]")
                TaskRepository(activity).updateTask(_modifyTaskDataTable)
                GetTaskListAsync(false).execute()
            }

        val datePicker = DatePickerDialog(
            context!!, datePickerDialog, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Date().time
        datePicker.show()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.frag_pay_fab_id -> {
                payInterface.payActionListener(-1)
            }
        }
    }

    interface PayInterface {
        fun payActionListener(slNo : Int)
    }
}
