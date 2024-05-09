package com.commonfriend

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.commonfriend.MainApplication.Companion.dataBaseRoot
import com.commonfriend.adapter.ChatDetailsAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.ActivityChatDetailsBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CHAT_ID_FOR_NOTIFICATION
import com.commonfriend.utils.ChatKeys
import com.commonfriend.utils.DATA
import com.commonfriend.utils.DiscardIsFrom
import com.commonfriend.utils.GENDER
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.STAGE
import com.commonfriend.utils.STATUS
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.gone
import com.commonfriend.utils.observeTextChange
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.ChatViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import org.json.JSONObject


class ChatDetailsActivity : BaseActivity(), View.OnClickListener, ChildEventListener,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: ActivityChatDetailsBinding
    private lateinit var chatDetailsAdapter: ChatDetailsAdapter
    private lateinit var chatViewModel: ChatViewModel
    private var chatId: String = ""
    private var profileId: String = ""
    private var oppositeUserGender: String = ""
    private var matchStatus: String = ""
    private var recommendationType: String = ""
    private var similarPercentage: String = ""
    private var commonQuestions: String = ""
    private var userStatus: String = ""
    private var opinionStatus: String = ""
    private var stage: String =
        "1" // 1 for introduced  2 for communicated 3 for in touch confirmed 4 for inTouchNotConfiremd
    private var preStage: String = ""
    private var firstMessage: Boolean = false
    private var firstResponseMessage: Boolean = false
    lateinit var dataBaseChats: DatabaseReference
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL
    private lateinit var layout: LinearLayoutManager
    private val userId = Pref.getStringValue(Pref.PREF_USER_ID, "")
    private var isViewInitialized = false
    private var profileFirstName = ""
    private var isCommonFriendIntro = ""
//    private var showAnimation = "0"
//    private var animationIsRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializations()
    }

    private fun initializations() {

        if (intent.hasExtra(ID)) chatId = intent.getStringExtra(ID).toString()
        CHAT_ID_FOR_NOTIFICATION = chatId

        if (intent.hasExtra(IS_FROM)) isFrom =
            intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        dataBaseChats = dataBaseRoot.child("chats")
        dataBaseChats.addChildEventListener(this)

        layout = LinearLayoutManager(this)
        layout.stackFromEnd = true
        binding.rvChatList.layoutManager = layout
        chatDetailsAdapter = ChatDetailsAdapter(this, this)
        binding.rvChatList.adapter = chatDetailsAdapter
        binding.btnSend.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
//        binding.imgOptions.setOnClickListener(this)
        binding.imgUnmatch.setOnClickListener(this)
        binding.llProfile.setOnClickListener(this)

        binding.nestedScrollView.setOnRefreshListener {
            binding.nestedScrollView.isRefreshing = true
            callApi(1, false)
        }


        runnable = Runnable {
            binding.txtGroupNames.visibleIf(isCommonFriendIntro == "1")
            binding.txtTyping.visibility = View.GONE
        }

        binding.edtMessage.observeTextChange {
            emit(isTyping = 1)
        }

        allApiResponses()
        callApi(1, true)

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    inner class ChatEditTextLayout(context: Context) : RelativeLayout(context) {

        var chatEditText: EditText = findViewById(R.id.edtMessage)
        var headerView: RelativeLayout = findViewById(R.id.rlHeaderView)

        init {
            chatEditText
            headerView
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)

            if (chatEditText.hasFocus()) {
                headerView.translationY = (-headerView.height).toFloat()
            } else {
                headerView.translationY = 0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CHAT_ID_FOR_NOTIFICATION = chatId
        Util.clearNotifications(this)
    }


    @SuppressLint("SetTextI18n")
    private fun setData(chatModel: ChatModel) {

        if (binding.llNames.isInvisible)
            binding.llNames.visibility = View.VISIBLE

        isCommonFriendIntro = chatModel.isCommonFriend

        profileFirstName =
            if (chatModel.name.contains(" ")) chatModel.name.split(" ")[0] else chatModel.name
        binding.txtSenderName.text =
            if (chatModel.isCommonFriend == "1") "$profileFirstName & You" else chatModel.name

        binding.txtTyping.gone()
        binding.txtGroupNames.visibleIf(chatModel.isCommonFriend == "1")
        binding.txtGroupNames.text = "$profileFirstName, Common Friend and You"

        profileId = chatModel.userId

//        if (chatModel.isCommonFriend == "1") binding.imgProfile.setImageResource(R.drawable.ic_chat_face)
//        else {
//            if (chatModel.senderProfilePic.isNotEmpty()) {
//                binding.imgProfile.setImageURI(chatModel.senderProfilePic)
//                binding.txtUserInits.visibility = View.INVISIBLE
//            } else {
//                binding.txtUserInits.visibility = View.VISIBLE
//                binding.txtUserInits.text = Util.getNameInitials(chatModel.name)
//            }
//        }

        binding.imgCommonFriendProfile.visibleIf(chatModel.isCommonFriend == "1")

        if (chatModel.senderProfilePic.isNotEmpty()) {
            binding.imgProfile.setImageURI(chatModel.senderProfilePic)
            binding.txtUserInits.visibility = View.INVISIBLE
        } else {
            binding.txtUserInits.visibility = View.VISIBLE
            binding.txtUserInits.text = Util.getNameInitials(chatModel.name)
        }


        if (chatModel.showKeyboard == "1") {
            if (binding.llBottomView.isGone) {
                binding.llBottomView.visible()

                // Create a TranslateY animator
                val translateAnimator = ObjectAnimator.ofFloat(
                    binding.llBottomView,
                    View.TRANSLATION_Y,
                    binding.llBottomView.height.toFloat(),
                    0f
                )
                translateAnimator.duration = 500 // Set the animation duration in milliseconds
                translateAnimator.start() // Start the animation
            }
        } else {
            binding.llBottomView.gone()
        }

        preStage = stage
        stage = chatModel.stage
        oppositeUserGender = chatModel.gender
        firstMessage = chatModel.isFirstMessage == "1"
        firstResponseMessage = chatModel.isFirstResponseMessage == "1"
        matchStatus = chatModel.matchStatus
        opinionStatus = chatModel.sneakPeakStatus
        recommendationType = chatModel.recommendationType
        similarPercentage = chatModel.similarAnswer
        commonQuestions = chatModel.commonQuestions
        userStatus = chatModel.userStatus

        if (preStage != stage) {
            if (stage == "1" || stage == "2" || stage == "3" || stage == "4") {
                bundle = Bundle().apply {
                    putString("gen_from_to", Util.genderInitalsForFirebase(oppositeUserGender))
                    putString(
                        "participated_ids",
                        "${Pref.getStringValue(Pref.PREF_USER_ID, "")}|${chatModel.userId}"
                    )
                    putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                    putString("user_status", userStatus)

                    if (stage == "3" || stage == "4") {
                        putString("intouch_confirmed", if (stage == "3") "Yes" else "No")
                    }

                    putString("candidate_gender", oppositeUserGender)
                }

                when (stage) {
                    "1" -> {
                        firebaseEventLog("introduced", bundle)
                    }

                    "3", "4" -> {
                        firebaseEventLog("intouch", bundle)
                    }

                    "2" -> {
                        firebaseEventLog("communicated", bundle)
                    }
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun allApiResponses() {
        chatViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[ChatViewModel::class.java]

        chatViewModel.chatDataResponse.observe(this) {
            Util.dismissProgress()
            binding.nestedScrollView.isRefreshing = false
            if (it.data.isNotEmpty()) {
//                if (showAnimation == "0") {

                binding.llMainView.visibility = View.VISIBLE
                isViewInitialized = true

                if (it.data[0].userDetailsData.isNotEmpty()) {
                    setData(it.data[0].userDetailsData[0])
                    chatDetailsAdapter.userDetailArray = it.data[0].userDetailsData[0]
                }
                if (it.data[0].chatDetailsData.isNotEmpty()) {
                    emit(status = 2)

//                        showAnimation =
//                            if (it.data[0].chatDetailsData.any { iit -> iit.systemMessage == 1 && iit.showAnimation == "1" })
//                                "1" else "0"
//
//                        if (showAnimation == "1") {
//
//                            startAnimation(it.data[0].chatDetailsData)
//                            binding.nestedScrollView.isEnabled = false
//
//                        } else {

                    if (chatDetailsAdapter.objList.isEmpty()) {
                        chatDetailsAdapter.addData(it.data[0].chatDetailsData, true)
                        smoothScrollToPosition()
                    } else chatDetailsAdapter.addMoreItemsAtStart(it.data[0].chatDetailsData)

//                        }


                    if (firstMessage || firstResponseMessage) {

                        if (firstMessage && profileId == Pref.getStringValue(
                                Pref.PREF_USER_ID, ""
                            ).toString()
                        ) {
                            firebaseEventLog("first_msg_receive", logBundle())
                        } else if (firstResponseMessage && profileId == Pref.getStringValue(
                                Pref.PREF_USER_ID, ""
                            ).toString()
                        ) {
                            firebaseEventLog("second_msg_receive", logBundle())
                        }
                    }
                }
//                }
            }
        }

        chatViewModel.getChatDataResponse.observe(this) {
            Util.dismissProgress()
            if (it.data.isNotEmpty()) {
                if (!isViewInitialized)
                    return@observe

                if (it.data[0].userDetailsData.isNotEmpty()) {
                    setData(it.data[0].userDetailsData[0])
                    chatDetailsAdapter.userDetailArray = it.data[0].userDetailsData[0]
                }
                if (it.data[0].chatDetailsData.isNotEmpty()) {
                    emit(status = 2)

                    var run = true
                    var pos = 0
                    while (run) {
                        if (pos < it.data[0].chatDetailsData.size) {
                            with(it.data[0].chatDetailsData[pos]) {
                                Util.print("--------Response------$pos-------------------------------------------------")
                                if (chatDetailsAdapter.objList.any { it.messageId == messageId }) {
                                    pos++
                                } else {
                                    run = false
                                    val list = it.data[0].chatDetailsData.subList(
                                        pos, it.data[0].chatDetailsData.size
                                    )
                                    chatDetailsAdapter.objList.addAll(list)
                                    chatDetailsAdapter.notifyDataSetChanged()

                                    if (getRecyclerViewVisiblePosition() > (chatDetailsAdapter.objList.size - 10)) {
                                        smoothScrollToPosition()
                                    } else ""
                                }
                            }
                        } else run = false
                    }


                    if (firstMessage && profileId != Pref.getStringValue(
                            Pref.PREF_USER_ID, ""
                        ).toString()
                    ) {
                        firebaseEventLog("first_msg_send", logBundle())
                    } else if (firstResponseMessage && profileId != Pref.getStringValue(
                            Pref.PREF_USER_ID, ""
                        ).toString()
                    ) {
                        firebaseEventLog("second_msg_send", logBundle())
                    }
                }
            }
        }


        chatViewModel.sendChatDataResponse.observe(this) {
//            Util.dismissProgress()
//            binding.btnSend.isClickable = true
            emit()
//            binding.edtMessage.setText("")
            binding.edtMessage.isFocusable = true
            binding.edtMessage.requestFocus()
//            callApi(1,false)
        }
    }

    /*@SuppressLint("NotifyDataSetChanged")
    private fun startAnimation(model: ArrayList<ChatModel>, index: Int = 0) {

        if (index < model.size) {
            animationIsRunning = true

            binding.txtTyping.text =
                StringBuilder().append(getString(R.string.common_friend_is_typing)).toString()
            binding.txtTyping.visible()
            binding.llBottomView.gone()

            if (model[index].systemMessage == 1) {

                if (model[index].showAnimation == "1") {

                    Handler(Looper.getMainLooper()).postDelayed({

                        addMessage(model, index)

                    }, (model[index].message.length * 55).coerceAtMost(3000).toLong())

                } else {

                    addMessage(model, index)

                }

            } else {

                addMessage(model, index)

            }

        } else {
            animationIsRunning = false

            binding.txtTyping.gone()
            binding.llBottomView.visible()
            binding.nestedScrollView.isEnabled = true

            // Create a TranslateY animator
            val translateAnimator = ObjectAnimator.ofFloat(
                binding.llBottomView,
                View.TRANSLATION_Y,
                binding.llBottomView.height.toFloat(),
                0f
            )
            translateAnimator.duration = 500 // Set the animation duration in milliseconds
            translateAnimator.start() // Start the animation
            callApi(4,false)
        }

    }*/

    /*private fun addMessage(model: ArrayList<ChatModel>, index: Int) {

        model[index].showAnimation = "0"
//        SHOW_ANIM[index] = "0"
        chatDetailsAdapter.objList.add(model[index])
        if (index > 0)
            chatDetailsAdapter.notifyItemChanged(index - 1)
        chatDetailsAdapter.notifyItemInserted(index)
//                chatDetailsAdapter.notifyDataSetChanged()
        smoothScrollToPosition()

        startAnimation(model, index + 1)
    }*/

    private fun logBundle(): Bundle {
        return Bundle().apply {
            putString(
                "user_id", Pref.getStringValue(Pref.PREF_USER_ID, "")
            )
            putString(
                "gen_from_to", Util.genderInitalsForFirebase(oppositeUserGender)
            )
            putString(
                "participated_ids", "${
                    Pref.getStringValue(
                        Pref.PREF_USER_ID, ""
                    )
                }|${profileId}"
            )
            putString(
                "user_id", Pref.getStringValue(Pref.PREF_USER_ID, "")
            )
            putString("user_status", userStatus)
//            putString("gender", oppositeUserGender)
        }

    }

    private fun getRecyclerViewVisiblePosition(): Int {

        val firstPosition = layout.findFirstVisibleItemPosition()
        val lastPosition = layout.findLastVisibleItemPosition()

        return lastPosition
    }

    private fun smoothScrollToPosition() {
        runOnUiThread {
            chatDetailsAdapter.let {
                if (it.itemCount > 0) binding.rvChatList.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            if (showProgress) Util.showProgress(this)
            when (tag) {
                1 -> {
                    chatViewModel.chatDetailApiRequest(
                        this@ChatDetailsActivity,
                        chatId,
                        if (chatDetailsAdapter.objList.isEmpty()) "0" else chatDetailsAdapter.objList[0].messageId
                    )
                }

                2 -> {
                    chatViewModel.sendMessageApiRequest(
                        this@ChatDetailsActivity,
                        intent.getStringExtra(ID).toString(),
                        Util.getTextValue(binding.edtMessage)
                    )
                    binding.edtMessage.setText("")
                    smoothScrollToPosition()
                }

                3 -> {
                    chatViewModel.readChatApiRequest(
                        intent.getStringExtra(ID).toString()
                    )
                }

                4 -> {
                    chatViewModel.getChatDetailApiRequest(
                        this@ChatDetailsActivity, chatId, "0"
                    )
                }
            }
        } else {
//            binding.btnSend.isClickable = true
        }
    }


    /*private fun popUpDialog(view: View) {
        // Create an instance of PopupWindow
        val customChatPopupBinding: CustomChatPopupBinding =
            CustomChatPopupBinding.inflate(layoutInflater)
        val popupView: View = customChatPopupBinding.root

        val popupWindow = PopupWindow(
            popupView,
            resources.getDimension(com.intuit.sdp.R.dimen._210sdp).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background drawable for the popup window
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        customChatPopupBinding.btnUnMatch.setOnClickListener {
            popupWindow.dismiss()
            errorDialogComponent = ErrorDialogComponent(
                this,
                ErrorDialogComponent.ErrorDialogFor.REPORTING,
                "Confirmation",
                "Are you sure you want to unmatch?",
                this
            ).apply { this.show() }
        }
        customChatPopupBinding.btnViewProfile.setOnClickListener {
            popupWindow.dismiss()
            openA<ProfileViewActivity> { putExtra(ID, profileId) }

        }

        customChatPopupBinding.rlClose.setOnClickListener {
            popupWindow.dismiss()
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val offsetX = location[0]
        val offsetY = location[1]

        // Show the popup window at the clicked location
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, offsetX, offsetY)
    }*/


    override fun onClick(view: View) {
        when (view.id) {

//            R.id.imgOptions -> {
//                popUpDialog(view)
//            }

            R.id.imgUnmatch -> {
                errorDialogComponent = ErrorDialogComponent(
                    this,
                    ErrorDialogComponent.ErrorDialogFor.REPORTING,
                    getString(R.string.confirmation),
                    getString(R.string.are_you_sure_you_want_to_unmatch),
                    this
                ).apply { this.show() }
            }

            R.id.btnSend -> {

                if (Util.getTextValue(binding.edtMessage).isNotEmpty()) {
                    callApi(2)
                }
            }

            R.id.imgBack -> {
                onBackPress()
            }

            R.id.llProfile -> {
                openA<ProfileViewActivity> { putExtra(ID, profileId) }
            }

        }
    }

    private fun onBackPress() {
        callApi(3)
        if (isFrom != ActivityIsFrom.CHAT_SCREEN) {
            startActivity(Intent(this, MainActivity::class.java).putExtra(DATA, 3))
        }
        finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        CHAT_ID_FOR_NOTIFICATION = ""
        dataBaseChats.removeEventListener(this)
    }


    private fun emit(isTyping: Int = 0, status: Int = 1) {

        Util.print("--------emit-----1111--------------------------------------")

        val newMessage: DatabaseReference = dataBaseChats.push()
        val jsonObject = JSONObject()
        jsonObject.put(ChatKeys.USER_ID.key, chatDetailsAdapter.userDetailArray.userId)
        jsonObject.put(ChatKeys.SENDER_ID.key, userId)
        jsonObject.put(ChatKeys.STATUS.key, status)
        jsonObject.put(ChatKeys.IS_TYPING.key, isTyping)
        jsonObject.put(ChatKeys.CHAT_ID.key, intent.getStringExtra(ID).toString())
        newMessage.setValue(jsonObject.toString())
        dataBaseRoot.removeValue()


    }


    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        if (dataSnapshot.value != null) {

            val jsonObj = JSONObject(dataSnapshot.value.toString())

            if (jsonObj.optString(ChatKeys.CHAT_ID.key) == intent.getStringExtra(ID).toString()) {

//                if (!animationIsRunning) {

                    if (jsonObj.optString(ChatKeys.IS_TYPING.key) == "1" && (jsonObj.optString(
                            ChatKeys.SENDER_ID.key
                        ) != userId)
                    ) {

                        binding.txtTyping.text =
                            if (isCommonFriendIntro == "1") {
                                StringBuilder().append(
                                    if (jsonObj.optString(ChatKeys.IS_COMMON_FRIEND.key) == "1") getString(
                                        R.string.common_friend
                                    ) else profileFirstName
                                ).append(" is typing...").toString()
                            } else {
                                StringBuilder().append("typing...").toString()
                            }

                        binding.txtGroupNames.visibility = View.GONE
                        binding.txtTyping.visibility = View.VISIBLE
                        if (handler != null && runnable != null) handler!!.removeCallbacks(runnable!!)

                        handler = Handler(Looper.myLooper()!!)

                        handler!!.postDelayed(runnable!!, 2000)

                    } else if (jsonObj.optString(ChatKeys.STATUS.key) == "2" && (jsonObj.optString(
                            ChatKeys.SENDER_ID.key
                        ) != userId)
                    ) {
                        for (data in chatDetailsAdapter.objList.filter { it.status == 1 }) {
                            val index = chatDetailsAdapter.objList.indexOf(data)
                            if (index == -1) continue
                            chatDetailsAdapter.objList[index].status = 2
                            chatDetailsAdapter.notifyItemChanged(index)
                        }
                    } else if ((jsonObj.optString(ChatKeys.IS_TYPING.key) != "1" && jsonObj.optString(
                            ChatKeys.STATUS.key
                        ) != "2")
                    ) {

                        if (jsonObj.optString(ChatKeys.SENDER_ID.key) == profileId) {
                            emit(status = 2)
                            dataBaseRoot.removeValue()
                        }
//                        binding.txtTyping.visibility = View.GONE
                        callApi(4, false)

                    }


                    /////////////////////////////

//                if (jsonObj.optString("status") == "2" && Pref.getStringValue(Pref.PREF_USER_ID, "")
//                        .toString() != jsonObj.optString("sender_id")
//                ) {
//                    for (data in chatDetailsAdapter.objList.filter { it.status == 1 }) {
//                        val index = chatDetailsAdapter.objList.indexOf(data)
//                        if (index == -1)
//                            continue
//                        chatDetailsAdapter.objList[index].status = 2
//                        chatDetailsAdapter.notifyItemChanged(index)
//                    }
//
//                } else if (jsonObj.optString("is_typing") == "1" && (jsonObj.optString("sender_id") != Pref.getStringValue(
//                        Pref.PREF_USER_ID,
//                        ""
//                    ).toString())
//                ) {
//                    binding.txtTyping.text =
//                        StringBuilder().append(jsonObj.optString("sender_name"))
//                            .append(" is typing...").toString()
//                    binding.txtTyping.visibility = View.VISIBLE
//                    if (handler != null && runnable != null)
//                        handler!!.removeCallbacks(runnable!!)
//
//                    handler = Handler(Looper.myLooper()!!)
//
//                    handler!!.postDelayed(runnable!!, 500)
//                } else if (jsonObj.optString("is_typing") != "1" && jsonObj.optString("status") != "2") {
//
//
//                    if (Pref.getStringValue(Pref.PREF_USER_ID, "")
//                            .toString() != jsonObj.optString("sender_id")
//                    ) {
//                        emit(status = 2)
//                        dataBaseRoot.removeValue()
//                    }
//                    binding.txtTyping.visibility = View.GONE
//                    chatDetailsAdapter.objList.add(
//                        Gson().fromJson(
//                            dataSnapshot.value.toString(),
//                            ChatModel::class.java
//                        )
//                    )
//                    chatDetailsAdapter.notifyDataSetChanged()
//                    if (chatDetailsAdapter.objList.isNotEmpty())
//                        smoothScrollToPosition()
//
//
//                }

//                }
            }


        }
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {

    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
    }

    override fun onCancelled(error: DatabaseError) {
    }

    override fun onPause() {
        super.onPause()
        CHAT_ID_FOR_NOTIFICATION = ""
        if (handler != null && runnable != null) handler!!.removeCallbacks(runnable!!)
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        if (itemID != "2") { // 0- Reject, 1-Reject and report, 2 - cancel
            openA<DiscardActivity> {
                putExtra(IS_FROM, DiscardIsFrom.CHAT)
                putExtra(ID, profileId)
                putExtra(GENDER, oppositeUserGender)
                putExtra(STAGE, stage)
                putExtra(STATUS, if (itemID == "0") "3" else "4")
                putExtra("SneakPeak", opinionStatus)
                putExtra("match_status", matchStatus)
                putExtra("recommendation_type", recommendationType)
                putExtra("similar_percentage", similarPercentage)
                putExtra("common_questions", commonQuestions)
                putExtra("user_status", userStatus)
            }
        }
    }


}