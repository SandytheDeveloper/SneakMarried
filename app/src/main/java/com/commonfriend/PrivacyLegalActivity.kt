package com.commonfriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityPrivacyLegalBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Util
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class PrivacyLegalActivity : BaseActivity() {
    private lateinit var binding: ActivityPrivacyLegalBinding
    private lateinit var userViewModel: UserViewModel
    private var isFrom: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyLegalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        isFrom = intent.getStringExtra(IS_FROM).toString()
        Util.statusBarColor(this@PrivacyLegalActivity, window)
        binding.customHeader.get().txtMainTitle.visibility = View.VISIBLE
        binding.customHeader.get().imgCross.visibility = View.VISIBLE
        binding.customHeader.get().txtPageNO.visibility = View.GONE
        binding.customHeader.get().progressBar.visibility = View.GONE
        binding.customHeader.get().view.visibility = View.VISIBLE
        binding.customHeader.get().btnLeft.visibility = View.INVISIBLE
        binding.customHeader.get().txtMainTitle.text =
            resources.getString(if (isFrom == "1") R.string.privacy else if(isFrom=="2") R.string.rules else R.string.terms_of_use)

        allApiResponses()
    }

    override fun onResume() {
        super.onResume()
        callApi(1)
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    userViewModel.privacyLegalRequestApi(this, isFrom)
                }
            }
        }
    }


    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        userViewModel.privacyLegalApiResponse.observe(this@PrivacyLegalActivity) {

            if (it.data != null && it.data.isNotEmpty()) {

                binding.apply {

                    webView.loadData(it.data[0].faqBody, "text/Html", "UTF-8")
                    webView.setBackgroundColor(
                        ContextCompat.getColor(
                            this@PrivacyLegalActivity,
                            R.color.color_white
                        )
                    )

                }
            }
        }
    }
}
