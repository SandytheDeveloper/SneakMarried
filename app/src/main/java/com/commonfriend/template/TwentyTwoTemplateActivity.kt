package com.commonfriend.template


import PaginationListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.window.OnBackInvokedDispatcher
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.adapter.InnerAdapterAdapter
import com.commonfriend.adapter.MCQOptionAdapter
import com.commonfriend.adapter.OptionHeaderAdapter
import com.commonfriend.adapter.QualificationListAdapter
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.adapter.ThirdTemplateAdapter
import com.commonfriend.databinding.ActivityTwentyTwoTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.models.QualificationModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject


class TwentyTwoTemplateActivity : BaseActivity(), OnClickListener,
    InnerAdapterAdapter.DegreeAdapterItemClickListener {

    /*education details page*/
    private lateinit var questionViewModel: QuestionViewModel
    private lateinit var binding: ActivityTwentyTwoTemplateBinding
    private lateinit var qualificationListAdapter: QualificationListAdapter
    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var bottomScreenBinding: DialogCastBinding

    private var lastPos = -1
    private var screenNo = 1
    private var pageNo = 0
    private var valuesChanged = false
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false
    private var degreeData: GeneralModel? = null

    private var selectedOption: QualificationModel? = null

    private lateinit var mainObj: JSONArray


    private var selectedDegreeId: String = ""
    private var selectedCollegeId: String = ""


    private lateinit var degreeHeaderListAdapter: OptionHeaderAdapter
    private lateinit var dialogCollegeAdapter: ThirdTemplateAdapter
    private lateinit var dialogSuggestionListAdapter: SuggestionDialogAdapter
    private lateinit var mcqOptionAdapter: MCQOptionAdapter
    private var searchKey = ""
    private var originalApiName = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwentyTwoTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    fun showNoDataString(shouldShow: Boolean) {
        bottomScreenBinding.llOther.visibleIf(shouldShow && screenNo == 2)
        bottomScreenBinding.txtNoDataFound.visibleIf(
            shouldShow && !Util.isEmptyText(
                bottomScreenBinding.edtSearch
            ) && screenNo == 1
        )
    }

    override fun onResume() {
        super.onResume()
        screenNo = 1
        manageMainContinueBtn()
        binding.buttonView.btnSkip.setOnClickListener(this)
        binding.buttonView.btnSkip.visibleIf(Util.isQuestionSkipable())

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {

            for (data in mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer) {
                selectedOption = QualificationModel().apply {
                    this.collegeId = data.candidateCollegeId
                    this.degreeId = data.candidateEducationLevel
                    this.degreeName = data.courseName
                    this.typeId = data.candidateCollegeType
                    this.isSpecialCertificate = data.isSpecialCertificate
                    this.isAddFromDesignation = data.isAddFromDesignation
                    this.isAddOther = data.isAddOther
                    this.typeName = when (data.candidateCollegeType) {
                        "2" -> resources.getString(R.string.full_time)
                        "1" -> resources.getString(R.string.other)
                        else -> ""
                    }
                    this.collegeName = data.otherName
                    removeDuplicate(this.degreeId)
                }

                qualificationListAdapter.objList.add(selectedOption!!)
                selectedOption = QualificationModel()
                manageScreen()
            }
            qualificationListAdapter.notifyDataSetChanged()
            binding.buttonView.btnContinue.isEnabled = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        selectedOption = QualificationModel()
        qualificationListAdapter = QualificationListAdapter(this, this)
        binding.rvSelectionList.layoutManager = LinearLayoutManager(this)
        binding.rvSelectionList.adapter = qualificationListAdapter
        binding.txtQuestion.text = Util.applyCustomFonts(
            this@TwentyTwoTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
            color = R.color.color_black
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.infoMessage.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage

        mcqOptionAdapter = MCQOptionAdapter(this, this)
        binding.rvMcqSingleOptions.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvMcqSingleOptions.adapter = mcqOptionAdapter
        optionsViewList()

        with(binding.customHeader.get()) {
            this.btnLeft.setOnClickListener(this@TwentyTwoTemplateActivity)
            this.txtTitle.text = mainObjList[CATEGORY_ID].categoryName
            this.progressBar.progress = LAST_POS + 1
            this.progressBar.visibility = View.VISIBLE
            this.progressBar.max = mainObjList[CATEGORY_ID].questionList.size
            this.txtPageNO.text =
                StringBuilder().append((LAST_POS + 1).toString()).append("/")
                    .append(mainObjList[CATEGORY_ID].questionList.size.toString())
        }
        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)

        degreeHeaderListAdapter = OptionHeaderAdapter(this, this)
        dialogCollegeAdapter = ThirdTemplateAdapter(this, this, 2)
        dialogSuggestionListAdapter = SuggestionDialogAdapter(this, this)


        originalApiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName

        allApiResponses()
        manageScreen()
        callApi(1)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    private fun optionsViewList() {

        var obj = GeneralModel()
        obj.name = resources.getString(R.string.full_time)
        obj.id = "2"
        mcqOptionAdapter.objMainList.add(obj)

        obj = GeneralModel()
        obj.name = resources.getString(R.string.other)
        obj.id = "1"
        mcqOptionAdapter.objMainList.add(obj)
        mcqOptionAdapter.addData(mcqOptionAdapter.objMainList)
    }


    private fun manageMainContinueBtn() {
        if (screenNo == 1) {
            binding.buttonView.btnContinue.isEnabled = qualificationListAdapter.objList.isNotEmpty()
            binding.buttonView.btnContinue.isClickable =
                qualificationListAdapter.objList.isNotEmpty()
        } else {
            binding.buttonView.btnContinue.isClickable =
                (mcqOptionAdapter.objList.any { it.isSelected == 1 })
            binding.buttonView.btnContinue.isEnabled =
                (mcqOptionAdapter.objList.any { it.isSelected == 1 })
        }
    }


    private fun manageScreen() {
        binding.rvSelectionList.visibility =
            if (screenNo == 1 && qualificationListAdapter.objList.isNotEmpty()) View.VISIBLE else View.GONE

        if (screenNo == 1 && qualificationListAdapter.objList.isEmpty()) {
            if (isFrom == ActivityIsFrom.FROM_EDIT) {
                binding.rlSpinner.visible()
            } else {
                binding.rlSpinner.visible()
            }
        } else {
            binding.rlSpinner.gone()
        }

        binding.rlMain.visibility = if (screenNo == 1) View.VISIBLE else View.GONE
        binding.rlTypeView.visibility = if (screenNo == 2) View.VISIBLE else View.GONE

        if (screenNo == 1) {
            mcqOptionAdapter.notifyDataSetChanged()
        }
        manageMainContinueBtn()

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {


        if (bottomSheetDialog.isNotNull() && bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()

        bottomSheetDialog =
            BottomSheetDialog(this@TwentyTwoTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        bottomSheetDialog!!.apply {
            searchKey = ""
            pageNo = 0
            setOnDismissListener {
                if (screenNo != 2) {
                    searchKey = ""
                    pageNo = 0
                    callApi(1)
                }
            }

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding.root)
            setCancelable(true)
            window?.let { Util.statusBarColor(this@TwentyTwoTemplateActivity, it) }

            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            show()
        }

        bottomScreenBinding.apply {
            edtSearch.requestFocus()
            this.btnDialogContinue.setOnClickListener(null)
            this.rvLocation.visibility = View.VISIBLE

            this.rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@TwentyTwoTemplateActivity, RecyclerView.HORIZONTAL, false)
            this.rvTextSuggestion.adapter = dialogSuggestionListAdapter


            this.rvLocation.layoutManager =
                LinearLayoutManager(this@TwentyTwoTemplateActivity, RecyclerView.VERTICAL, false)

            this.rvLocation.adapter =
                if (screenNo == 1) degreeHeaderListAdapter else dialogCollegeAdapter

            this.rvLocation.addOnScrollListener(object :
                PaginationListenerAdapter(this.rvLocation.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    if (!isLoading && screenNo == 1)
                        callApi(1)
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }

            })


            this.txtHeaderTitle.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].dialogTitle.split(HASH_SEPERATOR)[if (screenNo == 1) 0 else 1]
//            this.txtHeaderTitle.text =
//                if (screenNo == 1) "Select Degree" else "Write college name here"

            this.txtSuggestion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle.split(HASH_SEPERATOR)[if (screenNo == 1) 0 else 1]

            this.txtSuggestion.visibleIf(dialogSuggestionListAdapter.objList.isNotEmpty() && screenNo == 1)

            this.rvTextSuggestion.visibleIf(dialogSuggestionListAdapter.objList.isNotEmpty() && screenNo == 1)

            this.txtOtherTitle.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle

            this.txtChooseReligion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle

            dialogCollegeAdapter.objList.clear()
//            rvTextSuggestion.visibleIf(screenNo == 1)

            this.txtTitleClear.setOnClickListener {
                this.edtSurName.setText("")
            }
            this.txtClear.setOnClickListener {
                this.edtSearch.setText("")
                searchKey = ""
                pageNo = 0
                if (screenNo == 1) {
                    callApi(1, false)
                }
            }


            this.imgLeft.setOnClickListener { this.edtSearch.setText("") }
            this.llOther.setOnClickListener(this@TwentyTwoTemplateActivity)


            this.txtChooseReligion.visibility = View.GONE

            this.edtSearch.setOnClickListener {
                bottomScreenBinding.rvTextSuggestion.gone()
                bottomScreenBinding.txtSuggestion.gone()
            }


            this.edtSearch.observeTextChange(300L) { value ->
                pageNo = 0
                searchKey = value
                if (screenNo == 1 || value.isNotEmpty()) {
                    callApi(1, false)
                }
                this@apply.txtClear.visibleIf(value.isNotEmpty())
                this@apply.imgLeft.visibleIf(value.isNotEmpty())
                bottomScreenBinding.btnDialogContinue.apply {
                    isClickable = false
                    isEnabled = false
                }

                this@apply.imgSearch.visibleIf(value.isEmpty())




                if (screenNo == 2 && value.isEmpty()) {
                    dialogCollegeAdapter.objList.clear()
                    dialogCollegeAdapter.notifyDataSetChanged()
                }
            }

            this.edtSurName.observeTextChange { value ->
                this@apply.txtTitleClear.visibleIf(value.isNotEmpty())
                this@apply.btnDialogContinue.isEnabled = (value.isNotEmpty())
                bottomScreenBinding.btnDialogContinue.setOnClickListener(if (value.isNotEmpty()) this@TwentyTwoTemplateActivity else null)
            }

            this.btnCancel.setOnClickListener {
                bottomSheetDialog!!.dismiss()
            }
        }
    }


    private fun onBackPress() {
        if (screenNo == 1) {
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                finish()
            } else {
                finish()
                Util.manageBackClick(this@TwentyTwoTemplateActivity)
            }
        } else {
            screenNo = 1
            manageScreen()
            selectedOption = QualificationModel()
//            mcqOptionAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
            selectedCollegeId = ""
            selectedDegreeId = ""
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.llOtherOption -> {
                lastPos = view.tag.toString().toInt()

                mcqOptionAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                mcqOptionAdapter.objList[lastPos].isSelected = 1
                mcqOptionAdapter.notifyDataSetChanged()
                if (screenNo == 2) {
                    binding.buttonView.btnContinue.apply {
                        isClickable = true
                        isEnabled = true
                        performClick()
                    }
                }

            }


            R.id.btnSkip -> {
                Util.manageTemplate(this@TwentyTwoTemplateActivity)
            }


            R.id.btnLeft -> {
                onBackPress()
            }


            R.id.imgDelete -> {
                lastPos = view.tag.toString().toInt()
                qualificationListAdapter.objList.removeAt(lastPos)
                qualificationListAdapter.notifyDataSetChanged()
                valuesChanged = true
                manageScreen()
            }

            R.id.rlAddNew, R.id.imgAdd, R.id.addNewLayout -> {
                bottomSheetDialog()
                if (screenNo == 2)
                    searchKey = ""
                screenNo = 1
            }

            R.id.rlSpinner, R.id.txtLocationName, R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (screenNo == 2)
                    searchKey = ""
                screenNo = 1
            }


            R.id.btnContinue -> {
                if (screenNo == 1) {
                    saveData()
                } else {
                    screenNo = 2
                    if (selectedOption == null) selectedOption = QualificationModel()
                    selectedOption!!.typeId = mcqOptionAdapter.objList[lastPos].id
                    selectedOption!!.typeName = mcqOptionAdapter.objList[lastPos].name
                    bottomSheetDialog()
                }
            }


            R.id.llOther -> {
                bottomScreenBinding.txtOtherTitle.visibleIf(Util.getTextValue(bottomScreenBinding.txtOtherTitle).isNotEmpty())
                bottomScreenBinding.llCast.visibility = View.VISIBLE
                bottomScreenBinding.edtSurName.requestFocus()
//                bottomScreenBinding.txtHeaderTitle.text =
//                    getString(R.string.type_your_college_name_here)
                bottomScreenBinding.rlSearch.visibility = View.INVISIBLE
                bottomScreenBinding.llOther.visibility = View.GONE
                bottomScreenBinding.rvLocation.visibility = View.GONE
                if (screenNo == 1) {
                    binding.buttonView.btnContinue.isClickable = true
                    binding.buttonView.btnContinue.isEnabled = true
                }


            }


            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                Util.hideKeyBoard(this@TwentyTwoTemplateActivity, view)
                dialogCollegeAdapter.apply {
                    objList.find { it.isSelected == 1 }?.isSelected = 0
                    objList[lastPos].isSelected = 1
                    notifyDataSetChanged()
                }

                bottomScreenBinding.btnDialogContinue.apply {
                    isEnabled = true
                    setOnClickListener(this@TwentyTwoTemplateActivity)
                    performClick()
                }
            }


            R.id.btnDialogContinue -> {
                if (selectedOption == null)
                    selectedOption = QualificationModel()
                if (screenNo == 1) {
                    selectedOption!!.degreeName = degreeData?.name.toString()
                    selectedOption!!.degreeId = degreeData?.id.toString()
                    selectedOption!!.isSpecialCertificate =
                        degreeData?.isSpecialCertificate.toString()
                    bottomSheetDialog!!.dismiss()
                    if (selectedOption!!.isSpecialCertificate != "1") {
                        screenNo = if (screenNo == 1) 2 else 1
                        manageScreen()
                    } else {
                        removeDuplicate(selectedOption!!.degreeId)
                        qualificationListAdapter.objList.add(selectedOption!!)
                        qualificationListAdapter.notifyDataSetChanged()
                        Util.print("<>>><><><><><>><SIZE OF ARRAY SI>>>>>>>>>>${qualificationListAdapter.objList.size}")
                        selectedOption = QualificationModel()
                    }

                } else {
                    if (!Util.isEmptyText(bottomScreenBinding.edtSurName)) {
                        selectedOption!!.otherName =
                            Util.getTextValue(bottomScreenBinding.edtSurName)
                        selectedOption!!.collegeName = selectedOption!!.otherName
                        selectedOption!!.isAddOther = "1"
                    } else {
                        selectedOption!!.collegeId =
                            dialogCollegeAdapter.objList.find { it.isSelected == 1 }?.id.toString()
                        selectedOption!!.collegeName =
                            dialogCollegeAdapter.objList.find { it.isSelected == 1 }?.name.toString()
                        selectedOption!!.isAddOther = "0"
                    }
//                    removeDuplicate(selectedOption!!.degreeId)
                    qualificationListAdapter.objList.add(selectedOption!!)

//                    for (i in 0 until qualificationListAdapter.objList.size) {
//                        Util.print(">>>>>>>>>>>>>>>>${qualificationListAdapter.objList[i].collegeName}")
//                        Util.print(">>>>>>>>>>>>>>>>${qualificationListAdapter.objList[i].isAddOther}")
//                        Util.print(">>>>>>>>>>>>>>>>${qualificationListAdapter.objList[i].isAddOther}")
//                    }
                    qualificationListAdapter.notifyDataSetChanged()
                    bottomScreenBinding.edtSurName.setText("")
                    selectedOption = QualificationModel()

                    bottomSheetDialog!!.dismiss()
                    screenNo = if (screenNo == 1) 2 else 1
                    manageScreen()
//                    pageNo = 0
//                    callApi(1)
                }


            }


            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                if (selectedOption == null)
                    selectedOption = QualificationModel()

                if (screenNo == 1) {
                    degreeHeaderListAdapter.objList.find { it.id == dialogSuggestionListAdapter.objList[lastPos].id }?.isSelected =
                        1
                    selectedOption!!.degreeName =
                        dialogSuggestionListAdapter.objList[lastPos].optionName
                    selectedOption!!.degreeId = dialogSuggestionListAdapter.objList[lastPos].id
                    selectedOption!!.isSpecialCertificate =
                        dialogSuggestionListAdapter.objList[lastPos].isSpecialCertificate
                }

                binding.buttonView.btnContinue.isClickable = true
                bottomSheetDialog!!.dismiss()
                mcqOptionAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                if (selectedOption!!.isSpecialCertificate != "1") {
                    screenNo = if (screenNo == 1) 2 else 1
                    manageScreen()
                } else {
                    removeDuplicate(selectedOption!!.degreeId)
                    qualificationListAdapter.objList.add(selectedOption!!)
                    qualificationListAdapter.notifyDataSetChanged()
                    selectedOption = QualificationModel()
                    pageNo = 0
                    callApi(1)
                }
            }
        }
    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@TwentyTwoTemplateActivity, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]


        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNull()) {
                isLoading = false
                if (screenNo == 1) {
                    degreeHeaderListAdapter.addData(it.data[0].arrayData, pageNo == 0)
                } else {
                    dialogCollegeAdapter.addData(it.data[0].arrayData, true)
                    Util.print(">>>>>>>>>>>>>>SIZE OF ARRAY IS >>>>>>${dialogCollegeAdapter.objList.size}")
                }
                pageNo += 1
                isLastPage = it.data.isNullOrEmpty()




                if (screenNo == 1 && it.data[0].suggestestionList.isNotEmpty())
                    dialogSuggestionListAdapter.addData(it.data[0].suggestestionList)

                if (it.data[0].selectedDataArray!!.isNotEmpty()) {

                    if (screenNo == 1) {
                        selectedDegreeId = it.data[0].selectedDataArray!![0].id
                        degreeHeaderListAdapter.objList.apply {
// set Position Dialoge open but not click dialog continue button
                            find { it.isSelected == 1 }?.isSelected = 0
// set Position Dialoge open
                            find { it.id == selectedDegreeId }?.isSelected = 1
                        }
                        degreeHeaderListAdapter.notifyDataSetChanged()
                    } else {
                        selectedCollegeId = it.data[0].selectedDataArray!![0].id


                        dialogCollegeAdapter.objList.apply {
// set Position Dialoge open but not click dialog continue button
                            find { it.isSelected == 1 }?.isSelected = 0
// set Position Dialoge open
                            find { it.id == selectedCollegeId }?.isSelected = 1
                        }
                        dialogCollegeAdapter.notifyDataSetChanged()
                    }
                    manageMainContinueBtn()
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this)
        {
            Util.dismissProgress()
            if (it.success == 1) {


                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName.contains(
                        HASH_SEPERATOR
                    )
                ) {

                    val questionKeys =
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName.split(
                            HASH_SEPERATOR
                        )

                    firebaseEventLog(questionKeys[2], Bundle().apply {
                        putString("college_name",
                            qualificationListAdapter.objList.joinToString("|") { it.collegeName })
                    })

                    firebaseEventLog(questionKeys[1], Bundle().apply {
                        putString("degree_type",
                            qualificationListAdapter.objList.joinToString("|") { it.typeName })
                    })
                    firebaseEventLog(questionKeys[0], Bundle().apply {
                        putString("degree_name",
                            qualificationListAdapter.objList.joinToString("|") { it.degreeName })
                    })
                }
                searchKey = ""
                pageNo = 1
                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.size == qualificationListAdapter.objList.size && !valuesChanged) {
                        finish()
                    } else {
                        Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                        {
                            finish()
                        }
                    }
                } else
                    Util.manageTemplate(this@TwentyTwoTemplateActivity, isFrom)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgess: Boolean = false) {

        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isLoading) return
                    isLoading = true
                    if (showProgess) Util.showProgress(this)
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.contains(
                            HASH_SEPERATOR
                        )
                    ) {
                        val apiNames = originalApiName.split(HASH_SEPERATOR)
                        questionViewModel.questionOptionsListApiRequest(
                            this,
                            searchKey,
                            pageNo,
                            apiName = if (screenNo == 1) apiNames[0] else apiNames[1]
                        )
                    }
                }


                2 -> {
                    if (showProgess) Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this, arrayData = mainObj
                    )
                }
            }
        }
    }


    private fun saveData() {
        mainObj = JSONArray()

//        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.size == qualificationListAdapter.objList.size && !valuesChanged) {
//            searchKey = ""
//            pageNo = 1
//            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
//                finish()
//            } else
//                Util.manageTemplate(this@TwentyTwoTemplateActivity, isFrom)
//
//        } else {

            val questionKeys =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.split(HASH_SEPERATOR)
            for (element in qualificationListAdapter.objList) {
                val answersObj = JSONObject()
                answersObj.put(questionKeys[0], element.degreeId)
                answersObj.put(questionKeys[1], element.typeId)
                answersObj.put(questionKeys[2], element.collegeId)
                answersObj.put(
                    "is_add_from_designation",
                    element.isAddFromDesignation.ifEmpty { "0" })
                answersObj.put("is_add_other", element.isAddOther.ifEmpty { "0" }.toInt())
                answersObj.put("other_name", element.otherName)
                answersObj.put("is_lock", mainObjList[CATEGORY_ID].questionList[LAST_POS].isLock)
                mainObj.put(answersObj)
            }

            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObj}")
            callApi(2)
//        }
    }


    override fun onInnerItemClick(outerPosition: Int, innerPosition: Int, innerData: GeneralModel) {
        degreeHeaderListAdapter.clearSelections()
        degreeHeaderListAdapter.objList[outerPosition].qualificationListAdapter!!.objList[innerPosition].isSelected =
            if (degreeHeaderListAdapter.objList[outerPosition].qualificationListAdapter!!.objList[innerPosition].isSelected == 1) 0 else 1
        degreeHeaderListAdapter.notifyDataSetChanged()


        if (degreeData == null)
            degreeData = GeneralModel()
        degreeData?.name = innerData.name
        degreeData?.id = innerData.id
        degreeData?.isSpecialCertificate = innerData.isSpecialCertificate
        binding.buttonView.btnContinue.isClickable = true
        bottomScreenBinding.btnDialogContinue.apply {
            isEnabled = true
            setOnClickListener(this@TwentyTwoTemplateActivity)
            degreeHeaderListAdapter.clearSelections()
            performClick()
            mcqOptionAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
            manageScreen()
        }
    }

    private fun removeDuplicate(selectedID: String) {
        val filteredList = qualificationListAdapter.objList.filterNot { it.degreeId == selectedID }
        qualificationListAdapter.objList.clear()
        qualificationListAdapter.objList.addAll(filteredList)
        qualificationListAdapter.notifyDataSetChanged()
    }

}



