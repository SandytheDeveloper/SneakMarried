package com.commonfriend.template

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.R
import com.commonfriend.StepsActivity
import com.commonfriend.adapter.LocationAddressListAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivitySixteenTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject
import java.util.*

class SixteenTemplateActivity : BaseActivity(), OnClickListener {

    private lateinit var binding: ActivitySixteenTemplateBinding
    private var mainJsonObj: JSONObject? = null
    private lateinit var questionViewModel: QuestionViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var addressModel: GeneralModel? = null
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private var lastPos = -1
    private var locationCallback : LocationCallback? = null
    private var defaultText = ""

    private lateinit var locationListAdapter: LocationAddressListAdapter


    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var bottomScreenBinding: DialogCastBinding
    private var debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable = Runnable {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySixteenTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        try {
            binding.txtQuestion.text = Util.applyCustomFonts(
                this@SixteenTemplateActivity,
                null,
                mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
            )
        } catch (e:Exception){
            if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1"){
                startActivity(Intent(this, StepsActivity::class.java))
                finishAffinity()
            } else {
                finish()
            }
            return
        }

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)
        binding.infoMessage.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage
        defaultText = mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

        with(binding.customHeader.get()) {
            btnLeft.setOnClickListener(this@SixteenTemplateActivity)
            txtTitle.text = mainObjList[CATEGORY_ID].categoryName
            progressBar.progress = LAST_POS + 1
            progressBar.visibility = View.VISIBLE
            progressBar.max = mainObjList[CATEGORY_ID].questionList.size
            txtPageNO.text =
                StringBuilder().append((LAST_POS + 1).toString()).append("/")
                    .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        }
        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btnAddManually.setOnClickListener(this)



        locationListAdapter = LocationAddressListAdapter(
            this,
            this,/* "(cities)"*/"administrative_area_level_3"
        )

        allApiResponses()

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        debounceHandler.removeCallbacks(debounceRunnable)
        if (locationCallback.isNotNull())
            mFusedLocationClient.removeLocationUpdates(locationCallback!!)
    }

    private fun saveData() {
        mainJsonObj = JSONObject()
        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty() && Util.getTextValue(
                binding.txtLocationName
            ) == mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name
        ) {
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                finish()
            else
                Util.manageTemplate(this@SixteenTemplateActivity, isFrom)
        } else {
            if (addressModel!!.city.isEmpty())
                return

            mainJsonObj!!.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                addressModel!!.city
            )
            Pref.setStringValue(Pref.PREF_CURRENT_LAT, addressModel!!.latitude)
            Pref.setStringValue(Pref.PREF_CURRENT_LNG, addressModel!!.longitude)
            Pref.setStringValue(Pref.PREF_USER_CURRENT_CITY, addressModel!!.city)
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainJsonObj}")
            callApi(1)
        }
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@SixteenTemplateActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.buttonView.btnSkip.setOnClickListener(this)

    }

    override fun onPause() {
        super.onPause()
        if (locationCallback.isNotNull())
            mFusedLocationClient.removeLocationUpdates(locationCallback!!)
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@SixteenTemplateActivity,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.saveAnswerResponse.observe(this@SixteenTemplateActivity) {
            binding.buttonView.btnContinue.isEnabled = true
            Util.dismissProgress()


            bundle = Bundle().apply {
                putString(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    addressModel!!.city
                )
            }

            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)

            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                addressModel!!.city
            )

            Util.print("BKL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString()
            }")

            if (it.success == 1) {
                if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                    finish()
                else
                    Util.manageTemplate(this@SixteenTemplateActivity, isFrom)

            }
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()

        } else {
            startActivity(
                Intent(
                    this@SixteenTemplateActivity,
                    LocationPermissionActivity::class.java
                )
            )
            finish()
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private var isLocationFetchingInProcess = false


    private var isLocationFetchingFailed = true


    private fun getLocation() {
        if (isLocationEnabled()) {
            if (!isLocationFetchingInProcess) {
                isLocationFetchingInProcess = true

                binding.txtLocationName.setText(getString(R.string.detecting))
                binding.txtLocationName.setTextColor(
                    ContextCompat.getColor(
                        this@SixteenTemplateActivity,
                        R.color.color_black
                    )
                )

                addLocationManually(false)
                val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation

                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>TIME IN GETTING LOCATION IS >>>>${System.currentTimeMillis()}")

                        handleLocationResult(location)


                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>TIME IN AFTER SETTING LOCATION IS >>>>${System.currentTimeMillis()}")

                    }


                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                        if (!locationAvailability.isLocationAvailable) {
                            binding.buttonView.btnContinue.isEnabled = false
                            binding.buttonView.btnContinue.setOnClickListener(null)
                            addLocationManually(true)
                        } else {

                            isLocationFetchingInProcess = false
                            getLocation()
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    if (isLocationFetchingFailed) {
                                        binding.buttonView.btnContinue.isEnabled = false
                                        binding.buttonView.btnContinue.setOnClickListener(null)
                                        addLocationManually(true)
                                    }
                                },
                                try {
                                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationDelay.ifEmpty { "0" }
                                        .toLong()
                                }catch (e : Exception) {
                                    0
                                }
                            )
                        }
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (isLocationFetchingFailed) {
                            binding.buttonView.btnContinue.isEnabled = false
                            binding.buttonView.btnContinue.setOnClickListener(null)
                            addLocationManually(true)
                        }
                    },
                    try {
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].locationDelay.ifEmpty { "0" }
                            .toLong()
                    }catch (e : Exception) {
                        0
                    }
                )

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
            } else {
                // Location fetching is already in progress, ignore the request
            }
        } else {
            showOpenSettingsDialog(this@SixteenTemplateActivity)
        }
    }

    private fun handleLocationResult(location: Location?) {
        if (location != null) {
            // Process location data and update UI as needed
            try {
                val geocoder =
                    Geocoder(this@SixteenTemplateActivity, Locale.ENGLISH)
                val list =
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
                addressModel = GeneralModel()
                addressModel!!.city = list!![0].locality
                addressModel!!.country = list[0].countryName
                addressModel!!.latitude = list[0].latitude.toString()
                addressModel!!.longitude = list[0].longitude.toString()

                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TIME IN GEOCODING LOCATION>>>${System.currentTimeMillis()}")

                Handler(Looper.myLooper()!!).postDelayed({
                    if (addressModel!!.city.isNotEmpty()) {
                        binding.txtLocationName.setText(addressModel!!.city)
                        binding.txtLocationDialog.hint = defaultText
                        addLocationManually(false)
                        // Location fetching succeeded, enable manual location input
                        isLocationFetchingInProcess = false
                        isLocationFetchingFailed = false
                        binding.buttonView.btnContinue.isEnabled = true
                        binding.buttonView.btnContinue.setOnClickListener(this@SixteenTemplateActivity)
                    } else {
                        binding.buttonView.btnContinue.isEnabled = false
                        binding.buttonView.btnContinue.setOnClickListener(null)
                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>AM FROM THE  LOCATION FETCHING FAILED")
                        addLocationManually(true)
                    }
                }, 500)
            } catch (e: Exception) {
                isLocationFetchingInProcess = false
                isLocationFetchingFailed = true
                binding.buttonView.btnContinue.isEnabled = false
                binding.buttonView.btnContinue.setOnClickListener(null)
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>AM FROM THE EXCEPTION PART")
                addLocationManually(true)
            } finally {
                if (addressModel == null) {
                    binding.buttonView.btnContinue.isEnabled = false
                    binding.buttonView.btnContinue.setOnClickListener(null)
                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>AM FROM THE  LOCATION FETCHING FAILED")
                    addLocationManually(true)
                }
            }
        } else {
            isLocationFetchingInProcess = false
            isLocationFetchingFailed = true
            addLocationManually(true)
            binding.buttonView.btnContinue.isEnabled = false
            binding.buttonView.btnContinue.setOnClickListener(null)
        }

    }

    private fun showOpenSettingsDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.apply {
            setTitle("Permission Required")
            setMessage("To use this feature, you need to grant location permission. Would you like to open app settings now?")
            setPositiveButton("Yes") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                binding.buttonView.btnContinue.setOnClickListener(null)
                binding.buttonView.btnContinue.isEnabled = false
                addLocationManually(true)

            }

            setCancelable(false)
        }

        alertDialog.create().show()
    }


    /*
    @SuppressLint("MissingPermission", "SetTextI18n")
    suspend fun  getLocation() {
        if (isLocationEnabled()) {
            withTimeout(mainObjList[CATEGORY_ID].questionList[LAST_POS].locationDelay.ifEmpty { "0" }.toLong()) {
                val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation
                        if (location != null) {
                            // Process location data and update UI as needed
                            try {
                                val geocoder =
                                    Geocoder(this@SixteenTemplateActivity, Locale.ENGLISH)
                                val list =
                                    geocoder.getFromLocation(
                                        location.latitude,
                                        location.longitude,
                                        10
                                    )
                                addressModel!! = GeneralModel()
                                addressModel!!.city = list!![0].locality
                                addressModel!!.country = list[0].countryName
                                addressModel!!.latitude = list[0].latitude.toString()
                                addressModel!!.longitude = list[0].longitude.toString()

                                Handler(Looper.myLooper()!!).postDelayed({
                                    if (addressModel!!.city.isNotEmpty()) {
                                        binding.txtLocationName.setText(addressModel!!.city)
                                        binding.txtLocationDialog.hint =
                                            mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

                                        addLocationManually(false)


                                        binding.buttonView.btnContinue.apply {
                                            setOnClickListener(this@SixteenTemplateActivity)
                                            isEnabled = true
                                        }
                                    } else {
                                        addLocationManually(true)
                                    }

                                }, 500)
                            } catch (e: Exception) {
                                addLocationManually(true)
                            }

                        }
                    }

                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                        if (!locationAvailability.isLocationAvailable) {
                            addLocationManually(true)
                        } else {
                            binding.txtLocationName.setText(getString(R.string.detecting))
                            binding.txtLocationName.setTextColor(
                                ContextCompat.getColor(
                                    this@SixteenTemplateActivity,
                                    R.color.color_black
                                )
                            )
//                            getLocation()
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    addLocationManually(true)
                                },
                                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationDelay.ifEmpty { "0" }
                                    .toLong()
                            )
                        }
                        binding.btnAddManually.visibleIf(!locationAvailability.isLocationAvailable)
                    }
                }

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

        } else {
            Util.showToastMessage(this, "Please turn on location", false)
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }*/
    /*
        @SuppressLint("MissingPermission", "SetTextI18n")
        private fun getLocation() {
            if (isLocationEnabled()) {
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>IWAS IN ROOOOOOO>>>>")
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.ENGLISH)
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 10)
                        addressModel!! = GeneralModel()
                        addressModel!!.city = list!![0].locality
                        addressModel!!.country = list[0].countryName
                        addressModel!!.latitude = list[0].latitude.toString()
                        addressModel!!.longitude = list[0].longitude.toString()


                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>IWAS IN BROOOOOOO>>>>${addressModel!!.city}")

                        Handler(Looper.myLooper()!!).postDelayed({
                            binding.txtLocationName.setText(addressModel!!.city)
                            binding.txtLocationDialog.hint =
                                mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

                            binding.buttonView.btnContinue.apply {
                                setOnClickListener(this@SixteenTemplateActivity)
                                isEnabled = true
                            }

                        }, 500)
                    }
                }
            } else {
                Util.showToastMessage(this, "Please turn on location", false)
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
    */

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this@SixteenTemplateActivity,
                        mainJsonObj!!
                    )
                }
            }
        }
    }


    private fun addLocationManually(addLocation: Boolean) {
        if (addLocation) {
            binding.txtLocationName.setText("Unable to detect")
            binding.txtLocationDialog.hint = "Your Location"
        }

        binding.txtLocationName.setTextColor(
            ContextCompat.getColor(
                this@SixteenTemplateActivity,
                if (addLocation) R.color.color_red else R.color.color_black
            )
        )
        binding.btnAddManually.visibleIf(addLocation)

//        binding.buttonView.btnContinue.isEnabled = !addLocation
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnContinue -> {
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>I WAS CLICKED")
                binding.buttonView.btnContinue.isEnabled = false
                saveData()
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                locationListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                locationListAdapter.objList[lastPos].isSelected = 1
                binding.txtLocationName.setText(locationListAdapter.objList[lastPos].name)
                binding.txtLocationName.setTextColor(
                    ContextCompat.getColor(
                        this@SixteenTemplateActivity,
                        R.color.color_black
                    )
                )

//                locationListAdapter.setPlaceId(locationListAdapter.objList[lastPos].description)
                locationListAdapter.notifyDataSetChanged()
                Util.hideKeyBoard(this@SixteenTemplateActivity, view)
                bottomSheetDialog?.dismiss()
                bottomScreenBinding.btnDialogContinue.apply {
                    isEnabled = true
                    setOnClickListener(this@SixteenTemplateActivity)
                }
//                binding.buttonView.btnContinue.isEnabled = true
//                binding.buttonView.btnContinue.setOnClickListener(this@SixteenTemplateActivity)
                addressModel = GeneralModel().apply {
                    city = locationListAdapter.objList[lastPos].name
                    latitude = "0.00"//locationListAdapter.objList[lastPos].latitude
                    longitude = "0.00"//locationListAdapter.objList[lastPos].longitude
                }
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${addressModel!!.latitude}")
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${addressModel!!.longitude}")
                saveData()
            }

            R.id.btnDialogContinue -> {
                bottomSheetDialog?.dismiss()
            }

            R.id.btnAddManually -> {
                bottomSheetDialog()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.btnLeft -> {
                onBackPress()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {


        if (bottomSheetDialog.isNotNull() && bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()

        bottomSheetDialog =
            BottomSheetDialog(this@SixteenTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        bottomSheetDialog!!.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding.root)
            setCancelable(true)
            window?.let { Util.statusBarColor(this@SixteenTemplateActivity, it) }

            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            show()
        }

        bottomScreenBinding.apply {
            edtSearch.requestFocus()
            this.btnDialogContinue.setOnClickListener(null)
            this.rvLocation.visibility = View.VISIBLE

            this.rvTextSuggestion.gone()


            this.rvLocation.layoutManager =
                LinearLayoutManager(this@SixteenTemplateActivity, RecyclerView.VERTICAL, false)

            this.rvLocation.adapter = locationListAdapter

            this.txtHeaderTitle.text = "Your current location"
            this.txtSuggestion.gone()
            this.txtChooseReligion.gone()

            locationListAdapter.objList.clear()

            this.txtClear.setOnClickListener {
                this.edtSearch.setText("")
                locationListAdapter.objList.clear()
            }

            this.imgLeft.setOnClickListener { this.edtSearch.setText("") }
            this.txtChooseReligion.gone()

            this.edtSearch.observeTextChange(300L) { value ->
                txtClear.visibleIf(value.isNotEmpty())
                imgLeft.visibleIf(value.isNotEmpty())
                imgSearch.visibleIf(value.isEmpty())

                if (value.isNotEmpty()) {
                    debounceHandler.removeCallbacks(debounceRunnable)
                    debounceRunnable = Runnable {
                        locationListAdapter.filter.filter(value)
                        btnDialogContinue.setOnClickListener(null)
                    }
                    debounceHandler.postDelayed(debounceRunnable, 500)
                }
            }

            this.btnCancel.setOnClickListener {
                bottomSheetDialog!!.dismiss()
            }
        }
    }
}