package com.udayasreesoft.mybusinessanalysis.fragments


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udayasreesoft.businesslibrary.models.SingleEntityModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R

/**
 * A simple [Fragment] subclass.
 */
class UserClientsFragment : Fragment(), View.OnClickListener {

    private lateinit var preferenceSharedUtils : PreferenceSharedUtils
    private lateinit var progress : CustomProgressDialog

    private lateinit var clientListView : ListView
    private lateinit var emptyText : TextView
    private lateinit var clientEditText : EditText
    private lateinit var clientSave : Button
    private lateinit var clientCancel : Button
    private lateinit var clientFAB : FloatingActionButton
    private lateinit var clientLayout : LinearLayout
    private var arrayAdapter : ArrayAdapter<String>? = null

    private lateinit var clientsName : ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_clients, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        clientListView = view.findViewById(R.id.frag_client_listview_id)
        clientEditText = view.findViewById(R.id.frag_client_edittext_id)
        clientSave = view.findViewById(R.id.frag_client_save_id)
        clientCancel = view.findViewById(R.id.frag_client_cancel_id)
        clientFAB = view.findViewById(R.id.frag_client_fab_id)
        clientLayout = view.findViewById(R.id.frag_client_editor_layout)
        emptyText = view.findViewById(R.id.frag_client_empty_id)

        clientSave.setOnClickListener(this)
        clientCancel.setOnClickListener(this)
        clientFAB.setOnClickListener(this)

        preferenceSharedUtils = PreferenceSharedUtils(context!!).getInstance()
        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Please wait...")
        progress.build()

        if (!AppUtils.isAdminStatus) {
            clientFAB.hide()
        }
        visibility(true)
        readClientsFromFireBase()
    }

    private fun visibility(isEmpty : Boolean) {
        if (isEmpty) {
            clientListView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            clientLayout.visibility = View.GONE
        } else {
            clientListView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            clientLayout.visibility = View.GONE
        }
    }

    private fun setUpListView() {
        clientsName = ArrayList()
        arrayAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1,
            android.R.id.text1, clientsName)
        clientListView.adapter = arrayAdapter
    }

    private fun writeClientToFireBase(client: String) {
        if (client.isNotEmpty() && AppUtils.networkConnectivityCheck(context!!)) {
            if (AppUtils.OUTLET_NAME != null && AppUtils.OUTLET_NAME.isNotEmpty()
                && AppUtils.OUTLET_NAME.isNotBlank() && AppUtils.OUTLET_NAME != "NA"
            ) {
                val model = SingleEntityModel(client)
                FirebaseDatabase.getInstance()
                    .getReference(AppUtils.OUTLET_NAME)
                    .child(ConstantUtils.CLIENT)
                    .push()
                    .setValue(model)

                clientsName.add("${clientsName.size + 2}. $client")
                arrayAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun readClientsFromFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!)) {
            if (AppUtils.OUTLET_NAME != null && AppUtils.OUTLET_NAME.isNotEmpty()
                && AppUtils.OUTLET_NAME.isNotBlank() && AppUtils.OUTLET_NAME != "NA"
            ) {
                progress.show()
                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(AppUtils.OUTLET_NAME)
                    .child(ConstantUtils.CLIENT)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            val clientList = ArrayList<SingleEntityModel>()
                            for(ds in dataSnapShot.children) {
                                clientList.add(ds.getValue(SingleEntityModel::class.java)!!)
                            }
                            for (i in 0 until clientList.size) {
                                clientsName.add(" ${i+1}. ${clientList[i].inputData}")
                            }
                            arrayAdapter?.notifyDataSetChanged()
                            visibility(false)
                            progress.dismiss()
                        } else {
                            progress.dismiss()
                        }
                    }
                })
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.frag_client_fab_id -> {
                clientFAB.hide()
                clientLayout.visibility = View.VISIBLE
            }

            R.id.frag_client_cancel_id -> {
                clientLayout.visibility = View.GONE
                clientFAB.show()
            }

            R.id.frag_client_save_id -> {
                val text = clientEditText.text.toString()
                if (text.isNotEmpty()) {
                    var isFound = false
                    for (element in clientsName) {
                        if (text.toLowerCase() == element.toLowerCase()) {
                            isFound = true
                            Toast.makeText(context!!, "Client already exist", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                    if (!isFound) {
                        writeClientToFireBase(text)
                    }
                }
            }
        }
    }

}
