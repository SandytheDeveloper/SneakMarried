package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.adapter.*
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.*
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject

/*SETTLE LOCATION*/
class TwentyTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private lateinit var locationAddressListAdapter: SquareLocationListAdapter
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private lateinit var selectedLocationAdapter: SelectedLocationsListAdapter
    private lateinit var suggestionDialogAdapter: SuggestionLocationDialogAdapter
    private lateinit var questionViewModel: QuestionViewModel
    var lastPos = -1
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var mainJsonObj: JSONObject
    private var clearSelectedList = false
    private var selectedLocationsList: ArrayList<GeneralModel>? = null
    private var debounceHandler = Handler()
    private var debounceRunnable = Runnable {}
    private var isResumed: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }


    override fun onDestroy() {
        super.onDestroy()
        debounceHandler.removeCallbacks(debounceRunnable)
    }


    private fun initialization() {
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom


        with(binding) {
            rlSpinner.setOnClickListener(this@TwentyTemplateActivity)
            txtLocationName.setOnClickListener(this@TwentyTemplateActivity)
            txtLocationDialog.setOnClickListener(this@TwentyTemplateActivity)
            customHeader.get().btnLeft.setOnClickListener(this@TwentyTemplateActivity)
            buttonView.btnContinue.setOnClickListener(this@TwentyTemplateActivity)
        }
        suggestionDialogAdapter = SuggestionLocationDialogAdapter(this, this)
        selectedLocationAdapter = SelectedLocationsListAdapter(this, this)
        locationAddressListAdapter = SquareLocationListAdapter(this, this, "(regions)")

        allApiResponses()
        callApi(1)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding.txtChooseReligion.visibleIf(selectedLocationAdapter.objList.isEmpty())
        bottomScreenBinding.btnDialogContinue.isEnabled = shouldShow
        bottomScreenBinding.rlSelectedView.visibleIf(selectedLocationAdapter.objList.isNotEmpty())

        bottomScreenBinding.btnDialogContinue.isClickable =
            selectedLocationAdapter.objList.isNotEmpty()

        bottomScreenBinding.btnDialogContinue.setOnClickListener {
            mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer =
                selectedLocationAdapter.objList
            clearSelectedList = true
            manageBottomSheetContinueButtonVisibility()
            bottomSheetDialog!!.dismiss()
            binding.buttonView.btnContinue.performClick()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun manageBottomSheetContinueButtonVisibility() {
        selectedLocationAdapter.notifyDataSetChanged()
        locationAddressListAdapter.notifyDataSetChanged()

        binding.txtLocationName.setText(selectedLocationAdapter.objList.joinToString(COMMA_SPACE_SEPERATOR) { it.name })
        binding.txtLocationDialog.hint =
            if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
            else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText

        binding.buttonView.btnContinue.isClickable =
            (binding.txtLocationName.text.toString().trim().isNotEmpty())
        binding.buttonView.btnContinue.isEnabled =
            (binding.txtLocationName.text.toString().trim().isNotEmpty())
    }


    fun showNoDataFoundString(shouldShow: Boolean) {
        bottomScreenBinding.txtNoDataFound.visibleIf(
            shouldShow && !Util.isEmptyText(
                bottomScreenBinding.edtSearch
            )
        )
    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]


        questionViewModel.getSuggestedLocationsResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                if (it.data.isNotEmpty()) {
                    suggestionDialogAdapter.addData(it.data)
                    if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {
                        filterSuggestions()
                    }
                }
            }
        }
        questionViewModel.questionOptionsListResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                if (it.data.isNotEmpty()) {
                    locationAddressListAdapter.addData(it.data)
                    /*if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {
                        filterSuggestions()
                    }*/
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isClickable = true
            if (it.success == 1) {
//                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer =
//                    selectedLocationAdapter.objList

                isResumed = true

                bundle = Bundle().apply {
                    putString(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,Util.getTextValue(binding.txtLocationName).replace(",","|"))
                }

                firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,bundle)
                MainApplication.firebaseAnalytics.setUserProperty(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName.removeString(),Pref.getStringValue(Pref.PREF_USER_ID, "").toString())


                if (isFrom == ActivityIsFrom.FROM_CHECKLIST) {
                    finish()
                } else
                    Util.manageTemplate(this@TwentyTemplateActivity, isFrom)
            }
        }

    }

    private fun filterSuggestions() {
        if (suggestionDialogAdapter.objList.isNotEmpty()) {
            for (i in 0 until suggestionDialogAdapter.objList.size) {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.filter { it.placeId == suggestionDialogAdapter.objList[i].placeId }
                    .let {
                        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${suggestionDialogAdapter.objList[i].placeId}")
                        suggestionDialogAdapter.objList.removeAll(it)
                        suggestionDialogAdapter.notifyDataSetChanged()
                    }
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionViewModel.suggestedLocationsListApiRequest(this, "1")
                }

                3 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionViewModel.questionOptionsListApiRequest(this,"",0,"", mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName)
                }

                2 -> {
                    Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(this, mainJsonObj)
                }
            }
        } else {
            binding.buttonView.btnContinue.isClickable = true
        }
    }


    override fun onPause() {
        super.onPause()
        isResumed = true
    }


    override fun onStop() {
        super.onStop()
        isResumed = true
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        if (!isResumed)
            return
        super.onResume()
        binding.buttonView.btnSkip.setOnClickListener(this)
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE
        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@TwentyTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty()) binding.llPrivacy.visibility =
            View.INVISIBLE
        else binding.infoMessage.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                "\\n",
                System.lineSeparator()
            )
        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())


        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        selectedLocationAdapter.addData(mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer)
        locationAddressListAdapter.setPlaceId(mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.joinToString(",") {it.placeId})
        manageBottomSheetContinueButtonVisibility()
    }


    private fun bottomSheetDialog() {

        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) bottomSheetDialog!!.dismiss()

        bottomSheetDialog =
            BottomSheetDialog(this@TwentyTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)
        bottomSheetDialog!!.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(bottomScreenBinding.root)
            show()
            setCancelable(true)
            window?.let { Util.statusBarColor(this@TwentyTemplateActivity, it) }

            setOnDismissListener {
                if (clearSelectedList) {
                    if (selectedLocationsList != null)
//                        mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer = selectedLocationsList!!
                        selectedLocationsList = ArrayList()
                }
                clearSelectedList = !clearSelectedList
                isResumed = false
                callApi(1, false)
            }
        }

        val height = resources.getDimension(com.intuit.sdp.R.dimen._30sdp)
        bottomScreenBinding.rlTopView.layoutParams.height = height.toInt()

        bottomScreenBinding.apply {
            rvLocation.visible()
            edtSearch.setOnClickListener {
                txtSuggestion.gone()
                rvTextSuggestion.gone()
            }



            edtSearch.requestFocus()


            rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@TwentyTemplateActivity, RecyclerView.HORIZONTAL, false)

            rvTextSuggestion.adapter = suggestionDialogAdapter


            rvLocation.layoutManager = LinearLayoutManager(this@TwentyTemplateActivity)
            rvLocation.adapter = locationAddressListAdapter


            rvCitySuggestion.layoutManager =
                LinearLayoutManager(this@TwentyTemplateActivity, RecyclerView.HORIZONTAL, false)
            rvCitySuggestion.adapter = selectedLocationAdapter

            txtHeaderTitle.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
            txtOtherTitle.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
            txtSuggestion.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
            txtChooseReligion.text = mainObjList[CATEGORY_ID].questionList[LAST_POS].dialogTitle

            txtChooseReligion.visibility =
                if (selectedLocationAdapter.objList.isEmpty()) View.VISIBLE else View.GONE

            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SIZE OF SUGGESTION ADAPTER${suggestionDialogAdapter.objList.size}")

            txtClear.setOnClickListener {
                edtSearch.setText("")
                locationAddressListAdapter.objList.clear()
            }
            imgLeft.setOnClickListener {
                edtSearch.setText("")
                locationAddressListAdapter.objList.clear()
            }


            locationAddressListAdapter.objList.clear()

            selectedLocationAdapter.addData(mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer)

            if (selectedLocationAdapter.objList.isNotEmpty()) {


                (selectedLocationAdapter.objList.joinToString(
                    ", "
                ) { it.placeId })
                txtClear.visible()
                imgLeft.visible()
                imgSearch.gone()
            }


            filterSuggestions()
            Util.print(">>>>>>>>>>>>>>>>>>>>>BOTTOM SHEET DIALOG AP SIZE>>>>${suggestionDialogAdapter.objList.size}")
            bottomScreenBinding.txtSuggestion.visibleIf(suggestionDialogAdapter.objList.isNotEmpty())
            bottomScreenBinding.rvTextSuggestion.visibleIf(suggestionDialogAdapter.objList.isNotEmpty())


            edtSearch.observeTextChange {
                txtClear.visibility = if (it.isNotEmpty()) View.VISIBLE
                else View.GONE

                imgLeft.visibility = if (it.isNotEmpty()) View.VISIBLE
                else View.GONE

                imgSearch.visibility = if (it.isEmpty()) View.VISIBLE
                else View.GONE

                debounceHandler.removeCallbacks(debounceRunnable)
                debounceRunnable = Runnable {
                    locationAddressListAdapter.filter.filter(it)
                }
                debounceHandler.postDelayed(debounceRunnable, 300)
            }
            btnDialogContinue.setOnClickListener(this@TwentyTemplateActivity)
            showButton(selectedLocationAdapter.objList.isNotEmpty())
            btnCancel.setOnClickListener {
                clearSelectedList = true
                bottomSheetDialog!!.dismiss()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.rlSpinner, R.id.txtLocationName, R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }


            R.id.btnClose -> {
                lastPos = view.tag.toString().toInt()
                suggestionDialogAdapter.objList.find { it.placeId == selectedLocationAdapter.objList[lastPos].placeId }?.isSelected =
                    0
                suggestionDialogAdapter.objList.find { it.placeId == selectedLocationAdapter.objList[lastPos].placeId }?.isSuggested =
                    false
                locationAddressListAdapter.objList.find { it.placeId == selectedLocationAdapter.objList[lastPos].placeId }?.isSelected =
                    0

                if (selectedLocationAdapter.objList[lastPos].isSuggested) {
                    selectedLocationAdapter.objList[lastPos].isSelected = 0
                    selectedLocationAdapter.objList[lastPos].isSuggested = false
                    suggestionDialogAdapter.objList.add(selectedLocationAdapter.objList[lastPos])
                    bottomScreenBinding.txtSuggestion.visible()
                    bottomScreenBinding.rvTextSuggestion.visible()
                }
                selectedLocationAdapter.objList.removeAt(lastPos)
                selectedLocationAdapter.notifyDataSetChanged()
                locationAddressListAdapter.setPlaceId(selectedLocationAdapter.objList.joinToString(",") { it.placeId })
                showButton(selectedLocationAdapter.objList.isNotEmpty())
            }


            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }


            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                filterDataInSelectionList(locationAddressListAdapter.objList[lastPos], false)
                Util.hideKeyBoard(this@TwentyTemplateActivity, view)
//                bottomScreenBinding.edtSearch.setText("")
                showButton(selectedLocationAdapter.objList.isNotEmpty())
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                suggestionDialogAdapter.objList[lastPos].isSuggested = true
                filterDataInSelectionList(suggestionDialogAdapter.objList[lastPos], true)
                Util.hideKeyBoard(this@TwentyTemplateActivity, view)
                suggestionDialogAdapter.objList.removeAt(lastPos)
                suggestionDialogAdapter.notifyDataSetChanged()

                if (suggestionDialogAdapter.objList.isEmpty()) {
                    bottomScreenBinding.txtSuggestion.gone()
                    bottomScreenBinding.rvTextSuggestion.gone()
                }

                showButton(selectedLocationAdapter.objList.isNotEmpty())
            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                saveData()
            }

            R.id.btnLeft -> {
                onBackPress()
            }
        }
    }


    private fun dividingDataForLocation(selectedModel: GeneralModel): GeneralModel {
        if (selectedModel.description.contains(",")) {
            val terms = selectedModel.description.split(",")
            when (terms.size) {
                1 -> {
                    selectedModel.isCountry = true
                    selectedModel.country = terms[0].trim()
                }


                2 -> {
                    selectedModel.isState = true
                    selectedModel.country = terms[1].trim()
                    selectedModel.state = terms[0].trim()
                }


                3 -> {
                    selectedModel.isCity = true
                    selectedModel.country = terms[2].trim()
                    selectedModel.state = terms[1].trim()
                    selectedModel.city = terms[0].trim()
                }


                4 -> {
                    selectedModel.isArea = true
                    selectedModel.country = terms[3].trim()
                    selectedModel.state = terms[2].trim()
                    selectedModel.city = terms[1].trim()
                    selectedModel.area = terms[0].trim()
                }


                5 -> {
                    selectedModel.isArea = true
                    selectedModel.country = terms[4].trim()
                    selectedModel.state = terms[3].trim()
                    selectedModel.city = terms[2].trim()
                    selectedModel.area = terms[0].trim()
                }


                6 -> {
                    selectedModel.isArea = true
                    selectedModel.country = terms[5].trim()
                    selectedModel.state = terms[4].trim()
                    selectedModel.city = terms[3].trim()
                    selectedModel.area = terms[0].trim()
                }


            }
        }
        return selectedModel
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun filterDataInSelectionList(selectedData: GeneralModel, fromSuggestions: Boolean) {
        if (selectedLocationAdapter.objList.isNotEmpty()) {
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { it.isCountry && it.country == selectedData.country }
                .toSet())
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { it.isState && it.state == selectedData.state }
                .toSet())
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { it.isCity && it.city == selectedData.city }
                .toSet())
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { selectedData.isCountry && it.country == selectedData.country }
                .toSet())
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { selectedData.isState && it.state == selectedData.state }
                .toSet())
            selectedLocationAdapter.objList.removeAll(selectedLocationAdapter.objList.filter { selectedData.isCity && it.city == selectedData.city }
                .toSet())
            selectedLocationAdapter.notifyDataSetChanged()
        }


        if (selectedData.isSelected == 1) { // Already Select
            selectedData.isSelected = 0
            selectedLocationAdapter.objList.removeIf { it.placeId == selectedData.placeId }


        } else { // Not Select
            selectedData.isSelected = 1
            selectedLocationAdapter.objList.add(
                if (fromSuggestions) dividingDataForLocation(
                    selectedData
                ) else selectedData
            )
            selectedLocationAdapter.notifyDataSetChanged()
        }
        locationAddressListAdapter.setPlaceId(selectedLocationAdapter.objList.joinToString(", ") { it.placeId })
        locationAddressListAdapter.notifyDataSetChanged()


        selectedLocationsList = ArrayList()
        selectedLocationsList!!.addAll(selectedLocationAdapter.objList)
    }


    private fun saveData() {
        mainJsonObj = JSONObject()

        Util.print(">>>>SIZE >>>>${selectedLocationAdapter.objList.size}")
        val jsonArray = JSONArray()
        for (i in mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer) {
            val obj = JSONObject().apply {
                this.put("address", i.address)
                this.put("area", i.area)
                this.put("city", i.city)
                this.put("country", i.country)
                this.put("description", i.description)
                this.put("isArea", i.isArea)
                this.put("isCity", i.isCity)
                this.put("isCountry", i.isCountry)
                this.put("isSelected", i.isSelected)
                this.put("isState", i.isState)
                this.put("latitude", i.latitude)
                this.put("longitude", i.longitude)
                this.put("name", i.name)
                this.put("placeId", i.placeId)
                this.put("type", i.type)
                this.put("value", i.value)
                this.put("zipcode", i.zipcode)
            }
            jsonArray.put(obj)
        }

        mainJsonObj.put(
            mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
            jsonArray.toString()
        )
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainJsonObj}")
        callApi(2)
    }


    private fun onBackPress() {
        if (backPressedOnce) {

            backPressedOnce = false
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                finish()
            } else {
                finish()
                Util.manageBackClick(this@TwentyTemplateActivity)
            }
        }
    }


}

