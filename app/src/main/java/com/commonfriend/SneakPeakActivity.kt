package com.commonfriend

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivitySneakPeakBinding
import com.commonfriend.databinding.RowQuestionsCardBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionBankViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import kotlin.math.abs

class SneakPeakActivity : BaseActivity(), View.OnClickListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivitySneakPeakBinding
    private lateinit var visibleView: ViewGroup
    private lateinit var rowQuestionCardBinding: RowQuestionsCardBinding
    var questionBankArray: ArrayList<QuestionBankModel> = ArrayList()
    private lateinit var questionBankViewModel: QuestionBankViewModel
    private var opponentId: String = ""
    private var opponentProfile: String = ""
    private var opinionAnswer: Boolean = false
    private var isHide: String = ""
    private lateinit var isFrom: ActivityIsFrom
    private var firstTime: Boolean = true
    private var position = 0
    private var isRandom = 0
    private var startPosition: Float = 0f
    private var leftCount = 0
    private var leftTxtCount = 0
    private var questionId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySneakPeakBinding.inflate(layoutInflater)
        setContentView(binding.root)
        logForCurrentScreen(Screens.SNEAK_PEAK_SCREEN.screenType,Screens.SNEAK_PEAK_SCREEN.screenName)
        initialization()

    }


    //                      NOTE

    /*
    FROM - HomeScreen - on click of (SneakPeak) =

        startActivity(Intent(this,SneakPeakActivity::class.java)
        .putExtra(IS_FROM,MyEnum.SNEAK_PEAK)
        .putExtra(ID,"645de86a242588ce963cf7ac") // Opponent Id
        .putExtra(DATA,"opponentProfile")) // Opponent Profile


    FROM - HomeScreen - on click of (QuestionOfTheDay) =

        startActivity(Intent(this,SneakPeakActivity::class.java)
        .putExtra(IS_FROM,MyEnum.QUESTION_OF_THE_DAY)


    FROM - QuestionListActivity - on click of (Items) =

        startActivity(Intent(this,SneakPeakActivity::class.java)
            .putExtra(ID,view.tag.toString().toInt())
            .putExtra(DATA,questionListAdapter.objList)
            .putExtra(IS_FROM,MyEnum.QUESTION_LIST))


    FROM - Suggestion Screen - on click of (Opinions and Interests) =

        startActivity(Intent(this,SneakPeakActivity::class.java)
            .putExtra(IS_FROM,MyEnum.PROFILE))

        */


    //    imgYesMyProfile
//    imgNoMyProfile
    @SuppressLint("ClickableViewAccessibility")
    private fun initialization() {

        binding.customHeader.get().imgCross.visibility = View.VISIBLE
        binding.customHeader.get().btnLeft.visibility = View.GONE
        binding.customHeader.get().txtPageNO.visibility = View.GONE


        binding.btnNext.setOnClickListener(this)
        binding.btnPrevious.setOnClickListener(this)

        if (intent.hasExtra(IS_FROM))
            isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom


        allApiResponses()

        binding.firstView.setOnTouchListener(CrossSwipeTouchListener())

        if (isFrom == ActivityIsFrom.QUESTION_LIST || isFrom == ActivityIsFrom.FILTERED_QUESTION_LIST) {
            questionBankArray = intent.getSerializableExtra(DATA) as ArrayList<QuestionBankModel>
            position = intent.getIntExtra(ID, 0) // Position

            if (questionBankArray.isNotEmpty()) {
                addView(true)
                addView()
                if (isFrom == ActivityIsFrom.QUESTION_LIST)
                    fromQuestionList()
            }

        } else if (isFrom == ActivityIsFrom.SNEAK_PEAK) {
            opponentId = intent.getStringExtra(ID).toString() // OpponentID
            opponentProfile = intent.getStringExtra(DATA).toString() // OpponentProfile
//            opponentProfile = R.drawable.ic_dummy_img
            callApi(1, true)

        } else if (isFrom == ActivityIsFrom.PROFILE || // Mandatory Question only
            isFrom == ActivityIsFrom.QUESTION_OF_THE_DAY
        ) { // All questions
            if (intent.hasExtra(QUESTION_ID))
                questionId = intent.getStringExtra(QUESTION_ID).toString()

            callApi(1, true)
        }

        binding.btnNext.text = if (isFrom == ActivityIsFrom.SNEAK_PEAK) getString(R.string.next) else getString(R.string.skip)

    }

    private fun fromQuestionList(setPosition: String = "") {

        if (setPosition.isNotEmpty()) {

            firstTime = false
            if (questionBankArray.isNotEmpty() && questionBankArray.any { it.status != "0" }) {
                questionBankArray.removeAll { it.status != "0" }

                if (setPosition == "1")
                    position--
            }

            return
        }


        val firstPos = questionBankArray.indexOfFirst { it.status == "0" }
        val lastPos = questionBankArray.indexOfLast { it.status == "0" }

        if (firstPos == -1) {
            // Not found , All questions are Answered
            // disable NEXT and PREVIOUS buttons

            binding.btnNext.isEnabled = false
            binding.btnNext.setTextColor(resources.getColor(R.color.color_base_grey))
            binding.btnPrevious.isEnabled = false
            binding.btnPrevious.setTextColor(resources.getColor(R.color.color_base_grey))

        } else if (firstPos < position) {
            // not Answered question is still available before current Position

            binding.btnNext.isEnabled = false
            binding.btnNext.setTextColor(resources.getColor(R.color.color_base_grey))

            if (lastPos > position) {

                // not Answered questions are available after current Position
                binding.btnNext.isEnabled = true
                binding.btnNext.setTextColor(resources.getColor(R.color.color_black))

            }

        } else if (firstPos >= position) {
            // not answered question is still available after current Position

            binding.btnPrevious.isEnabled = false
            binding.btnPrevious.setTextColor(resources.getColor(R.color.color_base_grey))

        }
    }

    @SuppressLint("SetTextI18n")
    private fun addView(changeView: Boolean = false) {
        binding.customHeader.get().progressBar.max = questionBankArray.size
        binding.customHeader.get().progressBar.progress = position+1

        binding.btnPrevious.setTextColor(resources.getColor(if (position == 0) R.color.color_base_grey else R.color.color_black))
        binding.btnNext.setTextColor(resources.getColor(if (position == questionBankArray.lastIndex) R.color.color_base_grey else R.color.color_black))
        binding.btnNext.isEnabled = (position != questionBankArray.lastIndex)

        visibleView = if (!changeView) binding.firstView else binding.secondView

        visibleView.removeAllViews()


        rowQuestionCardBinding = RowQuestionsCardBinding.inflate(layoutInflater)
//        LayoutInflater.from(this).inflate(R.layout.row_questions_card, null)

        visibleView.addView(rowQuestionCardBinding.root)

        if (!changeView)
            binding.firstView.setTag(R.string.app_name, rowQuestionCardBinding)
        else
            binding.secondView.setTag(R.string.app_name, rowQuestionCardBinding)

        if (!changeView)
            callApi(3)

        with(rowQuestionCardBinding) {

            with(questionBankArray[position]) {

//                txtRating.text = if (rating.isEmpty()) "0.0" else String.format(
//                    Locale.US,
//                    "%.1f",
//                    rating.toFloat()
//                )
                txtNumber.text = "#$questionNumber"
                llPrivacy.visibility = if (shield == "1") View.VISIBLE else View.INVISIBLE
                shieldMessage.text = infoMessage

                llContributed.visibleIf(isContributed == "1")

//                val answeredQuestion = questionBankArray.filter { it.isMandatory == "1" && it.mySelectedAnswer.isNotEmpty()}.size
                var left = questionBankArray[0].mandatoryCount.trim().toInt() - leftTxtCount
                left = if (left < 1) 0 else left
                txtLeft.text = "$left left!"

//                txtLeft.text =
//                    "${questionBankArray.size - questionBankArray.filter { it.isMandatory == "0" }.size} left!"
                txtLeft.visibility =
                    if (isFrom == ActivityIsFrom.PROFILE && isMandatory == "1" && mySelectedAnswer.isEmpty())
                        if (left < 1) View.INVISIBLE else View.VISIBLE
                    else
                        View.INVISIBLE

                if (isFrom == ActivityIsFrom.QUESTION_OF_THE_DAY) {
                    txtHide.visibility = View.INVISIBLE
                } else if (status == "0") {
                    txtHide.visibility = View.INVISIBLE
                } else if (status == "1") {
                    txtHide.visibility = if (isHidden == 1) View.VISIBLE else View.INVISIBLE
                    txtHide.text = getString(R.string.hide_this_answer)
                    txtHide.setTextColor(ContextCompat.getColor(this@SneakPeakActivity,R.color.color_white))
                    txtHide.paintFlags = txtHide.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                } else if (status == "2") {
                    txtHide.visibility = View.VISIBLE
                    txtHide.text = getString(R.string.the_answer_is_hidden)
                    txtHide.setTextColor(ContextCompat.getColor(this@SneakPeakActivity,R.color.color_black))
                    txtHide.paintFlags = txtHide.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }

                imgYesMyProfile.setImageURI(Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, ""))
                imgNoMyProfile.setImageURI(Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, ""))

                txtQuestions.text = question
                txtQuestionTitle.text = questionTitle
                txtQuestions.adjustFontSize(10, 32f)


                txtYes.text = optionA
                txtNo.text = optionB

                if (isFrom == ActivityIsFrom.SNEAK_PEAK) {

                    txtHide.visibility = View.INVISIBLE
                    txtLeft.visibility = View.INVISIBLE
                    btnYes.isEnabled = false
                    btnNo.isEnabled = false


                    if (mySelectedAnswer == otherSelectedAnswer && mySelectedAnswer.isNotEmpty()) {

                        if (mySelectedAnswer == optionA) {

                            if (!changeView) {
                                if (sneakAvailable == "1") {
                                    Util.startAnimationForSameAnswer(
                                        this@SneakPeakActivity,
                                        imgYesMyProfile,
                                        imgYesOtherProfile,
                                        btnYes,
                                        txtYes
                                    )
                                    sneakAvailable = "0"
                                } else {
                                    imgYesMyProfile.visibility = View.VISIBLE
                                    imgYesOtherProfile.visibility = View.VISIBLE
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtYes.setTextColor(resources.getColor(R.color.color_black))
                                    btnYes.strokeColor = resources.getColor(R.color.color_white)
                                }
                            } else {
                                if (sneakAvailable != "1") {
                                    imgYesMyProfile.visibility = View.VISIBLE
                                    imgYesOtherProfile.visibility = View.VISIBLE
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtYes.setTextColor(resources.getColor(R.color.color_black))
                                    btnYes.strokeColor = resources.getColor(R.color.color_white)
                                }
                            }

//                            imgYesMyProfile.visibility = View.VISIBLE
//                            imgYesOtherProfile.visibility = View.VISIBLE
                            imgYesOtherProfile.setImageURI(opponentProfile)
//                            btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
//                            txtYes.setTextColor(resources.getColor(R.color.color_black))
//                            btnYes.strokeColor = resources.getColor(R.color.color_white)


                        } else if (mySelectedAnswer == optionB) {

                            if (!changeView) {
                                if (sneakAvailable == "1") {
                                    Util.startAnimationForSameAnswer(
                                        this@SneakPeakActivity,
                                        imgNoMyProfile,
                                        imgNoOtherProfile,
                                        btnNo,
                                        txtNo
                                    )
                                    sneakAvailable = "0"
                                } else {
                                    imgNoMyProfile.visibility = View.VISIBLE
                                    imgNoOtherProfile.visibility = View.VISIBLE
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtNo.setTextColor(resources.getColor(R.color.color_black))
                                    btnNo.strokeColor = resources.getColor(R.color.color_white)
                                }
                            } else {
                                if (sneakAvailable != "1") {
                                    imgNoMyProfile.visibility = View.VISIBLE
                                    imgNoOtherProfile.visibility = View.VISIBLE
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtNo.setTextColor(resources.getColor(R.color.color_black))
                                    btnNo.strokeColor = resources.getColor(R.color.color_white)
                                }
                            }

//                            imgNoMyProfile.visibility = View.VISIBLE
//                            imgNoOtherProfile.visibility = View.VISIBLE
                            imgNoOtherProfile.setImageURI(opponentProfile)
//                            btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
//                            txtNo.setTextColor(resources.getColor(R.color.color_black))
//                            btnNo.strokeColor = resources.getColor(R.color.color_white)
                        }
                    } else {
                        if (mySelectedAnswer == optionA) { // FOR MY ANSWER

                            if (!changeView ) {
                                if (sneakAvailable == "1") {
                                    Util.startAnimationForSingleAnswer(
                                        this@SneakPeakActivity,
                                        imgYesMyProfile,
                                        imgNoMyProfile,
                                        btnYes,
                                        txtYes,
                                        btnNo,
                                        txtNo
                                    )
                                    sneakAvailable = "0"
                                } else {

                                    imgYesMyProfile.visibility = View.VISIBLE
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtYes.setTextColor(resources.getColor(R.color.color_black))
                                    btnYes.strokeColor = resources.getColor(R.color.color_white)


                                    imgNoMyProfile.visibility = View.VISIBLE
                                    imgNoMyProfile.setImageURI(opponentProfile)
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_black))
                                    txtNo.setTextColor(resources.getColor(R.color.color_white))
                                    btnNo.strokeColor = resources.getColor(R.color.color_black)

                                }
                            } else {
                                if (sneakAvailable != "1"){

                                    imgYesMyProfile.visibility = View.VISIBLE
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtYes.setTextColor(resources.getColor(R.color.color_black))
                                    btnYes.strokeColor = resources.getColor(R.color.color_white)


                                    imgNoMyProfile.visibility = View.VISIBLE
                                    imgNoMyProfile.setImageURI(opponentProfile)
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_black))
                                    txtNo.setTextColor(resources.getColor(R.color.color_white))
                                    btnNo.strokeColor = resources.getColor(R.color.color_black)
                                }
                            }

                            imgNoMyProfile.setImageURI(opponentProfile)

//                            imgYesMyProfile.visibility = View.VISIBLE
//                            btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
//                            txtYes.setTextColor(resources.getColor(R.color.color_black))
//                            btnYes.strokeColor =
//                                resources.getColor(R.color.color_white)


                        } else if (mySelectedAnswer == optionB) {

                            if (!changeView) {
                                if (sneakAvailable == "1") {
                                    Util.startAnimationForSingleAnswer(
                                        this@SneakPeakActivity,
                                        imgNoMyProfile,
                                        imgYesMyProfile,
                                        btnNo,
                                        txtNo,
                                        btnYes,
                                        txtYes
                                    )
                                    sneakAvailable = "0"
                                } else {

                                    imgNoMyProfile.visibility = View.VISIBLE
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtNo.setTextColor(resources.getColor(R.color.color_black))
                                    btnNo.strokeColor = resources.getColor(R.color.color_white)

                                    imgYesMyProfile.visibility = View.VISIBLE
                                    imgYesMyProfile.setImageURI(opponentProfile)
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_black))
                                    txtYes.setTextColor(resources.getColor(R.color.color_white))
                                    btnYes.strokeColor = resources.getColor(R.color.color_black)

                                }
                            } else {
                                if (sneakAvailable != "1") {

                                    imgNoMyProfile.visibility = View.VISIBLE
                                    btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
                                    txtNo.setTextColor(resources.getColor(R.color.color_black))
                                    btnNo.strokeColor = resources.getColor(R.color.color_white)

                                    imgYesMyProfile.visibility = View.VISIBLE
                                    imgYesMyProfile.setImageURI(opponentProfile)
                                    btnYes.setCardBackgroundColor(resources.getColor(R.color.color_black))
                                    txtYes.setTextColor(resources.getColor(R.color.color_white))
                                    btnYes.strokeColor = resources.getColor(R.color.color_black)
                                }
                            }

                            imgYesMyProfile.setImageURI(opponentProfile)

//                            imgNoMyProfile.visibility = View.VISIBLE
//                            btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
//                            txtNo.setTextColor(resources.getColor(R.color.color_black))
//                            btnNo.strokeColor =
//                                resources.getColor(R.color.color_white)
                        }

                        /*if (otherSelectedAnswer == optionA) { // FOR OPPONENT ANSWER
//                            imgYesMyProfile.visibility = View.VISIBLE
                            imgYesMyProfile.setImageURI(opponentProfile)
//                            imgYesMyProfile.setImageResource(opponentProfile)
//                            btnYes.setCardBackgroundColor(resources.getColor(R.color.color_black))
//                            txtYes.setTextColor(resources.getColor(R.color.color_white))
//                            btnYes.strokeColor =
//                                resources.getColor(R.color.color_black)
                        } else if (otherSelectedAnswer == optionB) {
//                            imgNoMyProfile.visibility = View.VISIBLE
                            imgNoMyProfile.setImageURI(opponentProfile)
//                            imgNoMyProfile.setImageResource(opponentProfile)
//                            btnNo.setCardBackgroundColor(resources.getColor(R.color.color_black))
//                            txtNo.setTextColor(resources.getColor(R.color.color_white))
//                            btnNo.strokeColor =
//                                resources.getColor(R.color.color_black)
                        }*/
                    }

                } else {

                    if (mySelectedAnswer == optionA) {
                        imgYesMyProfile.visibility = View.VISIBLE
                        imgYesMyProfile.setImageURI(
                            Pref.getStringValue(
                                Pref.PREF_USER_DISPLAY_PICTURE,
                                ""
                            )
                        )
                        imgYesLock.visibility = View.VISIBLE
                        btnYes.setCardBackgroundColor(resources.getColor(R.color.color_white))
                        txtYes.setTextColor(resources.getColor(R.color.color_black))
                        btnYes.strokeColor =
                            resources.getColor(R.color.color_white)
                        btnYes.isEnabled = false
                        btnNo.isEnabled = false

                    } else if (mySelectedAnswer == optionB) {
                        imgNoMyProfile.visibility = View.VISIBLE
                        imgNoMyProfile.setImageURI(
                            Pref.getStringValue(
                                Pref.PREF_USER_DISPLAY_PICTURE,
                                ""
                            )
                        )
                        imgNoLock.visibility = View.VISIBLE
                        btnNo.setCardBackgroundColor(resources.getColor(R.color.color_white))
                        txtNo.setTextColor(resources.getColor(R.color.color_black))
                        btnNo.strokeColor =
                            resources.getColor(R.color.color_white)
                        btnYes.isEnabled = false
                        btnNo.isEnabled = false
                    }

                }


            }

            btnYes.setOnClickListener(this@SneakPeakActivity)
            btnNo.setOnClickListener(this@SneakPeakActivity)

            txtHide.setOnClickListener(this@SneakPeakActivity)

        }
    }

    private fun swipeUp(isAnswered: Boolean = false) {

        binding.btnNext.isEnabled = false
        binding.btnPrevious.isEnabled = false
        if (!isAnswered)
            position++

        if (isFrom == ActivityIsFrom.QUESTION_LIST && firstTime)
            fromQuestionList("1") // setPosition to Next not Answered Question

        val originalLayout = visibleView

//        val startPosition = originalLayout.y
        if (startPosition == 0f)
            startPosition = originalLayout.y
        val endPosition = -2000f
        val animator = originalLayout.animate()
        animator.apply {
            duration = 500 // set your desired duration
            y(endPosition)
        }

        animator.setListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)

                if (position < questionBankArray.size) {
                    addView(true)
                    rowQuestionCardBinding.btnYes.isEnabled = false
                    rowQuestionCardBinding.btnNo.isEnabled = false
                    if (position == questionBankArray.size - 1) binding.btnNext.isEnabled = false
                    binding.btnNext.setTextColor(resources.getColor(if (position == questionBankArray.size - 1) R.color.color_base_grey else R.color.color_black))
                } else {
                    binding.secondView.visibility = View.GONE
                    binding.btnNext.isEnabled = false
                    binding.btnNext.setTextColor(resources.getColor(R.color.color_base_grey))
                    binding.btnPrevious.isEnabled = false
                    binding.btnPrevious.setTextColor(resources.getColor(R.color.color_base_grey))

                }
                binding.btnNext.isEnabled = false
                binding.btnPrevious.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

//                if(isFrom == ActivityIsFrom.SNEAK_PEAK && (position != questionBankArray.size-1)){
//                    finish()
//                }
                binding.btnNext.isEnabled = (position != questionBankArray.size - 1)
                binding.btnPrevious.isEnabled = true

                if (position < questionBankArray.size)
                    addView()
                else {
                    binding.firstView.visibility = View.GONE
                    finish()
                }

                animator.setListener(null)
                originalLayout.y = startPosition

            }
        })

        // Start the animation
        animator.start()


    }

    private fun swipeDown() {

        binding.btnNext.isEnabled = false
        binding.btnPrevious.isEnabled = false
        position--
        if (isFrom == ActivityIsFrom.QUESTION_LIST && firstTime)
            fromQuestionList("2") // setPosition to Previous not Answered Question

        val originalLayout = visibleView

//        val startPosition = originalLayout.y
        if (startPosition == 0f)
            startPosition = originalLayout.y

        originalLayout.y = -2000f

        val animator = originalLayout.animate()

        animator.apply {
            duration = 500 // set your desired duration
            y(startPosition)
        }

        animator.setListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)

                if (position < questionBankArray.size) {
                    addView()
                } else
                    binding.secondView.visibility = View.GONE

                binding.btnNext.isEnabled = false
                binding.btnPrevious.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                binding.btnNext.isEnabled = (position != questionBankArray.lastIndex)
                binding.btnNext.setTextColor(resources.getColor(if (position != questionBankArray.lastIndex) R.color.color_black else R.color.color_base_grey))
                binding.btnPrevious.isEnabled = true

                addView(true)
                visibleView = binding.firstView
                rowQuestionCardBinding =
                    binding.firstView.getTag(R.string.app_name) as RowQuestionsCardBinding
                animator.setListener(null)

            }
        })

        // Start the animation
        animator.start()


    }

    private fun setButtons(optionA: Boolean) {

        opinionAnswer = optionA


        with(rowQuestionCardBinding) {

            Util.startAnimationForAnswer(this@SneakPeakActivity,if (optionA) imgYesMyProfile else imgNoMyProfile)

//            imgYesMyProfile.visibility = if (optionA) View.VISIBLE else View.GONE
            btnYes.setCardBackgroundColor(
                ContextCompat.getColor(
                    this@SneakPeakActivity,
                    if (optionA) R.color.color_white else R.color.color_blue
                )
            )

//            imgNoMyProfile.visibility = if (optionA) View.GONE else View.VISIBLE
            btnNo.setCardBackgroundColor(
                ContextCompat.getColor(
                    this@SneakPeakActivity,
                    if (optionA) R.color.color_blue else R.color.color_white
                )
            )

            txtYes.setTextColor(
                ContextCompat.getColor(
                    this@SneakPeakActivity,
                    if (optionA) R.color.color_black else R.color.color_white
                )
            )
            txtNo.setTextColor(
                ContextCompat.getColor(
                    this@SneakPeakActivity,
                    if (optionA) R.color.color_white else R.color.color_black
                )
            )

            binding.btnPrevious.isEnabled = false
            binding.btnNext.isEnabled = false
            btnYes.isEnabled = false
            btnNo.isEnabled = false

            callApi(2)
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnNext -> {
                if (position < questionBankArray.size)
                    swipeUp()
            }

            R.id.btnPrevious -> {
                if (position > 0) {
                    swipeDown()
                }
            }

            R.id.btnYes -> {
                setButtons(true)
            }

            R.id.btnNo -> {
                setButtons(false)
            }

            R.id.txtHide -> {
                errorDialogComponent = ErrorDialogComponent(
                    this,
                    ErrorDialogComponent.ErrorDialogFor.CONFIRMING,
                    "Confirmation",
                    getString(if (questionBankArray[position].status == "2") R.string.are_you_sure_you_want_to_unhide_this else R.string.are_you_sure_you_want_to_hide_this), this
                ).apply {
                    this.show()
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
                if (questionBankArray.isNotEmpty())
                    addView()
            }
        }

        questionBankViewModel.saveQuestionAnswerResponse.observe(this@SneakPeakActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                leftTxtCount++
                with(questionBankArray[position]) {
                    if (isMandatory == "1" && leftCount > 0)
                        leftCount--
//                        questionBankArray.forEach {  it.leftCount-- }
                }
                questionBankArray.removeAt(position)
                swipeUp(true)
            }
        }

        questionBankViewModel.questionViewResponse.observe(this@SneakPeakActivity) {
            Util.dismissProgress()
        }

        questionBankViewModel.hideUnhideResponse.observe(this@SneakPeakActivity) {
            Util.dismissProgress()
            errorDialogComponent?.dismiss()
            questionBankArray[position].status = if (isHide == "1") "2" else "1"
            rowQuestionCardBinding.txtHide
                .setTextColor(ContextCompat.getColor(this,if (isHide == "1") R.color.color_black else R.color.color_white))
            rowQuestionCardBinding.txtHide.text =
                getString(if (isHide == "1") R.string.the_answer_is_hidden else R.string.hide_this_answer)
            rowQuestionCardBinding.shieldMessage.text =
                if (isHide == "1") questionBankArray[position].hiddenPrivacyMessage else questionBankArray[position].infoMessage
            isHide = ""

            if (isFrom == ActivityIsFrom.PROFILE) {
                questionBankArray.removeAt(position)

                if (position == questionBankArray.size) {
                    swipeDown()
                } else {
                    swipeUp()
                }
            }
            Util.showLottieDialog(this, "done_lottie.json",wrapContent = true)

        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            when (tag) {
                1 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.getQuestionBankApiRequest(
                        this@SneakPeakActivity,
                        opponentId,
                        if (isFrom == ActivityIsFrom.PROFILE) "1" else if (isFrom == ActivityIsFrom.SNEAK_PEAK) "3" else "",
                        questionId
                    )
                }

                2 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.saveOpinionApiRequest(
                        this@SneakPeakActivity,
                        questionBankArray[position].id,
                        if (opinionAnswer)
                            questionBankArray[position].optionA
                        else
                            questionBankArray[position].optionB
                    )
                }

                3 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    questionBankViewModel.questionViewApiRequest(
                        this@SneakPeakActivity,
                        questionBankArray[position].id,
                        opponentId,
                        if (opponentId.isEmpty()) "0" else "1"
                    )
                }

                4 -> {
                    if (showProgress) Util.showProgress(this)
                    questionBankViewModel.hideUnhideAnswerApiRequest(
                        this@SneakPeakActivity,
                        questionBankArray[position].id, isHide
                    )
                }
            }
        }
    }

    inner class CrossSwipeTouchListener : View.OnTouchListener {

        private var velocityTracker: VelocityTracker? = null
        private var startX = 0f
        private var startY = 0f
        private var isSwiping = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker?.addMovement(event)
                    isSwiping = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)
                    velocityTracker?.computeCurrentVelocity(1000)
                    val velocityX = velocityTracker?.xVelocity ?: 0f
                    val velocityY = velocityTracker?.yVelocity ?: 0f
                    val deltaX = event.x - startX
                    val deltaY = event.y - startY
                    if (!isSwiping) {
                        if (abs(deltaX) > 50 || abs(deltaY) > 50) {
                            isSwiping = true
                            if (abs(deltaX) > abs(deltaY)) {
                                // Swipe left or right
                                if (velocityX > 0) {
                                    onSwipeRight()
                                } else {
                                    onSwipeLeft()
                                }
                            } else {
                                // Swipe up or down
                                if (velocityY > 0) {
                                    onSwipeDown()
                                } else {
                                    onSwipeUp()
                                }
                            }
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    velocityTracker?.recycle()
                    velocityTracker = null
                    return true
                }
            }
            return false
        }

        private fun onSwipeRight() {
            // Handle swipe right event
        }

        private fun onSwipeLeft() {
            // Handle swipe left event
        }

        private fun onSwipeUp() {
            // Handle swipe up event
            if (binding.btnNext.isEnabled)
                binding.btnNext.performClick()
        }

        private fun onSwipeDown() {
            // Handle swipe down event
            if (binding.btnPrevious.isEnabled)
                binding.btnPrevious.performClick()
        }
    }

    override fun onItemClick(itemID: String,isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        when (itemID) {
            "0" -> {
                isHide = if (questionBankArray[position].status == "2") "0" else "1"
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