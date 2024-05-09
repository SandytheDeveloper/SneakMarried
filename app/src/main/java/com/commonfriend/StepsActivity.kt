package com.commonfriend


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.StepsAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityStepsBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.CategoryModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory


class StepsActivity : BaseActivity(), OnClickListener {

    private lateinit var binding: ActivityStepsBinding
    private lateinit var stepsAdapter: StepsAdapter
    private lateinit var questionViewModel: QuestionViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private var otherId: String = ""
    private var lastPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }


    private fun initialization() {

        isFrom = if (intent.hasExtra(IS_FROM))
            intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
        else ActivityIsFrom.NORMAL

        otherId = if (intent.hasExtra(ID))
            intent.getStringExtra(ID).toString()
        else "" //Pref.getStringValue(Pref.PREF_USER_ID,"").toString()

        allApiResponses()

        binding.llSkipAll.setOnClickListener(this)

        stepsAdapter = StepsAdapter(this, this)
        binding.rvSteps.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvSteps.adapter = stepsAdapter
        binding.btnAdd.setOnClickListener(this)

        activityOnBackPressed(this,this){
            onBackPress()
        }

    }

    private fun onBackPress() {
        finishAffinity()
    }


    override fun onResume() {
        super.onResume()
        callApi()
        ONBOARDING_SKIP = false
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAdd -> {
                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${mainObjList.size}")
                binding.btnAdd.isEnabled = false
                mainObjList = ArrayList(mainObjList.filter { it.isCompleted == 0 })
                if (mainObjList.isNotEmpty()) {
                    when (mainObjList[0].hasQuestion) {
                        "0" -> {
                            this.openA<SectionBreakerActivity> {
                                putExtra(ID, otherId)
                                putExtra(IS_FROM, isFrom)
                            }
                        }

                        "1" -> openA<ChecklistActivity>()
                        "2" -> openA<PriorityActivity>()
                        "3" -> openA<EditProfileActivity>()
                    }
                } else {
                    openA<MainActivity>()
                    finishAffinity()
                }
            }

            R.id.llMain -> {
                lastPos = view.tag.toString().toInt()
                when (stepsAdapter.objList[lastPos].hasQuestion) {
                    "0" -> {
//                        var id = 0
                        for (i in 0 until mainObjList.size) {
                            if (mainObjList[i].categoryId == stepsAdapter.objList[lastPos].categoryId) {
                                val obj = mainObjList[i]
                                mainObjList.clear()
                                mainObjList.add(obj)
                                break
                            }
                        }
//                        mainObjList = ArrayList(mainObjList.subList(id, mainObjList.size))
//                        mainObjList = ArrayList(mainObjList.filter { it.hasQuestion == "0" })
                        this.openA<SectionBreakerActivity> {
                            putExtra(IS_FROM, ActivityIsFrom.FROM_EDIT_SECTION)
                        }
                    }

                    "1" -> openA<ChecklistActivity>()
                    "2" -> openA<PriorityActivity>()
                    "3" -> openA<EditProfileActivity>()
                }
            }

            R.id.llSkipAll -> {
                Util.skipAndOpenHomeScreen(this)
            }

        }
    }

    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            questionViewModel.categoryListApiRequest(this@StepsActivity)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]

        questionViewModel.getCategoryListResponse.observe(this@StepsActivity) {

            if (it.data.isNotEmpty()) {

                if (!it.data[0].isUserExist) {
                    Util.clearData(this)
                    return@observe
                }

                binding.llSkipAll.visibleIf(it.data[0].isSkippable == "1")

                stepsAdapter.addData(it.data.filter { item -> item.isHidden == "0" } as ArrayList<CategoryModel>)
                binding.rlMain.visible()
                mainObjList = ArrayList()

                val list =
                    stepsAdapter.objList.filter { item -> item.isCompleted == 0 } as ArrayList<CategoryModel>
                if (list.isNotEmpty()) {
                    stepsAdapter.objList.find { it.categoryName == list[0].categoryName }?.currentSelected =
                        true
                    stepsAdapter.notifyDataSetChanged()
                }

                mainObjList = it.data

//                    it.data.filter { item -> item.isCompleted == 0 } as ArrayList<CategoryModel>


                binding.txtMsgDesc.text =
                    mainObjList.filter { it.isCompleted == 0 && it.isHidden == "0" }.size.toString() + " " + resources.getString(
                        R.string.more_steps_to_go
                    )


                binding.btnAdd.visibleIf(it.data.isNotEmpty())
                binding.btnAdd.isEnabled = it.data.isNotEmpty()
            }
        }
    }
}



