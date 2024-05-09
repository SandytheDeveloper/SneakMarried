package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityAadharVerificationBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.DATA
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.observeTextChange
import com.commonfriend.utils.visible
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class AadharVerificationActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding : ActivityAadharVerificationBinding
    var count = 0
    private lateinit var userViewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAadharVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        logForCurrentScreen("aadhar_verification", Screens.AADHAR_VERIFICATION_SCREEN.value)
        initialization()
    }

    @SuppressLint("SetTextI18n")
    private fun initialization() {

        with(binding.customHeader) {
            get().btnLeft.gone()
            get().imgCross.visible()
            get().txtPageNO.gone()
            get().progressBar.gone()
        }

        binding.edtNumber.observeTextChange {
            binding.edtNumber.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                (resources.getDimension(if (it.isEmpty()) com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp))
            )

            val inputlength: Int = binding.edtNumber.text.toString().length

            if (count <= inputlength && (inputlength == 4 || inputlength == 9)){

                binding.edtNumber.setText(binding.edtNumber.text.toString() + " ")

                val pos = binding.edtNumber.text!!.length
                binding.edtNumber.setSelection(pos)

            } else if (count >= inputlength && (inputlength == 4 ||
                        inputlength == 9)) {
                binding.edtNumber.setText(binding.edtNumber.text.toString()
                    .substring(0, binding.edtNumber.text
                        .toString().length - 1))

                val pos = binding.edtNumber.text!!.length
                binding.edtNumber.setSelection(pos)
            }
            count = binding.edtNumber.text.toString().length

            binding.imgContinue.isClickable = (it.length == 14)
            binding.imgContinue.setColorFilter(
                ContextCompat.getColor(this, if (it.length == 14) R.color.color_blue else R.color.color_light_grey)
                , PorterDuff.Mode.SRC_IN)
        }

//        binding.txtMobileNumber.text = Util.applyCustomFonts(
//            this,
//            null,
//            resources.getString(R.string.let_s_get_your_nprofile_verified),
//            resources.getString(R.string.verified), R.color.color_brown
//        )

        allApiResponses()

        binding.imgContinue.setOnClickListener(this)
        binding.imgContinue.isClickable = false


    }

    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        userViewModel.getAadharOtpApiResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                Util.showToastMessage(this,it.msg,true)
                startActivity(Intent(this,AadharVerificationOtpActivity::class.java).putExtra(DATA,binding.edtNumber.text.toString().replace(" ","")))

            } else {
                Util.showToastMessage(this,it.msg,true)
            }
        }
    }

    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this,true)
            when (tag) {
                1 -> {
                    userViewModel.getAadharOtpApiRequest(this, binding.edtNumber.text.toString().replace(" ",""))
                }
            }
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.imgContinue -> {
                callApi(1)
            }
        }
    }
}