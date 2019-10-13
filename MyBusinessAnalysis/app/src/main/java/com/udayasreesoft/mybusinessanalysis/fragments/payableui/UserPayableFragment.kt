package com.udayasreesoft.mybusinessanalysis.fragments.payableui


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.udayasreesoft.mybusinessanalysis.R

/**
 * A simple [Fragment] subclass.
 */
class UserPayableFragment : Fragment() {

    private lateinit var payableTabLayout : TabLayout
    private lateinit var payableViewPager : ViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_payable, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {

        payableTabLayout = view.findViewById(R.id.frag_payable_tablayout_id)
        payableViewPager = view.findViewById(R.id.frag_payable_viewpager_id)

        setupTabLayout()
    }

    private fun setupTabLayout() {

        payableTabLayout.addTab(payableTabLayout.newTab().setText("Payable"))
        payableTabLayout.addTab(payableTabLayout.newTab().setText("Paid"))
        payableTabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val pagerAdapter = UserPayablePagerAdapter(activity?.supportFragmentManager, payableTabLayout.tabCount)
        payableViewPager.adapter = pagerAdapter
//        payableTabLayout.setupWithViewPager(payableViewPager)
        payableViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(payableTabLayout))

        payableTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                payableViewPager.setCurrentItem(tab?.position!!, true)
            }
        })
    }

}
