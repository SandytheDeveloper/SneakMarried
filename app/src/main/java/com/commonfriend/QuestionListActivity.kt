package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.commonfriend.adapter.FilterAdapter
import com.commonfriend.adapter.QuestionListAdapter
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityQuestionListBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.FilterModel
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionBankViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class QuestionListActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding : ActivityQuestionListBinding
    private var questionList : ArrayList<QuestionBankModel> = ArrayList()
    private lateinit var questionListAdapter: QuestionListAdapter
    private lateinit var questionBankViewModel: QuestionBankViewModel
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private var apiIsCalled = true
    private lateinit var filterAdapter: FilterAdapter
    private var selectedFilter = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Util.statusBarColor(this,window)

        initialization()
    }

    private fun initialization() {

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        filterAdapter = FilterAdapter(this, this)
        binding.rvFilter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilter.adapter = filterAdapter
        setFilterData()

        questionListAdapter = QuestionListAdapter(this, this)
        binding.rvQuestionList.layoutManager = LinearLayoutManager(this)
        binding.rvQuestionList.adapter = questionListAdapter

        /*binding.rvQuestionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val lastIndex = questionListAdapter.objList.lastIndex

                Util.print("-------$lastVisibleItemPosition---------$lastIndex-------------------------------------------------")

                val scrollProgress = (lastVisibleItemPosition.toFloat() / totalItemCount.toFloat()) * 100

                // Update the progress of the ProgressBar
//                binding.customHeader.get().progressBar.max = 100
//                binding.customHeader.get().progressBar.progress = scrollProgress.toInt()
            }
        })*/

        binding.btnCross.setOnClickListener(this)
        binding.llContribute.setOnClickListener(this)

        allApiResponses()

        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }

    private fun setFilterData() {

        val filterList : ArrayList<FilterModel> = ArrayList()
        // 0: not answer, 1 : answered , 2 : Answer with hidden

        var obj = FilterModel()
        obj.name = getString(R.string.all)
        obj.id = ""
        obj.isSelected = 1
        filterList.add(obj)

        obj = FilterModel()
        obj.name = getString(R.string.hidden)
        obj.id = "2"
        filterList.add(obj)

        obj = FilterModel()
        obj.name = getString(R.string.answered)
        obj.id = "1"
        filterList.add(obj)

        obj = FilterModel()
        obj.name = getString(R.string.unanswered)
        obj.id = "0"
        filterList.add(obj)

//        obj = FilterModel()
//        obj.name = getString(R.string.contribute)
//        obj.id = "3"
//        filterList.add(obj)

        filterAdapter.addData(filterList)

    }

    override fun onResume() {
        super.onResume()
        callApi(1)
    }

    private fun onBackPress() {

        if (isFrom == ActivityIsFrom.NOTIFICATION){
            startActivity(Intent(this,MainActivity::class.java).putExtra(DATA,1))
        }
        finish()
    }

    private fun allApiResponses() {

        questionBankViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionBankViewModel::class.java]

        questionBankViewModel.getQuestionBankResponse.observe(this) {
            Util.dismissProgress()
            apiIsCalled = true
            if (it.success == 1) {
                binding.rvFilter.visibleIf(it.data.isNotEmpty())
                if (it.data.isNotEmpty()) {
                    questionList.clear()
                    questionList.addAll(it.data)

                    // 0: not answer, 1 : answered , 2 : Answer with hidden
                    when (selectedFilter) {
                        "0","1","2" -> questionListAdapter.addData(questionList.filter {itt -> itt.status == selectedFilter } as ArrayList<QuestionBankModel>)
                        else -> questionListAdapter.addData(questionList)
                    }

                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            if (showProgress) Util.showProgress(this)
            when (tag) {
                1 -> {
                    if (apiIsCalled) {
                        apiIsCalled = false
                        questionBankViewModel.getQuestionBankApiRequest(
                            this@QuestionListActivity,
                            questionType = "2"
                        )
                    }
                }
            }
        }
    }




    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view : View) {
        when(view.id) {
            R.id.cvMain -> {

                val pos = view.tag.toString().toInt()
                // 0: not answer, 1 : answered , 2 : Answer with hidden

//                val newList = when(selectedFilter) {
//                    "0","1","2" -> questionListAdapter.objList
//                    else -> questionListAdapter.objList.filter { it.status == "0" || it.questionNumber == questionListAdapter.objList[pos].questionNumber } as ArrayList<QuestionBankModel>
//                }
//                val questionPosition = newList.indexOfFirst { it.id == questionListAdapter.objList[pos].id }
//
//                startActivity(Intent(this,SneakPeakActivity::class.java)
//                    .putExtra(ID,questionPosition)
//                    .putExtra(DATA,newList)
//                    .putExtra(IS_FROM,if (selectedFilter.isEmpty()) ActivityIsFrom.QUESTION_LIST else ActivityIsFrom.FILTERED_QUESTION_LIST))


                startActivity(Intent(this,SneakPeekActivity::class.java)
                    .putExtra(ID,pos)
                    .putExtra(DATA,questionListAdapter.objList)
                    .putExtra(IS_FROM,if (selectedFilter.isEmpty()) ActivityIsFrom.QUESTION_LIST else ActivityIsFrom.FILTERED_QUESTION_LIST))

            }
            R.id.btnCross -> {
                onBackPress()
            }
            R.id.llContribute -> {
                startActivity(Intent(this,ContributeActivity::class.java))
            }
            R.id.llMain -> {

                val pos = view.tag.toString().toInt()

                if (filterAdapter.objList[pos].id == "3"){

                    startActivity(Intent(this,ContributeActivity::class.java))

                } else {

                    filterAdapter.objList.forEach { it.isSelected = 0 }
                    filterAdapter.objList[pos].isSelected = 1
                    selectedFilter = filterAdapter.objList[pos].id
                    filterAdapter.notifyDataSetChanged()

                    // 0: not answer, 1 : answered , 2 : Answer with hidden
                    when (selectedFilter) {
                        "0", "1", "2" -> questionListAdapter.addData(questionList.filter { itt -> itt.status == selectedFilter } as ArrayList<QuestionBankModel>)
                        else -> questionListAdapter.addData(questionList)
                    }
                }

            }
        }
    }
}