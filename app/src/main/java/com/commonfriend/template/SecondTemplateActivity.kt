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
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.adapter.LocationListAdapter
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


/*CULTURE*/
class SecondTemplateActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityThirdTemplateBinding //ActivitySecondTemplateBinding
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var mainJsonObj: JSONObject? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private lateinit var locationListAdapter: LocationListAdapter
    private lateinit var suggetionDialogAdapter: SuggestionDialogAdapter
    var lastPos = -1
    private lateinit var questionViewModel: QuestionViewModel
    private var pageNo: Int = 0
    private var searchKey = ""
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var firstTime: Boolean = true
    private var isFrom = ActivityIsFrom.NORMAL
    private var selectedOption = GeneralModel()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Util.statusBarColor(this, window)

        initialization()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initialization() {


        if (intent.hasExtra(IS_FROM)) {
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        }

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@SecondTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
            color = R.color.color_black
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())
//            "${(LAST_POS + 1)}/${mainObjList[CATEGORY_ID].questionList.size}"

        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

        locationListAdapter = LocationListAdapter(this, this)
        suggetionDialogAdapter = SuggestionDialogAdapter(this, this)

        allApiResponses()



        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        activityOnBackPressed(this,this){
            onBackPress()
        }
    }

    private fun allApiResponses() {

        selectedOption = GeneralModel()
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {

                locationListAdapter.addData(it.data[0].arrayData, pageNo < 1)

                if(it.data[0].suggestestionList.isNotEmpty()) {
                    suggetionDialogAdapter.addData(it.data[0].suggestestionList)
                }

                isLastPage = it.data.isNullOrEmpty()
                isLoading = false
                firstTime = false

                if (it.data[0].arrayData.isNotEmpty()) pageNo += 1

                if (it.data[0].selectedDataArray!!.isNotEmpty()) {
                    binding.txtLocationName.setText(it.data[0].selectedDataArray?.get(0)?.name)
                    binding.buttonView.btnContinue.isClickable = true
                    binding.buttonView.btnContinue.isEnabled = true
                    locationListAdapter.objList.find { it1 ->
                        it1.id == it.data[0].selectedDataArray?.get(
                            0
                        )?.id
                    }?.isSelected = 1
                    selectedOption.id = it.data[0].selectedDataArray!![0].id
                    selectedOption.name = it.data[0].selectedDataArray!![0].name

                }
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {

                    binding.txtLocationName.setText(mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name)
                    binding.buttonView.btnContinue.isClickable = true
                    binding.buttonView.btnContinue.isEnabled = true
                    locationListAdapter.objList.find { it1 -> it1.id == mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id }?.isSelected =
                        1

                    selectedOption.id =
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id
                    selectedOption.name =
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name

                    Util.print(">>>>>>>>>>>>>>>>>${selectedOption.id}::::::::::${selectedOption.name}")

                }
            }
        }

        questionViewModel.saveAnswerResponse.observe(this@SecondTemplateActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer = ArrayList()
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.add(selectedOption)
                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT)
                    finish()
                else
                    Util.manageTemplate(this@SecondTemplateActivity, isFrom)

            } else {
                Util.showToastMessage(this, it.msg, true)
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
                    questionViewModel.questionOptionsListApiRequest(
                        this@SecondTemplateActivity,
                        searchKey,
                        pageNo,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }

                2 -> {
                    if (showProgess) Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this@SecondTemplateActivity,
                        mainJsonObj!!
                    )
                }
            }
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

//        binding.buttonView.btnContinue.isClickable = true
//        binding.buttonView.btnContinue.isEnabled = true
        callApi(1)
    }

    private fun onBackPress() {
        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@SecondTemplateActivity)
        }
    }


    private fun saveData() {
        mainJsonObj = JSONObject().apply {
            this.put(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey, selectedOption.id)
        }

        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainJsonObj}")

        callApi(2)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {

        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) bottomSheetDialog!!.dismiss()


        bottomSheetDialog =
            BottomSheetDialog(this@SecondTemplateActivity, R.style.AppBottomSheetDialogTheme)

        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)



        bottomScreenBinding.rvLocation.visibility = View.VISIBLE

        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding.root)
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setCancelable(true)




        bottomSheetDialog!!.window?.let { Util.statusBarColor(this@SecondTemplateActivity, it) }

        bottomScreenBinding.edtSearch.requestFocus()

        bottomScreenBinding.rvTextSuggestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        bottomScreenBinding.rvLocation.apply {
            this.layoutManager =
                LinearLayoutManager(this@SecondTemplateActivity, RecyclerView.VERTICAL, false)

            this.adapter = locationListAdapter

            this.addOnScrollListener(object :
                PaginationListenerAdapter(this.layoutManager as LinearLayoutManager) {
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


        }

        bottomScreenBinding.rvTextSuggestion.adapter = suggetionDialogAdapter

        bottomScreenBinding.txtHeaderTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle

        bottomScreenBinding.txtOtherTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle

        /*if (lastPos != -1) {

    //            locationListAdapter.objList[lastPos].isSelected =
    //                if (locationListAdapter.objList[lastPos].isSelected == 1) 0 else 1
            bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn)
            bottomScreenBinding.btnDialogContinue.setTextColor(
                ContextCompat.getColor(this, R.color.color_yellow)
            )
            bottomScreenBinding.btnDialogContinue.isClickable = true
            locationListAdapter.notifyDataSetChanged()
        }*/
        if (searchKey.isNotEmpty()) {
            bottomScreenBinding.edtSearch.setText(searchKey)
            bottomScreenBinding.txtHeaderTitle.visibility = View.GONE
            bottomScreenBinding.rvTextSuggestion.visibility = View.GONE
            bottomScreenBinding.txtClear.visibility = View.VISIBLE
            bottomScreenBinding.imgLeft.visibility = View.VISIBLE
            bottomScreenBinding.imgSearch.visibility = View.GONE
        }


        // set Position Dialoge open
        locationListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
        locationListAdapter.objList.find { it.id == selectedOption.id }?.isSelected = 1



        bottomScreenBinding.txtTitleClear.setOnClickListener {
            bottomScreenBinding.edtSurName.setText("")
        }
        bottomScreenBinding.txtClear.setOnClickListener {
            bottomScreenBinding.edtSearch.setText("")
            searchKey = ""
            isLoading = false
            pageNo = 0
            callApi(1, false)
        }
        bottomScreenBinding.imgLeft.setOnClickListener {
            bottomScreenBinding.edtSearch.setText(
                ""
            )
        }

        bottomScreenBinding.txtSuggestion.visibility = View.GONE
        bottomScreenBinding.txtChooseReligion.visibility = View.GONE


        bottomScreenBinding.edtSearch.apply {
            this.observeTextChange {
                isLoading = false
                pageNo = 0

                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                if (this.text!!.isNotEmpty()) bottomScreenBinding.txtHeaderTitle.visibility =
                    View.GONE
                else bottomScreenBinding.txtHeaderTitle.visibility = View.VISIBLE

                if (this.text!!.isNotEmpty()) bottomScreenBinding.rvTextSuggestion.visibility =
                    View.GONE
                else bottomScreenBinding.rvTextSuggestion.visibility = View.VISIBLE

                if (this.text!!.isNotEmpty()) bottomScreenBinding.txtClear.visibility =
                    View.VISIBLE
                else bottomScreenBinding.txtClear.visibility = View.GONE

                if (this.text!!.isNotEmpty()) bottomScreenBinding.imgLeft.visibility = View.VISIBLE
                else bottomScreenBinding.imgLeft.visibility = View.GONE

                bottomScreenBinding.imgSearch.visibility =
                    if (this.text!!.isEmpty()) View.VISIBLE else View.GONE


                searchKey = this.text.toString()
                callApi(1, false)

                if (this.text!!.isNotEmpty())
                    bottomScreenBinding.btnDialogContinue.isEnabled = true
            }

        }

        bottomScreenBinding.edtSurName.apply {
            this.observeTextChange {
                bottomScreenBinding.txtTitleClear.visibility = if (this.text.toString().length > 1)
                    View.VISIBLE
                else View.GONE
            }
        }




        bottomScreenBinding.btnDialogContinue.setOnClickListener(this)

        bottomScreenBinding.btnCancel.setOnClickListener {
            bottomSheetDialog!!.dismiss()
        }


    }

    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding.btnDialogContinue.isEnabled = shouldShow
        bottomScreenBinding.btnDialogContinue.isClickable = shouldShow


        bottomScreenBinding.btnDialogContinue.setOnClickListener {
            binding.txtLocationName.setText(selectedOption.name)
            binding.buttonView.btnContinue.isEnabled = true
            bottomSheetDialog!!.dismiss()

            if (locationListAdapter.objList.any { it.isSelected == 1 }) {

                //ADDED BY VAISHALI
                saveData()
            }

            binding.buttonView.btnContinue.isEnabled = true
            bottomSheetDialog!!.dismiss()

            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {

                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name =
                    selectedOption.name
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id =
                    selectedOption.id
            } else {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.add(GeneralModel())
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name =
                    selectedOption.name
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id =
                    selectedOption.id
            }
        }
    }


    fun showNoDataFoundString(shouldShow: Boolean) {
        bottomScreenBinding.txtNoDataFound.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {


            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.rlSpinner, R.id.txtLocationName, R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels;
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                locationListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                locationListAdapter.objList[lastPos].isSelected = 1


                selectedOption.id = locationListAdapter.objList[lastPos].id
                selectedOption.name = locationListAdapter.objList[lastPos].name

                locationListAdapter.notifyDataSetChanged()
                bottomScreenBinding.btnDialogContinue.isEnabled = true
                bottomScreenBinding.btnDialogContinue.isClickable = true
            }

            R.id.btnDialogContinue -> {
                binding.txtLocationName.setText(selectedOption.name)

                if (selectedOption.name.isNotEmpty()) {
                    binding.buttonView.btnContinue.isEnabled = true
                }
                bottomSheetDialog!!.dismiss()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                binding.txtLocationName.setText(suggetionDialogAdapter.objList[lastPos].optionName)
                bottomSheetDialog!!.dismiss()

                selectedOption.id = suggetionDialogAdapter.objList[lastPos].id
                selectedOption.name = suggetionDialogAdapter.objList[lastPos].optionName
                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.isClickable = true

                showButton(selectedOption.id.isNotEmpty())

                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {

                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name =
                        selectedOption.name
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id =
                        selectedOption.id
                } else {
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.add(GeneralModel())
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].name =
                        selectedOption.name
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0].id =
                        selectedOption.id
                }

            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                saveData()
            }
        }
    }
}