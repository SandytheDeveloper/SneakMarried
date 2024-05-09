package com.commonfriend.template


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.MainApplication
import com.commonfriend.base.BaseActivity
import com.commonfriend.R
import com.commonfriend.StepsActivity
import com.commonfriend.adapter.SelectedOptionsListAdapter
import com.commonfriend.adapter.SquareBoxAdapter
import com.commonfriend.adapter.SuggestionDialogAdapter
import com.commonfriend.databinding.ActivityThirdTemplateBinding
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.CheckListModel
import com.commonfriend.models.QuestionsModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject

class SettleLocationTemplate : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityThirdTemplateBinding
    private lateinit var questionViewModel: QuestionViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding
    private var lastPos = -1
    private val dataObj = JSONObject()
    private var selectedIds: List<String> = arrayListOf()
    private var preSelection: ArrayList<CheckListModel> = ArrayList()
    private lateinit var squareBoxDialogAdapter: SquareBoxAdapter
    private lateinit var suggestionDialogAdapter: SuggestionDialogAdapter
    private lateinit var selectedOptionsListAdapter: SelectedOptionsListAdapter
    private var pageNo: Int = 0
    private var searchKey = ""
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var isFrom = ActivityIsFrom.NORMAL
    private var suggestionList: ArrayList<QuestionsModel> = ArrayList()


    // template code 15
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThirdTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLocationName.setText("")

        initialization()
    }


    private fun initialization() {

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        squareBoxDialogAdapter = SquareBoxAdapter(this, this)
        suggestionDialogAdapter = SuggestionDialogAdapter(this, this)
        selectedOptionsListAdapter = SelectedOptionsListAdapter(this, this)

        binding.customHeader.get().txtPageNO.visibility =
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST) View.INVISIBLE else View.VISIBLE

        allApiResponses()

        with(binding) {
            rlSpinner.setOnClickListener(this@SettleLocationTemplate)
            txtLocationDialog.setOnClickListener(this@SettleLocationTemplate)
            txtLocationName.setOnClickListener(this@SettleLocationTemplate)
            buttonView.btnContinue.setOnClickListener(this@SettleLocationTemplate)

            try {

                txtLocationDialog.hint = mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle

            } catch (e:Exception){
                if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "").toString() != "1"){
                    startActivity(Intent(this@SettleLocationTemplate, StepsActivity::class.java))
                    finishAffinity()
                } else {
                    finish()
                }
                return
            }

            customHeader.get().btnLeft.setOnClickListener(this@SettleLocationTemplate)
            customHeader.get().txtTitle.text = mainObjList[CATEGORY_ID].categoryName
            customHeader.get().progressBar.progress = LAST_POS + 1
            customHeader.get().progressBar.max = mainObjList[CATEGORY_ID].questionList.size
            customHeader.get().txtPageNO.text =
                StringBuilder().append((LAST_POS + 1).toString()).append("/")
                    .append(mainObjList[CATEGORY_ID].questionList.size.toString())

            txtQuestion.text = Util.applyCustomFonts(
                this@SettleLocationTemplate,
                null,
                mainObjList[CATEGORY_ID].questionList[LAST_POS].question,
                if (mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts.isNotEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].handWrittenTexts[0] else "",
                color = R.color.color_blue
            )
        }

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this@SettleLocationTemplate, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]


        questionViewModel.questionOptionsListResponse.observe(this) {
            isLoading = false
            Util.dismissProgress()
            if (it.success == 1) {
                squareBoxDialogAdapter.addData(it.data[0].arrayData, pageNo == 0)
                if (it.data[0].suggestestionList.isNotEmpty() && (pageNo == 0 || pageNo == 1)) {
                    suggestionList = it.data[0].suggestestionList
                    suggestionDialogAdapter.addData(it.data[0].suggestestionList)
                }
//                if (it.data[0].arrayData.isNotEmpty()) pageNo += 1

                isLastPage = it.data.isNullOrEmpty()
                if (selectedOptionsListAdapter.objList.isNotEmpty()) {
                    for (i in selectedOptionsListAdapter.objList.indices) {
                        squareBoxDialogAdapter.objList.find { item -> item.id == selectedOptionsListAdapter.objList[i].id }?.isSelected =
                            1
                    }
                    squareBoxDialogAdapter.notifyDataSetChanged()
                }

                if (it.data[0].selectedDataArray!!.size > 0) {
                    preSelection = ArrayList()
                    for (i in 0 until it.data[0].selectedDataArray!!.size) {
                        val selectedObjList = CheckListModel()
                        selectedObjList.id = it.data[0].selectedDataArray!![i].id
                        selectedObjList.name = it.data[0].selectedDataArray!![i].name
                        selectedObjList.isAny = it.data[0].selectedDataArray!![i].isAny
                        preSelection.add(selectedObjList)
                        if (selectedOptionsListAdapter.objList.isNotEmpty()) {
                            selectedOptionsListAdapter.objList.remove(selectedOptionsListAdapter.objList.find { it1 -> it1.id == it.data[0].selectedDataArray!![i].id })
                        }
                        selectedOptionsListAdapter.objList.add(selectedObjList)
                        selectedOptionsListAdapter.notifyDataSetChanged()

                        if (selectedOptionsListAdapter.objList.size > 2) {
                            selectedOptionsListAdapter.objList.filter { it.isAny == "0" }
                                .forEach {
                                    it.isSelected = 0
                                }
                            selectedOptionsListAdapter.notifyDataSetChanged()
                        }
                        filteringSuggestions(it.data[0].selectedDataArray!![i].id, true)
                        squareBoxDialogAdapter.objList.find { it1 -> it1.id == it.data[0].selectedDataArray!![i].id }?.isSelected =
                            1
                        squareBoxDialogAdapter.notifyDataSetChanged()
                        binding.txtLocationName.setText(
                            selectedOptionsListAdapter.objList.joinToString(COMMA_SPACE_SEPERATOR) { it.name })
                        binding.txtLocationDialog.hint =
                            if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle else
                                mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText
                    }

                    mainObjList[CATEGORY_ID].questionList[LAST_POS].answer =
                        it.data[0].selectedDataArray!!.joinToString(
                            COMMA_SPACE_SEPERATOR
                        ) { item -> item.id }


                    binding.buttonView.btnContinue.isEnabled = true
                    binding.buttonView.btnContinue.isClickable = true

                    if (selectedIds.isNotEmpty()) {
                        for (i in selectedIds.indices) {
                            squareBoxDialogAdapter.objList.find { it.id == selectedIds[i] }?.isSelected =
                                1
                        }
                        squareBoxDialogAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        questionViewModel.saveAnswerResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                searchKey = ""
                pageNo = 0
                bundle = Bundle().apply {
                    putString(
                        mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                        Util.getTextValue(binding.txtLocationName).replace(",", "|")
                    )
                }
                firebaseEventLog(mainObjList[CATEGORY_ID].questionList[LAST_POS].eventName, bundle)

                MainApplication.firebaseAnalytics.setUserProperty(
                    mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey.removeString(),
                    Util.getTextValue(binding.txtLocationName).replace(",", "|")
                )

                selectedOptionsListAdapter.objList.clear()
                if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                    Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
                    {
                        finish()
                    }
                } else
                    Util.manageTemplate(this@SettleLocationTemplate, isFrom)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.buttonView.btnSkip.visibility =
            if (Util.isQuestionSkipable()) View.VISIBLE else View.INVISIBLE
        binding.buttonView.btnSkip.setOnClickListener(this)

        if (mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.isEmpty())
            binding.llPrivacy.visibility = View.INVISIBLE
        else
            binding.infoMessage.text =
                mainObjList[CATEGORY_ID].questionList[LAST_POS].infoMessage.replace(
                    "\\n",
                    System.lineSeparator()
                )

        callApi(1)
    }


    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
            finish()
        } else {
            finish()
            Util.manageBackClick(this@SettleLocationTemplate)
        }
    }

    fun showButton(shouldShow: Boolean) {
        bottomScreenBinding.txtChooseReligion.visibility =
            if (selectedOptionsListAdapter.objList.isEmpty()) View.VISIBLE else View.GONE

        with(bottomScreenBinding.btnDialogContinue) {
            isClickable = shouldShow
            isEnabled = shouldShow
            setOnClickListener {
                binding.buttonView.btnContinue.visibility = View.VISIBLE
                manageBottomSheetContinueButtonVisibility()
                bottomSheetDialog!!.dismiss()
                binding.buttonView.btnContinue.performClick()
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

                    if (showProgess)
                        Util.showProgress(this)
                    questionViewModel.questionOptionsListApiRequest(
                        this,
                        searchKey,
                        pageNo,
                        apiName = mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName,
                        currentLat = Pref.getStringValue(Pref.PREF_CURRENT_LAT, "0.00")?.toDouble(),
                        currentLong = Pref.getStringValue(Pref.PREF_CURRENT_LNG, "0.00")?.toDouble()
                    )
                }


                2 -> {
                    if (showProgess)
                        Util.showProgress(this)
                    questionViewModel.questionAnswerSaveApiRequest(
                        this, dataObj
                    )
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetDialog() {
        if (bottomSheetDialog != null)
            if (bottomSheetDialog!!.isShowing)
                bottomSheetDialog!!.dismiss()




        bottomScreenBinding = DialogCastBinding.inflate(layoutInflater)

        bottomSheetDialog =
            BottomSheetDialog(
                this@SettleLocationTemplate,
                R.style.AppBottomSheetDialogTheme
            ).apply {
                this.requestWindowFeature(Window.FEATURE_NO_TITLE)
                this.setContentView(bottomScreenBinding.root)
                this.setCancelable(true)
                this.window?.let { Util.statusBarColor(this@SettleLocationTemplate, it) }
                setOnDismissListener {
                    searchKey = ""
                    pageNo = 0
                }
            }


        bottomScreenBinding.rvLocation.apply {
            this.layoutManager =
                LinearLayoutManager(this@SettleLocationTemplate, RecyclerView.VERTICAL, false)
            this.adapter = squareBoxDialogAdapter

            setHeight(true)

            /*this.addOnScrollListener(object :
                PaginationListenerAdapter(this.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    if (!isLoading) {

                        callApi(1)
                    }
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }


                override fun isLoading(): Boolean {
                    return isLoading
                }
            })*/
        }

        bottomScreenBinding.edtSearch.requestFocus()

        bottomScreenBinding.rvTextSuggestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        bottomScreenBinding.rvTextSuggestion.adapter = suggestionDialogAdapter

        bottomScreenBinding.rvCitySuggestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        bottomScreenBinding.rvCitySuggestion.adapter = selectedOptionsListAdapter


        bottomScreenBinding.edtSearch.setOnClickListener {
            bottomScreenBinding.txtSuggestion.gone()
            bottomScreenBinding.rvTextSuggestion.gone()
            setHeight(bottomScreenBinding.txtSuggestion.isVisible)
        }


        bottomScreenBinding.txtTitleClear.setOnClickListener {
            bottomScreenBinding.edtSurName.setText("")
            searchKey = ""
            pageNo = 0
            callApi(1, false)
        }




        if (selectedOptionsListAdapter.objList.isNotEmpty()) {
            selectedOptionsListAdapter.objList.map { outer -> outer.id }.forEach {
                squareBoxDialogAdapter.objList.find { it1 -> it1.id == it }?.isSelected = 1
            }
        }

        bottomScreenBinding.rvLocation.visibility = View.VISIBLE

        bottomScreenBinding.txtHeaderTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
        bottomScreenBinding.txtOtherTitle.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].otherTitle
        bottomScreenBinding.txtSuggestion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].suggestionTitle
        bottomScreenBinding.txtChooseReligion.text =
            mainObjList[CATEGORY_ID].questionList[LAST_POS].dialogTitle




        bottomScreenBinding.txtClear.setOnClickListener {
            bottomScreenBinding.edtSearch.setText("")
            searchKey = ""
            isLoading = false
            pageNo = 0
            callApi(1, false)
        }
        bottomScreenBinding.imgLeft.setOnClickListener { bottomScreenBinding.edtSearch.setText("") }


        bottomScreenBinding.edtSearch.observeTextChange {
            searchKey = it
            isLoading = false
            with(bottomScreenBinding) {
                txtSuggestion.visibility =
                    if (it.isNotEmpty()) View.GONE else View.VISIBLE
                rvTextSuggestion.visibility =
                    if (it.isNotEmpty()) View.GONE else View.VISIBLE

                setHeight(txtSuggestion.isVisible)

                txtClear.visibility = if (it.isNotEmpty()) View.VISIBLE
                else View.GONE

                imgLeft.visibility =
                    if (it.isNotEmpty()) View.VISIBLE else View.GONE

                imgSearch.visibility =
                    if (it.isEmpty()) View.VISIBLE else View.GONE


//                btnDialogContinue.isEnabled = it.isNotEmpty()
//                btnDialogContinue.isClickable = it.isNotEmpty()
            }
            pageNo = 0
            callApi(1, false)
        }




        bottomScreenBinding.btnCancel.setOnClickListener {
            val selected = squareBoxDialogAdapter.objList.filter { it.isSelected == 1 }
            Util.print("===================selected==============${selected.size}")
            for (element in selected) {
                element.isSelected = 0
                squareBoxDialogAdapter.notifyDataSetChanged()
            }
            selectedOptionsListAdapter.objList.clear()
            selectedOptionsListAdapter.addData(preSelection)
            suggestionDialogAdapter.addData(suggestionList)
            bottomSheetDialog!!.dismiss()
        }

        bottomScreenBinding.btnDialogContinue.setOnClickListener(this)




        if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDialog!!.behavior.peekHeight =
                Resources.getSystem().displayMetrics.heightPixels
            bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheetDialog!!.show()
        manageBottomSheetContinueButtonVisibility()
        showButton(selectedOptionsListAdapter.objList.isNotEmpty())
    }

    private fun setHeight(setHeight: Boolean) {
        val layoutParams = bottomScreenBinding.rlTopView.layoutParams as RelativeLayout.LayoutParams
        layoutParams.height = if (setHeight) resources.getDimension(com.intuit.sdp.R.dimen._30sdp)
            .toInt() else ViewGroup.LayoutParams.WRAP_CONTENT
        bottomScreenBinding.rlTopView.layoutParams = layoutParams
    }

    // show No Data Found String
    fun showNoDataFoundString(shouldShow: Boolean) {
        bottomScreenBinding.txtNoDataFound.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun manageBottomSheetContinueButtonVisibility() {
// bottom shit dialog suggetion title visiblity
        selectedOptionsListAdapter.notifyDataSetChanged()
        squareBoxDialogAdapter.notifyDataSetChanged()

        binding.txtLocationName.setText(
            selectedOptionsListAdapter.objList.joinToString(
                COMMA_SPACE_SEPERATOR
            ) { it.name })
        binding.txtLocationDialog.hint =
            if (binding.txtLocationName.text!!.isEmpty()) mainObjList[CATEGORY_ID].questionList[LAST_POS].searchTitle
            else mainObjList[CATEGORY_ID].questionList[LAST_POS].defaultText


        binding.buttonView.btnContinue.isEnabled = (binding.txtLocationName.text.toString().trim()
            .isNotEmpty()
                ) //View.VISIBLE else View.GONE
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SIZE OF SELCTED LIST IS>>>>>>>..${selectedOptionsListAdapter.objList.size}")


        binding.buttonView.btnContinue.isClickable = true


        Util.print(">>>>>>>>>>>>>>>>size of selected array is >>>>>${selectedOptionsListAdapter.objList.size}")
        bottomScreenBinding.btnDialogContinue.isEnabled =
            selectedOptionsListAdapter.objList.isNotEmpty()
        bottomScreenBinding.btnDialogContinue.isClickable =
            selectedOptionsListAdapter.objList.isNotEmpty()

    }


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged")
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
                selectedOptionsListAdapter.objList.removeIf { it.isAny == "1" }
                squareBoxDialogAdapter.objList.find { it.isAny == "1" && it.isSelected == 1 }?.isSelected =
                    0
                squareBoxDialogAdapter.notifyDataSetChanged()

                Util.print("<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>selected adapter size before conditions>>>>>>>>>...${selectedOptionsListAdapter.objList.size}")

                if (squareBoxDialogAdapter.objList[lastPos].isAny == "1") {
                    if (selectedOptionsListAdapter.objList.isNotEmpty()) {
                        for (i in 0 until selectedOptionsListAdapter.objList.size) {
                            squareBoxDialogAdapter.objList.find { it.id == selectedOptionsListAdapter.objList[i].id && it.isAny != "1" }?.isSelected =
                                0
                        }
                        squareBoxDialogAdapter.notifyDataSetChanged()
                        selectedOptionsListAdapter.objList.clear()
                    }

                    selectedOptionsListAdapter.notifyDataSetChanged()
                } else {
                    for (i in 0 until selectedOptionsListAdapter.objList.size) {
                        if (selectedOptionsListAdapter.objList[i].isAny == "1") {
                            selectedOptionsListAdapter.objList.removeAt(i)
                        }
                    }

                    Util.print("<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>selected adapter size after consdition 1>>>>>>>>>...${selectedOptionsListAdapter.objList.size}")
//                    selectedOptionsListAdapter.notifyDataSetChanged()
                }
                if (squareBoxDialogAdapter.objList[lastPos].isSelected == 1) { // Already Select
                    squareBoxDialogAdapter.objList[lastPos].isSelected = 0
                    selectedOptionsListAdapter.objList.remove(selectedOptionsListAdapter.objList.find { it.id == squareBoxDialogAdapter.objList[lastPos].id })
                    selectedOptionsListAdapter.notifyDataSetChanged()
                    filteringSuggestions(squareBoxDialogAdapter.objList[lastPos].id, false)
                } else { // Not Select
                    squareBoxDialogAdapter.objList[lastPos].isSelected = 1
                    selectedOptionsListAdapter.objList.removeIf { it.isAny == "1" }
                    val obj = CheckListModel().apply {
                        name = squareBoxDialogAdapter.objList[lastPos].name
                        id = squareBoxDialogAdapter.objList[lastPos].id
                        isAny = squareBoxDialogAdapter.objList[lastPos].isAny
                        latitude = squareBoxDialogAdapter.objList[lastPos].latitude
                        longitude = squareBoxDialogAdapter.objList[lastPos].longitude
                    }
                    selectedOptionsListAdapter.objList.add(obj)
                    selectedOptionsListAdapter.notifyDataSetChanged()
                    filteringSuggestions(
                        squareBoxDialogAdapter.objList[lastPos].id,
                        true
                    )
                }
//                }
                squareBoxDialogAdapter.notifyDataSetChanged()
                showButton(selectedOptionsListAdapter.objList.isNotEmpty())
                bottomScreenBinding.txtSuggestion.visibility =
                    if (suggestionDialogAdapter.objList.isEmpty()) View.GONE else View.VISIBLE
                setHeight(suggestionDialogAdapter.objList.isNotEmpty())

                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>size after all the confdtio sd>>>>>>>${selectedOptionsListAdapter.objList.size}")
            }


// dialog open screen box name
            R.id.rlSpinner,
            R.id.txtLocationName,
            R.id.txtLocationDialog -> {
                bottomSheetDialog()
            }


            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()
                val selectedId = suggestionDialogAdapter.objList[lastPos].id

                squareBoxDialogAdapter.objList.find { it.isAny == "1" && it.isSelected == 1 }?.isSelected =
                    0
                selectedOptionsListAdapter.objList.removeIf { it.isAny == "1" }
                selectedOptionsListAdapter.notifyDataSetChanged()


                if (selectedOptionsListAdapter.objList.none { it.id == selectedId }) {
//                    if (checkForMaxSelection()) {

                    selectedOptionsListAdapter.objList.removeIf { it.isAny == "1" }

                    val obj = CheckListModel()
                    obj.name = suggestionDialogAdapter.objList[lastPos].optionName
                    obj.id = selectedId
                    obj.isAny = suggestionDialogAdapter.objList[lastPos].isAny
                    obj.longitude = suggestionDialogAdapter.objList[lastPos].longitude
                    obj.latitude = suggestionDialogAdapter.objList[lastPos].latitude
                    selectedOptionsListAdapter.objList.add(obj)
                    selectedOptionsListAdapter.notifyDataSetChanged()
                    filteringSuggestions(selectedId, true)
                    if (suggestionDialogAdapter.objList.isEmpty()) {
                        bottomScreenBinding.txtSuggestion.gone()
                        setHeight(false)
                    }

                    val mainListFilter =
                        squareBoxDialogAdapter.objList.filter { it.id == selectedId }
                    if (mainListFilter.isNotEmpty()) {
                        for (element in mainListFilter) {
                            element.isSelected = if (element.isSelected == 1) 0 else 1
                            squareBoxDialogAdapter.notifyDataSetChanged()
                        }
                    }
                    /*}
                } else {
                    val filterList =
                        selectedOptionsListAdapter.objList.filter { it.id == selectedId }
                    if (filterList.isNotEmpty()) {
                        selectedOptionsListAdapter.objList.removeAt(
                            selectedOptionsListAdapter.objList.indexOf(
                                filterList[0]
                            )
                        )
                    }*/

                } else {
                    val filterList =
                        selectedOptionsListAdapter.objList.filter { it.id == selectedId }
                    if (filterList.isNotEmpty()) {
                        selectedOptionsListAdapter.objList.removeAt(
                            selectedOptionsListAdapter.objList.indexOf(
                                filterList[0]
                            )
                        )
                    }
                    filteringSuggestions(selectedId, false)
                    suggestionDialogAdapter.notifyDataSetChanged()
                    bottomScreenBinding.txtSuggestion.visibility =
                        if (suggestionDialogAdapter.objList.isEmpty()) View.GONE else View.VISIBLE
                    setHeight(bottomScreenBinding.txtSuggestion.isVisible)

                }
                showButton(selectedOptionsListAdapter.objList.isNotEmpty())
                manageBottomSheetContinueButtonVisibility()

            }


            R.id.btnClose -> {
                val pos = view.tag.toString().toInt()
                val item =
                    squareBoxDialogAdapter.objList.filter { it.id == selectedOptionsListAdapter.objList[pos].id }
                for (element in item) {
                    element.isSelected = 0
                }
                filteringSuggestions(selectedOptionsListAdapter.objList[pos].id, false)
                squareBoxDialogAdapter.notifyDataSetChanged()
                selectedOptionsListAdapter.objList.removeAt(pos)
                selectedOptionsListAdapter.notifyDataSetChanged()
                bottomScreenBinding.txtSuggestion.visibility =
                    if (suggestionDialogAdapter.objList.isEmpty()) View.GONE else View.VISIBLE
                setHeight(bottomScreenBinding.txtSuggestion.isVisible)
                showButton(selectedOptionsListAdapter.objList.isNotEmpty())
            }

            R.id.btnContinue -> {
                mainObjList[CATEGORY_ID].questionList[LAST_POS].answerString =
                    binding.txtLocationName.text.toString()
                binding.buttonView.btnContinue.isClickable = false
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LASTPOS ON CONTI $LAST_POS")
                saveData()
            }
        }
    }


    private fun saveData() {
        val answer: ArrayList<String> = ArrayList()
        selectedOptionsListAdapter.objList.forEach {
            answer.add(it.id)
        }

        if (answer.joinToString(COMMA_SPACE_SEPERATOR) { it } == mainObjList[CATEGORY_ID].questionList[LAST_POS].answer) {
            selectedOptionsListAdapter.objList.clear()
            if (isFrom == ActivityIsFrom.FROM_CHECKLIST || isFrom == ActivityIsFrom.FROM_EDIT) {
                finish()
            } else
                Util.manageTemplate(this, isFrom)
        } else {

            dataObj.put(
                mainObjList[CATEGORY_ID].questionList[LAST_POS].questionKey,
                JSONArray(answer).toString()
            )
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${dataObj}")
            callApi(2)
        }
    }

    private fun filteringSuggestions(elementID: String, removeItem: Boolean) {
        selectedIds = selectedOptionsListAdapter.objList.map { it.id }
        if (removeItem)
            suggestionDialogAdapter.addData(ArrayList(suggestionList.filter { it.id !in selectedIds }))
        else
            suggestionList.filter { it.id == elementID }
                .let {
                    suggestionDialogAdapter.objList.removeAll(it)
                    suggestionDialogAdapter.objList.addAll(it)
                    suggestionDialogAdapter.notifyDataSetChanged()
                }
    }


}



