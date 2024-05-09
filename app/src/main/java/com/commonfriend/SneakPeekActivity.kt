package com.commonfriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonfriend.adapter.SneakPeekListAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivitySneakPeekBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.QUESTION_ID
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.scrollToPositionWithOffsetSmooth
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.QuestionBankViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class SneakPeekActivity : BaseActivity(), View.OnClickListener,ErrorDialogComponent.ErrorBottomSheetClickListener {

    lateinit var binding : ActivitySneakPeekBinding
    lateinit var sneakPeekListAdapter: SneakPeekListAdapter
    var questionBankArray: ArrayList<QuestionBankModel> = ArrayList()
    private var scrollPosition = 0
    var lastPos = -1
    private lateinit var questionBankViewModel: QuestionBankViewModel
    var opinionAnswer = true
    private var leftCount = 0
    private var leftTxtCount = 0
    private lateinit var isFrom: ActivityIsFrom
    private var opponentId: String = ""
    private var opponentProfile: String = ""
    private var questionId = ""
    private var isRandom = 0
    private var readIndex = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySneakPeekBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
    }

    private fun initialization() {

        with(binding.customHeader.get()){
            title = "1 left"
            btnLeft.gone()
            txtPageNO.gone()
            imgCross.visible()

        }

        val layout = LinearLayoutManager(this)
        layout.stackFromEnd = false
        binding.rvSneakPeek.layoutManager = layout
        sneakPeekListAdapter = SneakPeekListAdapter(this,this)
        binding.rvSneakPeek.adapter = sneakPeekListAdapter

        allApiResponses()

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        if (isFrom == ActivityIsFrom.QUESTION_LIST || isFrom == ActivityIsFrom.FILTERED_QUESTION_LIST) {

            questionBankArray = intent.getSerializableExtra(DATA) as ArrayList<QuestionBankModel>
            scrollPosition = intent.getIntExtra(ID, 0) // Position

            if (questionBankArray.isNotEmpty()) {

                questionBankArray.forEach { it.sneakAvailable ="0" }

                sneakPeekListAdapter.addData(questionBankArray)
                binding.rvSneakPeek.scrollToPosition(scrollPosition)

                readIndex = 0
                callApi(3) // To read First question

            }

        } else if (isFrom == ActivityIsFrom.SNEAK_PEAK) {
            opponentId = intent.getStringExtra(ID).toString() // OpponentID
            opponentProfile = intent.getStringExtra(DATA).toString() // OpponentProfile
            sneakPeekListAdapter.isFrom = isFrom

            callApi(1, true)

        } else if (isFrom == ActivityIsFrom.PROFILE || // Mandatory Question only
            isFrom == ActivityIsFrom.QUESTION_OF_THE_DAY
        ) { // All questions
            if (intent.hasExtra(QUESTION_ID))
                questionId = intent.getStringExtra(QUESTION_ID).toString()

            callApi(1, true)
        }

        var scrolledUP = true
        var byUser = false

        binding.rvSneakPeek.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    byUser = true

                if ((newState == RecyclerView.SCROLL_STATE_IDLE) && byUser && (isFrom == ActivityIsFrom.SNEAK_PEAK)) {

                    val firstVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val firstCompletelyVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                    readIndex = if (firstCompletelyVisibleItemPosition >= 0) firstCompletelyVisibleItemPosition else firstVisibleItemPosition
                    callApi(3)

                    Util.print("=======readIndex===== $readIndex ================================================")

                    if (sneakPeekListAdapter.objList[readIndex].sneakAvailable == "1") {
                        sneakPeekListAdapter.objList[readIndex].sneakAvailable = "0"
                        sneakPeekListAdapter.objList[readIndex].doButtonAnimation = true
                        sneakPeekListAdapter.notifyItemChanged(readIndex)
                    }

                    byUser = false
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE && isFrom != ActivityIsFrom.SNEAK_PEAK) {

                    readIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (sneakPeekListAdapter.objList.isNotEmpty())
                        callApi(3)
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrolledUP = dy >= 0 //"Down" else "Up"


                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findLastVisibleItemPosition() + 1
                val scrollPercentage = firstVisibleItemPosition.toFloat() / totalItemCount * 100

                binding.customHeader.get().progressBar.max = 100
                binding.customHeader.get().progressBar.progress = scrollPercentage.toInt()


            }

        })

        /*binding.rvSneakPeek.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    byUser = true

                if ((newState == RecyclerView.SCROLL_STATE_IDLE) && byUser && (isFrom == ActivityIsFrom.SNEAK_PEAK)) {
                    // Get the current visible position
                    val currentPosition =
                        if(scrolledUP)
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() + 1
                        else
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (scrolledUP)
                        binding.rvSneakPeek.scrollToPositionWithOffsetSmooth(currentPosition,0)
//                            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(currentPosition,0)
                    else
                        binding.rvSneakPeek.smoothScrollToPosition(currentPosition)

                    readIndex = currentPosition
                    callApi(3)

                    if (sneakPeekListAdapter.objList[currentPosition].sneakAvailable == "1") {
                        sneakPeekListAdapter.objList[currentPosition].sneakAvailable = "0"
                        sneakPeekListAdapter.objList[currentPosition].doButtonAnimation = true
                        sneakPeekListAdapter.notifyItemChanged(currentPosition)
                    }

                    byUser = false
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE && isFrom != ActivityIsFrom.SNEAK_PEAK) {

                    readIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    callApi(3)
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrolledUP = dy >= 0 //"Down" else "Up"


                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findLastVisibleItemPosition() + 1
                val scrollPercentage = firstVisibleItemPosition.toFloat() / totalItemCount * 100

                binding.customHeader.get().progressBar.max = 100
                binding.customHeader.get().progressBar.progress = scrollPercentage.toInt()


            }

        })*/

    }


    override fun onClick(view: View) {
        when(view.id) {
            R.id.imgYesVisibilityButton,R.id.imgNoVisibilityButton -> {
                
                lastPos = view.tag.toString().toInt()

                errorDialogComponent = ErrorDialogComponent(
                    this,
                    ErrorDialogComponent.ErrorDialogFor.CONFIRMING,
                    "Confirmation",
                    getString(if (sneakPeekListAdapter.objList[lastPos].status == "2") R.string.are_you_sure_you_want_to_unhide_this else R.string.are_you_sure_you_want_to_hide_this), this
                ).apply {
                    this.show()
                }
                
            }
        }
    }

    @SuppressLint("CheckResult")
    fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            when (tag) {

                1 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.getQuestionBankApiRequest(
                        this,
                        opponentId,
                        if (isFrom == ActivityIsFrom.PROFILE) "1" else if (isFrom == ActivityIsFrom.SNEAK_PEAK) "3" else "",
                        questionId
                    )
                }

                2 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.saveOpinionApiRequest(
                        this,
                        sneakPeekListAdapter.objList[lastPos].id,
                        if (opinionAnswer)
                            sneakPeekListAdapter.objList[lastPos].optionA
                        else
                            sneakPeekListAdapter.objList[lastPos].optionB
                    )
                    readIndex = lastPos
                    callApi(3) // To read Question
                }

                3 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionBankViewModel.questionViewApiRequest(
                        this,
                        sneakPeekListAdapter.objList[readIndex].id,
                        opponentId,
                        if (opponentId.isEmpty()) "0" else "1"
                    )
                }

                4 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.hideUnhideAnswerApiRequest(
                        this,
                        sneakPeekListAdapter.objList[lastPos].id, if (sneakPeekListAdapter.objList[lastPos].status == "2") "1" else "0"
                    )
                }
            }
        }
    }


    private fun allApiResponses() {

        questionBankViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionBankViewModel::class.java]


        questionBankViewModel.getQuestionBankResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                if (it.data.size > 0) {

                    leftTxtCount = it.data.filter { it.isMandatory == "1" && it.status == "1" }.size
                    Util.print("-------leftTxtCount11-----$leftTxtCount----------------------------------------------")

                    isRandom = it.data[0].isRandom
                    val totalLeftCount =
                        (if (it.data[0].mandatoryCount.isEmpty()) 0 else it.data[0].mandatoryCount.toInt()) - (questionBankArray.filter { it.isMandatory == "1" && it.mySelectedAnswer != "" }.size)
                    leftCount = if (totalLeftCount < 0) 0 else totalLeftCount

                }

                /*This logic for If User comes From Question Of the day widget and Filter Type from Admin config is Random then show same first question which in display in widget  */
                if (isRandom == 1 && (isFrom == ActivityIsFrom.QUESTION_OF_THE_DAY)) {
                    val index =
                        it.data.indexOf(it.data.find { items -> items.id == intent.getStringExtra(ID) })
                    if (index != -1) {
                        val modal = it.data[index]
                        it.data.removeAt(index)
                        it.data.add(0, modal)
                    }
                }

                when (isFrom) {
                    ActivityIsFrom.QUESTION_OF_THE_DAY -> {
                        it.data.removeAll { it.mySelectedAnswer.isNotEmpty() }
                    }

                    ActivityIsFrom.PROFILE -> {
                        it.data.removeAll { it.status != "0" }

                    }

                    ActivityIsFrom.SNEAK_PEAK -> {
                        it.data.removeAll { it.mySelectedAnswer.isEmpty() || it.otherSelectedAnswer.isEmpty() }

                    }

                    else -> {}
                }
                Util.print("-------leftTxtCount22-----$leftTxtCount----------------------------------------------")

                questionBankArray.addAll(it.data)
                if (questionBankArray.isNotEmpty()){
                    sneakPeekListAdapter.addData(questionBankArray)

                    readIndex = 0
                    callApi(3) // To read First question

                    updateQuestionLeftText()

                    if (isFrom == ActivityIsFrom.SNEAK_PEAK){ // for first time animation
                        sneakPeekListAdapter.profilePic = opponentProfile
                        if (sneakPeekListAdapter.objList[0].sneakAvailable == "1") {
                            sneakPeekListAdapter.objList[0].sneakAvailable = "0"
                            sneakPeekListAdapter.objList[0].doButtonAnimation = true
                            sneakPeekListAdapter.notifyItemChanged(0)
                        }
                    }

                }

            }
        }

        questionBankViewModel.questionViewResponse.observe(this) {
            Util.dismissProgress()
        }

        questionBankViewModel.saveQuestionAnswerResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1) {
                leftTxtCount++
                with(sneakPeekListAdapter.objList[lastPos]) {
                    if (isMandatory == "1" && leftCount > 0)
                        leftCount--

                }
            }
            updateQuestionLeftText()
        }

        questionBankViewModel.hideUnhideResponse.observe(this) {
            Util.dismissProgress()

        }
    }

    private fun updateQuestionLeftText() {

        var left = sneakPeekListAdapter.objList[0].mandatoryCount.trim().toInt() - leftTxtCount
        left = if (left < 1) 0 else left
        binding.customHeader.get().txtLeftForSneak.text = "$left left!"

        binding.customHeader.get().txtLeftForSneak.visibleIf(isFrom == ActivityIsFrom.PROFILE && left > 0)

    }

    fun scrollToNext(position : Int) {

        binding.rvSneakPeek.scrollToPositionWithOffsetSmooth(position,0)

    }


    override fun onItemClick(itemID: String,isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        when (itemID) {
            "0" -> {

                sneakPeekListAdapter.objList[lastPos].status = if (sneakPeekListAdapter.objList[lastPos].status == "2") "1" else "2"
                sneakPeekListAdapter.notifyItemChanged(lastPos)
                Util.showLottieDialog(this, "done_lottie.json",wrapContent = true)

                errorDialogComponent?.dismiss()

                callApi(4, false)
            }

            "1" -> {

                Handler(Looper.getMainLooper()).postDelayed({
                    errorDialogComponent?.dismiss()
                }, 300)
            }
        }
    }
    
}