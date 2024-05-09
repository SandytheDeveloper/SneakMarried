package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivitySectionBreakerBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import java.util.*

class SectionBreakerActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySectionBreakerBinding
    private lateinit var questionViewModel: QuestionViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySectionBreakerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initialization() {
        binding.customSubHeader.get().progressBar.gone()
        binding.customSubHeader.get().btnLeft.visible()
        binding.customSubHeader.get().llSkipAll.visibleIf(mainObjList[CATEGORY_ID].isSkippable == "1")

        Util.checkNotificationPermission(this)


        if (intent.hasExtra(IS_FROM)) {
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        }


        allApiResponses()

        try {
            binding.txtBottomTagLine.text = mainObjList[CATEGORY_ID].bottomTagLine
        } catch (e:Exception){
            if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1"){
                startActivity(Intent(this,StepsActivity::class.java))
                finishAffinity()
            } else {
                finish()
            }
            return
        }

        /*binding.txtTagLine.text = Util.applyCustomFonts(
            this@SectionBreakerActivity,
            null,
            mainObjList[CATEGORY_ID].tagLine,
            if (mainObjList[CATEGORY_ID].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].handWrittenTexts[0] else "",
            color = R.color.color_black
        )*/
        binding.txtTagLine.text =
            mainObjList[CATEGORY_ID].tagLine

        binding.customSubHeader.get().btnLeft.setOnClickListener(this)
        binding.btnAdd.setOnClickListener {
            binding.btnAdd.isEnabled = false
            if (mainObjList[CATEGORY_ID].questionList.isNotEmpty()) {
                LAST_POS = -1
                Util.manageTemplate(this@SectionBreakerActivity)
            } else {
                callApi()
            }
        }

        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CREATE_PROFILE) {
            startActivity(Intent(this@SectionBreakerActivity, MainActivity::class.java))
            finishAffinity()
        } else {
            startActivity(Intent(this@SectionBreakerActivity, StepsActivity::class.java))
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLeft -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            questionViewModel.getQuestionListApiRequest(
                this@SectionBreakerActivity,
                mainObjList[CATEGORY_ID].categoryId
            )
        }
    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.getQuestionsResponse.observe(this@SectionBreakerActivity) {
            Util.dismissProgress()
            Util.manageTemplate(this@SectionBreakerActivity, isFrom = isFrom)
            finishAffinity()
        }
    }
}
