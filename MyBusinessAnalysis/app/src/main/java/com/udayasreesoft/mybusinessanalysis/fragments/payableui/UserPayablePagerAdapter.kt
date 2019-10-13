package com.udayasreesoft.mybusinessanalysis.fragments.payableui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class UserPayablePagerAdapter(fm: FragmentManager?, private val tabCount : Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        var fragment : Fragment? = null
        when(position) {
            0 -> {
                fragment = PayFragment.newInstance(isPayable = false)
            }

            1-> {
                fragment = PayFragment.newInstance(isPayable = true)
            }
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {"Payable"} else {"Paid"}
    }

    override fun getCount(): Int {return tabCount}
}