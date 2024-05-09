package com.commonfriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityContributeBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.gone
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.QuestionBankViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class ContributeActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding : ActivityContributeBinding
    private var currentPage = 1
    private var questionCurrentWords = 0
    private var optionACurrentWords = 0
    private var optionBCurrentWords = 0
    private lateinit var questionBankViewModel: QuestionBankViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContributeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }

    private fun initialization() {

        with(binding.customHeader.get()){
            btnLeft.gone()
            txtPageNO.gone()
            imgCross.visible()
            imgCross.setOnClickListener(this@ContributeActivity)
            llMain.setBackgroundColor(ContextCompat.getColor(this@ContributeActivity,R.color.color_base_grey))

            binding.customHeader.get().progressBar.max = 3
        }


        binding.edtQuestion.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnQuestionNext.setBackgroundResource(if (s!!.trim().isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                binding.btnQuestionNext.isClickable = s.trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                questionCurrentWords = s!!.length
                binding.txtQuestionWordCount.text = "${100 - questionCurrentWords} characters left"

            }
        })


        binding.edtOptionA.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnOptionANext.setBackgroundResource(if (s!!.trim().isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                binding.btnOptionANext.isClickable = s.trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                optionACurrentWords = s!!.length
                binding.txtOptionAWordCount.text = "${15 - optionACurrentWords} characters left"

            }
        })


        binding.edtOptionB.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnContribute.setBackgroundResource(if (s!!.trim().isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                binding.btnContribute.isClickable = s.trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                optionBCurrentWords = s!!.length
                binding.txtOptionBWordCount.text = "${15 - optionBCurrentWords} characters left"

            }
        })

        managePage(1)

        binding.btnQuestionNext.setOnClickListener(this)
        binding.btnOptionANext.setOnClickListener(this)
        binding.btnContribute.setOnClickListener(this)

        binding.btnQuestionNext.isClickable = false
        binding.btnOptionANext.isClickable = false
        binding.btnContribute.isClickable = false

        allApiResponses()

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    private fun onBackPress() {
        when(currentPage){
            2 -> managePage(1)
            3 -> managePage(2)
            else -> finish()
        }
    }

    private fun managePage(page : Int) {
        binding.llFirstPage.visibleIf(page == 1)
        binding.llSecondPage.visibleIf(page == 2)
        binding.llThirdPage.visibleIf(page == 3)
        binding.customHeader.get().progressBar.progress = page
        currentPage = page

        binding.txtQuestionTitle.text =
            when(page){
                2 -> getString(R.string.what_is_option_a)
                3 -> getString(R.string.what_is_option_b)
                else -> getString(R.string.what_is_the_question)
            }

        when(page){
            1 -> binding.edtQuestion.requestFocus()
            2 -> binding.edtOptionA.requestFocus()
            3 -> binding.edtOptionB.requestFocus()
        }
    }


    private fun allApiResponses() {

        questionBankViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionBankViewModel::class.java]


        questionBankViewModel.addContributeQuestionsResponse.observe(this@ContributeActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                Util.showLottieDialog(this, "done_lottie.json", wrapContent = true) {
                    finish()
                }
            }
        }

    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (showProgress) Util.showProgress(this)
                    binding.btnContribute.isClickable = false
                    questionBankViewModel.addContributeQuestionsApiRequest(
                        this@ContributeActivity,
                        Util.getTextValue(binding.edtQuestion),
                        Util.getTextValue(binding.edtOptionA),
                        Util.getTextValue(binding.edtOptionB)
                    )
                }
            }
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.imgCross -> {
                finish()
            }
            R.id.btnQuestionNext -> {
                managePage(2)
            }
            R.id.btnOptionANext -> {
                managePage(3)
            }
            R.id.btnContribute -> {
                callApi(1,true)
            }
        }
    }


}