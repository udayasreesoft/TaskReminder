package com.udayasreesoft.mybusinessanalysis.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.udayasreesoft.businesslibrary.models.AmountViewModel
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.AmountViewAdapter

/**
 * A simple [Fragment] subclass.
 */
class UserHomeFragment : Fragment(), AmountViewAdapter.HomeInterface {

    private lateinit var homeRecyclerView : RecyclerView
    private lateinit var userHomeInterface: UserHomeInterface

    private lateinit var progress : CustomProgressDialog

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            userHomeInterface = context as UserHomeInterface
        } catch (e : ClassCastException) {
            throw ClassCastException(context.toString().plus(" must implement UserHomeFragment"))
        }
    }

    companion object {
        fun newInstance(amountViewModelList: ArrayList<AmountViewModel>) : Fragment {
            val fragment = UserHomeFragment()
            val args = Bundle()
            args.putParcelableArrayList("HOME_DETAILS", amountViewModelList)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        homeRecyclerView = view.findViewById(R.id.frag_home_recycler_id)
        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Please wait...")
        progress.build()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey("HOME_DETAILS")) {
                progress.show()
                val amountViewModelList : ArrayList<AmountViewModel>? = bundle.getParcelableArrayList("HOME_DETAILS")
                if (amountViewModelList != null && amountViewModelList.isNotEmpty()) {
                    val homeAdapter = AmountViewAdapter(context!!, amountViewModelList, this@UserHomeFragment)
                    val gridLayoutManager = GridLayoutManager(context!!, 2, GridLayoutManager.VERTICAL, false)
                    homeRecyclerView.layoutManager = gridLayoutManager
                    homeRecyclerView.adapter = homeAdapter
                    homeAdapter.notifyDataSetChanged()
                }
                progress.dismiss()
            }
        }
    }

    override fun homeListener(position : Int) {
        userHomeInterface.homeSelectListener(position)
    }

    interface UserHomeInterface {
        fun homeSelectListener(position : Int)
    }
}
