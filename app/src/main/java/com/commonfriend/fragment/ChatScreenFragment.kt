package com.commonfriend.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.ChannelActivity
import com.commonfriend.ChatDetailsActivity
import com.commonfriend.DiscardActivity
import com.commonfriend.MainActivity
import com.commonfriend.ProfileViewActivity
import com.commonfriend.R
import com.commonfriend.adapter.ChannelListAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.FragmentChatScreenBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CHANNEL_ID
import com.commonfriend.utils.DiscardIsFrom
import com.commonfriend.utils.GENDER
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.STAGE
import com.commonfriend.utils.STATUS
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.ChatViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView

class ChatScreenFragment : Fragment(),ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding : FragmentChatScreenBinding
    private lateinit var userViewModel: UserViewModel
    var errorDialogComponent: ErrorDialogComponent? = null
    private lateinit var chatViewModel: ChatViewModel

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
    var bundle: Bundle? = null
    private var retry = 0



    lateinit var client : ChatClient
    val userId = Pref.getStringValue(Pref.PREF_USER_ID,"").toString()
    private val userName = Pref.getStringValue(Pref.PREF_USER_NAME,"").toString()
    private val userProfilePic = Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE,"").toString()
    private var chatId = ""
    private var currentChannelId = ""
    private var userStreamChatToken = ""


    // adapter
    private lateinit var channelListAdapter: ChannelListAdapter



    val extraData = mapOf(
//        "name" to "One-on-One Chat",
        "chat_id" to "65c6202f34e69e0f7058ca32"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChatScreenBinding.inflate(layoutInflater,container,false)

        allApiResponses()
        initialization()

        return binding.root
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            (context as MainActivity).get().topView.visibleIf(!binding.shimmerEffect.isVisible)
            Util.clearNotifications(context as MainActivity)
            callApi(3)
        }
    }

    override fun onResume() {
        super.onResume()
        callApi(3)
    }

    @SuppressLint("NewApi")
    @OptIn(InternalStreamChatApi::class)
    private fun initialization() {
        (context as MainActivity).get().topView.gone()
        binding.shimmerEffect.startShimmer()
        binding.shimmerEffect.visible()
        binding.channelListView.gone()

        userStreamChatToken = Pref.getStringValue(Pref.PREF_STREAM_CHAT_TOKEN,"").toString()

        if (userStreamChatToken.isEmpty()) {
            callApi(1)
            return
        }


        channelListAdapter = ChannelListAdapter(requireActivity())
        binding.rvChannelList.adapter = channelListAdapter

        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = getString(R.string.notification_provider_name)))
        )


        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = requireContext())
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true
            ),
            appContext = requireContext()
        )


        client =
            if (ChatClient.isInitialized)
                ChatClient.instance()
            else
                ChatClient.Builder(getString(R.string.get_stream_api_key),requireContext().applicationContext)
                    .withPlugins(offlinePluginFactory, statePluginFactory)
                    .logLevel(ChatLogLevel.ALL)
                    .notifications(notificationConfig)
                    .build()


        val user = User(
            id = userId,
            name = userName,
            image = userProfilePic,
            invisible = true
        )

        client.connectUser(
            user = user,
            token = userStreamChatToken
        ).enqueue {
            if (it.isSuccess){
                val filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(user.id))
                )


                val viewModelFactory =
                    ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)

                val viewModel: ChannelListViewModel by viewModels { viewModelFactory }


                viewModel.state.observe(viewLifecycleOwner) { state ->

                    Util.print("------In state-----------------")


                    try {

                        (context as MainActivity).get().topView.visible()
                        binding.shimmerEffect.stopShimmer()
                        binding.shimmerEffect.gone()

                        if (state.isLoading) {

                            Util.print("------state.isLoading-----------------")

                        } else {

                            if (state.channels.isNotEmpty()){
                                binding.llNoChats.gone()
//                                binding.channelListView.visible()
                            } else {
                                binding.llNoChats.visible()
                                binding.channelListView.gone()
                            }


                            binding.channelListView.gone()
                            binding.rvChannelList.visible()


                            channelListAdapter.addData(state.channels)

                            val chatDot = if (state.channels.isNotEmpty()) {
                                state.channels.any { itt -> itt.hasUnread }
                            } else {
                                false
                            }
                            (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(chatDot)

                        }

                    } catch (e:Exception){
                        e.printStackTrace()
                    }


                }





                // Step 5 - Connect the ChannelListViewModel to the ChannelListView, loose
                //          coupling makes it easy to customize
                viewModel.bindView(binding.channelListView, this)
                binding.channelListView.setChannelItemClickListener { channel ->
                    startActivity(ChannelActivity.newIntent(requireContext(), channel.cid,
                        ActivityIsFrom.CHAT_SCREEN))
                }

                binding.channelListView.setChannelDeleteClickListener { channel ->

                    chatId = channel.getExtraValue("chat_id","")
                    currentChannelId = channel.id

                    if (chatId.isNotEmpty())
                        callApi(2)

                }

                binding.channelListView.setUserClickListener { user ->
                    requireContext().openA<ProfileViewActivity> { putExtra(ID, user.id) }
                }

            } else {
                Util.print( "something went wrong!")
                if (retry < 3) {
                    retry += 1
                    callApi(1)
                }
            }
        }


//        binding.btnCreate.setOnClickListener {
//            createChannel()
//        }


    }

    /*private fun initialization2() {
        (context as MainActivity).get().topView.gone()
        binding.shimmerEffect.startShimmer()
        binding.shimmerEffect.visible()
        binding.channelListView.gone()


        channelListAdapter = ChannelListAdapter(requireActivity())
        binding.rvChannelList.adapter = channelListAdapter

        (requireContext() as MainActivity).liveChatList.observe(requireActivity()) { channels ->

            try {

                (context as MainActivity).get().topView.visible()
                binding.shimmerEffect.stopShimmer()
                binding.shimmerEffect.gone()

                    if (channels.isNotEmpty()){
                        binding.llNoChats.gone()
                        binding.rvChannelList.visible()
                    } else {
                        binding.llNoChats.visible()
                        binding.rvChannelList.gone()
                    }


                    binding.channelListView.gone()


                    channelListAdapter.addData(channels)

                    val chatDot = if (channels.isNotEmpty()) {
                        channels.any { itt -> itt.hasUnread }
                    } else {
                        false
                    }
                    (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(chatDot)



            } catch (e:Exception){
                e.printStackTrace()
            }
        }

        binding.channelListView.setChannelItemClickListener { channel ->
            startActivity(ChannelActivity.newIntent(requireContext(), channel.cid,
                ActivityIsFrom.CHAT_SCREEN))
        }

        binding.channelListView.setChannelDeleteClickListener { channel ->

            chatId = channel.getExtraValue("chat_id","")
            currentChannelId = channel.id

            if (chatId.isNotEmpty())
                callApi(2)

        }

        binding.channelListView.setUserClickListener { user ->
            requireContext().openA<ProfileViewActivity> { putExtra(ID, user.id) }
        }

    }*/

    /*private fun createChannel() {

        // Creating a one-on-one channel with User A and User B
        client.createChannel(
            channelType = "messaging",
            channelId = "$userId-$user3",
            memberIds = listOf(userId, user3),
            extraData = extraData
        ).enqueue { result ->
            if (result.isSuccess) {
                Util.showToastMessage(requireContext(),"Channel Created",true)
            } else {
                Util.showToastMessage(requireContext(),"Error Channel not created",true)
                // Handle result.error()
            }
        }


    }*/







    private fun allApiResponses() {
        userViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiRepository())
        )[UserViewModel::class.java]

        chatViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[ChatViewModel::class.java]

        userViewModel.getStreamChatTokenApiResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.data.isNotEmpty()) {

                Pref.setStringValue(Pref.PREF_STREAM_CHAT_TOKEN,it.data[0].token)
                initialization()
            }

        }


        chatViewModel.chatDataResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()

            if (it.data.isNotEmpty()) {

                if (it.data[0].userDetailsData.isNotEmpty()) {
                    setData(it.data[0].userDetailsData[0])
                }


            }
        }


        chatViewModel.fetchChatDataResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1) {

                (requireContext() as MainActivity).get().customFooter.binding.imgAccountDot.visibleIf(
                    it.data[0].profileDot == "1"
                )

            }
        }

    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int) {
        if (Util.isOnline(requireContext())) {
            when (tag) {
                1 -> {
                    Util.showProgress(requireContext())
                    userViewModel.getStreamChatTokenApiRequest(requireContext())
                }

                2 -> {
                    Util.showProgress(requireContext())
                    chatViewModel.chatDetailApiRequest(
                        requireContext(),
                        chatId,
                        "0"
                    )
                }

                3 -> {
                    chatViewModel.fetchChatApiRequest(requireActivity())
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setData(chatModel: ChatModel) {

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
                        BaseActivity().firebaseEventLog("introduced", bundle)
                    }

                    "3", "4" -> {
                        BaseActivity().firebaseEventLog("intouch", bundle)
                    }

                    "2" -> {
                        BaseActivity().firebaseEventLog("communicated", bundle)
                    }
                }
            }
        }


        errorDialogComponent = ErrorDialogComponent(
            requireContext(),
            ErrorDialogComponent.ErrorDialogFor.REPORTING,
            getString(R.string.confirmation),
            getString(R.string.are_you_sure_you_want_to_unmatch),
            this
        ).apply { this.show() }

    }


    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        errorDialogComponent?.dismiss()
        if (itemID != "2") { // 0- Reject, 1-Reject and report, 2 - cancel
            requireContext().openA<DiscardActivity> {
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