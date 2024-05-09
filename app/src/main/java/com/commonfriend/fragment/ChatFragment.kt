package com.commonfriend.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.ChatDetailsActivity
import com.commonfriend.MainActivity
import com.commonfriend.MainApplication
import com.commonfriend.ProfileViewActivity
import com.commonfriend.R
import com.commonfriend.adapter.ChatAdapter
import com.commonfriend.adapter.ChatDataFullyLoaded
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.FragmentChatBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.ChatKeys
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNotNull
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

class ChatFragment : Fragment(),
    OnClickListener, ChildEventListener, ChatDataFullyLoaded {

    private var binding: FragmentChatBinding? = null
    private val _binding get() = binding!!
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var dataBaseChats: DatabaseReference
    private var lastPos = -1
    private var chatID = ""
    private val userId = Pref.getStringValue(Pref.PREF_USER_ID, "")

    private val initialColor = Color.parseColor("#00000000") // tranplaret
    private val finalColor = Color.parseColor("#CCFD4E58") // red
    var handler: Handler? = null
    private var runnable: Runnable? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        initializations()
        (requireActivity() as BaseActivity).logForCurrentScreen(
            Screens.CHAT_LIST_SCREEN.screenType,
            Screens.CHAT_LIST_SCREEN.screenName
        )
        return _binding.root
    }

    private fun initializations() {
        chatID = (requireActivity() as MainActivity).profileId
        chatAdapter = ChatAdapter(requireActivity(), this, this)
        _binding.rvUpcomingChetList.adapter = chatAdapter
        allApiResponses()

        dataBaseChats = MainApplication.dataBaseRoot.child("chats")
//        dataBaseChats.addChildEventListener(this)

        (context as MainActivity).get().topView.gone()
        _binding.shimmerEffect.startShimmer()
        _binding.shimmerEffect.visible()

    }

    override fun onResume() {
        super.onResume()
        callApi(1, true)
        dataBaseChats.addChildEventListener(this)
        Util.clearNotifications(context as MainActivity)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            (context as MainActivity).get().topView.visibleIf(!_binding.shimmerEffect.isVisible)
            callApi(1)
            Util.clearNotifications(context as MainActivity)
        }
    }

    override fun onPause() {
        super.onPause()
        dataBaseChats.removeEventListener(this)
    }

    private fun allApiResponses() {

        chatViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[ChatViewModel::class.java]

        chatViewModel.fetchChatDataResponse.observe(viewLifecycleOwner) {
            Util.dismissProgress()
            if (it.success == 1) {
                (context as MainActivity).get().topView.visible()
                _binding.shimmerEffect.stopShimmer()
                _binding.shimmerEffect.gone()
                if (it.data.isNotEmpty())
                    if (it.data[0].banData.isNotEmpty()) {
                        if (it.data[0].banData[0].isBan == "1") {
                            (requireActivity() as MainActivity).mListener.showLockedView(it.data[0].banData[0])
                        } else {
                            (requireActivity() as MainActivity).mListener.showNormalView()
                            it.data.add(ChatModel())
                        }

                    }

                (requireContext() as MainActivity).get().customFooter.binding.imgAccountDot.visibleIf(
                    it.data[0].profileDot == "1"
                )
                (requireContext() as MainActivity).get().customFooter.binding.imgChatDot.visibleIf(
                    it.data[0].chatDot == "1"
                )

                if (it.data[0].chatData.isNotEmpty()) {
                    chatAdapter.addData(it.data[0].chatData)
                    _binding.llNoChats.visibility = View.GONE
                    if (chatID.isNotEmpty()) {
                        if (chatAdapter.objList.any { itt -> itt.chatId == chatID }) {
                            startActivity(
                                Intent(requireActivity(), ChatDetailsActivity::class.java)
                                    .putExtra(ID, chatID)
                                    .putExtra(IS_FROM, ActivityIsFrom.CHAT_SCREEN)
                            )
                        }
                        chatID = ""
                    }
                } else {
                    chatAdapter.addData(ArrayList())
                    _binding.llNoChats.visibility = View.VISIBLE
                    chatID = ""
                }
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun callApi(tag: Int, showShimmer: Boolean = false) {
        if (Util.isOnline(requireActivity())) {
            when (tag) {
                1 -> {
//                    Util.showProgress(requireActivity())
                    chatViewModel.fetchChatApiRequest(requireActivity())
                }
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.cvMain -> {
                lastPos = view.tag.toString().toInt()

                startActivity(
                    Intent(requireActivity(), ChatDetailsActivity::class.java)
                        .putExtra(ID, chatAdapter.objList[lastPos].chatId)
                        .putExtra(IS_FROM, ActivityIsFrom.CHAT_SCREEN)
                )
            }
            R.id.imgSenderProfilePic -> {
                lastPos = view.tag.toString().toInt()
                (context as MainActivity).openA<ProfileViewActivity> { putExtra(ID, chatAdapter.objList[lastPos].userId) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataBaseChats.removeEventListener(this)
        removeHandlers()
        binding = null

    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        if (dataSnapshot.value != null) {

            if (chatAdapter.isNotNull()) {
                if (chatAdapter.objList.isNotEmpty()) {

                    val jsonObj = JSONObject(dataSnapshot.value.toString())

                    if (jsonObj.optString(ChatKeys.USER_ID.key) == userId) {
                        if ((jsonObj.optString(ChatKeys.IS_TYPING.key) != "1" && jsonObj.optString(
                                ChatKeys.STATUS.key
                            ) != "2")
                        ) {
//                            if (chatAdapter.objList.any {
//                                    it.chatId == jsonObj.optString(
//                                        ChatKeys.CHAT_ID.key
//                                    )
//                                }) {
                                callApi(1)
//                            }
                        }
                    }
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

    override fun dataLoadingCompleted() {
        handler = Handler(Looper.getMainLooper())
        val chatListCopy = ArrayList(chatAdapter.objList)
        runnable = Runnable {
            for (position in 0 until chatListCopy.size) {
                // Check if the position is still valid
                if (position < chatAdapter.objList.size) {
                    val chatModel = chatListCopy[position]

                    // Check if the item has already been processed for removal
                    if (!chatModel.isRemoved) {
                        val viewHolder =
                            binding!!.rvUpcomingChetList.findViewHolderForAdapterPosition(position) as? ChatAdapter.ViewHolder
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
                                            banFilter.setBackgroundColor(color)
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
                                                        // Handle the end of translation animation
                                                        // Remove item from the list after animation
                                                        handler?.post {
                                                            val originalPosition = chatAdapter.objList.indexOf(chatModel)
                                                            if (originalPosition != -1 && !chatModel.isRemoved) {
                                                                chatAdapter.objList[originalPosition].isRemoved = true
                                                                chatAdapter.objList.removeAt(originalPosition)
                                                                chatAdapter.notifyItemRemoved(originalPosition)

                                                                if (chatAdapter.objList.isEmpty()) {
                                                                    chatAdapter.addData(ArrayList())
                                                                    _binding.llNoChats.visibility = View.VISIBLE
                                                                    chatID = ""
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        removeHandlers()

    }

    private fun removeHandlers() {
        if (handler != null && runnable != null)
            handler?.removeCallbacks(runnable!!)
    }
}


