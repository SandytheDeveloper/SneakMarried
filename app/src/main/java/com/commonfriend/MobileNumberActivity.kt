package com.commonfriend


import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.CountryCodeListAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.EmojiInputFilter
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivityMobileNumberBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.CountryCodeModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken


class MobileNumberActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMobileNumberBinding
    private var lastPos = -1
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private var selectedCountryNo = "+91"
    private var selectedCountryName = "India"
    private var isFrom: ActivityIsFrom? = ActivityIsFrom.NORMAL
    private var versionName = ""

    private lateinit var userViewModel: UserViewModel

    private lateinit var countryCodeListAdapter: CountryCodeListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMobileNumberBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (intent.hasExtra(IS_FROM)) {
            isFrom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(IS_FROM, ActivityIsFrom::class.java)
            } else {
                (intent.getSerializableExtra(IS_FROM) as ActivityIsFrom)
            }
        }
        logForCurrentScreen(
            Screens.MOBILE_NO_SCREEN.screenType,
            Screens.MOBILE_NO_SCREEN.screenName
        )
        initialization()

    }

    private fun initialization() {

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        // Retrieve the version information
        versionName = packageInfo.versionName

        binding.imgContinue.setOnClickListener(this)
        binding.txtCountryCode.setOnClickListener(this)
        binding.txtCountryCode.setOnClickListener(this)


        binding.customHeader.get().imgCross.setOnClickListener(this)
        binding.customHeader.get().txtPageNO.visibility = View.GONE
        binding.customHeader.get().btnLeft.visibility = View.INVISIBLE
        binding.customHeader.get().progressBar.visibility = View.INVISIBLE
        binding.customHeader.get().imgCross.visibility =
            if (isFrom == ActivityIsFrom.FROM_MENU) View.VISIBLE else View.INVISIBLE

        if (isFrom == ActivityIsFrom.NORMAL) {
            Util.setToken()
        }
        allApiResponses()



        binding.edtNumber.observeTextChange {
            binding.edtNumber.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                (resources.getDimension(if (it.isEmpty()) com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp))
            )
            binding.imgContinue.isClickable = (it.length == 10)
            binding.imgContinue.setColorFilter(
                ContextCompat.getColor(
                    this@MobileNumberActivity,
                    if (it.length == 10) R.color.color_blue else R.color.color_grey
                ), PorterDuff.Mode.SRC_IN
            )

        }
        bottomSheetDialog()

        binding.llInTouchOnWhatsapp.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.imgContinue.isClickable = (binding.edtNumber.text!!.length == 10)
        binding.imgContinue.setColorFilter(
            ContextCompat.getColor(
                this@MobileNumberActivity,
                if (binding.edtNumber.text!!.length == 10) R.color.color_blue else R.color.color_grey
            ), PorterDuff.Mode.SRC_IN
        )

    }


    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]
        userViewModel.userDataResponse.observe(this) {
            if (it.success == 1 && it.data.isNotEmpty()) {

                binding.imgContinue.isClickable = true
                binding.imgContinue.setColorFilter(
                    ContextCompat.getColor(
                        this@MobileNumberActivity,
                        R.color.color_blue
                    ), PorterDuff.Mode.SRC_IN
                )

                Pref.setStringValue(Pref.PREF_MOBILE_NUMBER, Util.getTextValue(binding.edtNumber))
                openA<OtpActivity> {
                    putExtra(
                        ID,
                        "${if (lastPos != -1) selectedCountryNo else "+91"}-${
                            Util.getTextValue(binding.edtNumber)
                        }"
                    )
                    putExtra(IS_FROM, isFrom)
                }

            } else {
                ErrorDialogComponent(
                    this,
                    ErrorDialogComponent.ErrorDialogFor.SURETY,
                    "Error",
                    it.msg
                ).show()
            }
        }


    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()

        bottomSheetDialog =
            BottomSheetDialog(this@MobileNumberActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)


        bottomScreenBinding.rvLocation.visibility = View.VISIBLE


        val gson = GsonBuilder().create()
        countryCodeListAdapter = CountryCodeListAdapter(this, this)
        bottomScreenBinding.rvLocation.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        bottomScreenBinding.rvLocation.adapter = countryCodeListAdapter


        try {
            val jsonString =
                application.assets.open("country_codes.json").bufferedReader().use { it.readText() }
            val countryCodes: ArrayList<CountryCodeModel> =
                gson.fromJson(jsonString, object : TypeToken<List<CountryCodeModel>>() {}.type)
            countryCodeListAdapter.addData(countryCodes)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bottomScreenBinding.edtSearch.filters = arrayOf(EmojiInputFilter())


        if (bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()
        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding.root)
        bottomSheetDialog!!.setCancelable(true)


        bottomSheetDialog!!.setOnDismissListener {
            bottomScreenBinding.edtSearch.setText("")
        }

        bottomScreenBinding.txtHeaderTitle.text = getString(R.string.enter_country)
        bottomScreenBinding.edtSearch.hint = "Eg. India"

        bottomScreenBinding.txtTitleClear.setOnClickListener {
            bottomScreenBinding.edtSurName.setText("")
        }
        bottomScreenBinding.txtClear.setOnClickListener { bottomScreenBinding.edtSearch.setText("") }
        bottomScreenBinding.imgLeft.setOnClickListener { bottomScreenBinding.edtSearch.setText("") }


        bottomScreenBinding.txtSuggestion.visibility = View.GONE
        bottomScreenBinding.txtChooseReligion.visibility = View.GONE


        bottomScreenBinding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.>>${bottomScreenBinding.edtSearch.text.toString()}")
                countryCodeListAdapter.filter.filter(s.toString().lowercase())

                val text = bottomScreenBinding.edtSearch.text.toString()

                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                bottomScreenBinding.txtClear.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding.imgLeft.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding.imgSearch.visibility = if (text.isEmpty()) View.VISIBLE
                else View.GONE

            }
        })


        bottomScreenBinding.btnDialogContinue.setOnClickListener(this)


        bottomScreenBinding.btnCancel.setOnClickListener {

            bottomSheetDialog!!.dismiss()
        }
        if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDialog!!.behavior.peekHeight =
                Resources.getSystem().displayMetrics.heightPixels
            bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_MENU) {
            finish()
        } else
            finishAffinity()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgCross -> {
                finish()
            }

            R.id.imgContinue -> {
                callApi(if (isFrom == ActivityIsFrom.FROM_MENU) 2 else 1)
            }

            R.id.txtCountryCode -> {

                if (selectedCountryName.isNotEmpty()) {
                    countryCodeListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                    countryCodeListAdapter.objList.find { it.name == selectedCountryName }?.isSelected =
                        1
                    bottomScreenBinding.btnDialogContinue.isClickable = true
                    bottomScreenBinding.btnDialogContinue.isEnabled = true
                    countryCodeListAdapter.notifyDataSetChanged()
                } else {
                    bottomScreenBinding.btnDialogContinue.isClickable = false
                    bottomScreenBinding.btnDialogContinue.isEnabled = false
                    countryCodeListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                    countryCodeListAdapter.notifyDataSetChanged()

                }

                if (bottomSheetDialog != null)
                    bottomSheetDialog!!.show()
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                countryCodeListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0

                countryCodeListAdapter.objList[lastPos].isSelected = 1
                countryCodeListAdapter.notifyDataSetChanged()

                selectedCountryNo =
                    countryCodeListAdapter.objList.find { it.isSelected == 1 }?.dialCode.toString()
                selectedCountryName =
                    countryCodeListAdapter.objList.find { it.isSelected == 1 }?.name.toString()


                binding.txtCountryCode.text =
                    "($selectedCountryNo) $selectedCountryName"

                bottomSheetDialog!!.dismiss()


            }


            R.id.btnDialogContinue -> {

                if (lastPos != -1) {
                    if (countryCodeListAdapter.objList.isEmpty()) {
                        bottomSheetDialog!!.dismiss()
                        return
                    }
                    selectedCountryNo =
                        countryCodeListAdapter.objList.find { it.isSelected == 1 }?.dialCode ?: "+91"
                    selectedCountryName =
                        countryCodeListAdapter.objList.find { it.isSelected == 1 }?.name ?: ""

                    binding.txtCountryCode.text =
                        "($selectedCountryNo) $selectedCountryName"

                    bottomSheetDialog!!.dismiss()
                }
            }

            R.id.llInTouchOnWhatsapp -> {

                binding.imgInTouchOnWhatsapp.isSaveEnabled = !binding.imgInTouchOnWhatsapp.isSaveEnabled

                if (binding.imgInTouchOnWhatsapp.isSaveEnabled){
                    binding.imgInTouchOnWhatsapp.setImageResource(R.drawable.dr_square_check)
                    binding.txtInTouchOnWhatsapp.setTextColor(ContextCompat.getColor(this,R.color.color_blue))
                } else {
                    binding.imgInTouchOnWhatsapp.setImageResource(R.drawable.dr_square_uncheck)
                    binding.txtInTouchOnWhatsapp.setTextColor(ContextCompat.getColor(this,R.color.color_black))
                }


            }


        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this@MobileNumberActivity)
            when (tag) {
                1 -> {
                    binding.imgContinue.isClickable = false
                    binding.imgContinue.setColorFilter(
                        ContextCompat.getColor(
                            this@MobileNumberActivity,
                            R.color.color_grey
                        ), PorterDuff.Mode.SRC_IN
                    )
                    userViewModel.sendOtpApiRequest(
                        this@MobileNumberActivity,
                        if (lastPos != -1) selectedCountryNo else "+91",
                        Util.getTextValue(binding.edtNumber),
                        if (binding.imgInTouchOnWhatsapp.isSaveEnabled) 1 else 0,
                        versionName
                    )
                }

                2 -> {
                    userViewModel.changeNumberApiRequest(
                        this@MobileNumberActivity,
                        if (lastPos != -1) selectedCountryNo else "+91",
                        Util.getTextValue(binding.edtNumber),
                        if (binding.imgInTouchOnWhatsapp.isSaveEnabled) 1 else 0
                    )
                }
            }
        }

    }

}
