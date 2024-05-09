package com.commonfriend.template

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.adapter.LocationAddressListAdapter
import com.commonfriend.adapter.SuggestionLocationDialogAdapter
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

class EightTeenTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var mainJsonObj: JSONObject? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private lateinit var locationListAdapter: LocationAddressListAdapter
    private lateinit var suggestionDialogAdapter: SuggestionLocationDialogAdapter
    var lastPos = -1
    private lateinit var questionViewModel: QuestionViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    private var finalSelection = GeneralModel()
    private var debounceHandler = Handler()
    private var debounceRunnable = Runnable {}

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
        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)

        binding.txtQuestion.text = Util.applyCustomFonts(
            this@EightTeenTemplateActivity,
            null,
            mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else ""
        )

//        logForCurrentScreen("onboarding",mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey)

        binding.customHeader.get().progressBar.progress = LAST_POS + 1

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

        binding.customHeader.get().txtPageNO.text =
            StringBuilder().append((LAST_POS + 1).toString()).append("/")
                .append(mainObjList[CATEGORY_ID].questionList.size.toString())

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

        locationListAdapter = LocationAddressListAdapter(
            this,
            this,
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("locality")) "sublocality" else "(cities)"
        )
        suggestionDialogAdapter = SuggestionLocationDialogAdapter(this, this)

        apiResponses()

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName

        binding.buttonView.btnContinue.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)
        binding.buttonView.btnSkip.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    private fun apiResponses() {

        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.saveAnswerResponse.observe(this@EightTeenTemplateActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer = ArrayList()
                mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.add(finalSelection)
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.from api responses >>>>>>>>>>>>>>>>${finalSelection.name}")

                bundle = Bundle().apply {
                    putString(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,Util.getTextValue(binding.txtLocationName)/*.replace(",","|")*/)
                }

                firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName,bundle)


                MainApplication.firebaseAnalytics.setUserProperty(
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                Util.getTextValue(binding.txtLocationName))
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.BLAAH BLAAH >>>>>>>>>>>>>>>>${mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString()}")


                if (isFrom == ActivityIsFrom.FROM_CHECKLIST) {
                    finish()
                } else
                    Util.manageTemplate(this@EightTeenTemplateActivity, isFrom)
            } else {
                Util.showToastMessage(this, it.msg, true)
            }
        }

        questionViewModel.getSuggestedLocationsResponse.observe(this@EightTeenTemplateActivity) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isClickable = true
            if (it.success == 1) {
                suggestionDialogAdapter.addData(it.data)
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
                    if (showProgess) Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this@EightTeenTemplateActivity,
                        JSONObject().put(
                            mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                            finalSelection.description
                        )
                    )
                }

                2 -> {
                    if (showProgess) Util.showProgress(this)
                    questionViewModel.suggestedLocationsListApiRequest(
                        this@EightTeenTemplateActivity,
                        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains("locality")) "3" else "1"
                    )
                }
            }
        } else {
            binding.buttonView.btnContinue.isClickable = true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer.isNotEmpty()) {
            finalSelection = mainObjList[CATEGORY_ID].questionList[LAST_POS].locationAnswer[0]
            if (mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey == "candidate_birthplace" && finalSelection.name.contains(
                    ","
                )
            ) {
                binding.txtLocationName.setText(finalSelection.name.split(",")[0])
            } else
                binding.txtLocationName.setText(finalSelection.name)
        }
        binding.buttonView.btnContinue.isEnabled =
            (Util.getTextValue(binding.txtLocationName).isNotEmpty())
        binding.buttonView.btnContinue.isClickable = binding.buttonView.btnContinue.isEnabled
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> from onResume>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${finalSelection.name}")


//        callApi(2)
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@EightTeenTemplateActivity)
        }
    }


    private fun saveData() {
        Util.print("============saveData==========" + "::" + finalSelection.description);
        mainJsonObj = JSONObject()
//        mainJsonObj!!.put(mainObjList[CATEGORY_ID].categoryKey, JSONObject().put(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,finalSelection.description))
        callApi(1)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {
        bottomSheetDialog =
            BottomSheetDialog(this@EightTeenTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        if (bottomSheetDialog!!.isShowing) bottomSheetDialog!!.dismiss()
        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding.root)
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setCancelable(true)

        callApi(2)

        bottomSheetDialog!!.window?.let { Util.statusBarColor(this@EightTeenTemplateActivity, it) }
        bottomScreenBinding.apply {

            txtHeaderTitle.visibility =
                if (suggestionDialogAdapter.objList.isEmpty()) View.GONE else View.VISIBLE
            rvTextSuggestion.visibility =
                if (suggestionDialogAdapter.objList.isEmpty()) View.GONE else View.VISIBLE

            edtSearch.setOnClickListener {
                txtHeaderTitle.visibility = View.INVISIBLE
                rvTextSuggestion.visibility = View.GONE
            }

            rvLocation.visibility = View.VISIBLE
            rvTextSuggestion.layoutManager =
                LinearLayoutManager(this@EightTeenTemplateActivity, RecyclerView.HORIZONTAL, false)

            rvLocation.layoutManager =
                LinearLayoutManager(this@EightTeenTemplateActivity, RecyclerView.VERTICAL, false)

            rvLocation.adapter = locationListAdapter

            locationListAdapter.objList = ArrayList()

            rvTextSuggestion.adapter = suggestionDialogAdapter

            txtHeaderTitle.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle

            txtSuggestion.visibility = View.GONE

            if (finalSelection.description.isNotEmpty() && !Util.isEmptyText(binding.txtLocationName)) {
                edtSearch.setText(finalSelection.description)
                locationListAdapter.filter.filter(finalSelection.description)
                locationListAdapter.setPlaceId(finalSelection.description)
                //locationListAdapter.setPlaceId(selectedOption.id)--
                locationListAdapter.objList.find { it.description == finalSelection.description }?.isSelected =
                    1
                locationListAdapter.notifyDataSetChanged()
                //rvTextSuggestion.visibility = View.GONE
                txtClear.visibility = View.VISIBLE
                imgLeft.visibility = View.VISIBLE
                imgSearch.visibility = View.GONE
            }
            txtClear.setOnClickListener {
                edtSearch.setText("")
            }
            imgLeft.setOnClickListener {
                edtSearch.setText("")
            }

            txtChooseReligion.visibility = View.GONE
            rlSelectedView.visibility = View.GONE

            edtSearch.requestFocus()

            edtSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    txtClear.visibility = if (s.toString().isNotEmpty()) View.VISIBLE else View.GONE
                    imgLeft.visibility = if (s.toString().isNotEmpty()) View.VISIBLE else View.GONE
                    imgSearch.visibility = if (s.toString().isEmpty()) View.VISIBLE else View.GONE

                    if (s!!.isNotEmpty()) {
                        debounceHandler.removeCallbacks(debounceRunnable)
                        debounceRunnable = Runnable {
                            locationListAdapter.filter.filter(s)
                            btnDialogContinue.setOnClickListener(null)
                        }
                        debounceHandler.postDelayed(debounceRunnable, 500)
                    }
                }
            })

            btnDialogContinue.setOnClickListener(null)
            btnCancel.setOnClickListener {
                bottomSheetDialog!!.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        debounceHandler.removeCallbacks(debounceRunnable)
    }

    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding.btnDialogContinue.apply {
            isEnabled = shouldShow
            setOnClickListener(if (shouldShow) this@EightTeenTemplateActivity else null)
        }
    }

    fun showNoDataFoundString(shouldShow: Boolean) {
        bottomScreenBinding.txtNoDataFound.visibility =
            if (shouldShow && !Util.isEmptyText(bottomScreenBinding.edtSearch)) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()
                locationListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                locationListAdapter.objList[lastPos].isSelected = 1
                locationListAdapter.setPlaceId(locationListAdapter.objList[lastPos].description)

                locationListAdapter.notifyDataSetChanged()

                Util.hideKeyBoard(this@EightTeenTemplateActivity, view)
                bottomScreenBinding.btnDialogContinue.apply {
                    isEnabled = true
                    setOnClickListener(this@EightTeenTemplateActivity)
                }
            }

            R.id.btnDialogContinue -> {
                val location = locationListAdapter.objList.filter { it.isSelected == 1 }
                finalSelection.description = location[0].description
                if (location[0].description.contains(",")) {
                    finalSelection.name =
                        location[0].description.split(",")[0]
                } else {
                    finalSelection.name = location[0].description
                }
                finalSelection.id = location[0].placeId

                binding.txtLocationName.setText(finalSelection.name)
                locationListAdapter.setPlaceId(finalSelection.description)
                binding.buttonView.btnContinue.isEnabled = (finalSelection.name.isNotEmpty())
                //ADDED BY VAISHALI
                if (Util.getTextValue(binding.txtLocationName).isNotEmpty())
                    saveData()
                bottomSheetDialog!!.dismiss()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                binding.txtLocationName.setText(suggestionDialogAdapter.objList[lastPos].name)
                bottomSheetDialog!!.dismiss()

                finalSelection.id = suggestionDialogAdapter.objList[lastPos].placeId
                finalSelection.name = suggestionDialogAdapter.objList[lastPos].name
                finalSelection.description = suggestionDialogAdapter.objList[lastPos].description
                binding.buttonView.btnContinue.isEnabled = true
                binding.buttonView.btnContinue.isClickable = true
                showButton(finalSelection.id.isNotEmpty())

                //ADDED BY VAISHALI
                if (Util.getTextValue(binding.txtLocationName).isNotEmpty())
                    saveData()

            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                saveData()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }
        }
    }
}