package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.MainApplication.Companion.firebaseAnalytics
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityOtpBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.observeTextChange
import com.commonfriend.utils.openA
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory


class OtpActivity : BaseActivity(), View.OnClickListener {

    private var binding: ActivityOtpBinding? = null
    private val _binding get() = binding!!

    private lateinit var userViewModel: UserViewModel
    private var isFrom: ActivityIsFrom? = ActivityIsFrom.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(_binding.root)


        _binding.editOption.setOnClickListener(this)
        _binding.btnResendOtp.setOnClickListener(this)

        initailization()

    }

    @SuppressLint("SetTextI18n")
    private fun initailization() {

        allApiResponses()
        if (intent.hasExtra(IS_FROM)) {
            isFrom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(IS_FROM, ActivityIsFrom::class.java)
            } else {
                (intent.getSerializableExtra(IS_FROM) as ActivityIsFrom)
            }
        }

        with(_binding) {

            this.customHeader.get().btnLeft.setOnClickListener(this@OtpActivity)
            this.customHeader.get().progressBar.visibility = View.GONE
            this.txtMNo.text = intent.getStringExtra(ID)

            val spannableString =
                SpannableString(getString(R.string.by_logging_in_you_agree_to_out_term_privacy_policy))
            val termClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    startActivity(Intent(this@OtpActivity, PrivacyLegalActivity::class.java)
                        .putExtra(IS_FROM,"3"))
                }
            }
            val privacyClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    startActivity(Intent(this@OtpActivity, PrivacyLegalActivity::class.java)
                        .putExtra(IS_FROM,"1"))
                }
            }
            spannableString.setSpan(
                termClickableSpan,
                spannableString.indexOf(getString(R.string.term_)),
                spannableString.indexOf(getString(R.string.term_)) + getString(R.string.term_).length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                privacyClickableSpan,
                spannableString.indexOf(getString(R.string.privacy_policy)),
                spannableString.indexOf(getString(R.string.privacy_policy)) + getString(R.string.privacy_policy).length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

            val colorSpan =
                ForegroundColorSpan(ContextCompat.getColor(this@OtpActivity, R.color.color_grey))

            spannableString.setSpan(
                colorSpan,
                spannableString.indexOf(getString(R.string.term_)),
                spannableString.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

            _binding.txtTerms.text = spannableString
            _binding.txtTerms.movementMethod = LinkMovementMethod.getInstance()
            _binding.txtTerms.highlightColor = Color.TRANSPARENT

            this.edtOtp1.observeTextChange {
                if (this.edtOtp1.text.toString()
                        .isNotEmpty() && isValidate()
                ) //size as per your requirement
                {
                    this.edtOtp2.requestFocus()
                    callApi(if (isFrom == ActivityIsFrom.FROM_MENU) 1 else 3)

                }

            }
            this.edtOtp2.observeTextChange {
                if (this.edtOtp2.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp3.requestFocus()
                    callApi(if (isFrom == ActivityIsFrom.FROM_MENU) 1 else 3)
                }
            }
            this.edtOtp3.observeTextChange {

                if (this.edtOtp3.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp4.requestFocus()
                    callApi(if (isFrom == ActivityIsFrom.FROM_MENU) 1 else 3)
                }
            }
            this.edtOtp4.observeTextChange {
                if (this.edtOtp4.text!!.isNotEmpty() && isValidate()) {
                    callApi(if (isFrom == ActivityIsFrom.FROM_MENU) 1 else 3)
                }
            }

            this.edtOtp1.setOnKeyListener(GenericKeyEvent(this.edtOtp1, null))
            this.edtOtp2.setOnKeyListener(GenericKeyEvent(this.edtOtp2, this.edtOtp1))
            this.edtOtp3.setOnKeyListener(GenericKeyEvent(this.edtOtp3, this.edtOtp2))
            this.edtOtp4.setOnKeyListener(GenericKeyEvent(this.edtOtp4, this.edtOtp3))

        }
    }

    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]

        userViewModel.userDataResponse.observe(this) {



            bundle = Bundle().apply {
                putString("screen_type", Screens.OTP_SCREEN.screenType)
                putString("screen_name", Screens.OTP_SCREEN.screenName)
                putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
                putString("candidate_gender", Pref.getStringValue(Pref.PREF_USER_GENDER, "").toString())
            }
            firebaseEventLog("login_completed", bundle)
            firebaseAnalytics.setUserProperty("cf_id",Pref.getStringValue(Pref.PREF_USER_ID, "").toString())

            if (it.data.isNotEmpty()) {
                Pref.setStringValue(Pref.PREF_IS_ACCOUNT_BAN,it.data[0].isBan)
                Pref.setStringValue(Pref.PREF_UNDER_REVIEW,it.data[0].underReview)
            }

            openA<VerificationDoneActivity> { putExtra(IS_FROM, isFrom) }
            finish()
        }
        userViewModel.claimProfileApiResponse.observe(this) {
            openA<VerificationDoneActivity> { putExtra(IS_FROM, isFrom) }
            finish()
        }
        userViewModel.serviceDataResponse.observe(this) {
            Util.dismissProgress()
            Util.showToastMessage(this,it.msg,true)
        }
    }


    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?,
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.edtOtp1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }

    }

    private fun isValidate(): Boolean {
        var isError = true
        when {
            Util.isEmptyText(_binding.edtOtp1) -> {
                isError = false
                _binding.edtOtp1.requestFocus()
            }

            Util.isEmptyText(_binding.edtOtp2) -> {
                isError = false
                _binding.edtOtp2.requestFocus()
            }

            Util.isEmptyText(_binding.edtOtp3) -> {
                isError = false
                _binding.edtOtp3.requestFocus()
            }

            Util.isEmptyText(_binding.edtOtp4) -> {
                isError = false
                _binding.edtOtp4.requestFocus()
            }

        }
        return isError
    }


    override fun onClick(view: View) {
        when (view.id) {

            R.id.btnLeft, R.id.editOption -> {
                onBackPressedDispatcher.onBackPressed()
            }

            R.id.btnResendOtp -> {
                callApi(2)
            }


        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this)
            when (tag) {
                3 -> {
                    userViewModel.verifyOtpApiRequest(
                        this@OtpActivity, Util.getTextValue(_binding.edtOtp1)
                                + Util.getTextValue(_binding.edtOtp2)
                                + Util.getTextValue(_binding.edtOtp3)
                                + Util.getTextValue(_binding.edtOtp4),
                        Pref.getStringValue(Pref.PREF_MOBILE_NUMBER, "").toString()
                    )
                }

                1 -> {
                    userViewModel.changeNumberVerifyOtpApiRequest(
                        this@OtpActivity, Util.getTextValue(_binding.edtOtp1)
                                + Util.getTextValue(_binding.edtOtp2)
                                + Util.getTextValue(_binding.edtOtp3)
                                + Util.getTextValue(_binding.edtOtp4),
                        Pref.getStringValue(Pref.PREF_MOBILE_NUMBER, "").toString()
                    )
                }

                2 -> {

                    userViewModel.resendOtpApiRequest(
                        this@OtpActivity,
                        if (isFrom == ActivityIsFrom.FROM_MENU) "1" else "0"
                    )
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}