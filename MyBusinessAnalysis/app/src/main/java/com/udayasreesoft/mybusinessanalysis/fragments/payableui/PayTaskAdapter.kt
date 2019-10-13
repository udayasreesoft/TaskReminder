package com.udayasreesoft.mybusinessanalysis.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.v4.os.ConfigurationCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PayTaskAdapter(
    private val context: Context,
    private val taskList: ArrayList<TaskDataTable>,
    val taskInterface: TaskInterface
) :
    RecyclerView.Adapter<PayTaskAdapter.TaskHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): TaskHolder {
        return TaskHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_list_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return taskList.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val model = taskList[position]
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        with(model) {
            holder.nameTV.text = companyName
            if (taskCompleted) {
                holder.nameTV.setTextColor(context.resources.getColor(android.R.color.holo_orange_dark))
                if (holder.menuBtn.visibility == View.VISIBLE) {
                    holder.menuBtn.visibility = View.GONE
                }
            } else {
                holder.nameTV.setTextColor(context.resources.getColor(android.R.color.holo_green_dark))
                if (holder.menuBtn.visibility == View.GONE) {
                    holder.menuBtn.visibility = View.VISIBLE
                }
            }
            holder.daysLeft.visibility = View.GONE
            if (!taskCompleted) {
                calendar.set(Calendar.HOUR_OF_DAY, ConstantUtils.HOUR)
                calendar.set(Calendar.MINUTE, ConstantUtils.MINUTE)
                calendar.set(Calendar.SECOND, (ConstantUtils.SECOND - 1))

                val todayMillis = calendar.timeInMillis
                var diff = (Date(date.toLong()).time - Date(todayMillis).time)
                if (diff < 0) {
                    holder.daysLeft.text = "Exceed"
                    holder.daysLeft.visibility = View.VISIBLE
                } else {
                    var daysLeft = (diff / (1000 * 60 * 60 * 24))

                    if (daysLeft == 0L) {
                        holder.daysLeft.text = "Today"
                        holder.daysLeft.visibility = View.VISIBLE
                    } else if (daysLeft > 0L) {
                        calendar.timeInMillis = date
                        calendar.add(Calendar.DATE, (days * -1))

                        diff = (Date(calendar.timeInMillis).time - Date(todayMillis).time)
                        daysLeft = (diff / (1000 * 60 * 60 * 24))
                        if (daysLeft <= 0L) {
                            diff = (Date(date.toLong()).time - Date(todayMillis).time)
                            daysLeft = (diff / (1000 * 60 * 60 * 24))

                            holder.daysLeft.text = "$daysLeft day(s) left"
                            holder.daysLeft.visibility = View.VISIBLE
                        }
                    }
                }
            }

            holder.dateTV.text = simpleDateFormat.format(Date(date.toLong()))
            holder.chequeTV.text = chequeNo
            holder.amountTV.text = "Rs.${NumberFormat.getNumberInstance(
                ConfigurationCompat.getLocales(context.resources.configuration)[0]).format(amount.toInt())} /-"

            holder.menuBtn.setOnClickListener { view ->
                when (view?.id) {
                    R.id.row_task_company_menu_id -> {
                        val popup = PopupMenu(context, holder.menuBtn)
                        popup.inflate(R.menu.payable_action_menu)
                        popup.setOnMenuItemClickListener { menu ->
                            when (menu?.itemId) {
                                R.id.task_menu_payed_id -> {
                                    taskInterface.menuTaskAction(slNo, 0)
                                }

                                R.id.task_menu_modify_id -> {
                                    taskInterface.menuTaskAction(slNo, 1)
                                }

                                R.id.task_menu_delete_id -> {
                                    taskInterface.menuTaskAction(slNo, 2)
                                    removeItemAtPosition(position)
                                }
                            }
                            false
                        }
                        popup.show()
                    }
                }
            }
        }
    }


    inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV: TextView = view.findViewById(R.id.row_task_company_name_id)
        val dateTV: TextView = view.findViewById(R.id.row_task_company_date_id)
        val chequeTV: TextView = view.findViewById(R.id.row_task_company_cheque_id)
        val amountTV: TextView = view.findViewById(R.id.row_task_company_amount_id)
        val menuBtn: ImageView = view.findViewById(R.id.row_task_company_menu_id)
        val daysLeft: TextView = view.findViewById(R.id.row_task_days_left_id)

        init {
            val kendaltype = Typeface.createFromAsset(context.assets, "fonts/kendaltype.ttf")

            nameTV.typeface = Typeface.createFromAsset(context.assets, "fonts/sundaprada.ttf")
            dateTV.typeface = kendaltype
            chequeTV.typeface = kendaltype
            amountTV.typeface = kendaltype
            daysLeft.typeface = kendaltype

        }
    }

    private fun removeItemAtPosition(position: Int) {
        taskList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, taskList.size)
    }

    interface TaskInterface {
        fun menuTaskAction(slNo: Int, status: Int)
    }
}