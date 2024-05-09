package com.commonfriend.template

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivityTwentyFourTemplateBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CATEGORY_ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.LAST_POS
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.invisibleIf
import com.commonfriend.utils.mainObjList
import com.commonfriend.utils.removeString
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONObject

//networth,income
class TwentyFourTemplateActivity : BaseActivity(), View.OnClickListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivityTwentyFourTemplateBinding
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var questionViewModel: QuestionViewModel
    private var arrayData: ArrayList<GeneralModel> = ArrayList()
    var mainObj = JSONObject()
    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwentyFourTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }

    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        Util.statusBarColor(this, window)
        allApiResponses()

        binding.buttonView.btnContinue.isEnabled = true
        binding.buttonView.btnSkip.isEnabled = true
        binding.txtQuestion.text = Util.applyCustomFonts(
            this@TwentyFourTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )


        binding.infoMessage.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage


        binding.seekBar.addOnChangeListener { _, value, _ ->

            selectedPosition = value.toInt()
            setSeekBar()
            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnSkip.isEnabled = true

        }

        binding.customHeader.get().btnLeft.setOnClickListener(this)
        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName
        binding.customHeader.get().progressBar.progress = LAST_POS + 1
        binding.customHeader.get().progressBar.visibility = View.VISIBLE
        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        callApi(1)

        binding.customHeader.get().btnLeft.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.buttonView.btnContinue.isEnabled = false
        binding.btnIncrease.setOnClickListener(this)
        binding.btnDecrease.setOnClickListener(this)
        binding.buttonView.btnSkip.setOnClickListener(this)
        binding.buttonView.btnSkip.isEnabled = false

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    private fun setSeekBar() {
        binding.txtIncome.text = arrayData[selectedPosition].name
        binding.txtIncome.textSize = selectedPosition + 20f
    }

    private fun onBackPress() {

//        binding.buttonView.btnSkip.visibility =
//            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
//        binding.buttonView.btnSkip.setOnClickListener(this)

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@TwentyFourTemplateActivity)
        }
    }

    // before (prefer not to say)
    private fun saveData2(preferNotToSay : Boolean = false) {

        if (preferNotToSay) {
            mainObj = JSONObject()
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                arrayData[0].id
            )
            mainObj.put(
                "prefer_not_to_say",
                "1"
            )
            callApi(2)

            return
        }

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer == arrayData[selectedPosition].id) {
            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnSkip.isEnabled = true

            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)



            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
                finish()
            else
                Util.manageTemplate(this@TwentyFourTemplateActivity, isFrom)
        } else {
            mainObj = JSONObject()
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                arrayData[selectedPosition].id
            )
            mainObj.put(
                "prefer_not_to_say",
                "0"
            )
            callApi(2)
        }
    }

    private fun saveData(preferNotToSay: Boolean = false) { // preferNotToSay key only used for Net Worth

        firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)

        mainObj = JSONObject()
        mainObj.put(
            mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
            arrayData[if (preferNotToSay) 0 else selectedPosition].id
        )
        mainObj.put(
            "prefer_not_to_say",
            if (preferNotToSay) "1" else "0"
        )
        callApi(2)

    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@TwentyFourTemplateActivity, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnSkip.isEnabled = true
            if (it.data.isNotEmpty()) {
                arrayData = it.data[0].arrayData
                binding.seekBar.valueTo = arrayData.lastIndex.toFloat()

                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    selectedPosition =
                        arrayData.indexOf(arrayData.find { model -> model.id == it.data[0].selectedDataArray!![0].id })
                    binding.seekBar.value = selectedPosition.toFloat()
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                        it.data[0].selectedDataArray!![0].id
                }
                setSeekBar()

                binding.buttonView.btnSkip.invisibleIf(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey != "candidate_net_worth")
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey == "candidate_net_worth") {
                    binding.buttonView.btnSkip.text = getString(R.string.prefer_not_to_say)
                }

                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey == "candidate_net_worth" &&
                    Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED,"").toString() != "1" &&
                    isFrom != ActivityIsFrom.FROM_EDIT) {

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            showErrorBottomDialog()
                        }, 500
                    )
                }

            }
        }


        questionViewModel.saveAnswerResponse.observe(this@TwentyFourTemplateActivity) {
            Util.dismissProgress()


            bundle = Bundle().apply {
                putString(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    arrayData[selectedPosition].name.replace("₹", "")
                )
            }
            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnSkip.isEnabled = true
            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)
            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                arrayData[selectedPosition].name.replace("₹", "")
            )


            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
            {
                Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                {
                    finish()
                }
            }
            else
                Util.manageTemplate(this@TwentyFourTemplateActivity, isFrom)
        }

    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }

                2 -> {
                    Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(this, mainObj)
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnContinue -> {

                binding.buttonView.btnContinue.isEnabled = false
                binding.buttonView.btnSkip.isEnabled = false
                saveData()

            }

            R.id.btnSkip -> {
                binding.buttonView.btnContinue.isEnabled = false
                binding.buttonView.btnSkip.isEnabled = false
                saveData(true) // For Net Worth Only
            }

            R.id.btnIncrease -> {
                if (binding.seekBar.value < arrayData.lastIndex) {
                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnSkip.isEnabled = true
                    binding.seekBar.value = binding.seekBar.value + 1
                }
            }

            R.id.btnDecrease -> {
                if (binding.seekBar.value > 0) {
                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnSkip.isEnabled = true
                    binding.seekBar.value = binding.seekBar.value - 1
                }
            }

            R.id.btnLeft -> {
                onBackPress()
            }
        }
    }


    private fun showErrorBottomDialog() {
        errorDialogComponent = ErrorDialogComponent(this,
            ErrorDialogComponent.ErrorDialogFor.SURETY,
            getString(R.string.about_net_worth),
            getString(R.string.family_net_worth_is_what_you_and_your_family_owns),
            this
        ).apply {
            this.show()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onItemClick(itemID: String, isFrom:ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {

            }
        }
    }
}