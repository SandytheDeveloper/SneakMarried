package com.commonfriend.template

import PaginationListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.adapter.FourTemplateAdapter
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject


class FourTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private lateinit var mainObj: JSONObject
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomScreenBinding: DialogCastBinding? = null
    var lastPos = -1
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var fourTemplateAdapter: FourTemplateAdapter
    private lateinit var suggestionDialogAdapter: SuggestionDialogAdapter
    private lateinit var questionViewModel: QuestionViewModel
    private var selectedId = ""
    private var selectedOptionId = ""
    private var pageNo: Int = 0
    private var searchKey = ""
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var firstTime: Boolean = true



    // template code 5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLocationName.setText("")

        initilization()
    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@FourTemplateActivity)
        }
    }

    private fun initilization() {

        Util.statusBarColor(this, window)


        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@FourTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName



        suggestionDialogAdapter = SuggestionDialogAdapter(this, this)
        fourTemplateAdapter = FourTemplateAdapter(this, this)
        allApiResponses()

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }

    override fun onResume() {
        super.onResume()
//        binding.buttonView.btnContinue.isClickable = true
        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n", System.lineSeparator()
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

        bottomSheetDialog =
            BottomSheetDialog(this@FourTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        if (bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()
        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding!!.root)
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setCancelable(true)

        bottomScreenBinding!!.edtSearch.requestFocus()

        bottomSheetDialog!!.window?.let { Util.statusBarColor(this@FourTemplateActivity, it) }

        bottomScreenBinding!!.rvLocation.visibility = View.VISIBLE

        bottomScreenBinding!!.rvTextSuggestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        bottomScreenBinding!!.rvTextSuggestion.adapter = suggestionDialogAdapter

        bottomScreenBinding!!.rvLocation.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        bottomScreenBinding!!.rvLocation.adapter = fourTemplateAdapter


        bottomScreenBinding!!.txtHeaderTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding!!.txtOtherTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
        bottomScreenBinding!!.txtSuggestion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding!!.txtChooseReligion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle

        bottomScreenBinding!!.llOther.visibility = View.GONE


        bottomScreenBinding!!.edtSearch.setOnClickListener {
            bottomScreenBinding!!.txtHeaderTitle.visibility = View.INVISIBLE
            bottomScreenBinding!!.rvTextSuggestion.visibility = View.GONE
        }




        bottomScreenBinding!!.rvLocation.addOnScrollListener(object :
            PaginationListenerAdapter(bottomScreenBinding!!.rvLocation.layoutManager as LinearLayoutManager) {

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


        // set Position Dialoge open but not click dialog continue button
        fourTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0

        // set Position Dialoge open
        fourTemplateAdapter.objList.find { it.name == binding.txtLocationName.text.toString() }?.isSelected =
            1

        bottomScreenBinding!!.txtTitleClear.setOnClickListener {
            bottomScreenBinding!!.edtSurName.setText("")
        }

        bottomScreenBinding!!.txtClear.setOnClickListener {
            bottomScreenBinding!!.edtSearch.setText("")
        }
        bottomScreenBinding!!.imgLeft.setOnClickListener {
            bottomScreenBinding!!.edtSearch.setText("")

        }
        bottomScreenBinding!!.llOther.setOnClickListener(this)

        bottomScreenBinding!!.txtSuggestion.visibility = View.GONE
        bottomScreenBinding!!.txtChooseReligion.visibility = View.GONE

        if (searchKey.isNotEmpty()) {
            bottomScreenBinding!!.edtSearch.setText(searchKey)
            bottomScreenBinding!!.txtClear.visibility = View.VISIBLE
            bottomScreenBinding!!.imgLeft.visibility = View.VISIBLE
            bottomScreenBinding!!.imgSearch.visibility = View.GONE
        }

        bottomScreenBinding!!.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = bottomScreenBinding!!.edtSearch.text.toString()
                searchKey = text
                pageNo = 0
                isLastPage = false
                isLoading = false
                callApi(1, false)

                bottomScreenBinding!!.txtClear.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding!!.imgLeft.visibility = if (text.isNotEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding!!.imgSearch.visibility =
                    if (text.isEmpty()) View.VISIBLE else View.GONE


            }
        })

        bottomScreenBinding!!.edtSurName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val text = bottomScreenBinding!!.edtSurName.text!!.trim().toString()

                if (text.length > 1) bottomScreenBinding!!.txtTitleClear.visibility = View.VISIBLE
                else bottomScreenBinding!!.txtTitleClear.visibility = View.GONE

                bottomScreenBinding!!.btnDialogContinue.isEnabled = text.isNotEmpty()
                bottomScreenBinding!!.btnDialogContinue.isClickable = text.isNotEmpty()

            }
        })

        bottomScreenBinding!!.btnDialogContinue.setOnClickListener(this)

        bottomScreenBinding!!.btnCancel.setOnClickListener {
            bottomScreenBinding!!.edtSearch.setText("")
            bottomScreenBinding!!.edtSurName.setText("")
            bottomSheetDialog!!.dismiss()
        }

    }


    fun showButton(shouldShow: Boolean) {
        if (shouldShow) {

            bottomScreenBinding!!.btnDialogContinue.isEnabled = true
            bottomScreenBinding!!.btnDialogContinue.isClickable = true
        } else {
            bottomScreenBinding!!.btnDialogContinue.isEnabled = false
            bottomScreenBinding!!.btnDialogContinue.isClickable = false
        }
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

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                fourTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                fourTemplateAdapter.objList[lastPos].isSelected = 1
                fourTemplateAdapter.notifyDataSetChanged()
                bottomScreenBinding!!.btnDialogContinue.isEnabled = true
                bottomScreenBinding!!.btnDialogContinue.isClickable = true
                bottomScreenBinding!!.btnDialogContinue.performClick()
            }

            R.id.llOther -> {
                bottomScreenBinding!!.llCast.visibility = View.VISIBLE
                bottomScreenBinding!!.rlSearch.visibility = View.INVISIBLE
                bottomScreenBinding!!.llOther.visibility = View.GONE
                bottomScreenBinding!!.rvLocation.visibility = View.GONE
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LASTPOS$LAST_POS")
                saveData()
            }

            R.id.btnDialogContinue -> {
                val otherOptionText = bottomScreenBinding!!.edtSurName.text.toString()

                //show visibility in activity continue button
                if (otherOptionText != "") {
                    binding.txtLocationName.setText(otherOptionText)
                    bottomScreenBinding!!.btnDialogContinue.isClickable = true
                    binding.buttonView.btnContinue.isEnabled = true

                    //ADDED By Vaishali
                    saveData()

                } else {
                    if (fourTemplateAdapter.objList.any { it.isSelected == 1 }) {
                        selectedOptionId =
                            fourTemplateAdapter.objList.find { it.isSelected == 1 }!!.id
                        binding.txtLocationName.setText(fourTemplateAdapter.objList.find { it.isSelected == 1 }!!.name)
                        //ADDED By Vaishali
                        saveData()
                    }
                    bottomScreenBinding!!.btnDialogContinue.isEnabled = false

                    bottomScreenBinding!!.btnDialogContinue.isClickable = false
                }

                bottomScreenBinding!!.edtSearch.setText("")
                bottomScreenBinding!!.edtSurName.setText("")
                bottomSheetDialog!!.dismiss()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                binding.txtLocationName.setText(suggestionDialogAdapter.objList[lastPos].optionName)

                fourTemplateAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                fourTemplateAdapter.objList.find { it.id == suggestionDialogAdapter.objList[lastPos].id }?.isSelected =
                    1

                selectedOptionId = suggestionDialogAdapter.objList[lastPos].id

                bottomSheetDialog!!.dismiss()
                //ADDED By Vaishali
                saveData()

                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.isClickable = true

            }

            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding!!.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

        }
    }

    private fun saveData() {
        mainObj = JSONObject()
        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.contains(HASH_SEPERATOR)) {
            val questionKeys =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.split(HASH_SEPERATOR)
            mainObj.put(
                questionKeys[0],
                selectedOptionId
            )

            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SELECTED OPTION IS>>>${fourTemplateAdapter.objList.find { it.isSelected == 1 }?.id}")
            mainObj.put(
                questionKeys[1],
                if (!Util.isEmptyText(bottomScreenBinding!!.edtSurName)) "1" else "0"
            )
            mainObj.put(
                questionKeys[2],
                if (!Util.isEmptyText(bottomScreenBinding!!.edtSurName)) Util.getTextValue(
                    bottomScreenBinding!!.edtSurName
                ) else ""
            )
        } else {
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SELECTED OPTION IS>>>${fourTemplateAdapter.objList.find { it.isSelected == 1 }?.name}")
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
        }
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObj}")
        callApi(2)
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                fourTemplateAdapter.addData(it.data[0].arrayData, pageNo <= 0)
                if (pageNo == 0 && searchKey.isEmpty())
                    suggestionDialogAdapter.addData(it.data[0].suggestestionList)

                isLastPage = it.data.isNullOrEmpty()
                isLoading = false
                firstTime = false

                if (it.data[0].arrayData.isNotEmpty()) pageNo += 1


                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    fourTemplateAdapter.objList.find { it1 -> it1.id == it.data[0].selectedDataArray?.get(0)?.id }?.isSelected =
                        1
                    fourTemplateAdapter.notifyDataSetChanged()

                    selectedId = it.data[0].selectedDataArray!![0].id
                    selectedOptionId = selectedId

                    binding.txtLocationName.setText(it.data[0].selectedDataArray!![0].name)
                    binding.buttonView.btnContinue.isEnabled = true//View.VISIBLE
                }

            }
        }

        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isClickable = true
            if (it.success == 1) {
                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom==ActivityIsFrom.FROM_EDIT)
                    finish()
                else
                    Util.manageTemplate(this@FourTemplateActivity, isFrom)
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (isLoading)  return
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

}

