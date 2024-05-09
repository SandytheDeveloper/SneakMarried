package com.commonfriend.template

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.MainApplication
import com.commonfriend.R
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivityElevenTemplateBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ElevenTemplateActivity : BaseActivity(), OnClickListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {
    private lateinit var binding: ActivityElevenTemplateBinding
    private lateinit var mainObj: JSONObject
    private var answerValue = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var questionViewModel: QuestionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElevenTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }


    @SuppressLint("SetTextI18n", "NewApi")
    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        allApiResponses()

        binding.lengthPickerView.visibility =
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("tall")) View.VISIBLE else View.GONE

        binding.pickerView.visibility =
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("birthday")) View.VISIBLE else View.GONE

        binding.timeView.visibility =
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("time")) View.VISIBLE else View.GONE

        val today = Calendar.getInstance()


        binding.datePicker.init(
            today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)

        ) { _, year, month, day ->
            binding.buttonView.btnContinue.apply {
                isEnabled = true
                isClickable = true
            }
            answerValue = "$year/${month + 1}/$day"

        }

        binding.datePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        val maxDate = Calendar.getInstance()
        maxDate.add(
            Calendar.YEAR,
            -if (Pref.getStringValue(Pref.PREF_USER_GENDER, "") == "male") 21 else 18
        )

        val minDate = Calendar.getInstance()
        minDate.add(Calendar.YEAR, -74)
        binding.datePicker.maxDate = maxDate.timeInMillis
        binding.datePicker.minDate = minDate.timeInMillis


        val currentFormat = DateFormat.getDateTimeInstance().format(Date())


        val monthIndex = currentFormat.indexOf(Util.millisToDate(System.currentTimeMillis(), "MMM"))


        binding.lblFirst.text =
            resources.getString(if (monthIndex != -1 && monthIndex >= 1) R.string.day else R.string.month)
        binding.lblSecond.text =
            resources.getString(if (monthIndex != -1 && monthIndex >= 1) R.string.month else R.string.day)



        binding.numberPickerInches.setFormatter { p0 -> "$p0\"" }
        binding.numberPickerFeet.setFormatter { p0 -> "$p0'" }

        binding.numberPickerFeet.minValue = 4
        binding.numberPickerFeet.maxValue = 8

        binding.numberPickerInches.minValue = 0
        binding.numberPickerInches.maxValue = 11


        binding.numberPickerFeet.value = 5
        binding.numberPickerInches.value = 0

        binding.numberPickerFeet.displayedValues =
            arrayOf("4'", "5'", "6'", "7'", "8'")

        binding.numberPickerInches.displayedValues =
            arrayOf(
                "0\"",
                "1\"",
                "2\"",
                "3\"",
                "4\"",
                "5\"",
                "6\"",
                "7\"",
                "8\"",
                "9\"",
                "10\"",
                "11\"",
                "12\""
            )

        binding.numberPickerInches.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        binding.numberPickerFeet.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS

        binding.numberPickerFeet.wrapSelectorWheel = false
        binding.numberPickerInches.wrapSelectorWheel = false

        binding.numberPickerFeet.setOnValueChangedListener { _, _, _ ->
            binding.buttonView.btnContinue.isClickable = true
            binding.buttonView.btnContinue.isEnabled = true

        }


        binding.numberPickerInches.setOnValueChangedListener { _, _, _ ->
            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnContinue.isClickable = true
        }


        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->

            var hour = hour
            var am_pm = ""

            when {
                hour == 0 -> {
                    hour += 12
                    am_pm = "AM"
                }

                hour == 12 -> am_pm = "PM"
                hour > 12 -> {
                    hour -= 12
                    am_pm = "PM"
                }

                else -> am_pm = "AM"
            }



            answerValue =
                "${if (hour > 9) hour else "0$hour"}:${if (minute > 9) minute else "0$minute"} $am_pm"

            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnContinue.isClickable = true
        }

        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        binding.buttonView.btnContinue.isClickable = false


        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@ElevenTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.infoMessage.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage

        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@ElevenTemplateActivity,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            Util.print(">>>>>>>>>>>>>>>>>>>>>>ANSWER VALUE >>>>${answerValue}")

            mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                if (answerValue.contains("/")) Util.convertDateStringToString(
                    answerValue,
                    "yyyy/mm/dd",
                    "dd/mm/yyyy"
                ) else answerValue


            bundle = Bundle().apply {
                putString(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,mainObjList[CATEGORY_ID].questionList[LAST_POS].answer)
            }
            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,bundle)

            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                mainObjList[CATEGORY_ID].questionList[LAST_POS].answer)

            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
            {
                Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                {
                    finish()
                }
            }
            else
                Util.manageTemplate(this@ElevenTemplateActivity, isFrom)
        }
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@ElevenTemplateActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()

        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.buttonView.btnSkip.setOnClickListener(this)

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.isNotEmpty()) {
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.contains("/")) {
                val date = mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.split("/")
                binding.datePicker.updateDate(
                    date[2].toInt(),
                    date[1].toInt() - 1,
                    date[0].toInt()
                ) //yyyy-mm-dd
            } else if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.contains(":")) {

                val calendar = Calendar.getInstance()
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

                //if(mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.isNotEmpty())
                calendar.time =
                    timeFormat.parse(
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.replace(
                            "\u00A0",
                            " "
                        ).trim()
                    )!!

                binding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
                binding.timePicker.minute = calendar.get(Calendar.MINUTE)

            } else {
                val inches = (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.ifEmpty { "0" }
                    .toFloat()) % 12
                val feets = ((mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.ifEmpty { "0" }
                    .toFloat()) - inches) / 12
                binding.numberPickerFeet.value = feets.toInt()
                binding.numberPickerInches.value = inches.toInt()
            }

            binding.buttonView.btnContinue.isEnabled = true
            binding.buttonView.btnContinue.isClickable = true

            answerValue = mainObjList[CATEGORY_ID].questionList[LAST_POS].answer
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isEnabled = false
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("tall")) {
                    answerValue =
                        (((binding.numberPickerFeet.value) * 12) + binding.numberPickerInches.value).toString()
                }

                if (isFrom == ActivityIsFrom.FROM_EDIT && Pref.getStringValue(
                        Pref.PREF_PROFILE_CONFIRMATION,
                        ""
                    ) == "1"
                )
                    errorDialogComponent = ErrorDialogComponent(
                        this,
                        ErrorDialogComponent.ErrorDialogFor.CONFIRMING,
                        "Confirmation",
                        getString(R.string.are_you_sure),
                        this
                    ).apply {
                        this.show()
                    }
                else
                    saveData()
            }

            R.id.btnLeft -> onBackPress()
            R.id.btnSkip -> Util.manageTemplate(this)
        }
    }

    private fun saveData() {
        if(mainObjList[CATEGORY_ID].questionList[LAST_POS].answer!=answerValue) {
            mainObj = JSONObject()
            mainObj.put(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey, answerValue)
            callApi()
        }
        else
        {
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                finish()
            else
                Util.manageTemplate(this@ElevenTemplateActivity, isFrom)
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            Util.showProgress(this)
            questionViewModel.questionAnswerSaveApiRequest(this, mainObj)
        }
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {
                saveData()
            }

            "1" -> {
                binding.buttonView.btnContinue.isEnabled = true
            }
        }
    }
}