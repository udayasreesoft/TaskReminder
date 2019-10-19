package com.udayasreesoft.mybusinessanalysis.fragments


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.udayasreesoft.adminbusinessanalysis.retorfit.ApiClient
import com.udayasreesoft.adminbusinessanalysis.retorfit.ApiInterface
import com.udayasreesoft.businesslibrary.models.CompanyModel
import com.udayasreesoft.businesslibrary.utils.AppUtils
import com.udayasreesoft.businesslibrary.utils.ConstantUtils
import com.udayasreesoft.businesslibrary.utils.CustomProgressDialog
import com.udayasreesoft.businesslibrary.utils.PreferenceSharedUtils

import com.udayasreesoft.mybusinessanalysis.R
import com.udayasreesoft.mybusinessanalysis.retorfit.model.PostOffice
import com.udayasreesoft.mybusinessanalysis.retorfit.model.ZipcodeModel
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import kotlin.collections.ArrayList


class UserOutletSetupFragment : Fragment(), View.OnClickListener {

    private lateinit var outletTitle: TextView
    private lateinit var outletName: EditText
    private lateinit var outletContact: EditText
    private lateinit var outletAddress: AutoCompleteTextView
    private lateinit var outletZipcode: EditText
    private lateinit var outletSearch: ImageView
    private lateinit var outletLogoImage: ImageView
    private lateinit var outletBannerEdit: ImageView
    private lateinit var outletBannerImage: ImageView
    private lateinit var outletSave: Button

    private lateinit var progress: CustomProgressDialog
    private lateinit var displayOptions: DisplayImageOptions
    private lateinit var roundDisplayOption: DisplayImageOptions
    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils

    private var isLogoImage = false
    private var serverLogoUrl = ""
    private var serverBannerUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_outletsetup, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        outletTitle = view.findViewById(R.id.frag_setup_title_id)
        outletName = view.findViewById(R.id.frag_setup_name_id)
        outletContact = view.findViewById(R.id.frag_setup_contact_id)
        outletAddress = view.findViewById(R.id.frag_setup_address_id)
        outletZipcode = view.findViewById(R.id.frag_setup_zipcode_id)
        outletSearch = view.findViewById(R.id.frag_setup_search_id)
        outletLogoImage = view.findViewById(R.id.frag_setup_logo_id)
        outletBannerEdit = view.findViewById(R.id.frag_setup_edit_banner_id)
        outletBannerImage = view.findViewById(R.id.frag_setup_banner_id)
        outletSave = view.findViewById(R.id.frag_setup_save_btn_id)

        outletBannerImage.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.50).toInt()

        outletLogoImage.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.40).toInt()
        outletLogoImage.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.40).toInt()

        outletSave.setOnClickListener(this)
        outletSearch.setOnClickListener(this)
        outletLogoImage.setOnClickListener(this)
        outletBannerEdit.setOnClickListener(this)

        preferenceSharedUtils = PreferenceSharedUtils(context!!).getInstance()
        progress = CustomProgressDialog(context!!).getInstance()
        progress.setMessage("Connection to server. Please wait until process finish...")
        progress.build()

        setupImageLoader()
        readOutletDetailsFromFireBase()
    }

    private fun setupImageLoader() {
        roundDisplayOption = DisplayImageOptions.Builder()
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
            .showImageOnLoading(android.R.drawable.stat_sys_download_done)
            .showImageForEmptyUri(android.R.drawable.stat_notify_error)
            .showImageOnFail(android.R.drawable.stat_notify_error)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build()

        val config = ImageLoaderConfiguration.Builder(context)
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .denyCacheImageMultipleSizesInMemory()
            .defaultDisplayImageOptions(displayOptions)
            .build()

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(config)
    }

    private fun getZipCodeAddress(zipcode: String) {
        if (AppUtils.networkConnectivityCheck(context!!) && zipcode.isNotEmpty() || zipcode.isNotBlank()) {
            progress.show()
            val apiInterface = ApiClient.getZipCodeApiClient().create(ApiInterface::class.java)
            val call = apiInterface.getZipCodeAddress(zipcode)
            call.enqueue(object : Callback<ZipcodeModel> {
                override fun onFailure(call: Call<ZipcodeModel>, t: Throwable) {
                    addressFunc()
                }

                override fun onResponse(
                    call: Call<ZipcodeModel>,
                    response: retrofit2.Response<ZipcodeModel>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val zipCodeModel: ZipcodeModel? = response.body()
                        if (zipCodeModel != null && zipCodeModel.status == "Success") {
                            val postOffice: List<PostOffice> = zipCodeModel.postOffice
                            if (postOffice.isNotEmpty()) {
                                addressFunc()
                                val addressList = ArrayList<String>()
                                for (element in postOffice) {
                                    with(element) {
                                        addressList.add("$name, $division,\n$state, $country,\npincode - $zipcode")
                                    }
                                }

                                if (addressList.isNotEmpty()) {
                                    setupAddressTextView(addressList)
                                }
                            }
                        } else {
                            addressFunc()
                        }
                    }
                }
            })
        }
    }

    private fun setupAddressTextView(addressList: List<String>?) {
        if (addressList != null && addressList.isNotEmpty()) {
            val arrayAdapter = ArrayAdapter(
                context!!,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                addressList
            )
            outletAddress.threshold = 1
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            outletAddress.setAdapter(arrayAdapter)
            outletAddress.showDropDown()
            outletAddress.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    outletAddress.setText(arrayAdapter.getItem(position)!!)
                }
        }
    }

    private fun addressFunc() {
        with(outletAddress) {
            setSingleLine(false)
//            imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            maxLines = 5
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod.getInstance()
            scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            gravity = Gravity.TOP + Gravity.START
            inputType =
                InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_FLAG_CAP_WORDS + InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText("")
            hint = "Outlet Address"
            isFocusable = true
        }
        progress.dismiss()
    }

    private fun setupGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(
            Intent.createChooser(galleryIntent, "Select Image"),
            ConstantUtils.PERMISSION_GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == ConstantUtils.PERMISSION_GALLERY && resultCode == Activity.RESULT_OK && data != null && AppUtils.networkConnectivityCheck(
                    context!!
                )
            ) {
                val selectedImage = data.data

                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor =
                    context?.contentResolver?.query(
                        selectedImage ?: Uri.parse(""),
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                cursor?.moveToFirst()

                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val imagePath = cursor?.getString(columnIndex ?: 0)
                cursor?.close()

                if (isLogoImage) {
                    storeImageToFireBase("file://$imagePath", outletLogoImage)
                } else {
                    storeImageToFireBase("file://$imagePath", outletBannerImage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun storeImageToFireBase(url: String, imageView: ImageView) {
        if (url.isNotEmpty() && AppUtils.networkConnectivityCheck(context!!)) {

            imageLoader.displayImage(
                url, imageView, if (isLogoImage) {
                    roundDisplayOption
                } else {
                    displayOptions
                }, object : ImageLoadingListener {
                    override fun onLoadingComplete(
                        imageUri: String?,
                        view: View?,
                        loadedImage: Bitmap?
                    ) {
                        if (AppUtils.OUTLET_NAME.isNotEmpty() && AppUtils.OUTLET_NAME.isNotBlank() && AppUtils.OUTLET_NAME != "NA") {
                            progress.show()
                            val ext = url.substring(url.lastIndexOf("."))
                            val storageReference: StorageReference = FirebaseStorage.getInstance()
                                .getReference(AppUtils.OUTLET_NAME.plus("/"))
                                .child(ConstantUtils.OUTLET_PROFILE)
                                .child(
                                    "${AppUtils.OUTLET_NAME}_${if (isLogoImage) {
                                        "Logo"
                                    } else {
                                        "Banner"
                                    }}$ext"
                                )

                            storageReference.putFile(Uri.parse(url))
                                .addOnSuccessListener { taskSnapShot ->
                                    progress.dismiss()
                                    if (isLogoImage) {
                                        serverLogoUrl =
                                            taskSnapShot.metadata?.reference?.downloadUrl.toString()
                                    } else {
                                        serverBannerUrl =
                                            taskSnapShot.metadata?.reference?.downloadUrl.toString()
                                    }
                                }

                                .addOnFailureListener {
                                    progress.dismiss()
                                    Toast.makeText(
                                        context!!, "Fail to store Company ${if (isLogoImage) {
                                            "Logo"
                                        } else {
                                            "Banner"
                                        }}" +
                                                "Please try again", Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                    }

                    override fun onLoadingStarted(imageUri: String?, view: View?) {}
                    override fun onLoadingCancelled(imageUri: String?, view: View?) {}
                    override fun onLoadingFailed(
                        imageUri: String?,
                        view: View?,
                        failReason: FailReason?
                    ) {
                    }
                })
        }
    }

    private fun readOutletDetailsFromFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!) && AppUtils.OUTLET_NAME != null && AppUtils.OUTLET_NAME.isNotEmpty()
            && AppUtils.OUTLET_NAME.isNotBlank() && AppUtils.OUTLET_NAME != "NA"
        ) {
            progress.show()
            val fireBaseReference = FirebaseDatabase.getInstance()
                .getReference(AppUtils.OUTLET_NAME)
                .child(ConstantUtils.OUTLET_PROFILE)

            fireBaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    progress.dismiss()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val companyModel = dataSnapshot.getValue(CompanyModel::class.java)
                        if (companyModel != null) {
                            setModelDataToView(companyModel)
                        }
                    } else {
                        setModelDataToView(
                            CompanyModel(
                                AppUtils.OUTLET_NAME,
                                "", "", ConstantUtils.DEFAULT_LOGO, ConstantUtils.DEFAULT_BANNER
                            )
                        )
                    }
                }
            })

        }
    }

    private fun setModelDataToView(companyModel: CompanyModel) {
        outletName.setText(companyModel.outletName ?: "")
        outletContact.setText(companyModel.outletContact ?: "")
        outletAddress.setText(companyModel.outletAddress ?: "")
        outletZipcode.setText(companyModel.outletAddress.substring(companyModel.outletAddress.lastIndexOf("-")+1).trim())
        if (AppUtils.networkConnectivityCheck(context!!)) {

            serverLogoUrl = if (companyModel.outletLogo != null && companyModel.outletLogo.isNotEmpty()) {
                preferenceSharedUtils.setOutletLogoUrl(companyModel.outletLogo)
                companyModel.outletLogo
            } else {
                ConstantUtils.DEFAULT_LOGO
            }
            serverBannerUrl = if (companyModel.outletBanner != null && companyModel.outletBanner.isNotEmpty()) {
                preferenceSharedUtils.setOutletBannerUrl(companyModel.outletBanner)
                companyModel.outletBanner
            } else {
                ConstantUtils.DEFAULT_BANNER
            }

            imageLoader.displayImage(
                serverLogoUrl,
                outletLogoImage,
                roundDisplayOption
            )

            imageLoader.displayImage(
                serverBannerUrl,
                outletBannerImage,
                displayOptions
            )
        }
        progress.dismiss()
    }

    private fun writeOutletDetailsToFireBase() {
        if (AppUtils.networkConnectivityCheck(context!!) && AppUtils.OUTLET_NAME != null && AppUtils.OUTLET_NAME.isNotEmpty()
            && AppUtils.OUTLET_NAME.isNotBlank() && AppUtils.OUTLET_NAME != "NA"
        ) {
            val name = outletName.text.toString()
            val contact = outletContact.text.toString()
            val address = outletAddress.text.toString()

            if (name.isNotEmpty() && contact.isNotEmpty()
                && contact.length == 10 && address.isNotEmpty()
            ) {
                progress.show()
                FirebaseDatabase.getInstance()
                    .getReference(AppUtils.OUTLET_NAME)
                    .child(ConstantUtils.OUTLET_PROFILE)
                    .setValue(
                        CompanyModel(
                            name,
                            address,
                            contact,
                            serverLogoUrl ?: "NA",
                            serverBannerUrl ?: "NA"
                        )
                    )
                Toast.makeText(context!!, "Saved details successfully", Toast.LENGTH_SHORT).show()
            } else {
                if (name.isEmpty() && name.isBlank()) {
                    outletName.error = "Enter Outlet Name"
                }

                if (contact.isEmpty() && contact.isBlank() && contact.length != 10) {
                    outletContact.error = "Enter Outlet Contact"
                }

                if (address.isEmpty() && address.isBlank()) {
                    outletAddress.error = "Enter Outlet Address"
                }
            }
        }
        progress.dismiss()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.frag_setup_save_btn_id -> {
                writeOutletDetailsToFireBase()
            }

            R.id.frag_setup_search_id -> {
                getZipCodeAddress(outletZipcode.text.toString())
            }

            R.id.frag_setup_logo_id -> {
                isLogoImage = true
                setupGallery()
            }

            R.id.frag_setup_edit_banner_id -> {
                isLogoImage = false
                setupGallery()
            }
        }
    }
}
