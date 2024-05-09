package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.StepsActivity
import com.commonfriend.adapter.MCQAdapter
import com.commonfriend.databinding.ActivityEightTenTemplateBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.json.JSONObject


//RelationShip Eating habit
class EightTenTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityEightTenTemplateBinding
    private lateinit var mcqAdapter: MCQAdapter
    private var optionButtonView: View? = null

    private lateinit var questionViewModel: QuestionViewModel
    var mainObj = JSONObject()

    private var adapterLastPos = -1

    private var savedIds: String = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    // template code 8,10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEightTenTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@EightTenTemplateActivity)
        }
    }

    private fun initialization() {


        if (intent.hasExtra(IS_FROM)) {
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        }


        allApiResponses()


        val layout = FlexboxLayoutManager(this).apply {
            this.justifyContent = JustifyContent.CENTER
        }

        try {
            binding.rvMcqOptions.layoutManager =
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].templateType == 23) layout else
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } catch (e:Exception){
            if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1"){
                startActivity(Intent(this, StepsActivity::class.java))
                finishAffinity()
            } else {
                finish()
            }
            return
        }

        mcqAdapter =
            MCQAdapter(this, this, mainObjList[CATEGORY_ID].questionList[LAST_POS].templateType)

        binding.rvMcqOptions.adapter = mcqAdapter

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@EightTenTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
            color = R.color.color_black
        )
//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.customHeader.get().btnLeft.setOnClickListener(this)

        callApi(1)


        binding.buttonView.btnContinue.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@EightTenTemplateActivity, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                mcqAdapter.addData(it.data[0].arrayData)

                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    mcqAdapter.objList.find { it1 ->
                        it1.id == (it.data[0].selectedDataArray?.get(0)?.id ?: "")
                    }?.isSelected = 1
                    adapterLastPos =
                        mcqAdapter.objList.indexOf(mcqAdapter.objList.find { model -> model.isSelected == 1 })
                    savedIds = it.data[0].selectedDataArray!![0].id
                    binding.buttonView.btnContinue.isEnabled = true

                }
            }
        }


        questionViewModel.saveAnswerResponse.observe(this@EightTenTemplateActivity) {
            Util.dismissProgress()
            optionButtonView?.setOnClickListener(this)

            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_relationship") {
                RELATIONSHIP_STATUS = mcqAdapter.objList[adapterLastPos].hasSubsidary
            }

            bundle = Bundle().apply {
                putString(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    mcqAdapter.objList[adapterLastPos].name
                )
            }
            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)

            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                mcqAdapter.objList[adapterLastPos].name)


            binding.buttonView.btnContinue.isEnabled = true
            if ((isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) && RELATIONSHIP_STATUS != "1")
            {
                Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                {
                    finish()
                }
            }
            else
                Util.manageTemplate(this@EightTenTemplateActivity, isFrom)
        }

    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName


//        mcqAdapter =
//            MCQAdapter(this, this, mainObjList[CATEGORY_ID].questionList[LAST_POS].templateType)
//
//        binding.rvMcqOptions.adapter = mcqAdapter


//        binding.customHeader.get().txtSkip.visibility =
//            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.buttonView.btnSkip.setOnClickListener(this@EightTenTemplateActivity)
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.INVISIBLE
        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())
//            (LAST_POS + 1).toString() + "/" + mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE


        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.llOption -> {
                optionButtonView = view
                optionButtonView?.isEnabled = optionButtonView?.isEnabled == true
                adapterLastPos = view.tag.toString().toInt()
                mcqAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                mcqAdapter.objList[adapterLastPos].isSelected = 1
                mcqAdapter.notifyDataSetChanged()
                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.performClick()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isEnabled = false
                saveData()
            }

        }
    }

    private fun saveData() {
        optionButtonView?.setOnClickListener(null)
        if (mcqAdapter.objList[adapterLastPos].id != savedIds) {
            mainObj = JSONObject()
            RELATIONSHIP_STATUS = mcqAdapter.objList[adapterLastPos].hasSubsidary
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                mcqAdapter.objList[adapterLastPos].id
            )
            callApi(2)
        } else {
            binding.buttonView.btnContinue.isEnabled = true
            RELATIONSHIP_STATUS = mcqAdapter.objList[adapterLastPos].hasSubsidary
            if ((isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)) {
                if (RELATIONSHIP_STATUS != "1")
                    finish()
                else {
                    Util.manageTemplate(
                        this@EightTenTemplateActivity,
                        if (Pref.getStringValue(
                                Pref.PREF_PROFILE_CONFIRMATION,
                                "0"
                            ) == "0"
                        ) ActivityIsFrom.NORMAL else isFrom
                    )
                }
            } else
                Util.manageTemplate(this@EightTenTemplateActivity, isFrom)
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
}