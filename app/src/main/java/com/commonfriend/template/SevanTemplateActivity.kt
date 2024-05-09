package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.base.BaseActivity
import com.commonfriend.EditProfileActivity
import com.commonfriend.MainApplication
import com.commonfriend.R
import com.commonfriend.StepsActivity
import com.commonfriend.adapter.MCQOptionAdapter
import com.commonfriend.databinding.ActivitySevanTemplateBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/*Gender,Kids,Disabled,*/
class SevanTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySevanTemplateBinding
    private lateinit var mcqOptionAdapter: MCQOptionAdapter
    private var adapterLastPos = -1
    private var optionButtonView: View? = null
    var mainJsonObj = JSONObject()
    private lateinit var questionViewModel: QuestionViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL


    // template code 7
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySevanTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }


    private fun onBackPress() {

        finish()
        Util.manageBackClick(this@SevanTemplateActivity)
    }

    @SuppressLint("SetTextI18n")
    private fun initialization() {

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        Util.print(">>>>>>>>>>>>>>>>>> ISFROM IS THE >>>>>${isFrom}")
        mcqOptionAdapter = MCQOptionAdapter(this, this)
        binding.rvMcqSingleOptions.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvMcqSingleOptions.adapter = mcqOptionAdapter

        try { // for handling crashes
            binding.txtQuestion.text = Util.applyCustomFonts(
                this@SevanTemplateActivity,
                null,
                mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "".lowercase(),
                color = R.color.color_black
            )
        } catch (e:Exception){
            if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1"){
                startActivity(Intent(this,StepsActivity::class.java))
                finishAffinity()
            } else {
                finish()
            }
            return
        }

        binding.customHeader.get().btnLeft.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.buttonView.btnSkip.setOnClickListener(this)
        allApiResponses()

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }

    private fun optionsViewList() {

        mcqOptionAdapter.objMainList = ArrayList()

        var obj = GeneralModel()
        obj.name = resources.getString(R.string.no)
        mcqOptionAdapter.objMainList.add(obj)

        obj = GeneralModel()
        obj.name = resources.getString(R.string.yes)
        mcqOptionAdapter.objMainList.add(obj)

        mcqOptionAdapter.addData(mcqOptionAdapter.objMainList)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName
        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_gender")
            callApi(1)
        else {
            optionsViewList()
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.isNotEmpty()) {
                when (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer) {
                    resources.getString(R.string.no).lowercase() -> {
                        mcqOptionAdapter.objList.find {
                            it.name.lowercase() == resources.getString(R.string.no).lowercase()
                        }?.isSelected = 1
                        binding.buttonView.btnContinue.isEnabled = true

                        adapterLastPos = 0
                    }

                    resources.getString(R.string.yes).lowercase() -> {
                        mcqOptionAdapter.objList.find {
                            it.name.lowercase() == resources.getString(R.string.yes).lowercase()
                        }?.isSelected = 1
                        binding.buttonView.btnContinue.isEnabled = true
                        adapterLastPos = 1
                    }
                }
                mcqOptionAdapter.notifyDataSetChanged()
            }
        }


        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )


        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())
//            (LAST_POS + 1).toString() +"/"+ mainObjList[CATEGORY_ID].questionList.size

    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                mcqOptionAdapter.addData(it.data[0].arrayData)
                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    mcqOptionAdapter.objList.find { it1 ->
                        it1.id == it.data[0].selectedDataArray?.get(0)?.id
                    }?.isSelected = 1
                    adapterLastPos =
                        mcqOptionAdapter.objList.indexOf(mcqOptionAdapter.objList.find { model -> model.isSelected == 1 })

                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                        it.data[0].selectedDataArray?.get(0)?.name!!

                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnContinue.isClickable = true
                    optionButtonView?.setOnClickListener(this)
                    optionButtonView?.isEnabled = true
                    mcqOptionAdapter.notifyDataSetChanged()
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this@SevanTemplateActivity) {
            Util.dismissProgress()
            optionButtonView?.isEnabled = true
            binding.buttonView.btnContinue.isClickable = true
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_gender") {
                Pref.setStringValue(
                    Pref.PREF_USER_GENDER,
                    mcqOptionAdapter.objList.find { it.isSelected == 1 }?.name!!.lowercase().trim()
                )

            }

            bundle = Bundle().apply {
                putString(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    mcqOptionAdapter.objList.find { it.isSelected == 1 }?.name!!
                )
            }
            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)

            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                mcqOptionAdapter.objList.find { it.isSelected == 1 }?.name!!
            )



            when (isFrom) {
                ActivityIsFrom.FROM_CHECKLIST -> {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
                        finish()
                    }
                }

                ActivityIsFrom.FROM_EDIT -> {
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("kids")) {

                        Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                        {
                            val intent =
                                Intent(this@SevanTemplateActivity, EditProfileActivity::class.java)
                            intent.putExtra(
                                IS_FROM,
                                if (Pref.getStringValue(
                                        Pref.PREF_PROFILE_CONFIRMATION,
                                        "0"
                                    ) == "0"
                                ) ActivityIsFrom.NORMAL else isFrom
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    } else
                        Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                        {
                            finish()
                        }
                }

                else -> Util.manageTemplate(this@SevanTemplateActivity, isFrom)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this)
            when (tag) {
                1 -> {
                    questionViewModel.questionOptionsListApiRequest(
                        this@SevanTemplateActivity,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }

                2 -> {
                    questionViewModel.questionAnswerSaveApiRequest(
                        this@SevanTemplateActivity,
                        mainJsonObj
                    )
                }
            }
        } else {
            binding.buttonView.btnContinue.isClickable = true
        }
    }

    private fun saveData() {
        optionButtonView?.setOnClickListener(null)
        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer.lowercase() != mcqOptionAdapter.objList[adapterLastPos].name.lowercase()) {
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.contains(HASH_SEPERATOR)) {
                val questionKeys =
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.split(HASH_SEPERATOR)
                for (i in questionKeys.indices) {
                    mainJsonObj.put(questionKeys[i], "")
                }
            } else {
                mainJsonObj.put(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_gender") mcqOptionAdapter.objList[adapterLastPos].id else mcqOptionAdapter.objList[adapterLastPos].name.lowercase()
                )
            }

            mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                mcqOptionAdapter.objList[adapterLastPos].name.lowercase()
            callApi(2)
        } else {

            when (isFrom) {
                ActivityIsFrom.FROM_CHECKLIST -> finish()
                ActivityIsFrom.FROM_EDIT -> {
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("kids")) {
                        val intent =
                            Intent(this@SevanTemplateActivity, EditProfileActivity::class.java)
                        intent.putExtra(
                            IS_FROM,
                            if (Pref.getStringValue(
                                    Pref.PREF_PROFILE_CONFIRMATION,
                                    "0"
                                ) == "0"
                            ) ActivityIsFrom.NORMAL else isFrom
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else
                        finish()
                }

                else -> Util.manageTemplate(this@SevanTemplateActivity, isFrom)
            }
        }

        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainJsonObj}")
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                saveData()
            }

            R.id.llOtherOption -> {
                optionButtonView = view
                optionButtonView?.isEnabled = false
                adapterLastPos = view.tag.toString().toInt()
                mcqOptionAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                mcqOptionAdapter.objList[adapterLastPos].isSelected =
                    if (mcqOptionAdapter.objList[adapterLastPos].isSelected == 0) 1 else 0
                mcqOptionAdapter.notifyDataSetChanged()
                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.performClick()
            }
        }
    }
}

