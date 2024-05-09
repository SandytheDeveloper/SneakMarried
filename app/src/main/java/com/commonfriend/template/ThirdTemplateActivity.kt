package com.commonfriend.template


import PaginationListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.adapter.ThirdTemplateAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject

//RELIGION,LINE OF WORK,
class ThirdTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private var selectedId = ""
    private var selectedOptionId = ""

    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private lateinit var thirdTemplateAdapter: ThirdTemplateAdapter
    private lateinit var suggestionDialogAdapter: SuggestionDialogAdapter
    private lateinit var mainObj: JSONObject
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var firstTime: Boolean = true
    private var pageNo: Int = 0

    private lateinit var questionViewModel: QuestionViewModel

    var lastPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Util.statusBarColor(this@ThirdTemplateActivity, window)

        binding.txtLocationName.setText("")

        initialization()
    }

    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)

        binding.customHeader.get().btnLeft.setOnClickListener(this)
        binding.customHeader.get().progressBar.progress = LAST_POS + 1
        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        suggestionDialogAdapter = SuggestionDialogAdapter(this, this)
        thirdTemplateAdapter = ThirdTemplateAdapter(this, this)

        allApiResponses()

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }


    private fun onBackPress() {
        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@ThirdTemplateActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {

        if(bottomSheetDialog!=null)
        {
            if(bottomSheetDialog!!.isShowing)
            {
                bottomSheetDialog!!.dismiss()
            }
        }
        bottomSheetDialog =
            BottomSheetDialog(this@ThirdTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)


        bottomSheetDialog!!.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding.root)
            show()
            setCancelable(true)
            window?.let { Util.statusBarColor(this@ThirdTemplateActivity, it) }
        }

        bottomScreenBinding.apply {

            rvLocation.visibility = View.VISIBLE

            edtSearch.requestFocus()

            txtHeaderTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

            txtOtherTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
            txtSuggestion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
            txtChooseReligion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle

            rvLocation.layoutManager =
                LinearLayoutManager(this@ThirdTemplateActivity, RecyclerView.VERTICAL, false)
            rvLocation.adapter = thirdTemplateAdapter



//            txtHeaderTitle.visibleIf(suggestionDialogAdapter.objList.isNotEmpty())



            this.rvLocation.addOnScrollListener(object :
                PaginationListenerAdapter(this.rvLocation.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED")
                    if (!isLoading) {
                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED")
                        if (!firstTime) {
                            callApi(1)
                        }
                    }
                }

                override fun isLastPage(): Boolean {
                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED>>lastPage${isLastPage}")
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED>>>>>>isLoadibng${isLoading}")
                    return isLoading
                }
            })

            rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@ThirdTemplateActivity, RecyclerView.HORIZONTAL, false)
            rvTextSuggestion.adapter = suggestionDialogAdapter


            // set Position Dialoge open
            thirdTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
            thirdTemplateAdapter.objList.find { it.id == selectedOptionId }?.isSelected = 1
            thirdTemplateAdapter.notifyDataSetChanged()


            txtClear.setOnClickListener {
                edtSearch.setText("")
            }
            imgLeft.setOnClickListener { edtSearch.setText("") }
//            llOther.setOnClickListener(this@ThirdTemplateActivity)

            txtSuggestion.visibility = View.GONE
//            txtHeaderTitle.visibility = View.GONE
            txtChooseReligion.visibility = View.GONE
            rvTextSuggestion.visibility = View.GONE
            llSearch.visibility = View.GONE


            btnDialogContinue.setOnClickListener(this@ThirdTemplateActivity)

            btnCancel.setOnClickListener {
                bottomSheetDialog!!.dismiss()
            }
        }


    }

    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding.btnDialogContinue.apply {
            isClickable = shouldShow
            isEnabled = shouldShow

            if (shouldShow) {
                setOnClickListener {
                    binding.txtLocationName.setText(thirdTemplateAdapter.objList.find { it.isSelected == 1 }?.name)
                    binding.txtLocationDialog.hint = if(binding.txtLocationName.text!!.isEmpty())  mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle  else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

                    if (thirdTemplateAdapter.objList.any { it.isSelected == 1 }) {
                        selectedOptionId =
                            thirdTemplateAdapter.objList.find { it.isSelected == 1 }!!.id
                        binding.txtLocationName.setText(thirdTemplateAdapter.objList.find { it.isSelected == 1 }!!.name)
                        saveData()
                    }

                    binding.buttonView.btnContinue.isEnabled = true
                    bottomSheetDialog!!.dismiss()
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE

        binding.buttonView.btnSkip.setOnClickListener(this)

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@ThirdTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )

        binding.txtLocationDialog.hint =if(binding.txtLocationName.text!!.isEmpty())  mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle  else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())
//            (LAST_POS + 1).toString() + "/" + mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        isLastPage = false
        isLoading = false
        pageNo = 0
        callApi(1)
//
//        binding.buttonView.btnContinue.isEnabled = true
//        binding.buttonView.btnContinue.isClickable = true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {

            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {

                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels;
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }


            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false

                saveData()
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                thirdTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0

                thirdTemplateAdapter.objList[lastPos].isSelected = 1
                thirdTemplateAdapter.notifyDataSetChanged()

                mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString =
                    thirdTemplateAdapter.objList[lastPos].name

                showButton(true)
                bottomScreenBinding.btnDialogContinue.performClick()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                binding.txtLocationName.setText(suggestionDialogAdapter.objList[lastPos].optionName)
                bottomSheetDialog!!.dismiss()

                selectedOptionId = suggestionDialogAdapter.objList[lastPos].id

                thirdTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                thirdTemplateAdapter.objList.find { it.id == selectedOptionId }?.isSelected = 1

                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString.contains("<"))
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString =
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString.replace(
                            "<", getString(R.string.more_than)
                        )
                binding.buttonView.btnContinue.isEnabled = true
                saveData()

            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }
        }
    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                thirdTemplateAdapter.addData(it.data[0].arrayData, pageNo <= 0)
                if (pageNo == 0)
                    suggestionDialogAdapter.addData(it.data[0].suggestestionList)

                isLastPage = it.data.isNullOrEmpty()
                isLoading = false
                firstTime = false

                if (it.data[0].arrayData.isNotEmpty())
                    pageNo += 1

                if (it.data[0].selectedDataArray!!.isNotEmpty() && thirdTemplateAdapter.objList.none { it.isSelected == 1 }) {
                    thirdTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                    thirdTemplateAdapter.objList.find { it1 -> it1.id == it.data[0].selectedDataArray!![0].id }?.isSelected =
                        1
                    thirdTemplateAdapter.notifyDataSetChanged()
                    binding.txtLocationName.setText(it.data[0].selectedDataArray!![0].name)
                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnContinue.isClickable = true
                    selectedId = it.data[0].selectedDataArray!![0].id
                    selectedOptionId = selectedId
                    binding.txtLocationDialog.hint =if(binding.txtLocationName.text!!.isEmpty())  mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle  else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()

            bundle = Bundle().apply {
                putString(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,Util.getTextValue(binding.txtLocationName))
            }
            firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,bundle)

            MainApplication.firebaseAnalytics.setUserProperty(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                Util.getTextValue(binding.txtLocationName)
            )

            Util.print("BKL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString()
            }")



            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty()
                && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_profession") {

                WORK_AS =
                    if (thirdTemplateAdapter.objList.any { it.id == selectedOptionId }) thirdTemplateAdapter.objList.find { it.id == selectedOptionId }!!.hasSubsidary else suggestionDialogAdapter.objList.find { it.id == selectedOptionId }!!.hasSubsidary


                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
                {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
//                    finish()
                        if (isFrom == ActivityIsFrom.FROM_EDIT){
                            if(mainObjList.size > 1 && WORK_AS == "1"){

                                CATEGORY_ID += 1
                                LAST_POS = -1
                                Util.manageTemplate(this@ThirdTemplateActivity, isFrom)
                                finish()

                            } else {
                                finish()
                            }

                        } else {
                            finish()
                        }
                    }
                }
                else
                    Util.manageTemplate(this@ThirdTemplateActivity, isFrom)



            } else {

                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
                {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
                        finish()
                    }
                }
                else
                    Util.manageTemplate(this@ThirdTemplateActivity, isFrom)
            }
        }

    }

    private fun saveData() {

        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>$selectedId>>>>>>>>>>>>>>>>>>>>>>${selectedOptionId}")
//        if (selectedId == selectedOptionId) {
//            WORK_AS =
//                if (thirdTemplateAdapter.objList.any { it.id == selectedOptionId }) thirdTemplateAdapter.objList.find { it.id == selectedOptionId }!!.hasSubsidary else suggestionDialogAdapter.objList.find { it.id == selectedOptionId }!!.hasSubsidary
//
//            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>MANAGE TEMPLETE >>>>>>>>>>>>>>>>>>>>>>${selectedOptionId}")
//            Util.manageTemplate(this@ThirdTemplateActivity, isFrom)
//        } else {
            mainObj = JSONObject()
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                selectedOptionId
            )
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObj}")
            callApi(2)
//        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isLoading)
                        return
                    if (showProgress)
                        Util.showProgress(this)
                    isLoading = true
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        "",
                        pageNo,
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
