package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.commonfriend.adapter.ReasonsAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityDiscardBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ReasonsModel
import com.commonfriend.utils.CHANNEL_ID
import com.commonfriend.utils.DATA
import com.commonfriend.utils.DiscardIsFrom
import com.commonfriend.utils.GENDER
import com.commonfriend.utils.HASH_SEPERATOR
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.STAGE
import com.commonfriend.utils.STATUS
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.ProfileViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class DiscardActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityDiscardBinding
    private lateinit var reasonsAdapter: ReasonsAdapter
    private var reasonList: ArrayList<ReasonsModel> = ArrayList()
    private var lastPos: Int = 0
    private var visibleView: Int = 1
    private lateinit var userViewModel: UserViewModel
    private lateinit var isFrom: DiscardIsFrom
    private lateinit var profileViewModel: ProfileViewModel
    private var profileId: String = ""
    private var status: String = ""
    private var reason: String = ""
    private var otherReason: String = ""
    private var currentWords = 0
    private var oppositeGender: String = ""
    private var oppositeSneakPeak: String = ""
    private var similarPercentage: String = ""
    private var commonQuestions: String = ""
    private var recommendationType: String = ""
    private var userStatus: String = ""
    private var matchStatus: String = ""
    private var stage: String = ""
    private var channelId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logForCurrentScreen(Screens.DISCARD_SCREEN.screenType, Screens.DISCARD_SCREEN.screenName)
        initialization()

    }

    private fun initialization() {

        if (intent.hasExtra(IS_FROM)) {
            isFrom = intent.getSerializableExtra(IS_FROM) as DiscardIsFrom
            profileId = intent.getStringExtra(ID).toString()
            status = intent.getStringExtra(STATUS).toString()
        }

        if (intent.hasExtra(GENDER)) {
            oppositeGender = intent.getStringExtra(GENDER).toString()
        }

        if (intent.hasExtra(CHANNEL_ID)) {
            channelId = intent.getStringExtra(CHANNEL_ID).toString()
        }
        if (intent.hasExtra(STAGE)) {
            stage = intent.getStringExtra(STAGE).toString()
            Util.print(">>>>>>>>>>>>>>>>>>>>>>>stage from the discard activity is>>>>>${stage}")
        }
        if (intent.hasExtra("SneakPeak")) {
            oppositeSneakPeak = intent.getStringExtra("SneakPeak").toString()
        }
        if (intent.hasExtra("match_status")) {
            matchStatus = intent.getStringExtra("match_status").toString()
        }
        if (intent.hasExtra("recommendation_type")) {
            recommendationType = intent.getStringExtra("recommendation_type").toString()
        }
        if (intent.hasExtra("user_status")) {
            userStatus = intent.getStringExtra("user_status").toString()
        }
        if (intent.hasExtra("similar_percentage")) {
            similarPercentage = intent.getStringExtra("similar_percentage").toString()
        }
        if (intent.hasExtra("common_questions")) {
            commonQuestions = intent.getStringExtra("common_questions").toString()
        }



        with(binding) {
            reasonsAdapter = ReasonsAdapter(this@DiscardActivity, this@DiscardActivity)
            rvReasons.layoutManager =
                LinearLayoutManager(this@DiscardActivity, LinearLayoutManager.VERTICAL, false)
            rvReasons.adapter = reasonsAdapter

            binding.txtMsgDesc.text = getString(
                if (oppositeGender.trim().lowercase() == "female")
                    R.string.why_did_you_reject_her
                else if (oppositeGender.trim().lowercase() == "male")
                    R.string.why_did_you_reject_him
                else
                    R.string.why_did_you_reject_them
            )

            btnDone.setOnClickListener(this@DiscardActivity)
            btnContinue.setOnClickListener(this@DiscardActivity)
            btnClose.setOnClickListener(this@DiscardActivity)
            btnDone.isClickable = false
            binding.edtOther.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.btnDone.setBackgroundResource(if (s!!.trim().isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                    binding.btnDone.isClickable = s.trim().isNotEmpty()
                }

                override fun afterTextChanged(s: Editable?) {
                    currentWords = s!!.toString().trim().replace("\n", " ").split(" ").size
                    binding.txtWordCount.text = "${100 - currentWords} words left"

                    if (currentWords > 100) {
                        s.delete(s.length - 1, s.length)
                    }
                }
            })
        }


        allApiResponses()
        callApi(1)
        setView()

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }


    private fun setView() {
        binding.rlFirstView.visibleIf(visibleView == 1)
        binding.rlSecondView.visibleIf(!binding.rlFirstView.isVisible)
        if (binding.rlSecondView.isVisible)
            binding.edtOther.requestFocus()

    }

    private fun onBackPress() {

        if (visibleView == 2) {
            Util.hideKeyBoard(this, binding.btnClose)
            visibleView = 1
            setView()
        } else {
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                if (reasonsAdapter.objList[lastPos].isOther == 1 && reasonsAdapter.objList[lastPos].isSelected == 0) {
                    visibleView = 2
                    setView()
                } else {
                    reasonsAdapter.objList[lastPos].isSelected =
                        if (reasonsAdapter.objList[lastPos].isSelected == 1) 0 else 1
                    reasonsAdapter.notifyDataSetChanged()
                    binding.btnContinue.isEnabled =
                        reasonsAdapter.objList.any { it.isSelected == 1 }
                }


            }

            R.id.btnDone -> {
                otherReason = Util.getTextValue(binding.edtOther)
                visibleView = 1
                setView()
                reasonsAdapter.objList[lastPos].isSelected = 1
                reasonsAdapter.notifyDataSetChanged()
                binding.btnContinue.isEnabled = reasonsAdapter.objList.any { it.isSelected == 1 }

            }

            R.id.btnContinue -> {

                reason = reasonsAdapter.objList.filter { it.isSelected == 1 && it.isOther == 0 }
                    .joinToString(HASH_SEPERATOR) { it.id }

                if (reasonsAdapter.objList.none { it.isSelected == 1 && it.isOther == 1 })
                    otherReason = ""

                callApi(2)

            }

            R.id.btnClose -> {
                onBackPress()
            }
        }
    }

    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this@DiscardActivity,
            ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[ProfileViewModel::class.java]

        userViewModel.getDiscardReasonApiResponse.observe(this@DiscardActivity) {
            Util.dismissProgress()
            if (it.success == 1) {
                reasonList = it.data
                reasonsAdapter.addData(reasonList)
            }
        }

        profileViewModel.saveProfileExchangeApiResponse.observe(this@DiscardActivity) {
            if (it.success == 1) {
                bundle = Bundle().apply {
                    putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
                    putString("request_to", profileId)
                    putString("gen_from_to", Util.genderInitalsForFirebase(oppositeGender))
                    putString("client_id", "")
                    putString("match_status", matchStatus)
                    putString("similar_percentage", similarPercentage)
                    putString("common_questions", commonQuestions)
                    putString("opinion_status", oppositeSneakPeak)
                    putString("recommendation_type", recommendationType)
                    putString("user_status", userStatus)
                    putString(
                        "reason",
                        reasonsAdapter.objList.filter { it.isSelected == 1 }
                            .joinToString("|") { it1 -> it1.reason })
                    putString(
                        "request_type", if (isFrom == DiscardIsFrom.CHAT) {
                            "afterChat|${
                                when (stage) {
                                    "2" -> "Communicated"
                                    "3" -> "InTouchNotConfirmed"
                                    "4" -> "InTouchConfirmed"
                                    else -> "Introduced"
                                }
                            }"
                        } else "afterUnlockProfile"
                    )
                }

                firebaseEventLog(if(status=="4") "unmatched_and_reported" else  "unmatched_profile", bundle)
            }


            Util.showLottieDialog(this, "done_lottie.json", wrapContent = true)
            {
                startActivity(
                    Intent(this, MainActivity::class.java).putExtra(
                        DATA,
                        if (isFrom == DiscardIsFrom.CHAT) 3 else 2
                    )
                )
                finish()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(this)) {
            Util.showProgress(this)
            when (tag) {
                1 -> {
                    userViewModel.getDiscardReasonApiRequest(
                        this@DiscardActivity,
                        profileId,
                        if (status == "4") "2" else "1"
                    ) // 1 for reject and 2 for reject and report
                }

                2 -> {  //1=accept, 2=reject, 3 = discard request, 4 = discard and Report
                    profileViewModel.saveProfileExchangeApiRequest(
                        this,
                        profileId,
                        status,
                        reason,
                        otherReason,
                        channelId
                    )
                }
            }

        } else {
            Util.showToastMessage(
                this@DiscardActivity,
                resources.getString(R.string.please_check_internet_connection),
                true
            )
        }
    }
}