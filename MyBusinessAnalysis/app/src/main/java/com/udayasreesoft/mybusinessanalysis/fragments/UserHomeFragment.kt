package com.udayasreesoft.mybusinessanalysis.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.udayasreesoft.businesslibrary.models.HomeModel
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.adapters.HomeAdapter

/**
 * A simple [Fragment] subclass.
 */
class UserHomeFragment : Fragment(), HomeAdapter.HomeInterface {

    private lateinit var homeRecyclerView : RecyclerView

    companion object {
        fun newInstance(homeModelList: ArrayList<HomeModel>) : Fragment {
            val fragment = UserHomeFragment()
            val args = Bundle()
            args.putParcelableArrayList("HOME_DETAILS", homeModelList)
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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey("HOME_DETAILS")) {
                val homeModelList : ArrayList<HomeModel>? = bundle.getParcelableArrayList("HOME_DETAILS")
                if (homeModelList != null && homeModelList.isNotEmpty()) {
                    val homeAdapter = HomeAdapter(context!!, homeModelList, this@UserHomeFragment)
                    val gridLayoutManager = GridLayoutManager(context!!, 2, GridLayoutManager.VERTICAL, false)
                    homeRecyclerView.layoutManager = gridLayoutManager
                    homeRecyclerView.adapter = homeAdapter
                    homeAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun homeListener() {

    }
}
