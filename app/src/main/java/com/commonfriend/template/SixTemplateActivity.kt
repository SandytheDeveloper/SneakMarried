package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.adapter.PlanToSettleInDialogAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CATEGORY_ID
import com.commonfriend.utils.COMMA_SPACE_SEPERATOR
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.LAST_POS
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.mainObjList
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject


class SixTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding//ActivitySixTemplateBinding

    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private lateinit var mainObj: JSONObject

    private var selectedIds: List<String> = arrayListOf()
    private var lastPos = -1
    var selectedArray = ArrayList<GeneralModel>()
    var tempSelectedArray = ArrayList<GeneralModel>()

    private lateinit var questionViewModel: QuestionViewModel

    private lateinit var gridViewAdapter: PlanToSettleInDialogAdapter
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL


    // template code 6
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLocationName.setText("")

        initialization()
    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@SixTemplateActivity)
        }
    }

    private fun initialization() {

        if(intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        Util.statusBarColor(this, window)

        gridViewAdapter = PlanToSettleInDialogAdapter(this, this)


        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        allApiResponses()

        activityOnBackPressed(this,this){
            onBackPress()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@SixTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
            color = R.color.color_black
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


        binding.customHeader.get().txtPageNO.text =StringBuilder().append((LAST_POS + 1).toString() ).append("/").append(mainObjList[CATEGORY_ID].questionList.size.toString())
//            (LAST_POS + 1).toString() + "/" + mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().progressBar.progress = LAST_POS + 1
        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
//        binding.btnContinue.isClickable = true
//        binding.btnContinue.isEnabled = true

        callApi(1)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {

        bottomSheetDialog =
            BottomSheetDialog(this@SixTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        if (bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()
        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding.root)
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setCancelable(true)


        bottomSheetDialog!!.window?.let { Util.statusBarColor(this@SixTemplateActivity, it) }

        bottomScreenBinding.rvLocation.visibility = View.VISIBLE

        bottomScreenBinding.txtChooseReligion.visibility = View.GONE
        bottomScreenBinding.txtSuggestion.visibility = View.GONE
        bottomScreenBinding.llSearch.visibility = View.GONE
        bottomScreenBinding.rvCitySuggestion.visibility = View.GONE

        bottomScreenBinding.txtHeaderTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding.txtOtherTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
        bottomScreenBinding.txtSuggestion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding.txtChooseReligion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle

        var layout = FlexboxLayoutManager(this)
        layout.justifyContent = JustifyContent.CENTER
        bottomScreenBinding.rvLocation.layoutManager = layout
        bottomScreenBinding.rvLocation.adapter = gridViewAdapter


        tempSelectedArray.clear()
        if (tempSelectedArray.isEmpty()) {
            gridViewAdapter.objMainList.filter { it.isSelected == 1 }.forEach { it.isSelected = 0 }
            tempSelectedArray.addAll(selectedArray)

            for (element in tempSelectedArray) {
                gridViewAdapter.objMainList.find { it.id == element.id }?.isSelected = 1
            }

            gridViewAdapter.notifyDataSetChanged()
            showButton(true)
        }


        gridViewAdapter.notifyDataSetChanged()



        bottomScreenBinding.btnCancel.setOnClickListener {
            bottomSheetDialog!!.dismiss()
        }
        bottomScreenBinding.btnDialogContinue.setOnClickListener(this)
        bottomScreenBinding.btnDialogContinue.isClickable = false

        if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDialog!!.behavior.peekHeight =
                Resources.getSystem().displayMetrics.heightPixels;
            bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    fun showButton(shouldShow: Boolean) {

        bottomScreenBinding.btnDialogContinue.isClickable = shouldShow
        bottomScreenBinding.btnDialogContinue.isEnabled = shouldShow

//        bottomScreenBinding.btnDialogContinue.setTextColor(
//            ContextCompat.getColor(
//                this,
//                if (shouldShow) R.color.color_yellow else R.color.white
//            )
//        )
//
//        bottomScreenBinding.btnDialogContinue.setBackgroundResource(if (shouldShow) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)

        if (shouldShow) {

            bottomScreenBinding.btnDialogContinue.setOnClickListener {
                selectedArray.clear()
                selectedArray.addAll(tempSelectedArray)
                tempSelectedArray.clear()

                Util.print("==========${selectedArray.size}======")

                //set text in txtLocationName
                gridViewAdapter.notifyDataSetChanged()
                binding.txtLocationName.setText(gridViewAdapter.objMainList.filter { it.isSelected == 1 }
                    .joinToString(COMMA_SPACE_SEPERATOR) { it.name })


                binding.buttonView.btnContinue.isEnabled = true ;//View.VISIBLE
                binding.buttonView.btnContinue.isClickable = true ;//View.VISIBLE
                bottomSheetDialog!!.dismiss()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {

            R.id.btnDialogContinue -> {
                bottomSheetDialog!!.dismiss()
            }
            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString =
                    gridViewAdapter.objMainList[lastPos].name

                gridViewAdapter.objMainList[lastPos].isSelected =
                    if (gridViewAdapter.objMainList[lastPos].isSelected == 0) 1 else 0


                if (gridViewAdapter.objMainList[lastPos].isSelected == 1) {
                    tempSelectedArray.add(gridViewAdapter.objMainList[lastPos])
                    if (tempSelectedArray.size > 3) {
                        gridViewAdapter.objMainList.find { it.id == tempSelectedArray[0].id }?.isSelected =
                            0
                        tempSelectedArray.removeAt(0)
                    }
                } else {
                    val removeData =
                        tempSelectedArray.mapIndexed { index, element -> Pair(index, element) }
                            .filter { it.second.id == gridViewAdapter.objMainList[lastPos].id }
                    tempSelectedArray.removeAt(removeData.map { it.first }[0])
                }


                gridViewAdapter.notifyDataSetChanged()

                bottomScreenBinding.btnDialogContinue.isClickable = true
            }

            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {
                bottomSheetDialog()
            }
            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
//                selectedArray.clear()
//                selectedArray.addAll(tempSelectedArray)
//                tempSelectedArray.clear()

                saveData()
                Util.print(">>>>>>>>>>>>>>>>>>>>>>LASTPOS>>>>>>>>>>>>>>>>>$LAST_POS")
            }
        }
    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                gridViewAdapter.addData(it.data[0].arrayData)
                if (it.data[0].selectedDataArray!!.isNotEmpty()) {

                    selectedArray.clear()

                    for (i in 0 until it.data[0].selectedDataArray!!.size) {
                        gridViewAdapter.objMainList.filter { it1 ->
                            it1.id == it.data[0].selectedDataArray?.get(
                                i
                            )?.id
                        }.forEach {
                            it.isSelected = 1
                            selectedArray.add(it)
                        }
                        gridViewAdapter.notifyDataSetChanged()

                        binding.txtLocationName.setText(gridViewAdapter.objMainList.filter { it.isSelected == 1 }
                            .joinToString(COMMA_SPACE_SEPERATOR) { it.name })
                        binding.buttonView.btnContinue.isEnabled = true
                        binding.buttonView.btnContinue.isClickable = true

                        selectedIds = it.data[0].selectedDataArray!!.map { it.id }

                    }
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this) {
            selectedArray.clear()
            tempSelectedArray.clear()
            binding.buttonView.btnContinue.isClickable = true
            Util.dismissProgress()
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                finish()
            else
                Util.manageTemplate(this@SixTemplateActivity,isFrom)
        }

    }

    private fun saveData() {

        gridViewAdapter.objMainList.filter { it.isSelected == 1 }.forEach { it.isSelected = 0 }
        for (element in selectedArray) {
            gridViewAdapter.objMainList.filter { it.id == element.id }.forEach { it.isSelected = 1 }
        }

        if (selectedIds.isNotEmpty() && gridViewAdapter.objMainList.filter { it.isSelected == 1 }
                .map { it.id } == selectedIds) {
            binding.buttonView.btnContinue.isClickable = true
            Util.manageTemplate(this@SixTemplateActivity)
        } else {
            mainObj = JSONObject()
            val answer: ArrayList<String> = ArrayList()
            gridViewAdapter.objMainList.filter { it.isSelected == 1 }.forEach {
                answer.add(it.id)
            }
            mainObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                JSONArray(answer).toString()
            )


            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>$$ DATA IS>>>>>>>>${mainObj}")
            callApi(2)

        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this@SixTemplateActivity)
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }
                2 -> {
                    Util.showProgress(this@SixTemplateActivity)
                    questionViewModel.questionAnswerSaveApiRequest(this, mainObj)
                }
            }
        }else{
            binding.buttonView.btnContinue.isClickable = true
        }
    }
}
