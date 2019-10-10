package com.udayasreesoft.mybusinessanalysis.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navAppBar : AppBarLayout
    private lateinit var navToolbar : Toolbar

    private lateinit var fragmentContainer : FrameLayout
    private lateinit var displayOptions : DisplayImageOptions
    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initView()
    }

    private fun initView() {
        preferenceSharedUtils = PreferenceSharedUtils(this).getInstance()
        AppUtils.isAdminStatus = preferenceSharedUtils.getAdminStatus()
        drawerLayout = findViewById(R.id.home_nav_drawer_id)
        navigationView = findViewById(R.id.home_nav_view_id)
        navAppBar = findViewById(R.id.nav_appbar_home_id)
        fragmentContainer = findViewById(R.id.nav_appbar_container_id)
        navToolbar = findViewById(R.id.nav_appbar_toolbar_id)
        setSupportActionBar(navToolbar)
        setupImageLoader()
        setupNavigationDrawer()
        setupNavigationHeader()
    }

    private fun setupImageLoader() {
        displayOptions = DisplayImageOptions.Builder()
            .displayer(RoundedBitmapDisplayer(1000))
            .showImageOnLoading(android.R.drawable.stat_sys_download_done)
            .showImageForEmptyUri(android.R.drawable.stat_notify_error)
            .showImageOnFail(android.R.drawable.stat_notify_error)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build()

        val config = ImageLoaderConfiguration.Builder(this)
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .denyCacheImageMultipleSizesInMemory()
            .defaultDisplayImageOptions(displayOptions)
            .build()

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(config)
    }

    private fun setupNavigationDrawer() {
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, navToolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menu ->
            when(menu.itemId) {

                R.id.menu_drawable_home -> {

                }

                R.id.menu_drawable_amount -> {

                }

                R.id.menu_drawable_todaybusiness -> {

                }

                R.id.menu_drawable_purchase -> {

                }

                R.id.menu_drawable_availablestock -> {

                }

                R.id.menu_drawable_client -> {

                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)

        val headerBanner : ImageView = headerView.findViewById(R.id.nav_header_banner)
        val headerProfile : ImageView = headerView.findViewById(R.id.nav_header_profile)

        imageLoader.displayImage("", headerBanner, displayOptions)
        imageLoader.displayImage("", headerProfile, displayOptions)

        headerView.findViewById<TextView>(R.id.nav_header_name).text = preferenceSharedUtils.getUserName() ?: "NA"
        headerView.findViewById<TextView>(R.id.nav_header_mobile).text = preferenceSharedUtils.getMobileNumber() ?: "NA"
        headerView.findViewById<TextView>(R.id.nav_header_outletname).text = preferenceSharedUtils.getOutletName() ?: "NA"
    }
}
