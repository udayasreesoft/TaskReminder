package com.udayasreesoft.mybusinessanalysis.fragments


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.PaymentModel
import com.udayasreesoft.businesslibrary.models.PaymentModelMain
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.PaymentTaskAdapter
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("StaticFieldLeak")
class UserPaymentFragment : Fragment(), View.OnClickListener, PaymentTaskAdapter.TaskInterface {

    private lateinit var cardView : CardView
    private lateinit var payableLayout : FrameLayout
    private lateinit var paidLayout : FrameLayout
    private lateinit var paymentRecycler : RecyclerView
    private lateinit var paymentEmptyText : TextView
    private lateinit var payableText : TextView
    private lateinit var paidText : TextView
    private lateinit var paymentFAB : FloatingActionButton
    private lateinit var animation : Animation
    private lateinit var progress: CustomProgressDialog
    private lateinit var preferenceSharedUtils : PreferenceSharedUtils
    private lateinit var payInterface: PayInterface

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            payInterface = context as PayInterface
        } catch (e : ClassCastException){
            throw ClassCastException(context.toString().plus(" must implement PayFragment"))
        }
    }

    companion object {
        fun newInstance(isPaid: Boolean): Fragment {
            val fragment = UserPaymentFragment()
            val args = Bundle()
            args.putBoolean("payable_key", isPaid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_payment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        payableLayout = view.findViewById(R.id.frag_payment_payable_id)
        paidLayout = view.findViewById(R.id.frag_payment_paid_id)
        paymentRecycler = view.findViewById(R.id.frag_payment_recycler_id)
        paymentEmptyText = view.findViewById(R.id.frag_payment_empty_id)
        payableText = view.findViewById(R.id.frag_payment_payable_text)
        paidText = view.findViewById(R.id.frag_payment_paid_text)
        paymentFAB = view.findViewById(R.id.frag_payment_fab_id)
        cardView = view.findViewById(R.id.frag_payment_card_id)

        cardView.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.12).toInt()

        preferenceSharedUtils = PreferenceSharedUtils(context!!).getInstance()
        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Connecting to server. Please wait...")
        progress.build()

        paymentRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 /*|| dy < 0*/ && paymentFAB.isShown) {
                    paymentFAB.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    paymentFAB.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        payableLayout.setOnClickListener(this)
        paidLayout.setOnClickListener(this)
        paymentFAB.setOnClickListener(this)

        if (!AppUtils.isAdminStatus) {
            paymentFAB.hide()
        }

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

    private fun changePaymentInFireBase(paymentModelMain: PaymentModelMain?) {
        if (AppUtils.networkConnectivityCheck(context!!) && paymentModelMain != null) {
            progress.show()
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {
                FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT)
                    .child(paymentModelMain.uniqueKey)
                    .setValue(paymentModelMain)

                progress.dismiss()
            } else {
                progress.dismiss()
            }
        }
    }

    private fun deletePaymentInFireBase(result: TaskDataTable?) {
        if (AppUtils.networkConnectivityCheck(context!!) && result != null) {
            progress.show()
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA"
            ) {
                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT)
                    .child(result.uniqueKey)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            for (element in dataSnapShot.children) {
                                element.ref.removeValue()
                            }
                            TaskRepository(context!!).deleteTask(result)
                            progress.dismiss()
                        } else {
                            progress.dismiss()
                        }
                    }
                })
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
                        deletePaymentInFireBase(result)
                    }
                }
            }
        }
    }

    inner class GetTaskListAsync(private val status: Boolean) :
        AsyncTask<Void, Void, List<TaskDataTable>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            if (status) {
                animation = AnimationUtils.loadAnimation(context!!, R.anim.left_side)
            } else {
                animation = AnimationUtils.loadAnimation(context!!, R.anim.right_side)
            }
            selectedColor(status)
            progress.show()
        }

        override fun doInBackground(vararg p0: Void?): List<TaskDataTable> {
            return TaskRepository(activity).queryTask(status) as ArrayList<TaskDataTable>
        }

        override fun onPostExecute(result: List<TaskDataTable>?) {
            super.onPostExecute(result)
            if (result != null && result.isNotEmpty()) {
                paymentRecycler.visibility = View.VISIBLE
                paymentEmptyText.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                paymentRecycler.layoutManager = layoutManager

                val adapter = PaymentTaskAdapter(
                    activity!!,
                    result as ArrayList<TaskDataTable>,
                    this@UserPaymentFragment
                )
                paymentRecycler.adapter = adapter
                adapter.notifyDataSetChanged()
                paymentRecycler.startAnimation(animation)
            } else {
                paymentRecycler.visibility = View.GONE
                paymentEmptyText.visibility = View.VISIBLE
                paymentEmptyText.startAnimation(animation)
            }
            progress.dismiss()
        }
    }

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

                with(_modifyTaskDataTable!!) {
                    changePaymentInFireBase(PaymentModelMain(
                        uniqueKey,
                        PaymentModel(companyName.plus(" [$payedDate] "), amount, chequeNo, date,
                            true, days)
                    ))
                }
                _modifyTaskDataTable.taskCompleted = true
                _modifyTaskDataTable.companyName = _modifyTaskDataTable.companyName.plus(" [$payedDate]")
                TaskRepository(context!!).updateTask(_modifyTaskDataTable)
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

    private fun selectedColor(isPaid : Boolean) {
        if (isPaid) {
            paidLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            paidText.setTextColor(ContextCompat.getColor(context!!, R.color.window_background))

            payableLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.window_background))
            payableText.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
        } else {
            payableLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            payableText.setTextColor(ContextCompat.getColor(context!!, R.color.window_background))

            paidLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.window_background))
            paidText.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.frag_payment_payable_id -> {
                GetTaskListAsync(false).execute()
            }

            R.id.frag_payment_paid_id -> {
                GetTaskListAsync(true).execute()
            }

            R.id.frag_payment_fab_id -> {
                payInterface.payActionListener(-1)
            }
        }
    }

    interface PayInterface {
        fun payActionListener(slNo : Int)
    }
}
