package com.udayasreesoft.mybusinessanalysis.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.os.ConfigurationCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.udayasreesoft.businesslibrary.models.AmountViewModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.mybusinessanalysis.R
import java.text.NumberFormat

@SuppressLint("SetTextI18n")
class AmountViewAdapter(val context : Context, val amountViewModelList : ArrayList<AmountViewModel>, val homeInterface: HomeInterface)
    : RecyclerView.Adapter<AmountViewAdapter.HomeHolder>() {

    init {
        val model1 = amountViewModelList[0]
        for (i in 0 until amountViewModelList.size) {
            val element = amountViewModelList[i]
            if (element.title == "Expenses") {
                amountViewModelList[0] = element
                amountViewModelList[i] = model1
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): HomeHolder {
        return HomeHolder(LayoutInflater.from(this.context).inflate(R.layout.adapter_home_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return amountViewModelList.size ?: 0
    }

    override fun onBindViewHolder(holder: AmountViewAdapter.HomeHolder, position: Int) {
        with(amountViewModelList[position]) {
            holder.homeTitle.text = title.toUpperCase()
            holder.homeTotal.text = "Rs.${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(context.resources.configuration)[0]).format(total)}/-"
            if (title == "Expenses") {
                holder.homeLayout.setBackgroundColor(ContextCompat.getColor(context!!, android.R.color.holo_red_light))
            }
        }
    }

    inner class HomeHolder(view : View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.row_home_layout_id -> {
                    homeInterface.homeListener(adapterPosition)
                }
            }
        }

        val homeLayout = view.findViewById<LinearLayout>(R.id.row_home_layout_id)
        val homeTitle = view.findViewById<TextView>(R.id.row_home_title_id)
        val homeTotal = view.findViewById<TextView>(R.id.row_home_total_id)
        init {
            val kendaltype = Typeface.createFromAsset(context.assets, "fonts/kendaltype.ttf")
            homeTitle.typeface = kendaltype
            homeTotal.typeface = kendaltype
            homeLayout.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.49).toInt()
            homeLayout.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.30).toInt()

            homeLayout.setOnClickListener(this)
        }
    }

    interface HomeInterface {
        fun homeListener(position : Int)
    }
}