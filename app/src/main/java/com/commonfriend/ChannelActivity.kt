package com.commonfriend

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.custom.ImgurAttachmentFactory
import com.commonfriend.databinding.ActivityChannelBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CHANNEL_ID
import com.commonfriend.utils.CHAT_ID_FOR_NOTIFICATION
import com.commonfriend.utils.CID_FOR_NOTIFICATION
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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import kotlinx.coroutines.launch


class ChannelActivity : BaseActivity(), View.OnClickListener, ErrorDialogComponent.ErrorBottomSheetClickListener {
    
    lateinit var binding : ActivityChannelBinding
    var handler : Handler? = null
    var runnable : Runnable? = null

    private var profileId: String = ""
    private var isFirstMessageSend = false

    private var preStage: String = ""
    private var stage: String =
        "1" // 1 for introduced  2 for communicated 3 for in touch confirmed 4 for inTouchNotConfiremd
    private var oppositeUserGender: String = ""
    private var firstMessage: Boolean = false
    private var firstResponseMessage: Boolean = false
    private var matchStatus: String = ""
    private var opinionStatus: String = ""
    private var recommendationType: String = ""
    private var similarPercentage: String = ""
    private var commonQuestions: String = ""
    private var userStatus: String = ""

    private lateinit var chatViewModel: ChatViewModel
    private var chatId = ""
    private var cid = ""
    private var currentChannelId = ""
    private var isFrom = ActivityIsFrom.NORMAL
    private var callApiFirstTime = true
    private lateinit var chatClient : ChatClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }


    @SuppressLint("SetTextI18n")
    @OptIn(InternalStreamChatApi::class)
    private fun initialization(){

//        binding.clMainView.visibility = View.INVISIBLE
        allApiResponses()

//        chatClient = Util.getChatClient(this)

        if (ChatClient.isInitialized) {
            chatClient = ChatClient.instance()

        } else {
            Util.connectGetStreamUser(this) { isConnected, _ ->
                if (isConnected) {
                    chatClient = ChatClient.instance()
                    initialization()
                }
            }
            return
        }

        cid = checkNotNull(intent.getStringExtra(CID_KEY)) {

        }
        CID_FOR_NOTIFICATION = cid

        isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom

        Util.print("-------cid------- $cid --------------------")

        val factory = MessageListViewModelFactory(this, cid)

        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

        val imgurAttachmentViewFactory = ImgurAttachmentFactory()
        val attachmentViewFactory = AttachmentFactoryManager(listOf(imgurAttachmentViewFactory))
        binding.messageListView.setAttachmentFactoryManager(attachmentViewFactory)



        // handler for typing
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.txtTyping.gone()
            binding.txtGroupNames.visible()
        }


        // Send Message
        binding.btnSend.setOnClickListener {

            if (!Util.isEmptyText(binding.edtMessage)){

                // send message
                val message = Message( text = Util.getTextValue(binding.edtMessage) )
                binding.edtMessage.setText("")

                ChatClient.instance().channel(cid).sendMessage(message).enqueue { result ->
                    if (result.isSuccess) {
                        // val sentMessage: Message = result.data()
                        if (isFirstMessageSend)
                            callApi(2)

                        Util.print("MESSAGE SEND SUCCESSFULLY")
                    } else {
                        // Handle result.error()
                        Util.print("MESSAGE SEND FAILED")

                    }
                }

            }

        }



        var lastTypingEventTime = 0L
        val channelClient = chatClient.channel(cid)

        binding.edtMessage.observeTextChange {

            if (it.isNotEmpty()) {
                // Sends a typing.start event at most once every two seconds
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTypingEventTime >= 2000) {
                    channelClient.keystroke("self").enqueue()
                    lastTypingEventTime = System.currentTimeMillis()
                }
            } else {
                // Sends the typing.stop event
                chatClient.channel(cid).stopTyping().enqueue()
            }
        }


        // Receiving typing indicator events
        // Add typing start event handling
        channelClient.subscribeFor<TypingStartEvent> {
            // Handle event
            runOnUiThread {
                if (it.user.id != Pref.getStringValue(Pref.PREF_USER_ID,"").toString()){
                    binding.txtTyping.visible()
                    binding.txtGroupNames.gone()
                    binding.txtTyping.text = "${it.user.name} is typing..."
//                    binding.txtTyping.text = getString(R.string.typing)
                }
            }
        }

        // Add typing stop event handling
        channelClient.subscribeFor<TypingStopEvent> {
            // Handle event
            runOnUiThread {
                if (it.user.id != Pref.getStringValue(Pref.PREF_USER_ID,"").toString()) {
                    binding.txtTyping.gone()
                    binding.txtGroupNames.visible()
                }
            }
        }








        val textView = TextView(this).apply {
            text = ""
        }

        binding.messageListView.setEmptyStateView(
            view = textView,
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )








        messageListHeaderViewModel.channel.observe(this) { channel ->
//            val channelName = ChatUI.channelNameFormatter.formatChannelName(
//                channel = channel,
//                currentUser = ChatUI.currentUserProvider.getCurrentUser(),
//            )

            val commonFriendId = getString(R.string.common_friend_id)

//            val hasCommonFriend = channel.members.any { member -> member.user.id == commonFriendId }
            val hasCommonFriend = channel.getExtraValue("is_common_friend",false)

            val channelUser = Util.getChannelUser(this,channel.members)

            val channelProfile = channelUser.image
            val channelName = channelUser.name

            val firstName =
                if (channelName.contains(" ")) channelName.split(" ")[0] else channelName

            binding.txtChannelName.text =
                if (hasCommonFriend) "$firstName & You" else channelName
            currentChannelId = channel.id


            binding.txtUserInits.text = Util.getNameInitials(channelName)

            if (channelProfile.isNotEmpty()) {
                binding.imgChannelProfile.visible()
                binding.imgChannelProfile.setImageURI(channelProfile)
            } else {
                binding.imgChannelProfile.gone()
            }

            binding.imgCommonFriendProfile.visibleIf(hasCommonFriend)

            binding.llGroupNames.visibleIf(hasCommonFriend)
            binding.txtGroupNames.text = "$firstName, Common Friend and You"
            binding.txtGroupNames.visibleIf(hasCommonFriend)






            chatId = channel.getExtraValue("chat_id","")

            Util.print("------chatId-- $chatId -------currentChannelId--- $currentChannelId ----------------------")

            if (chatId.isNotEmpty()){
                if (callApiFirstTime)
                    callApi(1,true)
                callApiFirstTime = false
            }


            val members = channel.members
            val memberCount = members.size
            val currentUser: User? = ChatUI.currentUserProvider.getCurrentUser()

//            when{
//
//                memberCount == 2 && members.any { it.user.id == currentUser?.id } -> {
//                    val user = members.first { it.user.id != currentUser?.id }.user
//
//                    val profile = user.image
//                    profileId = user.id
//
//                    binding.imgProfile.setImageURI(profile)
//
//                }
//            }
        }



        messageListHeaderViewModel.bindView(binding.messageListHeaderView, this)
        messageListViewModel.bindView(binding.messageListView, this)
        messageComposerViewModel.bindView(binding.messageComposerView, this)

        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is MessageMode.MessageThread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                }

                is MessageMode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }

        // Step 4 - Let the message input know when we are editing a message
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }

        binding.messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }

        // Step 5 - Handle navigate up state
        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                finish()
            }
        }

        // Step 6 - Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }

//        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        onBackPressedDispatcher.addCallback(this) {
            backHandler()
        }

        setBackButtonClickListener(backHandler)


        // Custom typing info header bar
        val nobodyTyping = "nobody is typing"
//        binding.typingHeaderView.text = nobodyTyping

        val currentlyTyping = mutableSetOf<String>()

        // Observe typing events and update typing header depending on its state.
        chatClient
            .channel(cid)
            .subscribeFor(
                this, TypingStartEvent::class, TypingStopEvent::class, NewMessageEvent::class
            ) { event ->
                when (event) {
                    is TypingStartEvent -> {
                        currentlyTyping.add(event.user.name)
                    }
                    is TypingStopEvent -> {
                        currentlyTyping.remove(event.user.name)
                    }
                    is NewMessageEvent -> {

                        val senderId = event.message.user.id
                        val currentUser = Pref.getStringValue(Pref.PREF_USER_ID,"").toString()

                        if (isFirstMessageSend && (senderId == currentUser))
                            callApi(2)

                }
                    else -> {}
                }

//                binding.typingHeaderView.text = when {
//                    currentlyTyping.isNotEmpty() -> {
//                        currentlyTyping.joinToString(prefix = "typing: ")
//                    }
//                    else -> nobodyTyping
//                }
            }

//        binding.messageListView.setMessageLongClickListener{
//        }

        binding.messageListView.setUserClickListener {
            if (profileId.isNotEmpty() && it.id == profileId)
                openA<ProfileViewActivity> { putExtra(ID, profileId) }
        }

        binding.imgUnmatch.setOnClickListener(this)
        binding.llProfile.setOnClickListener(this)

        activityOnBackPressed(this,this) {
            onBackPress()
        }

    }

    private fun setBackButtonClickListener(listener: MessageListHeaderView.OnClickListener) {
        binding.imgBack.setOnClickListener {


            if (isFrom != ActivityIsFrom.CHAT_SCREEN) {
                startActivity(Intent(this, MainActivity::class.java).putExtra(DATA, 3))
                finish()
            } else {
                listener.onClick()
            }

        }
    }

    private fun onBackPress() {

        binding.imgBack.performClick()
    }


    companion object {
        private const val CID_KEY = "key:cid"

        fun newIntent(context: Context, cid: String,isFrom : ActivityIsFrom = ActivityIsFrom.NORMAL): Intent =
            Intent(context, ChannelActivity::class.java)
                .putExtra(CID_KEY, cid)
                .putExtra(IS_FROM, isFrom)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.imgUnmatch -> {

                if (profileId.isNotEmpty())
                    errorDialogComponent = ErrorDialogComponent(
                        this,
                        ErrorDialogComponent.ErrorDialogFor.REPORTING,
                        getString(R.string.confirmation),
                        getString(R.string.are_you_sure_you_want_to_unmatch),
                        this
                    ).apply { this.show() }
            }

            R.id.llProfile -> {
                if (profileId.isNotEmpty())
                    openA<ProfileViewActivity> { putExtra(ID, profileId) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CID_FOR_NOTIFICATION = cid
        Util.clearNotifications(this)
    }

    override fun onPause() {
        super.onPause()
        CID_FOR_NOTIFICATION = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        CID_FOR_NOTIFICATION = ""
    }

    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showProgress: Boolean = false) {
        if (Util.isOnline(this)) {
            if (showProgress) Util.showProgress(this)
            when (tag) {
                1 -> {
                    chatViewModel.chatDetailApiRequest(
                        this@ChannelActivity,
                        chatId,
                        "0"
                    )
                }
                2 -> {
                    chatViewModel.readFirstMessageApiRequest(
                        this@ChannelActivity,
                        chatId
                    )
                }

            }
        } else {
//            binding.btnSend.isClickable = true
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun allApiResponses() {
        chatViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[ChatViewModel::class.java]

        chatViewModel.chatDataResponse.observe(this) {
            Util.dismissProgress()

            if (it.data.isNotEmpty()) {

                if (it.data[0].userDetailsData.isNotEmpty()) {
                    setData(it.data[0].userDetailsData[0])
                }


            }
        }

        chatViewModel.readFirstMessageApiResponse.observe(this) {
            Util.dismissProgress()
            if (it.success == 1){
                isFirstMessageSend = false
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private fun setData(chatModel: ChatModel) {

        if (binding.clMainView.isInvisible)
            binding.clMainView.visibility = View.VISIBLE

        profileId = chatModel.userId
        isFirstMessageSend = chatModel.isFirstMessageSend == "1"

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


    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        if (itemID != "2") { // 0- Reject, 1-Reject and report, 2 - cancel
            openA<DiscardActivity> {
                putExtra(IS_FROM, DiscardIsFrom.CHAT)
                putExtra(ID, profileId)
                putExtra(CHANNEL_ID, currentChannelId)
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