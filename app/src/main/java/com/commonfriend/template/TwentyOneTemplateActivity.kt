package com.commonfriend.template


import PaginationListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.EditProfileActivity
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.adapter.FourTemplateAdapter
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.databinding.ActivityTwentyOneTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject


class TwentyOneTemplateActivity : BaseActivity(), OnClickListener {

    /*for work and industry template*/
    private lateinit var binding: ActivityTwentyOneTemplateBinding
    private lateinit var questionViewModel: QuestionViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomScreenBinding: DialogCastBinding? = null
    private var companyAnswer: String = ""

    var lastPos = -1
    var isIndustryLastPage: Boolean = false
    var isIndustryLoading: Boolean = false

    private lateinit var mainObj: JSONObject
    private var selectedIndustryId: String = ""
    private var industryPageNo = 0


    private var debounceHandler = Handler()
    private var debounceRunnable = Runnable {}
    private lateinit var dialogIndustryListAdapter: FourTemplateAdapter
    private lateinit var dialogSuggestionIndustryListAdapter: SuggestionDialogAdapter
    private var searchKey = ""
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwentyOneTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }


    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom


        binding.edtIndustry.setText("")
//        binding.edtCompanyName.setText("")


        Util.statusBarColor(this, window)

        dialogIndustryListAdapter = FourTemplateAdapter(this, this, 2)
        dialogSuggestionIndustryListAdapter = SuggestionDialogAdapter(this, this, 2)

        allApiResponses()

        binding.edtIndustry.isFocusable = false
        binding.edtIndustry.setOnClickListener(this)
        binding.rlSpinner.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@TwentyOneTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        with(binding.customHeader.get())
        {
            this.btnLeft.setOnClickListener(this@TwentyOneTemplateActivity)
            this.txtTitle.text = mainObjList[CATEGORY_ID].categoryName
            this.progressBar.progress = LAST_POS + 1
            this.progressBar.visibility = View.VISIBLE
            this.progressBar.max = mainObjList[CATEGORY_ID].questionList.size
            this.txtPageNO.text =
                StringBuilder().append((LAST_POS + 1).toString()).append("/")
                    .append(mainObjList[CATEGORY_ID].questionList.size.toString())
        }

//        binding.edtCompanyName.apply {
//            this.hint =
//                mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText.split(HASH_SEPERATOR)[0]
//            this.observeTextChange {
//                this.setTextSize(
//                    TypedValue.COMPLEX_UNIT_PX,
//                    resources.getDimension(if (this.text!!.isEmpty()) com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp)
//                )
//                manageMainContinueButton()
//            }
//        }
        binding.txtIndustryDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
        binding.edtIndustry.apply {
//            this.hint =
//                mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText.split(HASH_SEPERATOR)[1]


            this.observeTextChange {
                binding.edtIndustry.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(
                        if (this.text!!.isEmpty())
                            com.intuit.sdp.R.dimen._12sdp else com.intuit.sdp.R.dimen._20sdp
                    )
                )

            }
        }


        activityOnBackPressed(this,this) {
            onBackPress()
        }


        manageMainContinueButton()
        callApi(1)
    }


    override fun onDestroy() {
        super.onDestroy()
        debounceHandler.removeCallbacks(debounceRunnable)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.edtIndustry, R.id.rlSpinner -> {
                bottomSheetDialog()
            }


            R.id.btnContinue -> {
                saveData()
            }


            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.btnLeft -> {
                onBackPress()
            }


            R.id.llOther -> {
                bottomScreenBinding!!.llCast.visibility = View.VISIBLE
                bottomScreenBinding!!.rlSearch.visibility = View.INVISIBLE
                bottomScreenBinding!!.llOther.visibility = View.GONE
                bottomScreenBinding!!.rvLocation.visibility = View.GONE

                manageMainContinueButton()
            }


            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                dialogIndustryListAdapter.apply {
                    objList.find { it.isSelected == 1 }?.isSelected = 0
                    objList[lastPos].isSelected = 1
                    notifyDataSetChanged()
                }


                selectedIndustryId = dialogIndustryListAdapter.objList[lastPos].id
                showButton()
                bottomScreenBinding!!.btnDialogContinue.apply {
                    isClickable = true
                    isEnabled = true
                    performClick()
                }
            }


            R.id.btnDialogContinue -> {
                val otherReligionTxt = bottomScreenBinding!!.edtSurName.text.toString().trim()

//show visibility in activity continue button
                if (otherReligionTxt != "") {
                    binding.edtIndustry.setText(otherReligionTxt)
                    if (!Util.isEmptyText(binding.edtIndustry) /*&& !Util.isEmptyText(binding.edtCompanyName)*/) {
                        saveData()
                    }
                } else {
                    binding.edtIndustry.setText(dialogIndustryListAdapter.objList.find { it.isSelected == 1 }!!.name)
                    if (Util.getTextValue(binding.edtIndustry).isNotEmpty() /*&& !Util.isEmptyText(
                            binding.edtCompanyName
                        )*/
                    ) {
                        saveData()
                    }
                }
                bottomScreenBinding!!.btnDialogContinue.isClickable = false


                manageMainContinueButton()
                bottomSheetDialog!!.dismiss()
            }


            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()


                binding.edtIndustry.setText(dialogSuggestionIndustryListAdapter.objList[lastPos].optionName)
                bottomSheetDialog!!.dismiss()


                selectedIndustryId = dialogSuggestionIndustryListAdapter.objList[lastPos].id
                dialogIndustryListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                dialogSuggestionIndustryListAdapter.objList.find { it.isSelected == 1 }?.isSelected =
                    0
                dialogSuggestionIndustryListAdapter.objList[lastPos].isSelected =
                    if (dialogSuggestionIndustryListAdapter.objList[lastPos].isSelected == 1) 0 else 1
                dialogIndustryListAdapter.objList.find { it.id == dialogSuggestionIndustryListAdapter.objList[lastPos].id }?.isSelected =
                    1
                dialogIndustryListAdapter.notifyDataSetChanged()
//ADDED BY VAISHALI
                saveData()




                showButton()


                manageMainContinueButton()
            }
        }
    }


    private fun saveData() {

        if (mainObjList.isEmpty())
            return
        else if (mainObjList[CATEGORY_ID].questionList.isEmpty())
            return

        mainObj = JSONObject()

//        if (Util.getTextValue(binding.edtCompanyName) == companyAnswer && mainObjList[CATEGORY_ID].questionList[LAST_POS].answer == dialogIndustryListAdapter.objList.find { it.isSelected == 1 }?.id) {
//
//            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
//                finish()
//            } else
//                Util.manageTemplate(this@TwentyOneTemplateActivity, isFrom)
//        } else {
            val questionKeys =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.split(HASH_SEPERATOR)
            for (i in questionKeys.indices) {
//                mainObj.put(questionKeys[0], Util.getTextValue(binding.edtCompanyName))
//                mainObj.put("other_name", Util.getTextValue(binding.edtCompanyName))
                mainObj.put("is_add_other", "1")


                val industryId = dialogIndustryListAdapter.objList.find { it.isSelected == 1 }?.id
                mainObj.put(questionKeys[1], industryId ?: "")
                mainObj.put(
                    "is_add_other_industry",
                    if (industryId != null && industryId != "") "0" else "1"
                )
                mainObj.put(
                    "other_industry",
                    if (industryId != null && industryId != "") "" else Util.getTextValue(binding.edtIndustry)
                )
            }


            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObj}")
            callApi(2,true)
//        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {

        if (bottomSheetDialog != null) {
            if (bottomSheetDialog!!.isShowing)
                bottomSheetDialog!!.dismiss()
        }

        bottomSheetDialog =
            BottomSheetDialog(this@TwentyOneTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        bottomSheetDialog!!.apply {


            window?.let { Util.statusBarColor(this@TwentyOneTemplateActivity, it) }


            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding!!.root)
            setCancelable(true)


            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomScreenBinding!!.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            show()


            setOnDismissListener {
                searchKey = ""
            }
        }




        bottomScreenBinding!!.apply {


            this.edtSearch.requestFocus()


            this.btnDialogContinue.isEnabled = binding.edtIndustry.text!!.isNotEmpty()
            this.btnDialogContinue.isClickable = binding.edtIndustry.text!!.isNotEmpty()

            this.rvLocation.visibility = View.VISIBLE

            this.rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@TwentyOneTemplateActivity, RecyclerView.HORIZONTAL, false)


            this.rvLocation.layoutManager =
                LinearLayoutManager(this@TwentyOneTemplateActivity, RecyclerView.VERTICAL, false)


            this.rvLocation.adapter = dialogIndustryListAdapter


            this.rvTextSuggestion.adapter = dialogSuggestionIndustryListAdapter

            this.rvTextSuggestion.visibleIf(dialogSuggestionIndustryListAdapter.objList.isNotEmpty())




            this.txtHeaderTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle.split(HASH_SEPERATOR)[1]


            this.txtSuggestion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle.split(HASH_SEPERATOR)[1]




            bottomScreenBinding!!.edtSearch.setOnClickListener {
//                bottomScreenBinding!!.txtHeaderTitle.visibility = View.INVISIBLE
                bottomScreenBinding!!.rvTextSuggestion.visibility = View.GONE
            }

            this.txtOtherTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle


            this.txtChooseReligion.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle


            if (dialogIndustryListAdapter.objList.isEmpty()) {
                callApi(1)
            } /*else {
                showButton()
            }*/




            this.rvLocation.addOnScrollListener(object :
                PaginationListenerAdapter(bottomScreenBinding!!.rvLocation.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED")
                    if (!isIndustryLoading) {
                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> IWAS CALLED")
                        callApi(1)
                    }
                }


                override fun isLastPage(): Boolean {
                    return isIndustryLastPage
                }


                override fun isLoading(): Boolean {
                    return isIndustryLoading
                }
            })


            dialogIndustryListAdapter.objList.apply {
// set Position Dialoge open but not click dialog continue button
                find { it.isSelected == 1 }?.isSelected = 0
// set Position Dialoge open
                find { it.id == selectedIndustryId }?.isSelected = 1
            }
            dialogIndustryListAdapter.notifyDataSetChanged()




            this.txtTitleClear.setOnClickListener {
                this.edtSurName.setText("")
            }


            this.txtClear.setOnClickListener {
                this.edtSearch.setText("")
                industryPageNo = 0
                searchKey = ""
                callApi(1, false)
            }
            this.imgLeft.setOnClickListener { this.edtSearch.setText("") }
            this.llOther.setOnClickListener(this@TwentyOneTemplateActivity)


            this.txtSuggestion.visibility = View.GONE
            this.txtChooseReligion.visibility = View.GONE


            edtSearch.setOnEditorActionListener { _, actionId, _ ->

// Return false if the event is not consumed// Handle the "Done" button press or Search action here
// Return true to indicate that the event is consumed
                actionId == EditorInfo.IME_ACTION_SEARCH
            }


            this.edtSearch.observeTextChange {
                val text = this@apply.edtSearch.text.toString()


                industryPageNo = 0
                searchKey = text




                if (text.isNotEmpty()) {
                    debounceHandler.removeCallbacks(debounceRunnable)
                    debounceRunnable = Runnable {
                        callApi(1, false)
                    }
                    debounceHandler.postDelayed(debounceRunnable, 500)
                } else {
                    callApi(1, false)
                }


                this@apply.txtClear.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE

                this@apply.imgLeft.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE


                this@apply.imgSearch.visibility =
                    if (text.isEmpty()) View.VISIBLE else View.GONE
            }


            this.edtSurName.setOnEditorActionListener { _, actionId, _ ->
                actionId == EditorInfo.IME_ACTION_SEARCH
            }


            this.edtSurName.observeTextChange {
                val text = this@apply.edtSurName.text.toString().trim()


                if (text.length > 1) this@apply.txtTitleClear.visibility = View.VISIBLE
                else this@apply.txtTitleClear.visibility = View.GONE


//                this@apply.btnDialogContinue.visibility = View.VISIBLE
                this@apply.btnDialogContinue.isClickable = text.isNotEmpty()
                this@apply.btnDialogContinue.isEnabled = text.isNotEmpty()
            }




            this.btnDialogContinue.setOnClickListener(this@TwentyOneTemplateActivity)


            this.btnCancel.setOnClickListener {
                selectedIndustryId = ""
                bottomSheetDialog!!.dismiss()
            }
        }


    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@TwentyOneTemplateActivity)
        }
    }


    fun showButton() {
        val shouldShow =
            dialogIndustryListAdapter.objList.any { it.isSelected == 1 }
        if (bottomScreenBinding != null) {
            bottomScreenBinding!!.btnDialogContinue.apply {
                isClickable = shouldShow
                isEnabled = shouldShow
            }
        }


    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@TwentyOneTemplateActivity, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]


        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (it.data.isNotEmpty()) {
                dialogIndustryListAdapter.addData(it.data[0].arrayData, industryPageNo == 0)
                dialogSuggestionIndustryListAdapter.addData(it.data[0].suggestestionList)

                if (bottomScreenBinding != null) {
                    bottomScreenBinding!!.rvTextSuggestion.visibleIf(dialogSuggestionIndustryListAdapter.objList.isNotEmpty())
                }

                showButton()


                isIndustryLastPage = it.data.isEmpty()

                if (it.data[0].arrayData.isNotEmpty()) industryPageNo += 1
                isIndustryLoading = false




                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    selectedIndustryId = it.data[0].selectedDataArray!![0].id
                    dialogIndustryListAdapter.objList.apply {
// set Position Dialoge open but not click dialog continue button
                        find { it.isSelected == 1 }?.isSelected = 0
// set Position Dialoge open
                        find { it.id == selectedIndustryId }?.isSelected = 1
                    }
                    dialogIndustryListAdapter.notifyDataSetChanged()
                    binding.edtIndustry.setText(it.data[0].selectedDataArray!![0].name)
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                        it.data[0].selectedDataArray!![0].id
                    manageMainContinueButton()
//                    binding.txtLocationName.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText


                }
            }
        }


        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                searchKey = ""
                industryPageNo = 0

                if (mainObjList.isEmpty())
                    return@observe
                else if (mainObjList[CATEGORY_ID].questionList.isEmpty())
                    return@observe

                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName.contains(
                        HASH_SEPERATOR
                    )
                ) {
                    val questionkey =
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName.split(
                            HASH_SEPERATOR
                        )

//                    firebaseEventLog(questionkey[0], Bundle().apply {
//                        putString("candidate_office", Util.getTextValue(binding.edtCompanyName))
//                    })

                    firebaseEventLog(questionkey[1], Bundle().apply {
                        putString("candidate_industry", Util.getTextValue(binding.edtIndustry))
                    })

                } else {
                    bundle = Bundle().apply {
//                        putString("candidate_office", Util.getTextValue(binding.edtCompanyName))
                        putString("candidate_industry", Util.getTextValue(binding.edtIndustry))
                    }
                    firebaseEventLog(
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,
                        bundle
                    )
                }

//                MainApplication.firebaseAnalytics.setUserProperty(
//                    "office",Util.getTextValue(binding.edtCompanyName))

                MainApplication.firebaseAnalytics.setUserProperty(
                    "industry",Util.getTextValue(binding.edtIndustry))







                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
//                        finish()
                        val intent = Intent(this, EditProfileActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)
                        startActivity(intent)
                    }
                } else
                    Util.manageTemplate(this@TwentyOneTemplateActivity, isFrom)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun manageMainContinueButton() {
        val clickable =
            /*!Util.isEmptyText(binding.edtCompanyName) &&*/ !Util.isEmptyText(binding.edtIndustry)
        binding.buttonView.btnContinue.isClickable = clickable
        binding.buttonView.btnContinue.isEnabled = clickable
        binding.buttonView.btnContinue.setOnClickListener(if (clickable) this@TwentyOneTemplateActivity else null)
    }


    override fun onResume() {
        super.onResume()
        manageMainContinueButton()
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.buttonView.btnSkip.setOnClickListener(this)

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name.contains(
                HASH_SEPERATOR
            )
        ) {

//            binding.edtCompanyName.setText(
//                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name.split(
//                    HASH_SEPERATOR
//                )[0]
//            )
            companyAnswer =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name.split(
                    HASH_SEPERATOR
                )[0]
        }
        binding.llPrivacy.visibleIf(mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isNotEmpty())
        binding.infoMessage.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace("\\n","\n")

    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {


        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isIndustryLoading) return


                    isIndustryLoading = true


                    if (showProgress)
                        Util.showProgress(this)


                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        searchKey,
                        industryPageNo,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }

                2 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this, mainObj
                    )
                }
            }
        }
    }
}

