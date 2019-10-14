package com.udayasreesoft.mybusinessanalysis.adapters

import android.content.Context
import android.graphics.Typeface
import android.support.v4.os.ConfigurationCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.udayasreesoft.businesslibrary.models.HomeModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.mybusinessanalysis.R
import java.text.NumberFormat

class HomeAdapter(val context : Context,val homeModelList : List<HomeModel>, val homeInterface: HomeInterface)
    : RecyclerView.Adapter<HomeAdapter.HomeHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): HomeHolder {
        return HomeHolder(LayoutInflater.from(this.context).inflate(R.layout.adapter_home_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return homeModelList.size ?: 0
    }

    override fun onBindViewHolder(holder: HomeAdapter.HomeHolder, position: Int) {
        with(homeModelList[position]) {
            holder.homeTitle.text = title
            holder.homeTotal.text = "Rs.${NumberFormat.getNumberInstance(ConfigurationCompat.getLocales(context.resources.configuration)[0]).format(total)}/-"
        }
    }

    inner class HomeHolder(view : View) : RecyclerView.ViewHolder(view) {
        val homeLayout = view.findViewById<LinearLayout>(R.id.row_home_layout_id)
        val homeTitle = view.findViewById<TextView>(R.id.row_home_title_id)
        val homeTotal = view.findViewById<TextView>(R.id.row_home_total_id)
        init {
            val kendaltype = Typeface.createFromAsset(context.assets, "fonts/kendaltype.ttf")
            homeTitle.typeface = kendaltype
            homeTotal.typeface = kendaltype
            homeLayout.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.49).toInt()
            homeLayout.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.30).toInt()
        }
    }

    interface HomeInterface {
        fun homeListener()
    }
}