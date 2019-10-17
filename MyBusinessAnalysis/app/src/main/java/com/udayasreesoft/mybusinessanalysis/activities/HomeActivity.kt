package com.udayasreesoft.mybusinessanalysis.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer
import com.udayasreesoft.businesslibrary.models.AmountViewModel
import com.udayasreesoft.businesslibrary.models.PaymentModelMain
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils
import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.fragments.*
import com.udayasreesoft.mybusinessanalysis.fragments.UserPaymentFragment
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskDataTable
import com.udayasreesoft.mybusinessanalysis.roomdatabase.TaskRepository

@SuppressLint("StaticFieldLeak")
class HomeActivity : AppCompatActivity(), UserPaymentFragment.PayInterface, UserHomeFragment.UserHomeInterface {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navAppBar: AppBarLayout
    private lateinit var navToolbar: Toolbar

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var displayOptions: DisplayImageOptions
    private lateinit var roundDisplayOptions : DisplayImageOptions
    private lateinit var imageLoader: ImageLoader

    private var FRAGMENT_POSITION = 0

    private lateinit var amountViewModelList : ArrayList<AmountViewModel>
    private var totalPaidSum = 0
    private var totalPayableSum = 0
    private var isOneTime = true

    private var isPaid = false
    private var isBusiness = false

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils
    private lateinit var progress : CustomProgressDialog

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
        amountViewModelList = ArrayList<AmountViewModel>()

        progress = CustomProgressDialog(this@HomeActivity).getInstance()
        progress.setMessage("Connection to server. Please wait...")
        progress.build()

        drawerLayout = findViewById(R.id.home_nav_drawer_id)
        navigationView = findViewById(R.id.home_nav_view_id)
        navAppBar = findViewById(R.id.nav_appbar_home_id)
        fragmentContainer = findViewById(R.id.nav_appbar_container_id)
        navToolbar = findViewById(R.id.nav_appbar_toolbar_id)
        navToolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(navToolbar)
        setupImageLoader()
        setupNavigationDrawer()
        setupNavigationHeader()
        if (AppUtils.networkConnectivityCheck(this)) {
            readPaymentVersionToFireBase()
        } else {
            FetchAllTaskAsync().execute()
        }
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
        roundDisplayOptions = DisplayImageOptions.Builder()
            .displayer(RoundedBitmapDisplayer(1000))
            .showImageOnLoading(R.drawable.ic_default)
            .showImageForEmptyUri(R.drawable.ic_default)
            .showImageOnFail(R.drawable.ic_default)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build()

        displayOptions = DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_default)
            .showImageForEmptyUri(R.drawable.ic_default)
            .showImageOnFail(R.drawable.ic_default)
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
        actionBarDrawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)
        actionBarDrawerToggle.syncState()

        val navMenu = navigationView.menu
        if (!AppUtils.isAdminStatus) {
            navMenu.findItem(R.id.menu_outlet_setup_client).isVisible = false
        }

        navigationView.setNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.menu_drawable_home -> {
                    FRAGMENT_POSITION = 0
                    FetchAllTaskAsync().execute()
                }

                R.id.menu_drawable_amount -> {
                    FRAGMENT_POSITION = 1
                    isPaid = false
                    fragmentLauncher()
                }

                R.id.menu_drawable_todaybusiness -> {
                    FRAGMENT_POSITION = 2
                    navToolbar.title = "Business"
                    startActivity(Intent(this, BusinessActivity::class.java))
                }

                R.id.menu_drawable_purchase -> {
                    FRAGMENT_POSITION = 3
                    fragmentLauncher()
                }

                R.id.menu_outlet_setup_client -> {
                    FRAGMENT_POSITION = 4
                    fragmentLauncher()
                }

                R.id.menu_drawable_client -> {
                    FRAGMENT_POSITION = 5
                    fragmentLauncher()
                }

                R.id.menu_drawable_users -> {
                    FRAGMENT_POSITION = 6
                    fragmentLauncher()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun fragmentLauncher() {
        clearBackStack()
        var fragment: Fragment? = null
        when (FRAGMENT_POSITION) {
            0 -> {
                navToolbar.title = "Home"
                fragment = UserHomeFragment.newInstance(amountViewModelList)
            }

            1 -> {
                navToolbar.title = "Payable/Paid"
                fragment = UserPaymentFragment.newInstance(isPaid)
            }

            2 -> {

            }

            3 -> {
                navToolbar.title = "Purchase"
                fragment = UserPurchaseFragment()
            }

            4 -> {
                navToolbar.title = "Outlet"
                fragment = UserOutletSetupFragment()
            }

            5 -> {
                navToolbar.title = "Clients"
                fragment = UserClientsFragment()
            }

            6 -> {
                navToolbar.title = "Users"
                fragment = OutletUserFragment()
            }
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_right_exit)
                .replace(R.id.nav_appbar_container_id, fragment)
                .addToBackStack(fragment::class.java.simpleName)
                .commit()
        }
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)

        val headerBanner: ImageView = headerView.findViewById(R.id.nav_header_banner)
        val headerProfile: ImageView = headerView.findViewById(R.id.nav_header_profile)

        imageLoader.displayImage(preferenceSharedUtils.getOutletBannerUrl(), headerBanner, displayOptions)
        imageLoader.displayImage(preferenceSharedUtils.getOutletLogoUrl(), headerProfile, roundDisplayOptions)

        headerView.findViewById<TextView>(R.id.nav_header_name).text = preferenceSharedUtils.getUserName()
        headerView.findViewById<TextView>(R.id.nav_header_mobile).text = preferenceSharedUtils.getMobileNumber()
        headerView.findViewById<TextView>(R.id.nav_header_outletname).text = preferenceSharedUtils.getOutletName()
    }

    inner class DataBaseInsertDeleteAsync(private val dataSnapShot: DataSnapshot)
        : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean? {
            for (element in dataSnapShot.children) {
                val mainModel = element.getValue(PaymentModelMain::class.java)
                if (mainModel != null) {
                    val paymentModel = mainModel.paymentModel
                    if (paymentModel != null) {
                        if (isOneTime) {
                            isOneTime = false
                            TaskRepository(this@HomeActivity).clearDataBase()
                        }
                        with(paymentModel) {
                            TaskRepository(this@HomeActivity).insertTask(
                                TaskDataTable(mainModel.uniqueKey, clientName, dateInMillis, payAmount,
                                    chequeNumber, payStatus, preDays)
                            )
                        }
                    }
                }
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            FetchAllTaskAsync().execute()
        }
    }

    private fun readPaymentVersionToFireBase() {
        val outletNameForDB = preferenceSharedUtils.getOutletName()
        if (AppUtils.networkConnectivityCheck(this@HomeActivity)) {
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA") {
                progress.show()
                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT_VERSION)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }

                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            val version : Double = dataSnapShot.getValue(Double::class.java)!!
                            if (version > preferenceSharedUtils.getPayVersionUpdate()?.toDouble()!!) {
                                preferenceSharedUtils.setPayVersionUpdate(version.toFloat())
                                getPayAccountDetailsFromFireBase()
                            }
                            progress.dismiss()
                        } else {
                            progress.dismiss()
                        }
                    }
                })
            }
        }
    }


    private fun getPayAccountDetailsFromFireBase() {
        if (AppUtils.networkConnectivityCheck(this@HomeActivity)) {
            val outletNameForDB = preferenceSharedUtils.getOutletName()
            if (outletNameForDB != null && outletNameForDB.isNotEmpty()
                && outletNameForDB.isNotBlank() && outletNameForDB != "NA") {
                progress.show()
                val fireBaseReference = FirebaseDatabase.getInstance()
                    .getReference(outletNameForDB)
                    .child(ConstantUtils.PAYMENT)

                fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        progress.dismiss()
                    }
                    override fun onDataChange(dataSnapShot: DataSnapshot) {
                        if (dataSnapShot.exists()) {
                            isOneTime = true
                            DataBaseInsertDeleteAsync(dataSnapShot).execute()
                        } else {
                            progress.dismiss()
                            FetchAllTaskAsync().execute()
                        }
                    }
                })
            } else {
                FetchAllTaskAsync().execute()
            }
        } else {
            FetchAllTaskAsync().execute()
        }
    }

    inner class FetchAllTaskAsync : AsyncTask<Void, Void, List<TaskDataTable>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progress.show()
        }
        override fun doInBackground(vararg params: Void?): List<TaskDataTable> {
            return TaskRepository(this@HomeActivity).queryAllTask() as ArrayList<TaskDataTable>
        }

        override fun onPostExecute(result: List<TaskDataTable>?) {
            super.onPostExecute(result)
            if (result != null && result.isNotEmpty()) {
                totalPaidSum = 0
                totalPayableSum = 0
                for (element in result.iterator()) {
                    with(element){
                        if (taskCompleted) {
                            /*TODO: PAID*/
                            totalPaidSum += amount.toInt()
                        } else {
                            /*TODO: PAYABLE*/
                            totalPayableSum += amount.toInt()
                        }
                    }
                }
            }

            amountViewModelList.clear()
            amountViewModelList.add(AmountViewModel("Payable Amount", totalPayableSum))
            amountViewModelList.add(AmountViewModel("Paid Amount", totalPaidSum))
            progress.dismiss()
            fragmentLauncher()
        }
    }

    override fun homeSelectListener(position : Int) {
        when(position) {
            0 -> {
                isPaid = false
                FRAGMENT_POSITION = 1
                fragmentLauncher()
            }

            1 -> {
                isPaid = true
                FRAGMENT_POSITION = 1
                fragmentLauncher()
            }

            else -> {

            }
        }
    }

    override fun payActionListener(slNo : Int) {
        clearBackStack()
        when(slNo) {
            -1 -> {
                startActivityForResult(
                    Intent(this, AddTaskActivity::class.java), ConstantUtils.PAY_LIST_CODE
                )
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            else -> {
                startActivityForResult(
                    Intent(this, AddTaskActivity::class.java)
                        .putExtra(ConstantUtils.TASK_SLNO, slNo), ConstantUtils.PAY_LIST_CODE
                )
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantUtils.PAY_LIST_CODE && resultCode == Activity.RESULT_OK && FRAGMENT_POSITION == 1) {
            FRAGMENT_POSITION = 1
            readPaymentVersionToFireBase()
        }
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
        if (isBusiness && FRAGMENT_POSITION == 2) {

        } else if (FRAGMENT_POSITION > 0) {
            FRAGMENT_POSITION = 0
            clearBackStack()
            FetchAllTaskAsync().execute()
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
