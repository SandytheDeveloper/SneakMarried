package com.commonfriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityFeedbackBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.invisible
import com.commonfriend.utils.visible
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class FeedbackActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var userViewModel: UserViewModel
    private var isCallApi: Boolean = false
    private var currentWords = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        logForCurrentScreen("feedback", Screens.FEEDBACK_SCREEN.value)
        initialization()
    }

    @SuppressLint("SetTextI18n")
    private fun initialization() {
        allApiResponses()
        with(binding.customHeader.get()) {
            llMain.setBackgroundColor(
                ContextCompat.getColor(
                    this@FeedbackActivity,
                    R.color.color_base_grey
                )
            )
            this.txtMainTitle.visible()
            this.imgCross.visible()
            this.txtPageNO.gone()
            this.progressBar.gone()
            this.view.invisible()
            this.btnLeft.invisible()
            this.txtMainTitle.text = resources.getString(R.string.feedback)
        }
        binding.btnDone.isClickable = false
        binding.btnDone.setOnClickListener {
            if (!Util.isEmptyText(binding.edtFeedback))
                callApi(1)
        }


        binding.edtFeedback.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnDone.setBackgroundResource(if (s!!.isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                binding.btnDone.setTextColor(
                    ContextCompat.getColor(
                        this@FeedbackActivity,
                        if (s.isNotEmpty()
                        ) R.color.color_black else R.color.color_white
                    )
                )
                binding.btnDone.isClickable = s.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                currentWords = s!!.toString().trim().replace("\n"," ").split(" ").size
                binding.txtWordCount.text = "${ 100 - currentWords } words left"

                if (currentWords > 100) {
                    s.delete(s.length - 1, s.length)
                }
            }
        })

        binding.edtFeedback.requestFocus()
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isCallApi)
                        return
                    isCallApi = true
                    Util.showProgress(this)
                    userViewModel.sendFeedbackApiRequest(
                        this,
                        binding.edtFeedback.text.toString()
                    )
                }
            }
        }
    }


    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        userViewModel.sendFeedbackApiResponse.observe(this@FeedbackActivity) {
            isCallApi = false
            finish()
        }
    }

}
