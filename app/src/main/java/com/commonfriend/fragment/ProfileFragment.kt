package com.commonfriend.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.commonfriend.ChannelActivity
import com.commonfriend.DiscardActivity
import com.commonfriend.EditProfileActivity
import com.commonfriend.MainActivity
import com.commonfriend.PhotoAlbumActivity
import com.commonfriend.R
import com.commonfriend.SneakPeekActivity
import com.commonfriend.adapter.FilterAdapter
import com.commonfriend.adapter.ProfilePagerAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.FragmentProfileBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.DiscardIsFrom
import com.commonfriend.utils.GENDER
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.MAIN_PROFILE_LIST
import com.commonfriend.utils.Pref
import com.commonfriend.utils.SELECTED_FILTER
import com.commonfriend.utils.STATUS
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNotNull
import com.commonfriend.utils.isNull
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.ProfileViewModel
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class ProfileFragment :
    Fragment(), View.OnClickListener, RemoveProfileFromListInterface,
    ErrorDialogComponent.ErrorBottomSheetClickListener {

    private lateinit var binding: FragmentProfileBinding
    private var profilePagerAdapter: ProfilePagerAdapter? = null
    var profileArrayList: ArrayList<ProfileModel> = ArrayList()
    private var mainProfileArrayList: ArrayList<ProfileModel> = ArrayList()
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private var chatIntroduction: String = ""
    private var currentPosition = 0
    private var status = "3"
    private lateinit var filterAdapter: FilterAdapter
    private var referenceNumber = ""
    private var fromCandidateId = ""
    private var toCandidateId = ""
    private var firstTime = false
    private var userId = ""
    var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isFirstRequest: Boolean = false

//    private var isHiddenView = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        initialization()
        (requireActivity() as BaseActivity).logForCurrentScreen(
            Screens.PROFILE_SCREEN.screenType, Screens.PROFILE_SCREEN.screenName
        )
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
//        isHiddenView = hidden
//        if (!hidden){
//            (context as MainActivity).get().rvFilter.visibility = View.VISIBLE
//            (context as MainActivity).get().llProgress.visibility = View.VISIBLE
//            setData()
//            callApi(1, false)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initialization() {

        userId = (requireActivity() as MainActivity).profileId


        if (userId.isNotEmpty()) SELECTED_FILTER = ""

        (context as MainActivity).get().rvFilter.visibility = View.VISIBLE
        (context as MainActivity).get().llProgress.visibility = View.VISIBLE

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                profilePagerAdapter!!.currentPosition = position

                if (profileArrayList.isEmpty())
                    return

                setProgressBar(position, profileArrayList.size)
                setProfilePicture(profileArrayList[position].isProfileLocked == "1")

                callApi(2, false)
                Util.print("------onPageSelected---------$position----------------------------------------------")

                profilePagerAdapter!!.showProgressAnimation = true
                profilePagerAdapter!!.onPageChange.value = 1
//                profilePagerAdapter!!.notifyDataSetChanged()

                /*
                                if (profilePagerAdapter!!.objList[position].isBanned == "1") {
                                    showBannedDialog()
                                }*/

                /*if (profilePagerAdapter!!.objList[position].isProfileLocked == "1") {
                    showBannedDialog()
                }*/


            }
        })

        filterAdapter = FilterAdapter(requireContext(), this)
        (context as MainActivity).get().rvFilter.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        (context as MainActivity).get().rvFilter.adapter = filterAdapter
        allApiResponses()

//        if (MAIN_PROFILE_LIST.isEmpty()) {
        (context as MainActivity).get().topView.gone()
        binding.shimmerEffect.startShimmer()
        binding.shimmerEffect.visible()
//        } else {
//            setData()
//        }

    }

    override fun onResume() {
        super.onResume()
        if (profileArrayList.isNotEmpty() && firstTime)
            if (profileArrayList[0].isOpinions == "1")
                callApi(1, false)
            else
                callApi(6, false)
        else
            callApi(1, false)

        firstTime = true

        if (MAIN_PROFILE_LIST.isNotEmpty())
            setData()

    }

    override fun onPause() {
        super.onPause()
        if (profilePagerAdapter.isNotNull()) profilePagerAdapter!!.showProgressAnimation = false
    }

    fun setProgressBar(position: Int, size: Int) {

        (context as MainActivity).get().llProgress.removeAllViews()
        (context as MainActivity).get().llProgress.weightSum = size.toFloat()

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        params.setMargins(3, 0, 3, 0)
        params.weight = 1.0f

        for (i in 0 until size) {
            val rlView = RelativeLayout(requireActivity())
            rlView.layoutParams = params

            // FOR PROGRESS LINE
            val viewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
            viewParams.setMargins(0, 4, 0, 4)
            val view = View(requireActivity())
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    if (i <= position) R.color.color_black else R.color.color_light_grey
                )
            )
            view.layoutParams = viewParams
            rlView.addView(view)

            // FOR RED DOT
            if (profileArrayList.isNotEmpty()) {
                if (profileArrayList[i].statusDot == "1") {
                    val imgParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    val newImageView = ImageView(requireActivity())
                    newImageView.setImageResource(R.drawable.ic_red_circle)
                    newImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    newImageView.layoutParams = imgParams
                    rlView.addView(newImageView)
                }
            }

            (context as MainActivity).get().llProgress.addView(rlView)

            if (profileArrayList.isNotEmpty() && position < profileArrayList.size) {
                profileArrayList[position].apply {
                    statusDot = "0"
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged", "Recycle")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.floatingBtn, R.id.txtChatButton -> {
                if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "6") //if request received
                {
                    if (isFirstRequest) { // if true then ask for chat intro

                        askForChatIntroduction()

                    } else {
                        status = "1" // accept
                        callApi(4) // SaveExchangeAPI
                    }
                } else if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "3") { // send request

                    // send direct request to User
                    callApi(3) // send request (profile being send)

                } else if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "1") { // send request

                    if (profileArrayList[binding.viewPager.currentItem].chatId.isNotEmpty()) { // Redirect to Chat

//                        requireContext().openA<ChatDetailsActivity> {
//                            putExtra(ID, profileArrayList[binding.viewPager.currentItem].chatId)
//                            putExtra(IS_FROM, ActivityIsFrom.PROFILE)
//                        }

                    } else { // send direct request to User
                        if (isFirstRequest) { // if true then ask for chat intro

                            askForChatIntroduction()

                        } else {
                            callApi(3) // send request (chat request is being send)
                        }
                    }

                } else if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "2" || profileArrayList[binding.viewPager.currentItem].exchangeButton == "7") {

                    when (profileArrayList[binding.viewPager.currentItem].isPendingQuestions) {
                        //is_pending_questions  -  0 = nothing is pending, 1 = edit profile, 2 = Question bank pending, 3 = album pending
                        "1" -> {
                            (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                                requireContext(),
                                ErrorDialogComponent.ErrorDialogFor.COMPLETE_PROFILE,
                                getString(R.string.incomplete_profile),
                                getString(R.string.there_is_some_information_that_is_required),
                                this
                            ).apply {
                                this.show()
                            }

                        } // on Boarding pending
                        "2" -> {
                            (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                                requireContext(),
                                ErrorDialogComponent.ErrorDialogFor.QUESTIONS,
                                getString(R.string.share_your_opinions),
                                getString(R.string.answer_a_minimum_of) + " ${profileArrayList[0].questionLeft} " + getString(
                                    R.string.questions_to_start_connecting_and_receiving_requests
                                ),
                                this
                            ).apply {
                                this.show()
                            }
                            
                        }
                        "3" -> {
                            (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                                requireContext(),
                                ErrorDialogComponent.ErrorDialogFor.PHOTO_ALBUM_PENDING,
                                getString(R.string.photo_album_incomplete),
                                getString(R.string.your_photo_album_is_incomplete_upload),
                                this
                            ).apply {
                                this.show()
                            }

                        } // Album pending
                        /*"3" -> {
                            (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(requireContext(),ErrorDialogComponent.ErrorDialogFor.CONFIRMING,"Are you sure?","",this).apply {
                                this.show()
                            }*//*
                            startActivity(Intent(
                                requireContext(),
                                PhotoAlbumActivity::class.java)
                           .putExtra(IS_FROM, ActivityIsFrom.PROFILE))*//*
                        }*/ // both pending
                    }

                }
            }

            R.id.imgCancel -> {
                if (profileArrayList[binding.viewPager.currentItem].isProfileLocked == "1") {

                    (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                        requireContext(),
                        ErrorDialogComponent.ErrorDialogFor.CONFIRMING,
                        getString(R.string.confirmation),
                        getString(R.string.are_you_sure),
                        this
                    ).apply {
                        this.show()
                    }

                } else {
                    val exchangeButton =
                        profileArrayList[binding.viewPager.currentItem].exchangeButton
                    requireContext().openA<DiscardActivity> {
                        putExtra(IS_FROM, DiscardIsFrom.PROFILE)
                        putExtra(ID, profileArrayList[binding.viewPager.currentItem].id)
                        putExtra(
                            "SneakPeak",
                            profileArrayList[binding.viewPager.currentItem].sneakPeakStatus
                        )
                        putExtra(
                            "match_status",
                            profileArrayList[binding.viewPager.currentItem].statusText
                        )
                        putExtra(
                            "recommendation_type",
                            if (profileArrayList[binding.viewPager.currentItem].isProfileLocked == "1") "Locked" else "Unlocked"
                        )
                        putExtra(
                            "similar_percentage",
                            profileArrayList[binding.viewPager.currentItem].similarAnswer
                        )
                        putExtra(
                            "common_questions",
                            profileArrayList[binding.viewPager.currentItem].commonQuestions
                        )
                        putExtra(
                            "user_status",
                            profileArrayList[binding.viewPager.currentItem].userStatus
                        )
                        putExtra(
                            GENDER, profileArrayList[binding.viewPager.currentItem].gender
                        )
                        putExtra(
                            STATUS, if (exchangeButton == "6" || exchangeButton == "7") "2" else "3"
                        )
                    }
                }
            }

            R.id.txtOpinionsEnable -> {
                requireContext().openA<SneakPeekActivity> {
                    putExtra(IS_FROM, ActivityIsFrom.PROFILE)
                }
            }
            
            R.id.imgViewSneakPeak -> {

                callApi(7, false)
                startActivity(
                    Intent(context, SneakPeekActivity::class.java).putExtra(
                        IS_FROM, ActivityIsFrom.SNEAK_PEAK
                    ).putExtra(ID, profileArrayList[binding.viewPager.currentItem].id).putExtra(
                        DATA, profileArrayList[binding.viewPager.currentItem].profilePic
                    )
                )

//                (context as MainActivity).overridePendingTransition(
//                    R.anim.enter_zoom_activity_anim, R.anim.exit_zoom_activity_anim
//                )

//                }
            }/*R.id.llLikes -> {
                showCustomPopup(view)
            }*//*R.id.imgNameQuestionMark -> {
                startActivity(
                    Intent(requireActivity(), FaqsActivity::class.java)
                        .putExtra(
                            IS_FROM,
                            FaqIsFrom.PROFILE_LOCKED
                        ))
            }*//*R.id.imgOpinionsQuestionMark-> {
                startActivity(
                    Intent(requireActivity(), FaqsActivity::class.java)
                        .putExtra(
                            IS_FROM,
                            FaqIsFrom.OPINIONS_AND_INTEREST
                        ))
            }*//*R.id.imgReferenceQuestionMark-> {

                startActivity(Intent(requireActivity(), FaqsActivity::class.java)
                    .putExtra(
                        IS_FROM,
                        FaqIsFrom.REFERENCE))
            }*//*R.id.txtReferenceView -> {

                startActivity(Intent(requireContext(),ReferenceScreenActivity::class.java)
                    .putExtra(ID,profileArrayList[binding.viewPager.currentItem].id)
                    .putExtra(DATA,profileArrayList[binding.viewPager.currentItem].commonReference))

//                referenceBottomSheetDialog(1)
//                if (bottomSheetDialog!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
//                    bottomSheetDialog!!.behavior.peekHeight =
//                        Resources.getSystem().displayMetrics.heightPixels;
//                    bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                }
            }*/

            R.id.llMain -> {

                val pos = view.tag.toString().toInt()
                binding.viewPager.visibility = View.INVISIBLE
                filterAdapter.objList.forEach { it.isSelected = 0 }
                filterAdapter.objList[pos].isSelected = 1
                filterAdapter.notifyDataSetChanged()
                SELECTED_FILTER = filterAdapter.objList[pos].name
                binding.viewPager.currentItem = 0
                setProfileData(refresh = true)
//                callApi(1,true)
            }
        }
    }

    private fun askForChatIntroduction() {

        (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
            requireContext(),
            ErrorDialogComponent.ErrorDialogFor.CHAT_INTRODUCTION,
            getString(R.string.prefer_to_get_introduced),
            getString(R.string.do_you_want_me_to_introduce_you_over),
            this
        ).apply {
            this.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setProfileData(refresh : Boolean = false) {
        if (mainProfileArrayList.isEmpty()) binding.viewPager.gone()
        profileArrayList.clear()
        removeHandlers()

        when (SELECTED_FILTER.lowercase()) {
            getString(R.string.filter_all) -> profileArrayList.addAll(mainProfileArrayList)
            getString(R.string.filter_new) -> profileArrayList.addAll(mainProfileArrayList.filter {
                it.statusText.lowercase() == getString(
                    R.string.filter_new
                )
            })

            getString(R.string.filter_sent) -> profileArrayList.addAll(mainProfileArrayList.filter {
                it.statusText.lowercase() == getString(
                    R.string.filter_request_sent
                )
            })

            getString(R.string.filter_received_) -> profileArrayList.addAll(mainProfileArrayList.filter {
                it.statusText.lowercase() == getString(
                    R.string.filter_request_received
                )
            })

            getString(R.string.filter_unlocked) -> profileArrayList.addAll(mainProfileArrayList.filter { it.isProfileLocked == "0" })
            getString(R.string.filter_locked) -> profileArrayList.addAll(mainProfileArrayList.filter { it.isProfileLocked == "1" })
            getString(R.string.filter_introduced) -> profileArrayList.addAll(mainProfileArrayList.filter {
                it.statusText.lowercase() == getString(
                    R.string.filter_introduced
                )
            })

            getString(R.string.filter_missed) -> profileArrayList.addAll(mainProfileArrayList.filter {
                it.statusText.lowercase() == getString(
                    R.string.filter_missed
                )
            })

            else -> profileArrayList.addAll(mainProfileArrayList)
        }

        (context as MainActivity).get().llProgress.visibleIf(profileArrayList.isNotEmpty())

        if (profileArrayList.isNotEmpty()) { // Profile Data

            if (!binding.viewPager.isVisible) {
                handler = Handler(Looper.getMainLooper())
                runnable = Runnable {
                    binding.viewPager.visibility = View.VISIBLE
                    profilePagerAdapter!!.notifyDataSetChanged()
                }
                handler?.postDelayed(runnable!!, 500) // Delayed execution after 1 second

            }

            binding.llNoProfiles.visibility = View.GONE

            if (profilePagerAdapter.isNull() || refresh) {
                profilePagerAdapter = ProfilePagerAdapter(requireContext(), this, this)
                profilePagerAdapter!!.ALBUM_SCREEN_TYPE = MAIN_PROFILE_LIST[0].albumScreenType
                profilePagerAdapter!!.addData(profileArrayList)
                binding.viewPager.adapter = profilePagerAdapter
//                binding.viewPager.offscreenPageLimit = 10
            } else {
                profilePagerAdapter!!.ALBUM_SCREEN_TYPE = MAIN_PROFILE_LIST[0].albumScreenType
                profilePagerAdapter!!.addData(profileArrayList)
            }

            setProgressBar(binding.viewPager.currentItem, profileArrayList.size)

            setProfilePicture(profileArrayList[0].isProfileLocked == "1")

            val pos =
                if (userId.isNotEmpty() && profileArrayList.any { it.id == userId }) profileArrayList.indexOf(
                    profileArrayList.find { it.id == userId }) else currentPosition


            binding.viewPager.post {
                binding.viewPager.currentItem = pos
                profilePagerAdapter!!.notifyDataSetChanged()

//                (context as MainActivity).get().topView.visible()
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    binding.shimmerEffect.stopShimmer()
//                    binding.shimmerEffect.gone()
//                }, 500)
            }

            userId = ""
//            if (profileArrayList.isNotEmpty())
//                callApi(2, false) // Profile View
        } else {
            binding.llNoProfiles.visibility = View.VISIBLE
            binding.viewPager.visibility = View.INVISIBLE
//            setProgressBar(-1, 1)
        }
    }

    private fun setData() {

        (context as MainActivity).get().topView.visible()
        binding.shimmerEffect.stopShimmer()
        binding.shimmerEffect.gone()

        (requireContext() as MainActivity).get().customFooter.binding.imgAccountDot.visibleIf(
            MAIN_PROFILE_LIST[0].profileDot == "1"
        )
//        (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(
//            MAIN_PROFILE_LIST[0].chatDot == "1"
//        )

        isFirstRequest = MAIN_PROFILE_LIST[0].isFirstRequest == "1"

        if (MAIN_PROFILE_LIST[0].filterData.isNotEmpty()) { // FilterData
            filterAdapter.addData(MAIN_PROFILE_LIST[0].filterData)
            if (SELECTED_FILTER.isNotEmpty()) {
                filterAdapter.objList.filter { itt -> itt.name == SELECTED_FILTER }
                    .forEach { itt -> itt.isSelected = 1 }
                (context as MainActivity).get().rvFilter.scrollToPosition(filterAdapter.objList.indexOfFirst { itt -> itt.isSelected == 1 })
            } else {
                filterAdapter.objList[0].isSelected = 1
            }
        }
        mainProfileArrayList.clear()
        mainProfileArrayList.addAll(MAIN_PROFILE_LIST[0].profileData)
        setProfileData()

        if (MAIN_PROFILE_LIST[0].profileData.size > 1 && Pref.getStringValue(
                Pref.PREF_PROFILE_FIRST_TIME_DAILOG, "1"
            ) == "1"
        ) {
            (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                requireContext(),
                ErrorDialogComponent.ErrorDialogFor.SURETY,
                getString(R.string.discovering_profiles),
                getString(R.string.swipe_left_or_right_to_discovery_profiles_swiping_does_not_accept_or_reject_the_profile),
                this
            ).apply {
                this.show()
            }
            Pref.setStringValue(Pref.PREF_PROFILE_FIRST_TIME_DAILOG, "0")
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.M)
    fun allApiResponses() {
        profileViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[ProfileViewModel::class.java]

        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]


        userViewModel.chatIntroSettingApiResponse.observe(requireActivity()) {
            if (it.success == 1 && it.data.isNotEmpty()) {
                isFirstRequest = false
                Pref.setBooleanValue(Pref.PREF_CHAT_INTRODUCTION,it.data[0].chatIntroduction == "1")
            }
        }

        profileViewModel.profileListApiResponse.observe(viewLifecycleOwner) {

            if (it.success == 1 && it.data.isNotEmpty()) {

                MAIN_PROFILE_LIST = it.data

                setData()

            }

        }

        profileViewModel.singleProfileListApiResponse.observe(viewLifecycleOwner) {

            if (it.success == 1 && it.data.isNotEmpty()) {

                val model = it.data[0].profileData[0]

                val index =
                    MAIN_PROFILE_LIST[0].profileData.indexOfFirst { itt -> itt.id == model.id }

                profileArrayList[binding.viewPager.currentItem] = model

                if (index != -1) {
                    MAIN_PROFILE_LIST[0].profileData[index] = model
                    mainProfileArrayList.clear()
                    mainProfileArrayList.addAll(MAIN_PROFILE_LIST[0].profileData)
                }

                val position = binding.viewPager.currentItem
                profilePagerAdapter!!.objList[position] = model
                profilePagerAdapter!!.notifyDataSetChanged()

                setProfilePicture(model.isProfileLocked == "1")

            }
        }

        profileViewModel.readExchangedProfileResponse.observe(viewLifecycleOwner) {
            Util.print(it.msg)
            if (it.success == 1 && it.data.isNotEmpty()) {
                if (it.data[0].isBan == "1") {
                    (requireActivity() as MainActivity ).mListener.showLockedView(it.data[0])
                }
            }
        }


        profileViewModel.sendProfileExchangeApiResponse.observe(viewLifecycleOwner) {
            if (it.success == 1) {

                (context as BaseActivity).let { activity ->
                    if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "1") {
                        activity.bundle = Bundle().apply {
                            putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                            putString("request_to", profileArrayList[currentPosition].id)
                            putString(
                                "gen_from_to",
                                Util.genderInitalsForFirebase(profilePagerAdapter!!.objList[currentPosition].gender)
                            )
                            putString("match_status", profileArrayList[currentPosition].statusText)
                            putString(
                                "similar_percentage",
                                profileArrayList[currentPosition].similarAnswer
                            )
                            putString(
                                "common_questions",
                                profileArrayList[currentPosition].commonQuestions
                            )
                            putString(
                                "opinion_status", profileArrayList[currentPosition].sneakPeakStatus
                            )
                            putString(
                                "user_status", profileArrayList[currentPosition].userStatus
                            )
                            putString(
                                "recommendation_type",
                                if (profileArrayList[currentPosition].isProfileLocked == "1") "Locked" else "Unlocked"
                            )
                            putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        }
                        activity.firebaseEventLog("chat_req_sent", activity.bundle)
                    } else if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "3") {
                        activity.bundle = Bundle().apply {
                            putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                            putString("request_to", profileArrayList[currentPosition].id)
                            putString(
                                "gen_from_to",
                                Util.genderInitalsForFirebase(profilePagerAdapter!!.objList[currentPosition].gender)
                            )
                            putString("match_status", profileArrayList[currentPosition].statusText)
                            putString(
                                "similar_percentage",
                                profileArrayList[currentPosition].similarAnswer
                            )
                            putString(
                                "common_questions",
                                profileArrayList[currentPosition].commonQuestions
                            )
                            putString(
                                "opinion_status", profileArrayList[currentPosition].sneakPeakStatus
                            )
                            putString(
                                "user_status", profileArrayList[currentPosition].userStatus
                            )
                            putString(
                                "recommendation_type",
                                if (profileArrayList[currentPosition].isProfileLocked == "1") "Locked" else "Unlocked"
                            )
                            putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        }
                        activity.firebaseEventLog("unlock_profile_reqt_sent", activity.bundle)
                    }
                }

                if (!it.data.isNullOrEmpty()) {
                    if (it.data[0].showError == "1") {

                        (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                            requireContext(),
                            ErrorDialogComponent.ErrorDialogFor.SURETY_WITH_CLICK,
                            it.data[0].title,
                            it.data[0].description,
                            this
                        ).apply {
                            this.show()
                        }
                        return@observe
                    }
                }

                Util.showLottieDialog(
                    requireContext(),
                    if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "1") "chat_request_sent_lottie.json"
                    else "profile_unlocked_lottie.json",
                    soundType = 1,
                    wrapContent = true,
                    isBlackBackground = 1
                )

//                callApi(1)
                callApi(6)
            }
        }

        profileViewModel.saveProfileExchangeApiResponse.observe(viewLifecycleOwner) {
            if (it.success == 1) {
                (context as BaseActivity).let { activity ->

                    if (profileArrayList.isEmpty())
                        return@observe

                    activity.bundle = Bundle().apply {
                        putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("request_to", profileArrayList[currentPosition].id)
                        putString(
                            "gen_from_to",
                            Util.genderInitalsForFirebase(profileArrayList[currentPosition].gender)
                        )
                        putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("match_status", profileArrayList[currentPosition].statusText)
                        putString(
                            "similar_percentage", profileArrayList[currentPosition].similarAnswer
                        )
                        putString(
                            "common_questions", profileArrayList[currentPosition].commonQuestions
                        )
                        putString(
                            "opinion_status", profileArrayList[currentPosition].sneakPeakStatus
                        )
                        putString(
                            "user_status", profileArrayList[currentPosition].userStatus
                        )
                        putString(
                            "recommendation_type",
                            if (profileArrayList[currentPosition].isProfileLocked == "1") "Locked" else "Unlocked"
                        )
                        putString(
                            "request_type",
                            if (profileArrayList[currentPosition].exchangeButton == "3") "recommendations" else "chat"
                        )
                    }
                    activity.firebaseEventLog(
                        if (profileArrayList[currentPosition].exchangeButton == "3") "unmatched_profile" else "request_accepted",
                        activity.bundle
                    )
                }

                if (!it.data.isNullOrEmpty()) {
                    if (it.data[0].showError == "1") {

                        (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                            requireContext(),
                            ErrorDialogComponent.ErrorDialogFor.SURETY_WITH_CLICK,
                            it.data[0].title,
                            it.data[0].description,
                            this
                        ).apply {
                            this.show()
                        }
                        return@observe
                    }
                }

                // Lottie Animation
                if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "6" && status == "1") {
                    Util.showLottieDialog(
                        requireContext(), "chat_request_accept_lottie.json", soundType = 1,wrapContent = true,isBlackBackground = 1
                    ) { iit ->

                        // on Animation Complete
                        if (!it.data.isNullOrEmpty()) {

                            // Get Stream Flow

                            if (it.data[0].cid.isNotEmpty()) { // cid

                                startActivity(ChannelActivity.newIntent(requireContext(), it.data[0].cid,ActivityIsFrom.PROFILE))

                            } else {
                                (context as MainActivity).changeFragment(3)
                            }







                            // Normal Flow
                            /*if (it.data[0].chatId.isNotEmpty()) {
                                requireContext().openA<ChatDetailsActivity> {
                                    putExtra(ID, it.data[0].chatId)
                                    putExtra(IS_FROM, ActivityIsFrom.PROFILE)
                                }
                            } else {
                                (context as MainActivity).changeFragment(3)
                            }*/



                        } else {
                            (context as MainActivity).changeFragment(3)
                        }

                    }

                } else {
                    callApi(1)
                }

            }
        }



        profileViewModel.likeDislikeResponse.observe(viewLifecycleOwner) {
            if (it.success == 1) {
                profileArrayList[binding.viewPager.currentItem].isLike = 1
                profilePagerAdapter!!.notifyDataSetChanged()
            }
        }

        profileViewModel.readSneakPeakApiResponse.observe(viewLifecycleOwner) {
            if (it.data.isNotEmpty()) {
                (context as BaseActivity).let { activity ->
                    activity.bundle = Bundle().apply {
                        putString("request_from", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("request_to", profileArrayList[currentPosition].id)
                        putString(
                            "gen_from_to",
                            Util.genderInitalsForFirebase(profileArrayList[currentPosition].gender)
                        )
                        putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                        putString("match_status", profileArrayList[currentPosition].statusText)
                        putString(
                            "similar_percentage", profileArrayList[currentPosition].similarAnswer
                        )
                        putString(
                            "common_questions", profileArrayList[currentPosition].commonQuestions
                        )
                        putString(
                            "opinion_status", it.data[0].sneakPeakStatus)
                        putString(
                            "user_status", profileArrayList[currentPosition].userStatus
                        )
                        putString(
                            "recommendation_type",
                            if (profileArrayList[currentPosition].isProfileLocked == "1") "Locked" else "Unlocked"
                        )
                        putString(
                            "request_type",
                            if (profileArrayList[currentPosition].exchangeButton == "3") "recommendations" else "chat"
                        )
                    }
                    activity.firebaseEventLog("view_opinions",activity.bundle)
                }
            }
        }

    }

    private fun setProfilePicture(condition : Boolean) {
        if (condition) {
            (context as MainActivity).get().imgCustomProfile.setImageResource(R.drawable.dr_ic_grey_lock)
            (context as MainActivity).get().txtUserInits.visibility = View.GONE
        } else {
            Util.setHeaderProfile(context as MainActivity)
        }
    }

    fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(requireContext())) {
            if (showProgress) Util.showProgress(requireContext())
            when (tag) {
                1 -> {
                    profileViewModel.profileListApiRequest(requireContext()/*,SELECTED_FILTER*/)
                }

                2 -> { //  Profile View
                    profileViewModel.readExchangedProfileApiRequest(
                        requireContext(),
                        profileArrayList[binding.viewPager.currentItem].id,
                        profileArrayList[binding.viewPager.currentItem].isBanned,
                    )
                }

                3 -> { // send request
                    profileViewModel.sendProfileExchangeApiRequest(
                        requireContext(), profileArrayList[binding.viewPager.currentItem].id
                    )
                }

                4 -> {  //1=accept, 2=reject, 3 = discard request
                    profileViewModel.saveProfileExchangeApiRequest(
                        requireContext(), profileArrayList[binding.viewPager.currentItem].id, status
                    )
                }

                5 -> {
                    // likeAPI
                    profileViewModel.likeDislikeApiRequest(
                        requireActivity(), profileArrayList[binding.viewPager.currentItem].id
                    )  //0=unlike, 1=like

                }

                6 -> { // for Single Profile
                    profileViewModel.singleProfileListApiRequest(
                        requireActivity(), profileArrayList[binding.viewPager.currentItem].id
                    )

                }

                7 -> {
                    profileViewModel.readSneakPeakApiRequest(
                        requireContext(), profileArrayList[binding.viewPager.currentItem].id
                    )
                }

                8 -> {
                    profileViewModel.readReferencesApiRequest(
                        requireContext(),
                        profileArrayList[binding.viewPager.currentItem].id,
                        referenceNumber,
                        fromCandidateId,
                        toCandidateId
                    )
                }

                9 -> {

                    userViewModel.chatIntroSettingApiRequest(requireContext(),chatIntroduction) // 1 for yes, 2 for no

                }
            }
        }
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        (requireActivity() as BaseActivity).errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {
                if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.UPLOAD_PHOTOS) {
                    requireContext().openA<PhotoAlbumActivity> {
                        putExtra(IS_FROM, ActivityIsFrom.PROFILE)
                    }
                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.QUESTIONS) {
                    requireContext().openA<SneakPeekActivity> {
                        putExtra(IS_FROM, ActivityIsFrom.PROFILE)
                    }
                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.COMPLETE_PROFILE) {
                    requireContext().openA<EditProfileActivity> {
                        putExtra(IS_FROM, ActivityIsFrom.FROM_MENU)
                    }
                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.CHAT_INTRODUCTION) {
                    // Get introduced
                    // sent request and accept request
                    chatIntroduction = "1" // 1 for yes, 2 for no
                    callApi(9,true)

                    if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "1") {

                        callApi(3) // send request (chat request is being send)

                    } else {

                        status = "1" // accept request
                        callApi(4) // SaveExchangeAPI

                    }

                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.PHOTO_ALBUM_PENDING) { // redirect to album screen
                    requireContext().openA<PhotoAlbumActivity> {
                        putExtra(IS_FROM, ActivityIsFrom.PROFILE)
                    }
                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.CONFIRMING) { // reject profile
                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as BaseActivity).errorDialogComponent?.dismiss()
                        status = "3"
                        callApi(4) // reject profile
                        removeItem()
                    }, 200)
                }
            }

            "1" -> {
                if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.CHAT_INTRODUCTION) {
                    // Dont need an Introduction
                    // sent request and accept request
                    chatIntroduction = "2" // 1 for yes, 2 for no
                    callApi(9,true)

                    if (profileArrayList[binding.viewPager.currentItem].exchangeButton == "1") {

                        callApi(3) // send request (chat request is being send)

                    } else {

                        status = "1" // accept request
                        callApi(4) // SaveExchangeAPI

                    }

                    (context as BaseActivity).errorDialogComponent?.dismiss()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as BaseActivity).errorDialogComponent?.dismiss()
                    }, 300)
                }
            }

            "2" -> {
                if (isFrom != null && isFrom == ErrorDialogComponent.ErrorDialogFor.CHAT_INTRODUCTION) {
                    // Cancel
                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as BaseActivity).errorDialogComponent?.dismiss()
                    }, 300)
                }
            }
        }
    }


    private fun removeHandlers() {
        if (handler != null && runnable != null)
            handler?.removeCallbacks(runnable!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem() {
        if (profileArrayList.size == 1){
            mainProfileArrayList.remove(profilePagerAdapter!!.objList[currentPosition])
            profileArrayList.remove(profilePagerAdapter!!.objList[currentPosition])
            profilePagerAdapter!!.objList.removeAt(currentPosition)
            Util.dismissAnimationProgress()

            setProfileData()

        } else {
            mainProfileArrayList.remove(profilePagerAdapter!!.objList[currentPosition])
            profileArrayList.remove(profilePagerAdapter!!.objList[currentPosition])
            profilePagerAdapter!!.objList.removeAt(currentPosition)
            profilePagerAdapter!!.notifyItemRemoved(currentPosition)
            setProgressBar(binding.viewPager.currentItem, profileArrayList.size)

            handler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                Util.dismissAnimationProgress()
                profilePagerAdapter!!.notifyDataSetChanged()
            }
            handler?.postDelayed(runnable!!, 1000) // Delayed execution after 1 second
        }
    }

    override fun removeProfileCallBack(profile: ProfileModel) {
        removeItem()
    }
}


interface RemoveProfileFromListInterface {
    fun removeProfileCallBack(profile: ProfileModel)
}