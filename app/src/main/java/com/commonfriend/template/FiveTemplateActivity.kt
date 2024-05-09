package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.base.BaseActivity

import com.commonfriend.R
import com.commonfriend.adapter.CollageListAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject


class FiveTemplateActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding// ActivityFiveTemplateBinding
    private lateinit var mainJsonObj: JSONObject
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    var otherOptionText = ""
    var lastPos = -1
    private var searchKey = ""
    private var selectedId = ""
    private lateinit var collageListAdapter: CollageListAdapter
    private lateinit var questionViewModel: QuestionViewModel
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var pageNo: Int = 0

    // template code 5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLocationName.setText("")

        initialization()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
//        binding.btnContinue.isClickable = binding.btnContinue.isEnabled


        binding.txtQuestion.text = Util.applyCustomFonts(
            this@FiveTemplateActivity,
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

        binding.customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size

//        binding.customHeader.get().txtSkip.visibility =
//            if (Util.isQuestionSkipable()) View.VISIBLE else View.GONE

        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.INVISIBLE
        binding.buttonView.btnSkip.setOnClickListener(this)

        binding.customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName
        binding.customHeader.get().txtPageNO.text =StringBuilder().append((LAST_POS + 1).toString() ).append("/").append(mainObjList[CATEGORY_ID].questionList.size.toString())

//            (LAST_POS + 1).toString() + "/" + mainObjList[CATEGORY_ID].questionList.size
        binding.txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
        binding.customHeader.get().progressBar.progress = LAST_POS + 1


//
//        binding.buttonView.btnContinue.isEnabled =true
//        binding.buttonView.btnContinue.isClickable=true
        /*if(searchKey )
        isLastPage=false
        isLoading=false
        pageNo=0

        callApi(1)*/

    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.questionOptionsListResponse.observe(this) { it ->
            Util.dismissProgress()
            if (it.data.isNotEmpty()) {
                Util.print("===================== DATA :: " + searchKey + "::" + pageNo)
//                if (searchKey.isNotEmpty()) {
                    collageListAdapter.addData(it.data[0].arrayData, pageNo <= 0)
//                }
                if (it.data[0].arrayData.isNotEmpty())
                    pageNo += 1
                if (it.data[0].selectedDataArray!!.isNotEmpty() && collageListAdapter.objList.none { it.isSelected == 1 }) {
                    collageListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                    collageListAdapter.objList.find { it1 -> it1.id == it.data[0].selectedDataArray!![0].id }?.isSelected =
                        1
                    collageListAdapter.notifyDataSetChanged()
//                    binding.txtLocationName.setText(it.data[0].selectedDataArray!![0].name)
                    binding.buttonView.btnContinue.isEnabled =true
                    binding.buttonView.btnContinue.isClickable=true
                    selectedId = it.data[0].selectedDataArray!![0].id
//                    selectedOptionId = selectedId
                }

            }
            isLastPage = it.data.isEmpty()
            isLoading = false
        }
        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            binding.buttonView.btnContinue.isClickable=true
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST)
                finish()
            else
                Util.manageTemplate(this@FiveTemplateActivity,isFrom)
        }
    }

    private fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(this@FiveTemplateActivity)) {
            when (tag) {
                1 -> {
                    if (isLoading)
                        return
                    isLoading = true
                    if (showProgress)
                        Util.showProgress(this@FiveTemplateActivity)
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        searchKey,
                        pageNo,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName
                    )
                }
                2 -> {
                    Util.showProgress(this@FiveTemplateActivity)
                    questionViewModel.questionAnswerSaveApiRequest(this, mainJsonObj)
                }
            }
        }else{
            binding.buttonView.btnContinue.isClickable =true
        }
    }

    private fun initialization() {
        if(intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        Util.statusBarColor(this, window)

        binding.rlSpinner.setOnClickListener(this)
        binding.txtLocationDialog.setOnClickListener(this)
        binding.txtLocationName.setOnClickListener(this)
        binding.customHeader.get().btnLeft.setOnClickListener(this)

        collageListAdapter = CollageListAdapter(this, this)
        allApiResponses()
        binding.buttonView.btnContinue.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@FiveTemplateActivity)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {

        bottomSheetDialog =
            BottomSheetDialog(this@FiveTemplateActivity, R.style.AppBottomSheetDialogTheme)
        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        if (bottomSheetDialog!!.isShowing)
            bottomSheetDialog!!.dismiss()
        bottomSheetDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetDialog!!.setContentView(bottomScreenBinding.root)
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setCancelable(true)

        bottomSheetDialog!!.window?.let { Util.statusBarColor(this@FiveTemplateActivity, it) }

        bottomScreenBinding.edtSearch.requestFocus()
        collageListAdapter = CollageListAdapter(this, this)
        bottomScreenBinding.rvLocation.layoutManager = LinearLayoutManager(this)
        bottomScreenBinding.rvLocation.adapter = collageListAdapter

        bottomScreenBinding.rvLocation.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Check if we have reached the end of the list
                if ((lastVisibleItem == totalItemCount - 5) && !isLastPage && searchKey.isNotEmpty()) {
                    // Load more items or trigger pagination here
                    Util.print("=============== Pagination :: " + lastVisibleItem + "::" + totalItemCount + "::" + pageNo + "::" + isLoading);
                    callApi(1, false)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Disable dragging of the BottomSheet
                    bottomSheetDialog!!.behavior.isDraggable = false
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Enable dragging of the BottomSheet
                    bottomSheetDialog!!.behavior.isDraggable = true
                }
            }
        })


        bottomScreenBinding.txtHeaderTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].dialogTitle
        bottomScreenBinding.txtOtherTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
        bottomScreenBinding.txtSuggestion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding.txtChooseReligion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].religionTitle



        if (collageListAdapter.objList.isNotEmpty() && selectedId.isNotEmpty()) {
            collageListAdapter.objList.find { it.id == selectedId }?.isSelected = 1
            bottomScreenBinding.btnDialogContinue.isClickable = true
            collageListAdapter.notifyDataSetChanged()
        }


        showButton(collageListAdapter.objList.any { it.isSelected == 1 })


        bottomScreenBinding.txtChooseReligion.visibility = View.GONE
        bottomScreenBinding.txtSuggestion.visibility = View.GONE
        bottomScreenBinding.rvCitySuggestion.visibility = View.GONE
        bottomScreenBinding.txtOtherTitle.visibility = View.GONE
        bottomScreenBinding.rlSelectedView.visibility = View.GONE

/*

        val layoutParams = bottomScreenBinding.llSearch.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = com.intuit.sdp.R.dimen._5sdp
        bottomScreenBinding.llSearch.layoutParams = layoutParams
*/

        bottomScreenBinding.txtTitleClear.setOnClickListener {
            bottomScreenBinding.edtSurName.setText("")
        }

        bottomScreenBinding.txtClear.setOnClickListener { bottomScreenBinding.edtSearch.setText("") }
        bottomScreenBinding.imgLeft.setOnClickListener { bottomScreenBinding.edtSearch.setText("") }
        bottomScreenBinding.llOther.setOnClickListener(this)

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].question.lowercase().contains(resources.getString(R.string.gotra_))) {

            callApi(1, false)
        }

        bottomScreenBinding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                bottomScreenBinding.txtClear.visibility =
                    if (s.toString().isNotEmpty()) View.VISIBLE
                    else View.GONE

                bottomScreenBinding.imgLeft.visibility = if (s.toString().isNotEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding.imgSearch.visibility = if (s.toString().isEmpty()) View.VISIBLE
                else View.GONE

                bottomScreenBinding.rvLocation.visibility =
                    if (s.toString().isNotEmpty()) View.VISIBLE
                    else View.GONE

                bottomScreenBinding.llOther.visibility =
                    if (s.toString().length >= 3) View.VISIBLE else View.GONE

                if(searchKey != s.toString()) {
                    searchKey = s.toString()

                    if (searchKey.trim().isNotEmpty()) {
                        pageNo = 0
                        isLastPage = false
                        isLoading = false
                        Util.print("===========onTextChanged====  :: " + "::" + pageNo + "::" + isLoading + s.toString());

                        callApi(1, false)
                    }
                }
            }
        })

        bottomScreenBinding.edtSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Handle the "Done" button press or Search action here
                true // Return true to indicate that the event is consumed
            } else {
                false // Return false if the event is not consumed
            }
        }


        bottomScreenBinding.edtSurName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                if (s!!.toString().trim().length > 1) bottomScreenBinding.txtTitleClear.visibility =
                    View.VISIBLE
                else bottomScreenBinding.txtTitleClear.visibility = View.GONE

                if (s.trim().toString().isNotEmpty()) {
//                    bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn)
//                bottomScreenBinding.btnDialogContinue.setTextColor(
//                    ContextCompat.getColor(
//                        this@FiveTemplateActivity,
//                        R.color.color_yellow
//                    )
//                )
                    bottomScreenBinding.btnDialogContinue.isEnabled=bottomScreenBinding.edtSurName.text.toString().trim().isNotEmpty()
                    bottomScreenBinding.btnDialogContinue.isClickable = bottomScreenBinding.edtSurName.text.toString().trim().isNotEmpty()
                }

            }
        })


        bottomScreenBinding.btnCancel.setOnClickListener {
            searchKey = ""
            bottomScreenBinding.edtSurName.setText("")
            bottomSheetDialog!!.dismiss()
        }
        bottomScreenBinding.btnDialogContinue.setOnClickListener(this)

        bottomScreenBinding.btnDialogContinue.isClickable = false

    }

    fun showButton(shouldShow: Boolean) {
//        bottomScreenBinding.btnDialogContinue.setBackgroundResource(if (shouldShow) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
//        bottomScreenBinding.btnDialogContinue.setTextColor(
//            ContextCompat.getColor(
//                this,
//                if (shouldShow) R.color.color_yellow else R.color.white
//            )
//        )
        bottomScreenBinding.btnDialogContinue.isEnabled = shouldShow
        bottomScreenBinding.btnDialogContinue.isClickable = shouldShow




        if (shouldShow) {
            bottomScreenBinding.btnDialogContinue.setOnClickListener {
                // selected adapter text
                binding.txtLocationName.setText(collageListAdapter.objList.find { it.isSelected == 1 }?.name.toString())

                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>selectedId>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>COLLEGE NAME IS ?>>>>${(collageListAdapter.objList.find { it.isSelected == 1 }?.name.toString())}>>")

                // select other option set text in rlSpinner view
                otherOptionText = bottomScreenBinding.edtSurName.text.toString().trim()
                if (otherOptionText.isNotEmpty())
                    binding.txtLocationName.setText(otherOptionText)
                bottomScreenBinding.btnDialogContinue.isEnabled = (otherOptionText.isNotEmpty())
//                bottomScreenBinding.btnDialogContinue.setTextColor(
//                    ContextCompat.getColor(
//                        this,
//                        if (otherOptionText.isNotEmpty()) R.color.color_yellow else R.color.white
//                    )
//                )
                bottomScreenBinding.btnDialogContinue.isClickable = otherOptionText.trim() != ""
                binding.buttonView.btnContinue.isEnabled = binding.txtLocationName.text!!.trim().isNotEmpty()
                binding.buttonView.btnContinue.isClickable = binding.buttonView.btnContinue.isEnabled

//                binding.btnContinue.visibility = View.VISIBLE

                //ADDED BY VAISHALI
                saveData()
                bottomSheetDialog!!.dismiss()
            }
        }
    }

    fun showNoDataString(shouldShow: Boolean) {
        bottomScreenBinding.llOther.visibility =
            if (shouldShow && searchKey.isNotEmpty()) View.VISIBLE else View.GONE
    }


    override fun onClick(view: View) {
        when (view.id) {

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                selectedId = collageListAdapter.objList[lastPos].id

                mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString = collageListAdapter.objList[lastPos].name
                collageListAdapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                collageListAdapter.objList[lastPos].isSelected = 1

                collageListAdapter.notifyDataSetChanged()

                showButton(selectedId.isNotEmpty())


            }

            R.id.btnLeft -> {
                onBackPress()
            }

            R.id.btnSkip -> {
                Util.manageTemplate(this)
            }

            R.id.llOther -> {

                bottomScreenBinding.llCast.visibility = View.VISIBLE
                bottomScreenBinding.rlSearch.visibility = View.INVISIBLE
                bottomScreenBinding.llOther.visibility = View.GONE
                bottomScreenBinding.rvLocation.visibility = View.GONE

//                bottomScreenBinding.imgLocation.visibility = View.GONE
//                bottomScreenBinding.txtLocation.visibility = View.GONE
//                bottomScreenBinding.rlSearch.visibility = View.GONE
//
//                bottomScreenBinding.llCast.visibility = View.VISIBLE

//                binding.btnContinue.setBackgroundResource(R.drawable.dr_bg_btn)
//                binding.btnContinue.setTextColor(
//                    ContextCompat.getColor(
//                        this@FiveTemplateActivity,
//                        R.color.color_yellow
//                    )
//                )
//                binding.buttonView.btnContinue.isClickable = true
//                binding.buttonView.btnContinue.isEnabled = true

            }

//            R.id.btnDialogContinue -> {

//                otherOptionText = bottomScreenBinding.edtSurName.text.toString()

//                if (otherOptionText != "")
//                    binding.txtLocationName.setText(otherOptionText)


//                    bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn)
//                    bottomScreenBinding.btnDialogContinue.setTextColor(
//                        ContextCompat.getColor(
//                            this,
//                            R.color.color_yellow
//                        )
//                    )

//                    binding.btnContinue.visibility = View.VISIBLE
//                    binding.buttonView.btnContinue.isEnabled = true
//                    binding.buttonView.btnContinue.isClickable = true

//                } else {

                  /*  bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn_light)
                    bottomScreenBinding.btnDialogContinue.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
*/
//                    bottomScreenBinding.btnDialogContinue.isEnabled =false
//                    binding.buttonView.btnContinue.isEnabled = false
//                    bottomScreenBinding.btnDialogContinue.isClickable = false
//                    saveData()
//                }

//                bottomSheetDialog!!.dismiss()
//            }

            R.id.btnContinue -> {
                binding.buttonView.btnContinue.isClickable = false
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LASTPOS ON CONTI $LAST_POS")
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LASTPOS ON CONTI $otherOptionText")
                saveData()
            }

            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {
                bottomSheetDialog()
                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomScreenBinding.btnCancel.setImageResource(R.drawable.dr_ic_drop_black)
                    bottomSheetDialog!!.behavior.peekHeight =
                        Resources.getSystem().displayMetrics.heightPixels
                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun saveData() {
        mainJsonObj = JSONObject()
        mainJsonObj.put(mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey, selectedId)
        mainJsonObj.put("add_other_name", otherOptionText)
        mainJsonObj.put("is_add_other", if (otherOptionText.isNotEmpty()) "1" else "0")
        callApi(2)
    }
}