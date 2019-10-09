package com.udayasreesoft.adminbusinessanalysis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navAppBar : AppBarLayout
    private lateinit var navToolbar : Toolbar

    private lateinit var fragmentContainer : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)
        initView()
    }

    private fun initView() {

        drawerLayout = findViewById(R.id.admin_home_drawer_id)
        navigationView = findViewById(R.id.admin_navigation_view)
        navAppBar = findViewById(R.id.admin_nav_appbar_home_id)
        fragmentContainer = findViewById(R.id.admin_nav_appbar_container_id)
        navToolbar = findViewById(R.id.admin_nav_appbar_toolbar_id)
        setSupportActionBar(navToolbar)

        setupNavigationDrawer()
//        setupNavigationHeader()
    }

    private fun setupNavigationDrawer() {
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, navToolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menu ->
            when(menu.itemId) {

                R.id.menu_drawable_home_admin -> {

                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
