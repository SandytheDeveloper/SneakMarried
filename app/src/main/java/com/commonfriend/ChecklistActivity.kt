package com.commonfriend


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.commonfriend.adapter.CheckListAdapter
import com.commonfriend.adapter.SelectedLocationsListAdapter
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityChecklistBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.CheckListModel
import com.commonfriend.models.GeneralModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.CheckListViewModel
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ChecklistActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityChecklistBinding
    private lateinit var relationshipListAdapter: CheckListAdapter
    private lateinit var disabilityListAdapter: CheckListAdapter
    private lateinit var religionListAdapter: CheckListAdapter
    private lateinit var professionListAdapter: CheckListAdapter
    private lateinit var eatingHabitsListAdapter: CheckListAdapter

    private lateinit var isFrom: ActivityIsFrom
    private lateinit var selectedLocationAdapter: SelectedLocationsListAdapter
    private lateinit var mainJsonObj: JSONObject
    private var heightInInches = ""
    private lateinit var checkListViewModel: CheckListViewModel
    private lateinit var questionViewModel: QuestionViewModel
    var selectedPositionsList: MutableMap<Int, String>? = null
    private var lastPos = LAST_POS
    private var checkListAllData: ArrayList<CheckListModel> = ArrayList()
    private var lastPosition: Int = -1
    private var forLocation = false // if true only update settle location text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        logForCurrentScreen(Screens.CHECKLIST_SCREEN.screenType, Screens.CHECKLIST_SCREEN.screenType)
        initialization()
    }

    override fun onResume() {
        super.onResume()
        callApi(1)
    }

    @SuppressLint("NewApi")
    private fun initialization() {

        isFrom = if (intent.hasExtra(IS_FROM))
            intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        else
            ActivityIsFrom.NORMAL

        binding.btnBack.visibleIf(isFrom == ActivityIsFrom.NORMAL)
        binding.btnCross.visibleIf(isFrom != ActivityIsFrom.NORMAL)
        binding.llSkipAll.visibleIf(isFrom == ActivityIsFrom.NORMAL)


//        binding.customHeader.get().apply {
//            imgRight.visibility = View.GONE
//            txtPageNO.visibility = View.GONE
//            rlCustomProfile.visibility = View.GONE
//            progressBar.visibility = View.GONE
//            txtMainTitle.visibility = View.VISIBLE
//            txtMainTitle.text = getString(R.string.checklist)
//            btnLeft.visibility = View.VISIBLE
//            if (isFrom == ActivityIsFrom.NORMAL) {
//                btnLeft.setImageResource(R.drawable.dr_ic_cross)
//            } else {
//                btnLeft.gone()
//                imgCross.visible()
//            }
//            binding.customHeader.setBtnImageLeft(isFrom == ActivityIsFrom.FROM_MENU)
//            llHeaderView.setBackgroundColor(
//                    ContextCompat.getColor(
//                            this@ChecklistActivity,
//                            R.color.color_white
//                    )
//            )
//
//
//        }


        selectedPositionsList = mutableMapOf()

        binding.rvRelationshipList.layoutManager = GridLayoutManager(this, 1)
        relationshipListAdapter = CheckListAdapter(this, this, 1, 4)
        binding.rvRelationshipList.adapter = relationshipListAdapter

        binding.rvDisabilityList.layoutManager = GridLayoutManager(this, 2)
        disabilityListAdapter = CheckListAdapter(this, this, 2, 4)
        binding.rvDisabilityList.adapter = disabilityListAdapter

        binding.rvReligionList.layoutManager = GridLayoutManager(this, 2)
        religionListAdapter = CheckListAdapter(this, this, 3)
        binding.rvReligionList.adapter = religionListAdapter

        binding.rvProfessionList.layoutManager = GridLayoutManager(this, 1)
        professionListAdapter = CheckListAdapter(this, this, 5)
        binding.rvProfessionList.adapter = professionListAdapter

        binding.rvEatingHabitsList.layoutManager = GridLayoutManager(this, 1)
        eatingHabitsListAdapter = CheckListAdapter(this, this, 6)
        binding.rvEatingHabitsList.adapter = eatingHabitsListAdapter

        selectedLocationAdapter = SelectedLocationsListAdapter(this, this)


        binding.ageSeekBar.setLabelFormatter(null)
        binding.ageSeekBar.valueFrom = if (Pref.getStringValue(Pref.PREF_USER_GENDER, "") == "male") 18F else 21F

        binding.ageSeekBar.addOnChangeListener { _, value, _ ->

            binding.txtAge.text = String.format(value.toInt().toString(), "years")
        }

        binding.heightSeekBar.addOnChangeListener { _, value, _ ->

            binding.txtHeight.text = "${value.toInt() / 12}'${value.toInt() % 12} inches"
            heightInInches = value.toInt().toString()
        }

        binding.btnSave.setOnClickListener(this)
        binding.btnSave.isClickable = true
        binding.btnIncAge.setOnClickListener(this)
        binding.btnDecAge.setOnClickListener(this)
        binding.btnIncHeight.setOnClickListener(this)
        binding.btnDecHeight.setOnClickListener(this)
        binding.llSettleLoc.setOnClickListener(this)
        binding.btnReligionUnlock.setOnClickListener(this)
        binding.btnRelationshipStatusUnlock.setOnClickListener(this)
        binding.btnProfessionUnlock.setOnClickListener(this)
        binding.btnEatingHabitUnlock.setOnClickListener(this)
        binding.btnIdealHeightUnlock.setOnClickListener(this)
        binding.btnIdealAgeUnlock.setOnClickListener(this)
        binding.txtSettleLocationName.setOnClickListener(this)
        binding.btnUp.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnCross.setOnClickListener(this)
        binding.llSkipAll.setOnClickListener(this)


        allApiResponses()

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    private fun onBackPress() {

        LAST_POS = lastPos
        FOR_SCREEN = 3
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20 && resultCode == Activity.RESULT_OK) {
            if (data != null)
                selectedLocationAdapter.objList = ArrayList()
            selectedLocationAdapter.objList =
                data!!.getSerializableExtra(DATA) as ArrayList<GeneralModel>
            selectedLocationAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {
//            R.id.btnLeft -> {
//                LAST_POS = lastPos
//                FOR_SCREEN = 3
//                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>$LAST_POS")
//                finish()
//            }

            R.id.btnBack, R.id.btnCross -> {
                onBackPress()
            }

            R.id.txtSettleLocationName, R.id.btnUp -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].settleLocationData.isNotEmpty())

                    callApi(
                        3,
                        checkListAllData[0].settleLocationData[0].categoryId,
                        checkListAllData[0].settleLocationData[0].questionId
                    )
                forLocation = true
            }

            R.id.btnIdealHeightUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].idealHeight.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].idealHeight[0].categoryId,
                        checkListAllData[0].idealHeight[0].questionId
                    )
            }

            R.id.btnIdealAgeUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].idealAge.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].idealAge[0].categoryId,
                        checkListAllData[0].idealAge[0].questionId
                    )
            }

            R.id.btnReligionUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].religionList.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].religionList[0].categoryId,
                        checkListAllData[0].religionList[0].questionId
                    )
            }


            R.id.btnProfessionUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].professionsList.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].professionsList[0].categoryId,
                        checkListAllData[0].professionsList[0].questionId
                    )
            }

            R.id.btnEatingHabitUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].eatingHabitsList.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].eatingHabitsList[0].categoryId,
                        checkListAllData[0].eatingHabitsList[0].questionId
                    )
            }

            R.id.btnRelationshipStatusUnlock -> {
                if (checkListAllData.isNotEmpty() && checkListAllData[0].relationshipList.isNotEmpty())
                    callApi(
                        3,
                        checkListAllData[0].relationshipList[0].categoryId,
                        checkListAllData[0].relationshipList[0].questionId
                    )
            }

            R.id.btnSave -> {
                saveData()
            }

            R.id.btnIncAge -> {
                if (binding.ageSeekBar.value < 100.00)
                    binding.ageSeekBar.value = binding.ageSeekBar.value + 1
            }

            R.id.btnDecAge -> {
                if (binding.ageSeekBar.value > 18.00)
                    binding.ageSeekBar.value = binding.ageSeekBar.value - 1
            }

            R.id.btnIncHeight -> {
                if (binding.heightSeekBar.value < 120.00)
                    binding.heightSeekBar.value = binding.heightSeekBar.value + 1
            }

            R.id.btnDecHeight -> {
                if (binding.heightSeekBar.value > 0.00)
                    binding.heightSeekBar.value = binding.heightSeekBar.value - 1
            }

            R.id.rlMain -> {
                lastPosition = view.tag.toString().toInt()
                val clickType = view.getTag(R.string.app_name).toString().toInt()
                val adapter = when (clickType) {
                    1 -> relationshipListAdapter
                    2 -> disabilityListAdapter
                    3 -> religionListAdapter
                    5 -> professionListAdapter
                    else -> eatingHabitsListAdapter
                }

                when (adapter.viewType) {
                    else -> {

                        //clicktype()>>religion(1),community(2),culture(3),surname(4),selectedSurname(5),suggestedSurname(6),
                        //relationship(7),disability(8),profession(9),household(10),eating(11),manglik(12),dosh(13),reftype(14)
                        //gotraType(15) excludedSurname(16)
                        when (clickType) {
                            3, 4, 5, 6 -> {
                                if (adapter.objList[lastPosition].name == resources.getString(R.string.Any)) {
                                    adapter.objList.filter { it.isSelected == 1 }
                                        .forEach { it.isSelected = 0 }
                                    adapter.objList[lastPosition].isSelected = 1
                                } else {
                                    adapter.objList.find { it.name == resources.getString(R.string.Any) }?.isSelected =
                                        0
                                    adapter.objList[lastPosition].isSelected =
                                        if (adapter.objList[lastPosition].isSelected == 0) 1 else 0
                                    selectedPositionsList?.set(
                                        clickType,
                                        adapter.objList.filter { it.isSelected == 1 }
                                            .joinToString(",") { it.id })
                                }

                                if (adapter.objList.none { it.isSelected == 1 }) {
                                    adapter.objList.find { it.name == resources.getString(R.string.Any) }?.isSelected =
                                        1
                                }
                                adapter.notifyDataSetChanged()

                            }

                            else -> {
                                adapter.objList.find { it.isSelected == 1 }?.isSelected = 0
                                adapter.objList[lastPosition].isSelected = 1
                                adapter.notifyDataSetChanged()
                                if ((adapter.objList[lastPosition].name != resources.getString(R.string.Any)))
                                    selectedPositionsList?.set(
                                        clickType,
                                        adapter.objList[lastPosition].id
                                    )
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                selectedPositionsList?.forEach {
                    Util.print("?>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SELECTED POSITIONS ARE>>>>>>>>>>>>>${it.key}::::::::${it.value}")
                }
            }

            R.id.llSkipAll -> {
                Util.skipAndOpenHomeScreen(this)
            }
        }
    }


    private fun saveData() {
        mainJsonObj = JSONObject()
        val any = "[\"${resources.getString(R.string.Any)}\"]"

        //clicktype()>>religion(1),community(2),culture(3),surname(4),selectedSurname(5),suggestedSurname(6),
        //relationship(7),disability(8),profession(9),household(10),eating(11),manglik(12),dosh(13),reftype(14)
        //gotraType(15) excludedSurname(16)

        val dataObj = JSONObject().apply {
            put(
                "candidate_relationship_status",
                relationshipListAdapter.objList.find { it.isSelected == 1 }?.id
            )

            put(
                "candidate_disability",
                disabilityListAdapter.objList.find { it.isSelected == 1 }?.name
            )

            put(
                "candidate_religion",
                if (religionListAdapter.objList.any { it.isSelected == 1 })
                    JSONArray(religionListAdapter.objList.filter { it.isSelected == 1 }
                        .joinToString(",") { it.id }.split(",")).toString()
                else
                    any
            )
            put(
                "candidate_profession",
                if (professionListAdapter.objList.any { it.isSelected == 1 })
                    JSONArray(professionListAdapter.objList.filter { it.isSelected == 1 }
                        .joinToString(",") { it.id }.split(",")).toString()
                else
                    any
            )

            put(
                "candidate_habits_eating",
                if (eatingHabitsListAdapter.objList.any { it.isSelected == 1 })
                    JSONArray(eatingHabitsListAdapter.objList.filter { it.isSelected == 1 }
                        .joinToString(",") { it.id }.split(",")).toString()
                else
                    any
            )

            put("candidate_ideal_height", heightInInches)
            put("candidate_ideal_age", binding.ageSeekBar.value.toInt().toString())

        }



        mainJsonObj.put("preferences", dataObj)

        mainJsonObj.put("device_type", DEVICE_TYPE)
        mainJsonObj.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        mainJsonObj.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        mainJsonObj.put("timezone", TimeZone.getDefault().id)
        Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainJsonObj}")
        callApi(2)
    }

    fun showButton(shouldShow: Boolean) {
        binding.btnSave.setBackgroundResource(if (shouldShow) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
        binding.btnSave.setTextColor(
            ContextCompat.getColor(
                this,
                if (shouldShow) R.color.color_black else R.color.color_white
            )
        )
        binding.btnSave.isClickable = shouldShow
    }

    private fun allApiResponses() {
        checkListViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[CheckListViewModel::class.java]
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        checkListViewModel.checkListDataResponse.observe(this) {
            Util.dismissProgress()
            if (!it.data.isNullOrEmpty()) {
                checkListAllData = it.data
                setData()
                binding.llMainView.visibility = View.VISIBLE
            }

        }
        checkListViewModel.saveCheckListDataResponse.observe(this) {
            bundle = Bundle().apply {
                putString("preferred_height", Util.getTextValue(binding.txtHeight))
                putString(
                    "preferred_disability",
                    disabilityListAdapter.objList.find { it.isSelected == 1 }?.name
                )
                putString(
                    "preferred_relationship_status",
                    relationshipListAdapter.objList.find { it.isSelected == 1 }?.name
                )
                putString("preferred_eating_habits",
                    if (eatingHabitsListAdapter.objList.any { it.isSelected == 1 })
                        eatingHabitsListAdapter.objList.filter { it.isSelected == 1 }
                            .joinToString("|") { it.name } else "Any")
                putString("preferred_religion",
                    if (religionListAdapter.objList.any { it.isSelected == 1 })
                        religionListAdapter.objList.filter { it.isSelected == 1 }
                            .joinToString("|") { it.name } else "Any")
                putString("preferred_age", Util.getTextValue(binding.txtAge))
                putString("preferred_profession",
                    if (professionListAdapter.objList.any { it.isSelected == 1 })
                        professionListAdapter.objList.filter { it.isSelected == 1 }
                            .joinToString("|") { it.name } else "Any")
                putString("client_id", "")
                putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
            }

            firebaseEventLog("preferences_submitted", bundle)

            Util.dismissProgress()
            Pref.setStringValue(Pref.PREF_CHECK_LIST_PROVIDED, "1")
            if (isFrom == ActivityIsFrom.FROM_MENU) {
                Util.showLottieDialog(this, "done_lottie.json",wrapContent = true)
                {
                    finish()
                }
            } else {
                openA<PriorityActivity>()
            }

        }
        questionViewModel.getSingleQuestionsResponse.observe(this) {
            Util.dismissProgress()
            CATEGORY_ID = 0
            LAST_POS = 0
            if (mainObjList.isNotEmpty()) {
                Util.templateMovement(this@ChecklistActivity, ActivityIsFrom.FROM_CHECKLIST)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(
        tag: Int,
        categoryId: String = "",
        questionId: String = "",
        generalId: String = ""
    ) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    Util.showProgress(this)
                    checkListViewModel.checkListApiRequest(this)
                }

                2 -> {
                    Util.showProgress(this)
                    checkListViewModel.saveCheckListApiRequest(this, mainJsonObj.toString())
                }

                3 -> {
                    Util.showProgress(this)
                    questionViewModel.getSingleQuestionListApiRequest(
                        this,
                        categoryId, questionId, generalId
                    )
                }
            }
        }
    }

    private fun setData() {

        binding.scrollView.visibility = View.VISIBLE

        if (checkListAllData.isNotEmpty()) {

            if (!forLocation) {

                if (checkListAllData[0].idealAge.isNotEmpty()) {
                    binding.btnIncAge.setOnClickListener(if (checkListAllData[0].idealAge[0].isEnabled == "1") this else null)
                    binding.btnDecAge.setOnClickListener(if (checkListAllData[0].idealAge[0].isEnabled == "1") this else null)
                    binding.ageSeekBar.isEnabled = checkListAllData[0].idealAge[0].isEnabled == "1"
                    binding.btnIncAge.setColorFilter(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (binding.ageSeekBar.isEnabled) R.color.color_black else R.color.color_light_grey
                        )
                    )
                    binding.btnDecAge.setColorFilter(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (binding.ageSeekBar.isEnabled) R.color.color_black else R.color.color_light_grey
                        )
                    )
                    binding.btnIdealAgeUnlock.visibility =
                        if (checkListAllData[0].idealAge[0].isEnabled != "1") View.VISIBLE else View.GONE

                    binding.imgAgeLock.visibility = binding.btnIdealAgeUnlock.visibility
                    binding.lblIdealAge.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (checkListAllData[0].idealAge[0].isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )
                    binding.ageSeekBar.value =
                        if (checkListAllData[0].idealAge[0].selectedAge.ifEmpty { "0" }
                                .toFloat() >= binding.ageSeekBar.valueFrom) checkListAllData[0].idealAge[0].selectedAge.toFloat() else binding.ageSeekBar.valueFrom
                }

                if (checkListAllData[0].idealHeight.isNotEmpty()) {
                    binding.btnIncHeight.setOnClickListener(if (checkListAllData[0].idealHeight[0].isEnabled == "1") this else null)
                    binding.btnDecHeight.setOnClickListener(if (checkListAllData[0].idealHeight[0].isEnabled == "1") this else null)
                    binding.heightSeekBar.isEnabled =
                        checkListAllData[0].idealHeight[0].isEnabled == "1"
                    binding.btnIncHeight.setColorFilter(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (binding.heightSeekBar.isEnabled) R.color.color_black else R.color.color_light_grey
                        )
                    )
                    binding.btnDecHeight.setColorFilter(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (binding.heightSeekBar.isEnabled) R.color.color_black else R.color.color_light_grey
                        )
                    )

                    binding.btnIdealHeightUnlock.visibility =
                        if (checkListAllData[0].idealHeight[0].isEnabled != "1") View.VISIBLE else View.GONE
                    binding.imgHeightLock.visibility = binding.btnIdealHeightUnlock.visibility
                    binding.lblIdealHeight.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (checkListAllData[0].idealHeight[0].isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )

                    if (checkListAllData[0].idealHeight[0].selectedHeight != null)
                        binding.heightSeekBar.value =
                            if (checkListAllData[0].idealHeight[0].selectedHeight.ifEmpty { "0" }
                                    .toFloat() <= binding.heightSeekBar.valueTo) checkListAllData[0].idealHeight[0].selectedHeight.toFloat() else binding.heightSeekBar.valueTo
                }

                if (checkListAllData[0].religionList.isNotEmpty()) {
//                    onboardingReligionId =
//                        checkListAllData[0].religionList[0].userOnboardingSelectedId
                    religionListAdapter.isEnabled = checkListAllData[0].religionList[0].isEnabled
                    religionListAdapter.addData(checkListAllData[0].religionList[0].data)
                    binding.btnReligionUnlock.visibility =
                        if (religionListAdapter.isEnabled != "1") View.VISIBLE else View.GONE
                    binding.imgReligionLock.visibility = binding.btnReligionUnlock.visibility
                    binding.lblReligion.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (religionListAdapter.isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )
                }


                if (checkListAllData[0].relationshipList.isNotEmpty()) {
                    binding.llRelationShip.visibility =
                        if (checkListAllData[0].relationshipList[0].data.any {
                                it.name.lowercase() == getString(
                                    R.string.never_married
                                )
                            })
                            View.VISIBLE else View.GONE
                    if (!checkListAllData[0].relationshipList[0].data.any {
                            it.name.lowercase() == getString(
                                R.string.never_married
                            )
                        }) {
                        checkListAllData[0].relationshipList[0].data.forEach { it.isSelected = 0 }
                        checkListAllData[0].relationshipList[0].data.find {
                            it.name.lowercase() == getString(
                                R.string.any
                            )
                        }?.apply {
                            isSelected = 1
                        }
                    }


                    relationshipListAdapter.isEnabled =
                        (checkListAllData[0].relationshipList[0].isEnabled)
                    relationshipListAdapter.addData(checkListAllData[0].relationshipList[0].data)
                    binding.btnRelationshipStatusUnlock.visibility =
                        if (relationshipListAdapter.isEnabled != "1") View.VISIBLE else View.GONE
                    binding.imgRelationLock.visibility =
                        binding.btnRelationshipStatusUnlock.visibility
                    binding.lblRelationShip.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (relationshipListAdapter.isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )
                }

                if (checkListAllData[0].professionsList.isNotEmpty()) {
                    professionListAdapter.isEnabled =
                        (checkListAllData[0].relationshipList[0].isEnabled)
                    professionListAdapter.addData(checkListAllData[0].professionsList[0].data)
                    binding.btnProfessionUnlock.visibility =
                        if (professionListAdapter.isEnabled != "1") View.VISIBLE else View.GONE
                    binding.imgProfessionLock.visibility = binding.btnProfessionUnlock.visibility
                    binding.lblProfession.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (professionListAdapter.isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )
                }

                if (checkListAllData[0].eatingHabitsList.isNotEmpty()) {
                    eatingHabitsListAdapter.isEnabled =
                        (checkListAllData[0].eatingHabitsList[0].isEnabled)
                    eatingHabitsListAdapter.addData(checkListAllData[0].eatingHabitsList[0].data)
                    binding.btnEatingHabitUnlock.visibility =
                        if (eatingHabitsListAdapter.isEnabled != "1") View.VISIBLE else View.GONE
                    binding.imgEatingHabitsLock.visibility = binding.btnEatingHabitUnlock.visibility
                    binding.lblEatingHabits.setTextColor(
                        ContextCompat.getColor(
                            this@ChecklistActivity,
                            if (eatingHabitsListAdapter.isEnabled != "1") R.color.color_light_grey else R.color.color_dark_grey
                        )
                    )
                }


                binding.llDisability.visibility =
                    if (checkListAllData[0].selectedDisability.any {
                            it.name.lowercase() == getString(
                                R.string.yes_
                            )
                        })
                        View.GONE else View.VISIBLE
                if (checkListAllData[0].selectedDisability.any { it.name.lowercase() == getString(R.string.yes_) }) {
                    checkListAllData[0].selectedDisability.forEach { it.isSelected = 0 }
                    checkListAllData[0].selectedDisability.find {
                        it.name.lowercase() == getString(
                            R.string.any
                        )
                    }?.apply {
                        isSelected = 1
                    }
                }

                disabilityListAdapter.addData(checkListAllData[0].selectedDisability)

            }

            selectedLocationAdapter.addData(checkListAllData[0].settleLocationData[0].arrayData)
            binding.txtSettleLocationName.text =
                selectedLocationAdapter.objList.joinToString(COMMA_SPACE_SEPERATOR) { it.name }

            forLocation = false
        }
    }
}