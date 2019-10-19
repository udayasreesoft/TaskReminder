package com.udayasreesoft.mybusinessanalysis.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.UserSignInModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.UsersAdapter

/**
 * A simple [Fragment] subclass.
 */
class OutletUsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText : TextView
    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private lateinit var progress : CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_outlet_users, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.frag_users_recycler_id)
        emptyText = view.findViewById(R.id.frag_users_empty_id)
        preferenceSharedUtils = PreferenceSharedUtils(context!!).getInstance()
        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Connecting to server. Please wait...")
        progress.build()

        visibility(true)
        readUsersFromFireBase()
    }

    private fun readUsersFromFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!) && AppUtils.OUTLET_NAME.isNotEmpty() && AppUtils.OUTLET_NAME != "NA") {
            progress.show()
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(AppUtils.OUTLET_NAME)
                .child(ConstantUtils.USERS)

            fireBaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.exists()) {
                        val userModelList = ArrayList<UserSignInModel>()
                        for (element in dataSnapShot.children) {
                            val userModel = element.getValue(UserSignInModel::class.java)
                            if (userModel != null) {
                                userModelList.add(userModel)
                            }
                        }
                        setupRecyclerView(userModelList)
                        progress.dismiss()
                        visibility(false)
                    } else {
                        progress.dismiss()
                    }
                }
            })
        }
    }

    private fun setupRecyclerView(userModelList : ArrayList<UserSignInModel>) {
        if (userModelList.isNotEmpty()) {
            val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            val adapter = UsersAdapter(context!!, userModelList)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun visibility(isEmpty : Boolean) {
        if (isEmpty) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
    }
}
