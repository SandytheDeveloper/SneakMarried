package com.commonfriend.template


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.databinding.ActivityFirstTemplateBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONObject

class FirstTemplateActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityFirstTemplateBinding
    private var mainJsonObj: JSONObject? = null
    private lateinit var questionViewModel: QuestionViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Util.statusBarColor(this, window)

        initialization()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        binding.customHeader.get().progressBar.progress = LAST_POS + 1


        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.INVISIBLE
        binding.buttonView.btnSkip.setOnClickListener(this)
        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName


        binding.txtQuestion.text = Util.applyCustomFonts(
            this@FirstTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
            color = R.color.color_black
        )

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.isNotEmpty()) {
            val names = mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.split(HASH_SEPERATOR)
            if (names.isNotEmpty())
                binding.edtFirstName.setText(names[0])
            if (names.size >= 2)
                binding.edtLastName.setText(names[1])
        }
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.saveAnswerResponse.observe(this@FirstTemplateActivity)
        {
            if (it.success == 1) {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                    Util.getTextValue(binding.edtFirstName) + HASH_SEPERATOR + Util.getTextValue(
                        binding.edtLastName
                    )
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ANSEWER NAME IS>>>>>>>${mainObjList[CATEGORY_ID].questionList[LAST_POS].answer}")
                firebaseEventLog(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,
                    mainJsonObj?.toBundle()
                )

                if (isFrom == ActivityIsFrom.FROM_EDIT) {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
                        finish()
                    }
                } else
                    Util.manageTemplate(this@FirstTemplateActivity, isFrom)
            }
        }
    }


    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        allApiResponses()

//        binding.edtFirstName.filters = arrayOf(EmojiInputFilter())
//        binding.edtLastName.filters = arrayOf(EmojiInputFilter())

        binding.edtFirstName.observeTextChange {
            binding.edtFirstName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(
                    if (it.isEmpty())
                        com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp
                )
            )
            changeContinueBtn(
                it.isNotEmpty() && Util.getTextValue(binding.edtLastName).isNotEmpty()
            )

        }

        binding.edtLastName.observeTextChange {
            binding.edtLastName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(
                    if (it.isEmpty())
                        com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp
                )
            )

            changeContinueBtn(
                it.isNotEmpty() && Util.getTextValue(binding.edtFirstName).isNotEmpty()
            )

        }
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        activityOnBackPressed(this, this) {
            onBackPress()
        }
    }

    private fun changeContinueBtn(clickable: Boolean) {
        with(binding.buttonView.btnContinue) {
            isClickable = clickable
            isEnabled = clickable
        }
    }


    private fun onBackPress() {
        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@FirstTemplateActivity)
        }
    }


    private fun isValidate(): Boolean {
        var isError = true
        when {
            Util.isEmptyText(binding.edtFirstName) -> {
                isError = false
            }

            Util.isEmptyText(binding.edtLastName) -> {
                isError = false
            }


        }

        return isError
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                if (isValidate()) {
                    saveData()
                }
            }

            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }


        }

    }

    private fun saveData() {
        mainJsonObj = JSONObject()
        val answer =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.split(HASH_SEPERATOR)
        if (answer[0].trim() == Util.getTextValue(binding.edtFirstName) && answer[1].trim() == Util.getTextValue(
                binding.edtLastName
            )
        ) {
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                finish()
            else
                Util.manageTemplate(this@FirstTemplateActivity, isFrom)
        } else {
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.contains(HASH_SEPERATOR)) {
                val questionKeys =
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.split(HASH_SEPERATOR)
                for (i in questionKeys.indices) {
                    mainJsonObj!!.put(questionKeys[0], Util.getTextValue(binding.edtFirstName))
                    mainJsonObj!!.put(questionKeys[1], Util.getTextValue(binding.edtLastName))
                }
            } else {
                mainJsonObj!!.put(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    Util.getTextValue(binding.edtFirstName) + " " + Util.getTextValue(binding.edtLastName)
                )

            }
            callApi()
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            Util.showProgress(this)
            questionViewModel.questionAnswerSaveApiRequest(this, mainJsonObj!!)

        }
    }
}
