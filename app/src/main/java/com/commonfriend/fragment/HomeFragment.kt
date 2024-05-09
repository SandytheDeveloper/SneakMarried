package com.commonfriend.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.commonfriend.*
import com.commonfriend.adapter.*
import com.commonfriend.databinding.DialogCastBinding
import com.commonfriend.databinding.FragmentHomeBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.*
import com.commonfriend.utils.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import org.json.JSONArray
import org.json.JSONObject


class HomeFragment() : Fragment(), View.OnClickListener,
    ChildEventListener {
    private lateinit var binding: FragmentHomeBinding
//    private lateinit var messageAdapter: MessageAdapter
    private lateinit var questionOfTheDayAdapter: QuestionOfTheDayAdapter
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var sneakPeakProfileList: SneakPeakProfileAdapter
    private lateinit var chatRequestAlertList: ChatRequestAlertAdapter
    private lateinit var matchesAdapter: MatchesAdapter
    var matchesArrayList : ArrayList<ProfileModel> = ArrayList()
    private var currentPos: Int = 0
    private var selectedAnsFor: Int = 0
    private var sneakPeakCurrentPos: Int = 0
    private lateinit var selectMobileNumberAdapter: SelectedOptionsListAdapter
    private lateinit var contactNumberAdapter: ContactNumberAdapter
    private var selectedContactList = ""
    private var currentWords = 0
    private var toResume: Boolean = true
    private lateinit var mainObj: JSONObject
    private lateinit var userViewModel: UserViewModel
    private lateinit var questionViewModel: QuestionViewModel
    private var allData: HomeModel = HomeModel()
    lateinit var dataBaseChats: DatabaseReference
    var lastPos = -1

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var bottomScreenBinding: DialogCastBinding

    private lateinit var channelListAdapter: ChannelListAdapter
    private var timer : CountDownTimer? = null
    private var appliedAccessCode = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        initialization()

        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            (context as MainActivity).get().topView.visibleIf(!binding.shimmerEffect.isVisible)
            callApi(1)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initialization() {

        (context as MainActivity).get().topView.gone()
        binding.shimmerEffect.startShimmer()
        binding.shimmerEffect.visible()
        Util.showProgress(requireContext())
        dataBaseChats = MainApplication.dataBaseRoot.child("chats")

        Util.checkNotificationPermission(requireActivity())

        binding.swipeRefresh.setOnRefreshListener {
            callApi(1)
        }

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(PUSH_NOTIFICATION))

        binding.rvChatRequestAlertList.addOnScrollListener(
            object : OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    currentPos = layoutManager.findLastCompletelyVisibleItemPosition()
                    activeDeActivePageIndicator()
                }


            })
        binding.rvSneakPeakList.addOnScrollListener(
            object : OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    sneakPeakCurrentPos = layoutManager.findLastCompletelyVisibleItemPosition()
                    activeDeActiveSneakPeakPageIndicator()
                }


            })



//        binding.rvMessage.layoutManager = LinearLayoutManager(requireContext())
//        messageAdapter = MessageAdapter(requireContext(), this)
//        binding.rvMessage.adapter = messageAdapter

        binding.rvMessage.layoutManager = LinearLayoutManager(requireContext())
        channelListAdapter = ChannelListAdapter(requireActivity(),true)
        binding.rvMessage.adapter = channelListAdapter


        binding.rvOpinionsList.layoutManager = LinearLayoutManager(requireContext())
        questionOfTheDayAdapter = QuestionOfTheDayAdapter(requireContext(), this)
        binding.rvOpinionsList.adapter = questionOfTheDayAdapter


        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        reminderAdapter = ReminderAdapter(requireContext(), this)
        binding.rvReminders.adapter = reminderAdapter

        binding.rvSneakPeakList.layoutManager =
            LinearLayoutManager(requireContext(), HORIZONTAL, false)
        sneakPeakProfileList = SneakPeakProfileAdapter(requireContext(), this)
        binding.rvSneakPeakList.adapter = sneakPeakProfileList

        binding.rvChatRequestAlertList.layoutManager =
            LinearLayoutManager(requireContext(), HORIZONTAL, false)
        chatRequestAlertList = ChatRequestAlertAdapter(requireContext(), this)
        binding.rvChatRequestAlertList.adapter = chatRequestAlertList


        matchesAdapter = MatchesAdapter(requireContext(), this)
        binding.viewPagerMatches.adapter = matchesAdapter

        binding.viewPagerMatches.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 2
        }

        //increase this offset to show more of left/right
        val offsetPx =
            resources.getDimension(com.intuit.sdp.R.dimen._30sdp).toInt().dpToPx(resources.displayMetrics)
        binding.viewPagerMatches.setPadding(0, 0, offsetPx, 0)

        //increase this offset to increase distance between 2 items
        val pageMarginPx =
            resources.getDimension(com.intuit.sdp.R.dimen._1sdp).toInt().dpToPx(resources.displayMetrics)
        val marginTransformer = MarginPageTransformer(pageMarginPx)
        binding.viewPagerMatches.setPageTransformer(marginTransformer)


        binding.viewPagerMatches.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                matchesAdapter.currentPosition = position
                matchesAdapter.showProgressAnimation = true
                matchesAdapter.onPageChange.value = position

            }
        })


        allApiResponses()

        binding.txtRecommendation.setOnClickListener(this)
        binding.btnReferenceChainContinue.setOnClickListener(this)
        binding.llReferenceView.setOnClickListener(this)
        binding.btnRefOkay.setOnClickListener(this)
        binding.llUnpauseSearchView.setOnClickListener(this)
        binding.imgContinue.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.btnEditProfile.setOnClickListener(this)
        binding.btnReviewEditProfile.setOnClickListener(this)
        binding.txtProfileUnlocked.setOnClickListener(this)
        binding.btnContribute.setOnClickListener(this)
        binding.btnSetCode.setOnClickListener(this)
        binding.btnRemoveAccessCode.setOnClickListener(this)
//        binding.btnFeedback.setOnClickListener(this)

//        binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
//            val editTextY = getRelativePosition(binding.edtNumber) + resources.getDimension(com.intuit.sdp.R.dimen._20sdp)
//            val feedBackY = getRelativePosition(binding.txtWordCount) //+ resources.getDimension(com.intuit.sdp.R.dimen._20sdp)
//
//            if (feedBackY < binding.nestedScrollView.height){
//                if (!binding.edtOther.hasFocus()) {
//                    binding.edtOther.requestFocus()
//                }
//            } else if (editTextY < binding.nestedScrollView.height){
//                if (!binding.edtNumber.hasFocus()) {
//                    binding.edtNumber.requestFocus()
//                }
//            } else {
//                binding.edtNumber.clearFocus()
//                binding.edtOther.clearFocus()
//            }
//        }


//        binding.edtFeedback.setOnClickListener {
//            binding.edtFeedback.requestFocus().apply {
//                binding.nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN)
//            }
//        }

        binding.edtFeedback.setOnTouchListener { view, event ->
            if (view.id == R.id.edtFeedback) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        binding.edtFeedback.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnDone.setBackgroundResource(if (s!!.trim().isNotEmpty()) R.drawable.dr_bg_btn else R.drawable.dr_bg_btn_light)
                binding.btnDone.isClickable = s.trim().isNotEmpty()
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                currentWords = s!!.toString().trim().replace("\n", " ").split(" ").size
                binding.txtWordCount.text = "${100 - currentWords} words left"

                if (currentWords > 100) {
                    s.delete(s.length - 1, s.length)
                }
            }
        })

        binding.btnDone.isClickable = false
//        setChatList()
    }


    /*private fun setChatList() {
        (requireContext() as MainActivity).liveChatList.observe(requireActivity()) { channels ->
            (requireContext() as MainActivity).runOnUiThread {

                val unreadChannelList = channels.filter { itt -> itt.hasUnread }

                (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(unreadChannelList.isNotEmpty())

                binding.llMessage.visibleIf(unreadChannelList.isNotEmpty())

                channelListAdapter.addData(unreadChannelList)

                Util.print("--2233----${unreadChannelList.size}------------------------------")

            }
        }
    }*/

//    private fun getRelativePosition(view: View): Int {
//        val location = IntArray(2)
//        view.getLocationInWindow(location)
//        return location[1]
//    }


    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            Util.print("-----BroadcastReceiver--------------------------------------------")
            callApi(1, context!!)
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NotifyDataSetChanged")
    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]

        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]
        userViewModel.homeDataResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            binding.swipeRefresh.isRefreshing = false
            if (it.data.isNotEmpty()) {

                if (!it.data[0].isUserExist){
                    Util.clearData(requireActivity())
                    return@observe
                }

                (context as MainActivity).get().topView.visible()
                binding.shimmerEffect.stopShimmer()
                binding.shimmerEffect.gone()


                allData = it.data[0]


                Pref.setStringValue(
                    Pref.PREF_USER_NAME,
                    it.data[0].userName
                )
                Pref.setStringValue(Pref.PREF_USER_DISPLAY_PICTURE, it.data[0].userProfilePic)


                Pref.setStringValue(Pref.PREF_AADHAR_VERIFIED, allData.isAadharVerified)
                Pref.setStringValue(Pref.PREF_DELETE_POPUP_TEXT, allData.deletePopupText)


                if (allData.ban.isEmpty())
                    allData.ban.add(BanModel())

                if (allData.review.isEmpty())
                    allData.review.add(HomeModel())

//                allData.ban[0].isBan = "0"
//                allData.review[0].underReviewScreen = "1"
//                allData.review[0].isUnderReview = "0"

                Pref.setStringValue(Pref.PREF_IS_ACCOUNT_BAN, allData.ban[0].isBan)
//                Pref.setStringValue(Pref.PREF_UNDER_REVIEW,allData.review[0].underReviewScreen,)

                if (allData.ban[0].isBan == "1") {
                    (requireActivity() as MainActivity).mListener.showLockedView(allData.ban[0])
//                } else if (allData.review[0].underReviewScreen == "1") {
//                    lockListerner.showUnderReviewView(allData.review[0])
                } else {
                    (requireActivity() as MainActivity).mListener.showNormalView()
                    setData()
                }
                Util.setHeaderProfile(context as MainActivity)

            }
        }

        userViewModel.getHomeDataResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            binding.swipeRefresh.isRefreshing = false
            if (it.data.isNotEmpty()) {
//                messageAdapter.addData(it.data[0].messageData)
//                binding.llMessage.visibility =
//                    if (it.data[0].messageData.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        userViewModel.referReadResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1) {
                binding.cvReferApp.visibility = View.GONE
            }
        }

        userViewModel.sendReminderResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1)
                callApi(1)
        }

        questionViewModel.referContactResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1) {

                binding.edtNumber.setText("")
                Util.showLottieDialog(requireContext(), "done_lottie.json", wrapContent = true)
            }
        }

        userViewModel.sendFeedbackApiResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1) {

                Util.showLottieDialog(requireContext(), "done_lottie.json", wrapContent = true)
            }
        }

        userViewModel.getStreamChatTokenApiResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.data.isNotEmpty()) {

                Pref.setStringValue(Pref.PREF_STREAM_CHAT_TOKEN,it.data[0].token)
                Util.connectGetStreamUser(requireContext()) {isComplete, viewModelFactory ->
                    if (isComplete)
                        setChatList(viewModelFactory!!)
                }
            }

        }


        userViewModel.pauseUnPauseSearchResponse.observe(viewLifecycleOwner) {
            Util.showLottieDialog(requireContext(), "done_lottie.json", wrapContent = true) {

                Pref.setStringValue(Pref.PREF_SEARCH_PAUSED, "0")
                binding.llUnpauseSearchView.gone()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        binding.swipeRefresh.visibility = View.VISIBLE

        (requireContext() as MainActivity).get().customFooter.binding.imgAccountDot.visibleIf(
            allData.profileDot == "1"
        )
//        (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(allData.chatDot == "1")

        matchesAdapter.addData(allData.matchesData)
//        messageAdapter.addData(allData.messageData)
        questionOfTheDayAdapter.addData(allData.opinionData)
        sneakPeakProfileList.addData(allData.sneakPeakData)
        reminderAdapter.addData(allData.reminderData)
        chatRequestAlertList.addData(allData.chatInterestReceived)


        if (allData.chatInterestReceived.size <= 1) {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 0)
            binding.rvChatRequestAlertList.layoutParams = layoutParams
        }

        binding.txtRecommendation.text =
            allData.weeklyRecommendation.ifEmpty { allData.newRecommendation }

        binding.txtProfileUnlocked.text =
            allData.profileUnlock.ifEmpty { allData.profileUnlock }

        allData.weeklyRecommendation.ifEmpty { allData.newRecommendation }
        binding.llRemmendations.visibility =
            if (allData.newRecommendation.isNotEmpty() || allData.weeklyRecommendation.isNotEmpty()) View.VISIBLE else View.GONE




        if (allData.referenceData.isNotEmpty()) {
            binding.cvRefernceChain.visibility = View.VISIBLE
            binding.txtReferenceTitle.text = allData.referenceData[0].title
            binding.txtRefernceChain.text = allData.referenceData[0].message
        }
        if (allData.referredNotification.isNotEmpty()) {
            binding.txtRefTitle.text = allData.referredNotification[0].title
            binding.cvReferApp.visibility = View.VISIBLE
            binding.txtRefName.text = allData.referredNotification[0].name
            binding.txtRefInfo.text = allData.referredNotification[0].message
        }


        Util.print("=======allData.isPause ::" + allData.isPause)
        Pref.setStringValue(Pref.PREF_SEARCH_PAUSED, allData.isPause)

        binding.llUnpauseSearchView.visibility =
            if (allData.isPause == "1") View.VISIBLE else View.GONE

        binding.llMatches.visibility =
            if (allData.matchesData.isNotEmpty()) View.VISIBLE else View.GONE

//        binding.llMessage.visibility =
//            if (allData.messageData.isNotEmpty()) View.VISIBLE else View.GONE

        binding.llOpinions.visibility =
            if (allData.opinionData.isNotEmpty()) View.VISIBLE else View.GONE
        binding.llSuggestions.visibility =
            if (allData.newRecommendation.isEmpty() && allData.weeklyRecommendation.isEmpty()) View.GONE else View.VISIBLE

        binding.llConnectionView.visibility =
            if (allData.referenceData.isEmpty() && allData.chatInterestReceived.isEmpty() && allData.sneakPeakData.isEmpty()) View.GONE else View.VISIBLE
        binding.llReminders.visibility =
            if (allData.reminderData.isNotEmpty()) View.VISIBLE else View.GONE
        binding.llSneakPeak.visibility =
            if (allData.sneakPeakData.isNotEmpty()) View.VISIBLE else View.GONE
        binding.llSneakPeakNoIndicator.visibility =
            if (allData.sneakPeakData.size > 1) View.VISIBLE else View.GONE
        binding.llChatNoIndicator.visibility =
            if (allData.chatInterestReceived.size > 1) View.VISIBLE else View.GONE

        binding.llProfileUnlocked.visibility =
            if (allData.profileUnlock.isNotEmpty()) View.VISIBLE else View.GONE

        binding.rvChatRequestAlertList.visibility =
            if (allData.chatInterestReceived.isNotEmpty()) View.VISIBLE else View.GONE

        if (chatRequestAlertList.objList.isNotEmpty()) {
            setPagerIndicator()
        }
        if (sneakPeakProfileList.objList.isNotEmpty()) {
            setSneakPeakPagerIndicator()
        }

        if (allData.lock.isNotEmpty()) {
            with(allData.lock[0]) {
                Pref.setStringValue(Pref.PREF_IS_ACCOUNT_LOCKED, allData.lock[0].isLock)
                binding.cvAccountLocked.visibleIf(isLock == "1")
                if (isLock == "1") {
                    binding.txtLockedTitle.text = title
                    binding.txtLockedDescription.text = description
                    binding.txtLockedSubDescription.text = subDescription
                    binding.txtLockedSubDescription2.text = subDescription2
                    binding.btnEditProfile.visibleIf(isUnderReview != "1")
//                    binding.txtUnderReview.visibleIf(isUnderReview == "1")
                }
            }
        }

        if (allData.review.isNotEmpty()) {
            with(allData.review[0]) {
                Pref.setStringValue(Pref.PREF_UNDER_REVIEW, underReviewScreen)
                binding.cvAccountUnderReview.visibleIf(underReviewScreen == "1")
                if (underReviewScreen == "1") {
                    binding.txtReviewTitle.text = title
                    binding.txtReviewDescription.text = description
                    binding.txtReviewSubDescription.text = subDescription
//                    binding.btnReviewEditProfile.visibleIf(isUnderReview != "1")


                    binding.edtAccessCode.hint = hintText.ifEmpty { getString(R.string.enter_early_access_code) }
                    appliedAccessCode = accessCode
                    setCodeView(accessCode)

                    // convert seconds in milliseconds
                    val remainingTimes = remainingTime.ifEmpty { "0" }.toLong() * 1000L

                    if (timer.isNotNull())
                        timer!!.cancel()

                    timer = object : CountDownTimer(remainingTimes, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val days = (millisUntilFinished / (1000 * 60 * 60 * 24)).toInt().toString()
                            val hours = ((millisUntilFinished / (1000 * 60 * 60)) % 24).toInt().toString()
                            val minutes = ((millisUntilFinished / (1000 * 60)) % 60).toInt().toString()
                            val seconds = ((millisUntilFinished / 1000) % 60).toString()

                            binding.txtTimerDays.text = days
                            binding.txtTimerHours.text = hours
                            binding.txtTimerMinutes.text = minutes
                            binding.txtTimerSeconds.text = seconds
                        }

                        override fun onFinish() {
                            callApi(1)
                        }
                    }

                    if (remainingTimes > 0) {
                        timer!!.start()
                    } else {
                        timer!!.cancel()
                    }

                    binding.llTimer.visibleIf(remainingTimes > 0)

                }
            }
        }



        if (Pref.getStringValue(Pref.PREF_STREAM_CHAT_TOKEN, "").toString().isEmpty())
            callApi(7) // getStreamChatTokenApiRequest
        else {
            Util.connectGetStreamUser(requireContext()) { isComplete, viewModelFactory ->

                if (isComplete)
                    setChatList(viewModelFactory!!)
                else
                    callApi(7) // getStreamChatTokenApiRequest
            }
        }


    }

    private fun setCodeView(accessCode : String) {
        binding.txtAccessCode.text = accessCode
        val isEditable : Boolean = accessCode.isEmpty()
        binding.llSelectedCode.apply {
            if (isEditable) invisible() else visible()
        }

        val color = ContextCompat.getColor(requireContext(),if (isEditable) R.color.color_white else R.color.color_light_blue_3)
        binding.edtAccessCode.apply {
            isEnabled = isEditable
            setHintTextColor(color)
        }
        binding.btnSetCode.apply {
            isEnabled = isEditable
            imageTintList = ColorStateList.valueOf(color)
        }
        binding.accessCodeBottomView.setBackgroundColor(color)
    }

    private fun setChatList(viewModelFactory : ChannelListViewModelFactory) {

        val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

        viewModel.state.observe(viewLifecycleOwner) { state ->

            Util.print("---HOME---In state-----------------")

            try {

                if (state.isLoading) {

                    Util.print("---HOME---state.isLoading-----------------")

                } else {

                    val unreadChannelList = state.channels.filter { itt -> itt.hasUnread }

                    (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(unreadChannelList.isNotEmpty())

                    binding.llMessage.visibleIf(unreadChannelList.isNotEmpty())

                    channelListAdapter.addData(unreadChannelList)

                }

            } catch (e:Exception){
                e.printStackTrace()
            }


        }

//        viewModel.bindView(binding.channelListView, this)
//        binding.channelListView.setChannelItemClickListener { channel ->
//            startActivity(ChannelActivity.newIntent(requireContext(), channel.cid,
//                ActivityIsFrom.CHAT_SCREEN))
//        }
    }

    /*private fun setChatList2(viewModelFactorys : ChannelListViewModelFactory?) {


        val userId = ChatClient.instance().getCurrentUser()?.id

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(userId!!))
        )


        val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)


        val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

        viewModel.state.observe(viewLifecycleOwner) { state ->

            Util.print("---HOME---In state-----------------")

            try {

                if (state.isLoading) {

                    Util.print("---HOME---state.isLoading-----------------")

                } else {

                    val unreadChannelList = state.channels.filter { itt -> itt.hasUnread }

                    (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(unreadChannelList.isNotEmpty())

                    binding.llMessage.visibleIf(unreadChannelList.isNotEmpty())

                    channelListAdapter.addData(unreadChannelList)

                }

            } catch (e:Exception){
                e.printStackTrace()
            }


        }

//        viewModel.bindView(binding.channelListView, this)
//        binding.channelListView.setChannelItemClickListener { channel ->
//            startActivity(ChannelActivity.newIntent(requireContext(), channel.cid,
//                ActivityIsFrom.CHAT_SCREEN))
//        }
    }*/


    /*private fun banChats() {
        handler = Handler(Looper.getMainLooper())
        val messageListCopy = ArrayList(messageAdapter.objList)
        runnable = Runnable {
            for (position in 0 until messageAdapter.itemCount) {
                if (position < messageAdapter.objList.size) {
                    val chatModel = messageListCopy[position]
                    // Check if the item has already been processed for removal
                    if (!chatModel.isRemoved) {
                        val viewHolder =
                            binding.rvMessage.findViewHolderForAdapterPosition(position) as? MessageAdapter.ViewHolder
                        viewHolder?.let { holder ->
                            // Access your views from the ViewHolder
                            val bannedLottie = holder.binding.bannedLottie
                            val banFilter = holder.binding.banFilter
                            val cvMain = holder.binding.cvMain

                            if (chatModel.isBanned == "1") {
                                bannedLottie.visible()
                                bannedLottie.setAnimation("banned_lottie.json")
                                bannedLottie.scaleType = ImageView.ScaleType.CENTER_CROP
                                bannedLottie.repeatCount = 0
                                bannedLottie.playAnimation()

                                val bganimator =
                                    ValueAnimator.ofArgb(initialColor, finalColor).apply {
                                        interpolator = AccelerateDecelerateInterpolator()
                                        duration = 1000L
                                        addUpdateListener { animation ->
                                            val color = animation.animatedValue as Int
                                            banFilter.visible()
                                            banFilter.setCardBackgroundColor(color)
                                        }

                                        addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationStart(animation: Animator) {
                                                // Color animation started
                                            }

                                            override fun onAnimationEnd(animation: Animator) {
                                                val initialTranslation = 0f
                                                val finalTranslation = cvMain.width.toFloat()
                                                cvMain.translationX = initialTranslation

                                                val translationAnimator = cvMain.animate()
                                                    .translationX(finalTranslation)
                                                    .setDuration(1000)

                                                translationAnimator.setListener(object :
                                                    Animator.AnimatorListener {
                                                    override fun onAnimationStart(animation: Animator) {
                                                        // Translation animation started
                                                    }

                                                    override fun onAnimationEnd(animation: Animator) {
                                                        handler?.post {
                                                            val originalPosition =
                                                                messageAdapter.objList.indexOf(
                                                                    chatModel
                                                                )
                                                            if (originalPosition != -1 && !chatModel.isRemoved) {
                                                                messageAdapter.objList[originalPosition].isRemoved =
                                                                    true
                                                                messageAdapter.objList.removeAt(
                                                                    position
                                                                )
                                                                messageAdapter.notifyItemRemoved(
                                                                    position
                                                                )
                                                                if (messageAdapter.objList.isEmpty()) {
//                                                                    binding.llMessage.gone()
                                                                }
                                                            }
                                                        }
                                                    }

                                                    override fun onAnimationCancel(animation: Animator) {
                                                        // Handle animation cancellation if needed
                                                    }

                                                    override fun onAnimationRepeat(animation: Animator) {
                                                        // Handle animation repeat if needed
                                                    }
                                                })

                                                translationAnimator.start()
                                            }

                                            override fun onAnimationCancel(animation: Animator) {
                                                // Color animation canceled
                                            }

                                            override fun onAnimationRepeat(animation: Animator) {
                                                // Color animation repeated
                                            }
                                        })
                                    }

                                bannedLottie.addAnimatorListener(object :
                                    Animator.AnimatorListener {
                                    override fun onAnimationStart(animation: Animator) {
                                        bganimator.start()
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        // Optional: Code to be executed when the animation ends
                                    }

                                    override fun onAnimationCancel(animation: Animator) {
                                        // Optional: Code to be executed when the animation is canceled
                                    }

                                    override fun onAnimationRepeat(animation: Animator) {
                                        // Optional: Code to be executed when the animation is repeated
                                    }
                                })
                            } else {
                                bannedLottie.gone()
                                banFilter.gone()
                                banFilter.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_white
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        handler?.postDelayed(runnable!!, 1000) // Delayed execution after 1 second


    }*/


    private fun setPagerIndicator() {
        binding.llChatNoIndicator.removeAllViews()
        for (i in chatRequestAlertList.objList.indices) {
            val imgView = ImageView(requireActivity())

            val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
            )
            param.setMargins(
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
            )


            imgView.id = i
            imgView.layoutParams = param
            imgView.setImageResource(if (currentPos == i) R.drawable.dr_bg_purple else R.drawable.dr_bg_lightgray)

            binding.llChatNoIndicator.addView(imgView)
        }
    }

    fun activeDeActivePageIndicator() {
        for (i in 0 until binding.llChatNoIndicator.childCount) {
            (binding.llChatNoIndicator.getChildAt(i) as ImageView).setImageResource(if (currentPos == i) R.drawable.dr_bg_purple else R.drawable.dr_bg_lightgray)
        }
    }

    private fun setSneakPeakPagerIndicator() {
        binding.llSneakPeakNoIndicator.removeAllViews()
        for (i in sneakPeakProfileList.objList.indices) {
            val imgView = ImageView(requireActivity())

            val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
            )
            param.setMargins(
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp),
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
            )


            imgView.id = i
            imgView.layoutParams = param
            imgView.setImageResource(if (sneakPeakCurrentPos == i) R.drawable.dr_bg_purple else R.drawable.dr_bg_lightgray)

            binding.llSneakPeakNoIndicator.addView(imgView)
        }
    }

    fun activeDeActiveSneakPeakPageIndicator() {
        for (i in 0 until binding.llSneakPeakNoIndicator.childCount) {
            //setPagerIndicator()
            (binding.llSneakPeakNoIndicator.getChildAt(i) as ImageView).setImageResource(if (sneakPeakCurrentPos == i) R.drawable.dr_bg_purple else R.drawable.dr_bg_lightgray)
        }
    }


    fun showButton(shouldShow: Boolean) {
        if (shouldShow) {
            bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn)
            bottomScreenBinding.btnDialogContinue.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.color_white
                )
            )
            bottomScreenBinding.btnDialogContinue.isClickable = true
            bottomScreenBinding.btnDialogContinue.setOnClickListener {
                addContacts()
                bottomSheetDialog!!.dismiss()
            }
        } else {


            bottomScreenBinding.btnDialogContinue.setBackgroundResource(R.drawable.dr_bg_btn_light)
            bottomScreenBinding.btnDialogContinue.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.color_white
                )
            )
            bottomScreenBinding.btnDialogContinue.isClickable = false
        }
    }

    private fun manageBottomSheetContinueButtonVisibility() {
        // bottom sheet dialog suggetion title visiblity

        bottomScreenBinding.rvCitySuggestion.visibility =
            if (selectMobileNumberAdapter.objList.isEmpty()) View.GONE else View.VISIBLE

        bottomScreenBinding.btnDialogContinue.alpha =
            if (selectMobileNumberAdapter.objList.isEmpty()) 0.5f else 1f
        bottomScreenBinding.btnDialogContinue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selectMobileNumberAdapter.objList.isEmpty()) R.color.color_white else R.color.color_white
            )
        )
        bottomScreenBinding.btnDialogContinue.isClickable =
            selectMobileNumberAdapter.objList.isNotEmpty()
    }


    private fun referMe() {

        if (Util.getTextValue(binding.edtNumber) != Pref.getStringValue(
                Pref.PREF_MOBILE_NUMBER,
                ""
            )
        ) {

            mainObj = JSONObject()
            val jsonArray = JSONArray()
            val dataObj = JSONObject()
            dataObj.put("name", "")
            dataObj.put("mobile_no", Util.getTextValue(binding.edtNumber))
            dataObj.put("country_code", "+91")
            jsonArray.put(dataObj)

            mainObj.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())

            mainObj.put("data", jsonArray.toString())
            callApi(3) // Refer Me

        } else {
            Util.showToastMessage(
                requireContext(),
                resources.getString(R.string.you_can_not_add_your_self),
                true
            )
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        when (view.id) {

            R.id.llUnpauseSearchView -> {
                callApi(8) // pauseUnPauseSearchApiRequest
            }

            R.id.btnViewProfile -> {
                lastPos = view.tag.toString().toInt()
                if (sneakPeakProfileList.objList[lastPos].isIntroduced == "1") {
                    startActivity(
                        Intent(requireContext(), ProfileViewActivity::class.java).putExtra(
                            ID, sneakPeakProfileList.objList[lastPos].userId
                        )
                    )
                } else {
                    (context as MainActivity).selectedPosition = 2
                    (context as MainActivity).changeFragment(
                        2,
                        sneakPeakProfileList.objList[lastPos].userId
                    )
                }
            }

            R.id.btnChatViewProfile -> {
                lastPos = view.tag.toString().toInt()
                (context as MainActivity).selectedPosition = 2
                (context as MainActivity).changeFragment(
                    2,
                    chatRequestAlertList.objList[lastPos].userId
                )
            }

            R.id.btnRefOkay -> {
                callApi(5)
            }

            R.id.txtRecommendation, R.id.txtProfileUnlocked -> {
                (activity as MainActivity).changeFragment(2)
            }

            R.id.btnReferenceChainContinue,
            R.id.llReferenceView -> { // ReferenceFound
                (activity as MainActivity).changeFragment(
                    if (allData.referenceData[0].forScreen == "1") 1 else 2,
                    allData.referenceData[0].userId
                )
            }

            R.id.btnEdit -> {
                lastPos = view.tag.toString().toInt()

            }

            R.id.imgContinue -> {
                if (Util.getTextValue(binding.edtNumber).length > 9) {
                    referMe()
                }
            }

            R.id.btnDone -> {
                if (!Util.isEmptyText(binding.edtFeedback))
                    callApi(6) // send Feedback
            }

            R.id.rlMain -> {
                lastPos = view.tag.toString().toInt()

                if (contactNumberAdapter.objList[lastPos].isSelected == 1) { // Already Select
                    contactNumberAdapter.objList[lastPos].isSelected = 0

                    val filterList =
                        selectMobileNumberAdapter.objList.filter { it.name == contactNumberAdapter.objList[lastPos].names }
                    if (filterList.isNotEmpty()) {
                        selectMobileNumberAdapter.objList.removeAt(
                            selectMobileNumberAdapter.objList.indexOf(
                                filterList[0]
                            )
                        )
                    }
                } else { // Not Select
                    contactNumberAdapter.objList[lastPos].isSelected = 1
                    val obj = CheckListModel()
                    obj.name = contactNumberAdapter.objList[lastPos].names
                    selectMobileNumberAdapter.objList.add(obj)
                    selectMobileNumberAdapter.notifyDataSetChanged()
                }

                contactNumberAdapter.notifyDataSetChanged()

                manageBottomSheetContinueButtonVisibility()
            }

            R.id.rlMainSuggestionView -> {
                lastPos = view.tag.toString().toInt()


                val list = contactNumberAdapter.objList.filter {
                    it.name.equals(selectMobileNumberAdapter.objList[lastPos].location, false)
                }

                if (list.isNotEmpty()) {
                    val index = contactNumberAdapter.objList.indexOf(list[0])
                    contactNumberAdapter.objList[index].isSelected = 1

                    val obj = CheckListModel()
                    obj.location = contactNumberAdapter.objList[index].name
                    selectMobileNumberAdapter.objList.add(obj)
                }

                bottomSheetDialog!!.dismiss()
                manageBottomSheetContinueButtonVisibility()
            }

            R.id.btnClose -> {
                val pos = view.tag.toString().toInt()
                contactNumberAdapter.objList.filter { it.name == selectMobileNumberAdapter.objList[pos].name }
                    .forEach { it.isSelected = 0 }
                selectMobileNumberAdapter.objList.removeAt(pos)
                selectMobileNumberAdapter.notifyDataSetChanged()
                contactNumberAdapter.notifyDataSetChanged()
            }


            R.id.btnCancel -> {
                lastPos = view.tag.toString().toInt()
                selectedAnsFor = 3
                callApi(4)
//                ConfirmationDialog(requireContext(), this@HomeFragment)
            }

            R.id.btnEditProfile, R.id.btnReviewEditProfile -> {
                startActivity(
                    Intent(requireContext(), EditProfileActivity::class.java)
                        .putExtra(IS_FROM, ActivityIsFrom.LOCKED_ACCOUNT)
                )
            }

            R.id.btnContribute -> {

                startActivity(Intent(requireContext(), ContributeActivity::class.java))

            }

            R.id.btnSetCode -> {

                val accessCode = Util.getTextValue(binding.edtAccessCode).uppercase()

                if (accessCode.isNotEmpty()) {
                    appliedAccessCode = accessCode
                    callApi(9) // set access Code
                    setCodeView(appliedAccessCode)
                    Util.showLottieDialog(requireContext(), "done_lottie.json",wrapContent = true)
                    binding.edtAccessCode.setText("")
                }

            }

            R.id.btnRemoveAccessCode -> {

                appliedAccessCode = ""
                callApi(9) // set access Code
                setCodeView(appliedAccessCode)

            }
        }

    }


    private fun addContacts() {
        val selectedContacts = contactNumberAdapter.objList.filter { it.isSelected == 1 }
        val candidatePhoneBookList: ArrayList<ContactModel> = ArrayList()
        var obj: ContactModel
        for (items in selectedContacts) {
            obj = ContactModel()
            obj.name = items.names
            obj.number = items.phone
            candidatePhoneBookList.add(obj)
        }
        val gson = Gson()
        selectedContactList = gson.toJson(candidatePhoneBookList)

        callApi(2)
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, context: Context = requireContext(),showProgress : Boolean = false) {
        if (Util.isOnline((context))) {
//            Util.showProgress(requireContext())
            when (tag) {
                1 -> {
                    if (showProgress) Util.showProgress(context)
                    userViewModel.homePageApiRequest(context)
                }

                2 -> {
                    userViewModel.getHomePageApiRequest(context)
                }

                3 -> {
                    Util.showProgress(context)
                    questionViewModel.referContactApi(context, mainObj)
                }

                5 -> {
                    userViewModel.readReferredApiRequest(
                        context,
                        allData.referredNotification[0].referId
                    )
                }

                6 -> {
                    Util.showProgress(context)
                    userViewModel.sendFeedbackApiRequest(
                        context,
                        binding.edtFeedback.text.toString()
                    )
                    binding.edtFeedback.setText("")
                }

                7 -> {
                    Util.showProgress(requireContext())
                    userViewModel.getStreamChatTokenApiRequest(requireContext())
                }

                8 -> {
                    userViewModel.pauseUnPauseSearchApiRequest(requireContext(), "0") //1=pause, 0=not pause
                }

                9 -> { // Apply Access Code
                    userViewModel.accessCodeApplyApiRequest(requireContext(), appliedAccessCode) //1=pause, 0=not pause
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        dataBaseChats.addChildEventListener(this)
        if (toResume) {
            callApi(1)
            LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(receiver, IntentFilter(PAUSE_SEARCH))
        }
    }


    override fun onPause() {
        super.onPause()
        dataBaseChats.removeEventListener(this)
        toResume = false
        if (binding.edtFeedback.hasFocus())
            binding.edtFeedback.clearFocus()
        if (binding.edtNumber.hasFocus())
            binding.edtNumber.clearFocus()
//        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(receiver)
    }

    override fun onStop() {
        super.onStop()
        toResume = true

    }

    override fun onDestroy() {
        super.onDestroy()
        dataBaseChats.removeEventListener(this)
        if (handler != null && runnable != null)
            handler?.removeCallbacks(runnable!!)
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(receiver)

    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        if (dataSnapshot.value != null) {

            val jsonObj = JSONObject(dataSnapshot.value.toString())

            if ((jsonObj.optString(ChatKeys.USER_ID.key) == Pref.getStringValue(
                    Pref.PREF_USER_ID,
                    ""
                ))
            ) {
                if ((jsonObj.optString(ChatKeys.IS_TYPING.key) != "1" && jsonObj.optString(
                        ChatKeys.STATUS.key
                    ) != "2")
                ) {
                    Util.print("-----------HOMEFRAGMENT----123123----------------------------------------------------------")
                    callApi(2)
                }
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

}