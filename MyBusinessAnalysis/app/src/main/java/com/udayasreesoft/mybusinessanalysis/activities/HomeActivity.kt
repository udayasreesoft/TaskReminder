package com.udayasreesoft.mybusinessanalysis.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
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
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.fragments.*

class HomeActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navAppBar: AppBarLayout
    private lateinit var navToolbar: Toolbar

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var displayOptions: DisplayImageOptions
    private lateinit var imageLoader: ImageLoader

    private var FRAGMENT_POSITION = 0

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setupMPermissions()
        }
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
        fragmentLauncher()
    }

    private fun setupMPermissions() {
        val permissions: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionNeeded = ArrayList<String>()
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded.add(p)
            }
        }
        if (permissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionNeeded.toArray(arrayOfNulls<String>(permissionNeeded.size)),
                ConstantUtils.PERMISSION_REQUESTED
            )
        }
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
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, navToolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.menu_drawable_home -> {
                    FRAGMENT_POSITION = 0
                    fragmentLauncher()
                }

                R.id.menu_drawable_amount -> {
                    FRAGMENT_POSITION = 1
                    fragmentLauncher()
                }

                R.id.menu_drawable_todaybusiness -> {
                    FRAGMENT_POSITION = 2
                    fragmentLauncher()
                }

                R.id.menu_drawable_purchase -> {
                    FRAGMENT_POSITION = 3
                    fragmentLauncher()
                }

                R.id.menu_drawable_availablestock -> {
                    FRAGMENT_POSITION = 4
                    fragmentLauncher()
                }

                R.id.menu_outlet_setup_client -> {
                    FRAGMENT_POSITION = 5
                    fragmentLauncher()
                }

                R.id.menu_drawable_client -> {
                    FRAGMENT_POSITION = 6
                    fragmentLauncher()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun fragmentLauncher() {
        var fragment: Fragment? = null
        when (FRAGMENT_POSITION) {
            0 -> {
                fragment = UserHomeFragment()
            }

            1 -> {
                fragment = UserPayableFragment()
            }

            2 -> {
                fragment = UserBusinessFragment()
            }

            3 -> {
                fragment = UserPurchaseFragment()
            }

            4 -> {
                fragment = UserStockFragment()
            }

            5 -> {
                fragment = UserOutletSetupFragment()
            }

            6 -> {
                fragment = UserClientsFragment()
            }
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_appbar_container_id, fragment)
                .addToBackStack(fragment::class.java.simpleName)
                .commit()
        }
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)

        val headerBanner: ImageView = headerView.findViewById(R.id.nav_header_banner)
        val headerProfile: ImageView = headerView.findViewById(R.id.nav_header_profile)

        imageLoader.displayImage("", headerBanner, displayOptions)
        imageLoader.displayImage("", headerProfile, displayOptions)

        headerView.findViewById<TextView>(R.id.nav_header_name).text = preferenceSharedUtils.getUserName()
        headerView.findViewById<TextView>(R.id.nav_header_mobile).text = preferenceSharedUtils.getMobileNumber()
        headerView.findViewById<TextView>(R.id.nav_header_outletname).text = preferenceSharedUtils.getOutletName()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isAllGranted = true
        if (requestCode == ConstantUtils.PERMISSION_REQUESTED) {
            for (element in grantResults) {
                if (element != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                    break
                }
            }
            if (!isAllGranted) {
                val builder = AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Please grant all the permission to proceed further. \n The Permission requested are not harmful to your device and data.")
                    .setCancelable(false)
                    .setPositiveButton("Exit App", DialogInterface.OnClickListener { dialogInterface, _ ->
                        dialogInterface?.dismiss()
                        finishAffinity()
                    })

                builder.create().show()
            }
        }
    }

    private fun clearBackStack() {
        try {
            val fragments: Int = supportFragmentManager.backStackEntryCount
            for (i in fragments downTo 1) {
                supportFragmentManager.popBackStackImmediate()
            }
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        if (FRAGMENT_POSITION > 0) {
            FRAGMENT_POSITION = 0
            clearBackStack()
            fragmentLauncher()
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            clearBackStack()
            startActivity(intent)
            finishAffinity()
            super.onBackPressed()
        }
    }
}
