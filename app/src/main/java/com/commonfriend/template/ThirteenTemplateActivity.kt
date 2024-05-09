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
import com.commonfriend.adapter.InnerAdapterAdapter
import com.commonfriend.adapter.OptionHeaderAdapter
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject


//Designation
class ThirteenTemplateActivity : BaseActivity(), View.OnClickListener,
    InnerAdapterAdapter.DegreeAdapterItemClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private lateinit var mainObj: JSONObject
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomScreenBinding: DialogCastBinding? = null
    var lastPos = -1
    private var isDialogOpen: Boolean = false
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var optionHeaderAdapter: OptionHeaderAdapter
    private lateinit var suggestionDialogAdapter: SuggestionDialogAdapter
    private lateinit var questionViewModel: QuestionViewModel
    private var selectedOptionId = ""
    private var pageNo: Int = 0
    private var searchKey = ""
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var firstTime: Boolean = true
    private var headerId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLocationName.setText("")

        initialization()
    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@ThirteenTemplateActivity)
        }
    }

    private fun initialization() {

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        if (mainObjList.isEmpty()) {
            onBackPress()
            return
        }

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE



        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@ThirteenTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName



        suggestionDialogAdapter = SuggestionDialogAdapter(this, this)
        optionHeaderAdapter = OptionHeaderAdapter(this, this)
        allApiResponses()

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )

        binding.buttonView.btnSkip.setOnClickListener(this)
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.INVISIBLE
        isLastPage = false
        isLoading = false
        pageNo = 0
        callApi(1)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun bottomSheetDialog() {
        if (bottomSheetDialog != null) {
            if (bottomSheetDialog!!.isShowing)
                isDialogOpen = false
            bottomSheetDialog!!.dismiss()
        }
        isDialogOpen = true

        bottomSheetDialog =
            BottomSheetDialog(this@ThirteenTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)


        bottomSheetDialog!!.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding!!.root)
            show()
            setCancelable(true)
            window?.let { Util.statusBarColor(this@ThirteenTemplateActivity, it) }
            setOnDismissListener {
                isDialogOpen = false
                searchKey = ""
                pageNo = 0
            }
        }





        with(bottomScreenBinding!!) {
            rvLocation.visibility = View.VISIBLE
            rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@ThirteenTemplateActivity, RecyclerView.HORIZONTAL, false)
            rvTextSuggestion.adapter = suggestionDialogAdapter

            rvTextSuggestion.visibleIf(suggestionDialogAdapter.objList.isNotEmpty())

            rvLocation.layoutManager =
                LinearLayoutManager(this@ThirteenTemplateActivity, RecyclerView.VERTICAL, false)
            rvLocation.adapter = optionHeaderAdapter

            txtHeaderTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
            txtOtherTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
            txtSuggestion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
            txtChooseReligion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle

            llOther.gone()

            edtSearch.setOnClickListener {
//                txtHeaderTitle.gone()
                rvTextSuggestion.gone()
            }

            rvLocation.addOnScrollListener(object :
                PaginationListenerAdapter(rvLocation.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    if (!isLoading) {
                        if (!firstTime) {
                            callApi(1)
                        }
                    }
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }
            })



            txtTitleClear.setOnClickListener {
                edtSurName.setText("")
            }

            txtClear.setOnClickListener {
                edtSearch.setText("")
            }
            imgLeft.setOnClickListener {
                edtSearch.setText("")

            }
            llOther.setOnClickListener(this@ThirteenTemplateActivity)

            txtSuggestion.visibility = View.GONE
            txtChooseReligion.visibility = View.GONE

            edtSearch.observeTextChange {
                if (isDialogOpen) {
                    searchKey = it.trim()
                    pageNo = 0
                    isLoading = false
                    callApi(1, false)
                    txtClear.visibleIf(it.isNotEmpty())
                    imgLeft.visibleIf(it.isNotEmpty())
                    imgSearch.visibleIf(it.isEmpty())
                }
            }
            edtSurName.observeTextChange {
                txtTitleClear.visibleIf(it.trim().length > 1)
                btnDialogContinue.isEnabled = it.isNotEmpty()
                btnDialogContinue.isClickable = it.isNotEmpty()
            }

            btnDialogContinue.setOnClickListener(this@ThirteenTemplateActivity)
            btnCancel.setOnClickListener {
                edtSearch.setText("")
                edtSurName.setText("")
                bottomSheetDialog!!.dismiss()
            }
            bottomScreenBinding!!.edtSearch.requestFocus()

            showButton(selectedOptionId.isNotEmpty())

        }
    }


    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding!!.btnDialogContinue.isEnabled = shouldShow
        bottomScreenBinding!!.btnDialogContinue.isClickable = shouldShow
    }

    fun showNoDataString(shouldShow: Boolean) {
        if (bottomScreenBinding != null)
            bottomScreenBinding!!.llOther.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.llOther -> {
                bottomScreenBinding!!.llCast.visibility = View.VISIBLE
                bottomScreenBinding!!.rlSearch.visibility = View.INVISIBLE
                bottomScreenBinding!!.llOther.visibility = View.GONE
                bottomScreenBinding!!.rvLocation.visibility = View.GONE
                bottomScreenBinding!!.edtSurName.requestFocus()
                bottomScreenBinding!!.txtOtherTitle.visibleIf(Util.getTextValue(bottomScreenBinding!!.txtOtherTitle).isNotEmpty())
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LASTPOS$LAST_POS")
                saveData()
            }

            R.id.btnDialogContinue -> {
                val otherOptionText = bottomScreenBinding!!.edtSurName.text.toString()
                if (otherOptionText != "") {
                    binding.txtLocationName.setText(otherOptionText)
                    binding.txtLocationDialog.hint =
                        if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
                    bottomScreenBinding!!.btnDialogContinue.isClickable = true
                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnContinue.isClickable = true
                }
//                saveData()
                binding.buttonView.btnContinue.performClick()
                bottomScreenBinding!!.btnDialogContinue.isEnabled = false
                bottomScreenBinding!!.btnDialogContinue.isClickable = false
                bottomScreenBinding!!.edtSearch.setText("")
                bottomScreenBinding!!.edtSurName.setText("")
                bottomSheetDialog!!.dismiss()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                binding.txtLocationName.setText(suggestionDialogAdapter.objList[lastPos].optionName)
                binding.txtLocationDialog.hint =
                    if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

                optionHeaderAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                optionHeaderAdapter.objList.find { it.id == suggestionDialogAdapter.objList[lastPos].id }?.isSelected =
                    1

                selectedOptionId = suggestionDialogAdapter.objList[lastPos].id

                bottomSheetDialog!!.dismiss()
//                saveData()
                binding.buttonView.btnContinue.performClick()

                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.isClickable = true

            }

            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding!!.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    isDialogOpen = true
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun saveData() {
        mainObj = JSONObject()
//        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].answer == selectedOptionId) {
//            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
//                finish()
//            else
//                Util.manageTemplate(this@ThirteenTemplateActivity, isFrom)
//
//        } else {
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                selectedOptionId
            )
            mainObj.put(
                "is_add_other",
                if (bottomScreenBinding != null && !Util.isEmptyText(bottomScreenBinding!!.edtSurName)) "1" else "0"
            )
            mainObj.put(
                "other_name",
                if (bottomScreenBinding != null && !Util.isEmptyText(bottomScreenBinding!!.edtSurName)) Util.getTextValue(
                    bottomScreenBinding!!.edtSurName
                ) else ""
            )
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObj}")
            callApi(2,true)
//        }
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                optionHeaderAdapter.addData(it.data[0].arrayData, pageNo <= 0)
                if (pageNo == 0 && searchKey.isEmpty()) {
                    suggestionDialogAdapter.addData(it.data[0].suggestestionList)
                }

                isLastPage = it.data.isNullOrEmpty()
                isLoading = false
                firstTime = false

                if (it.data[0].arrayData.isNotEmpty()) pageNo += 1
                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    headerId = it.data[0].selectedDataArray?.get(0)?.type
                    selectedOptionId = it.data[0].selectedDataArray!![0].id
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answer = selectedOptionId
                    binding.txtLocationName.setText(it.data[0].selectedDataArray!![0].name)
                    binding.txtLocationDialog.hint =
                        if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
                    binding.buttonView.btnContinue.isEnabled = true

                    for (i in 0 until optionHeaderAdapter.objList.size) {
                        if (optionHeaderAdapter.objList[i].name == headerId) {
                            optionHeaderAdapter.notifyDataSetChanged()
                            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>I WAS IN ${optionHeaderAdapter.objList[i].name}")
                            if (it.data[0].arrayData.isNotEmpty() && it.data[0].arrayData[i].subArrayData.isNotEmpty()) {
                                optionHeaderAdapter.objList[i].qualificationListAdapter =
                                    InnerAdapterAdapter(
                                        this,
                                        this,
                                        i,
                                        it.data[0].arrayData[i].subArrayData
                                    )
                                optionHeaderAdapter.objList[i].qualificationListAdapter?.let { it1 ->
                                    if (it1.objList.isNotEmpty()) {
                                        for (j in 0 until it1.objList.size) {
                                            if (it1.objList[j].id == selectedOptionId) {
                                                it1.objList[j].isSelected = 1
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    optionHeaderAdapter.notifyDataSetChanged()
                }
            }
        }

        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isClickable = true
            if (it.success == 1) {

                bundle = Bundle().apply {
                    putString(
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                        Util.getTextValue(binding.txtLocationName)
                    )
                }
                firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)
                MainApplication.firebaseAnalytics.setUserProperty(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                    Util.getTextValue(binding.txtLocationName)
                )

                mainObjList[CATEGORY_ID].questionList[LAST_POS].answer = selectedOptionId

                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
                        finish()
                        Util.manageTemplate(this@ThirteenTemplateActivity, isFrom)

                    }
                } else
                    Util.manageTemplate(this@ThirteenTemplateActivity, isFrom)
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isLoading) return
                    isLoading = true
                    if (showProgress)
                        Util.showProgress(this)
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        searchKey,
                        pageNo,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }

                2 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(this, mainObj)
                }
            }
        } else {
            binding.buttonView.btnContinue.isClickable = true
        }
    }


    override fun onInnerItemClick(outerPosition: Int, innerPosition: Int, innerData: GeneralModel) {
        optionHeaderAdapter.clearSelections()
        headerId = optionHeaderAdapter.objList[outerPosition].name
        optionHeaderAdapter.objList[outerPosition].qualificationListAdapter!!.objList[innerPosition].apply {
            this.isSelected = if (this.isSelected == 1) 0 else 1
            optionHeaderAdapter.notifyDataSetChanged()
            bottomScreenBinding!!.btnDialogContinue.isEnabled = true
            selectedOptionId = this.id
            binding.txtLocationName.setText(this.name)
            binding.txtLocationDialog.hint =
                if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
            isDialogOpen = false
        }
        binding.buttonView.btnContinue.isEnabled = true
        bottomScreenBinding!!.btnDialogContinue.performClick()
    }
}



