package com.commonfriend

import android.content.Intent
import android.graphics.Color
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
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityAadharVerificationOtpBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.observeTextChange
import com.commonfriend.utils.openA
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class AadharVerificationOtpActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding : ActivityAadharVerificationOtpBinding
    private lateinit var userViewModel: UserViewModel
    private var aadharNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAadharVerificationOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        logForCurrentScreen("aadhar_otp",Screens.AADHAR_OTP_SCREEN.value)
        initialization()
    }

    private fun initialization() {

        allApiResponses()
        aadharNumber = intent.getStringExtra(DATA).toString()

        with(binding) {

            this.customHeader.get().progressBar.visibility = View.GONE
            this.txtVerification.text = Util.applyCustomFonts(
                this@AadharVerificationOtpActivity,
                null,
                resources.getString(R.string.enter_your_verification_code),
                resources.getString(R.string.code), R.color.color_black)

            val spannableString =
                    SpannableString(getString(R.string.by_logging_in_you_agree_to_out_term_privacy_policy))
            val termClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    startActivity(Intent(this@AadharVerificationOtpActivity, PrivacyLegalActivity::class.java)
                            .putExtra(IS_FROM,"3"))
                }
            }
            val privacyClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    startActivity(Intent(this@AadharVerificationOtpActivity, PrivacyLegalActivity::class.java)
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
                    ForegroundColorSpan(ContextCompat.getColor(this@AadharVerificationOtpActivity, R.color.color_grey))

            spannableString.setSpan(
                    colorSpan,
                    spannableString.indexOf(getString(R.string.term_)),
                    spannableString.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

            txtTerms.text = spannableString
            txtTerms.movementMethod = LinkMovementMethod.getInstance()
            txtTerms.highlightColor = Color.TRANSPARENT

            this.edtOtp1.observeTextChange {
                if (this.edtOtp1.text.toString()
                        .isNotEmpty() && isValidate()
                ) //size as per your requirement
                {
                    this.edtOtp2.requestFocus()
                    callApi(1)

                }
            }

            this.edtOtp2.observeTextChange {
                if (this.edtOtp2.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp3.requestFocus()
                    callApi(1)
                }
            }

            this.edtOtp3.observeTextChange {
                if (this.edtOtp3.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp4.requestFocus()
                    callApi(1)
                }
            }

            this.edtOtp4.observeTextChange {
                if (this.edtOtp4.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp5.requestFocus()
                    callApi(1)
                }
            }

            this.edtOtp5.observeTextChange {
                if (this.edtOtp5.text!!.isNotEmpty() && isValidate()) //size as per your requirement
                {
                    this.edtOtp6.requestFocus()
                    callApi(1)
                }
            }

            this.edtOtp6.observeTextChange {
                if (this.edtOtp6.text!!.isNotEmpty() && isValidate()) {
                    callApi(1)
                }
            }

            this.edtOtp1.setOnKeyListener(GenericKeyEvent(this.edtOtp1, null))
            this.edtOtp2.setOnKeyListener(GenericKeyEvent(this.edtOtp2, this.edtOtp1))
            this.edtOtp3.setOnKeyListener(GenericKeyEvent(this.edtOtp3, this.edtOtp2))
            this.edtOtp4.setOnKeyListener(GenericKeyEvent(this.edtOtp4, this.edtOtp3))
            this.edtOtp5.setOnKeyListener(GenericKeyEvent(this.edtOtp5, this.edtOtp4))
            this.edtOtp6.setOnKeyListener(GenericKeyEvent(this.edtOtp6, this.edtOtp5))
            btnResendOtp.setOnClickListener(this@AadharVerificationOtpActivity)

        }
    }

    private fun isValidate(): Boolean {
        var isError = true
        when {
            Util.isEmptyText(binding.edtOtp1) -> {
                isError = false
                binding.edtOtp1.requestFocus()
            }

            Util.isEmptyText(binding.edtOtp2) -> {
                isError = false
                binding.edtOtp2.requestFocus()
            }

            Util.isEmptyText(binding.edtOtp3) -> {
                isError = false
                binding.edtOtp3.requestFocus()
            }

            Util.isEmptyText(binding.edtOtp4) -> {
                isError = false
                binding.edtOtp4.requestFocus()
            }

            Util.isEmptyText(binding.edtOtp5) -> {
                isError = false
                binding.edtOtp5.requestFocus()
            }

            Util.isEmptyText(binding.edtOtp6) -> {
                isError = false
                binding.edtOtp6.requestFocus()
            }

        }
        return isError
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

    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
                this,
                ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        userViewModel.verifyAadharOtpApiResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                Pref.setStringValue(Pref.PREF_AADHAR_VERIFIED,"1") // Set 1 after verified

                Util.showToastMessage(this,it.msg,true)
//                startActivity(Intent(this,MainActivity::class.java))
                openA<VerificationDoneActivity> { putExtra(IS_FROM, ActivityIsFrom.FROM_MENU) }
                finishAffinity()

            } else {
                Util.showToastMessage(this,it.msg,true)
            }
        }

        userViewModel.getAadharOtpApiResponse.observe(this) {
            Util.dismissProgress()
            Util.showToastMessage(this,it.msg,true)

        }
    }

    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this,true)
            when (tag) {
                1 -> {

                    val otp = (Util.getTextValue(binding.edtOtp1)
                    + Util.getTextValue(binding.edtOtp2)
                    + Util.getTextValue(binding.edtOtp3)
                    + Util.getTextValue(binding.edtOtp4)
                    + Util.getTextValue(binding.edtOtp5)
                    + Util.getTextValue(binding.edtOtp6))

                    userViewModel.verifyAadharOtpApiRequest(this,otp)

                }

                2 -> {
                    userViewModel.getAadharOtpApiRequest(this,aadharNumber )
                }
            }
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.btnResendOtp -> {
                callApi(2)
            }
        }
    }
}