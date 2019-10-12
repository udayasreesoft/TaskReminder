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
import com.google.firebase.database.FirebaseDatabase
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


class UserOutletSetupFragment : Fragment(), View.OnClickListener {

    private lateinit var outletTitle: TextView
    private lateinit var outletName: EditText
    private lateinit var outletContact: EditText
    private lateinit var outletAddress: AutoCompleteTextView
    private lateinit var outletSearch: ImageView
    private lateinit var outletLogoText: TextView
    private lateinit var outletLogoImage: ImageView
    private lateinit var outletBannerText: TextView
    private lateinit var outletBannerImage: ImageView
    private lateinit var outletSave: Button

    private lateinit var progress: CustomProgressDialog
    private lateinit var displayOptions: DisplayImageOptions
    private lateinit var roundDisplayOption: DisplayImageOptions
    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceSharedUtils: PreferenceSharedUtils

    private var isLogoImage = false
    private var outletLogoUrl = ""
    private var outletBannerUrl = ""

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
        outletSearch = view.findViewById(R.id.frag_setup_zipcode_id)
        outletLogoText = view.findViewById(R.id.frag_setup_logo_text_id)
        outletLogoImage = view.findViewById(R.id.frag_setup_logo_id)
        outletBannerText = view.findViewById(R.id.frag_setup_banner_text_id)
        outletBannerImage = view.findViewById(R.id.frag_setup_banner_id)
        outletSave = view.findViewById(R.id.frag_setup_save_btn_id)

        outletBannerImage.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.70).toInt()

        outletLogoImage.layoutParams.width = (AppUtils.SCREEN_WIDTH * 0.40).toInt()
        outletLogoImage.layoutParams.height = (AppUtils.SCREEN_WIDTH * 0.40).toInt()

        outletSave.setOnClickListener(this)
        outletSearch.setOnClickListener(this)
        outletBannerImage.setOnClickListener(this)
        outletLogoImage.setOnClickListener(this)

        preferenceSharedUtils = PreferenceSharedUtils(context!!).getInstance()
        progress = CustomProgressDialog(context!!).getInstance()

        outletName.setText(preferenceSharedUtils.getOutletName())
        setupImageLoader()
    }

    private fun setupImageLoader() {
        roundDisplayOption = DisplayImageOptions.Builder()
            .displayer(RoundedBitmapDisplayer(1000))
            .showImageOnLoading(android.R.drawable.stat_sys_download_done)
            .showImageForEmptyUri(android.R.drawable.stat_notify_error)
            .showImageOnFail(android.R.drawable.stat_notify_error)
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
            progress.setMessage("Fetching your Address...")
            progress.build()
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
            outletAddress.threshold = 0
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
        outletSearch.visibility = View.GONE
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
            if (requestCode == ConstantUtils.PERMISSION_GALLERY && resultCode == Activity.RESULT_OK && data != null && AppUtils.networkConnectivityCheck(context!!)) {
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
                    val logoUrl = "file://$imagePath"
                    storeImageToFireBase(logoUrl, outletLogoImage)
                } else {
                    val bannerUrl = "file://$imagePath"
                    storeImageToFireBase(bannerUrl, outletBannerImage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun storeImageToFireBase(url: String, imageView : ImageView) {
        if (url.isNotEmpty() && AppUtils.networkConnectivityCheck(context!!)) {

            imageLoader.displayImage(url, imageView, if (isLogoImage){roundDisplayOption} else{displayOptions}, object : ImageLoadingListener {
                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    val companyName = preferenceSharedUtils.getOutletName() ?: ""
                    if (companyName.isNotEmpty() && companyName.isNotBlank()) {
                        progress.show()
                        val ext = url.substring(url.lastIndexOf("."))
                        val storageReference: StorageReference = FirebaseStorage.getInstance()
                            .getReference(companyName.plus("/"))
                            .child(ConstantUtils.PROFILES)
                            .child("${companyName}_${if (isLogoImage) {"Logo"} else {"Banner"}}$ext")

                        storageReference.putFile(Uri.parse(url))
                            .addOnSuccessListener { taskSnapShot ->
                                progress.dismiss()
                                if (isLogoImage) {
                                    outletLogoUrl = taskSnapShot.metadata?.reference?.downloadUrl.toString()
                                } else {
                                    outletBannerUrl = taskSnapShot.metadata?.reference?.downloadUrl.toString()
                                }
                            }

                            .addOnFailureListener {
                                progress.dismiss()
                                Toast.makeText(context!!, "Fail to store Company ${if (isLogoImage) {"Logo"} else {"Banner"}}" +
                                        "Please try again", Toast.LENGTH_SHORT).show()
                            }
                    }

                }
                override fun onLoadingStarted(imageUri: String?, view: View?) {}
                override fun onLoadingCancelled(imageUri: String?, view: View?) {}
                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {}
            })
        }
    }

    private fun writeOutletDetailsToFireBase() {
        val name = outletName.text.toString()
        val contact = outletContact.text.toString()
        val address = outletAddress.text.toString()

        if (name.isNotEmpty() && contact.isNotEmpty() && contact.length == 10
            && address.isNotEmpty() && AppUtils.networkConnectivityCheck(context!!)) {

            FirebaseDatabase.getInstance()
                .getReference(ConstantUtils.DETAILS)
                .child(ConstantUtils.COMPANY)
                .setValue(CompanyModel(name, address, contact, outletLogoUrl ?: "NA", outletBannerUrl ?: "NA"))

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
        progress.dismiss()
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.frag_setup_save_btn_id -> {
                progress.setTitle("Uploading Details to server")
                progress.setMessage("Please wait until uploading finish")
                progress.build()
                progress.show()
                writeOutletDetailsToFireBase()
            }

            R.id.frag_setup_zipcode_id -> {
                if (outletSearch.visibility == View.VISIBLE) {
                    getZipCodeAddress(outletAddress.text.toString())
                }
            }

            R.id.frag_setup_logo_id -> {
                isLogoImage = true
                setupGallery()
            }

            R.id.frag_setup_banner_id -> {
                isLogoImage = false
                setupGallery()
            }
        }
    }
}
