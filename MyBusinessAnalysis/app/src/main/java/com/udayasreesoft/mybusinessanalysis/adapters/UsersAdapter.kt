package com.udayasreesoft.mybusinessanalysis.adapters

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.udayasreesoft.businesslibrary.models.UserSignInModel
import com.udayasreesoft.mybusinessanalysis.R

class UsersAdapter(val context: Context, val userModelList : ArrayList<UserSignInModel>) : RecyclerView.Adapter<UsersAdapter.UsersHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): UsersAdapter.UsersHolder {
        return UsersHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return userModelList.size ?: 0
    }

    override fun onBindViewHolder(holder: UsersAdapter.UsersHolder, position: Int) {
        val model = userModelList[position]
        with(model) {
            if (codeVerified) {
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
                holder.status.text = "Activated"
            } else {
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                holder.status.text = "In-Activated"
            }
            holder.name.text = userName
            holder.mobile.text = userMobile
            holder.verificationCode.text = "Code - $verificationCode"
        }
    }

    class UsersHolder(view: View) : RecyclerView.ViewHolder(view) {
        val status = view.findViewById<TextView>(R.id.row_users_status_id)
        val name = view.findViewById<TextView>(R.id.row_users_name_id)
        val mobile = view.findViewById<TextView>(R.id.row_users_mobile_id)
        val verificationCode = view.findViewById<TextView>(R.id.row_users_verificationcode_id)
    }
}