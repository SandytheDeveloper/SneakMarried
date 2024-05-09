package com.commonfriend.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.ChecklistActivity
import com.commonfriend.EditProfileActivity
import com.commonfriend.FeedbackActivity
import com.commonfriend.MainActivity
import com.commonfriend.MobileNumberActivity
import com.commonfriend.NotificationActivity
import com.commonfriend.PhotoAlbumActivity
import com.commonfriend.PriorityActivity
import com.commonfriend.PrivacyLegalActivity
import com.commonfriend.QuestionListActivity
import com.commonfriend.R
import com.commonfriend.adapter.NavigationMenuAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.databinding.FragmentMenuBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DataUtil
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.MAIN_PROFILE_LIST
import com.commonfriend.utils.Pref
import com.commonfriend.utils.SELECTED_FILTER
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.isNotNull
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import io.getstream.chat.android.client.ChatClient

class MenuFragment : Fragment(), View.OnClickListener, ErrorDialogComponent.ErrorBottomSheetClickListener {


    private var lastPos = -1
    private var binding: FragmentMenuBinding? = null
    private lateinit var userViewModel: UserViewModel

    private val _binding get() = binding!!

    private var pauseSearch = ""
    private var menuAdapter: NavigationMenuAdapter? = null
    private var chatIntroduction: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        initialization()
        (requireActivity() as BaseActivity).logForCurrentScreen(Screens.MENU_SCREEN.screenType,Screens.MENU_SCREEN.screenName)
        return _binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            (context as MainActivity).get().topView.visible()
            if (menuAdapter.isNotNull())
                DataUtil.menuList(requireContext(), menuAdapter!!)
        }
    }

    private fun initialization() {
        (context as MainActivity).get().topView.visible()
        menuAdapter = NavigationMenuAdapter(requireContext(), this)
        _binding.rvMenuView.adapter = menuAdapter
        DataUtil.menuList(requireContext(), menuAdapter!!)
        allApiResponses()

        chatIntroduction = "" // empty to get response
        callApi(5,false) //  check chatIntroduction is enable or not
    }

    override fun onResume() {
        super.onResume()
        Util.setHeaderProfile(context as MainActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.llMenu -> {
                lastPos = view.tag.toString().toInt()
                when (menuAdapter!!.objList[lastPos].id) {
//                    "22" -> { //Chat Screen
//                        requireContext().openA<ChatMainActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
//                    }
                    "0" -> { //Question List
                        requireContext().openA<QuestionListActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "1" -> { //Edit Profile
                        requireContext().openA<EditProfileActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "2" -> { // CheckList
                        requireContext().openA<ChecklistActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "3" -> { //Priority
                        requireContext().openA<PriorityActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "4" -> {//Photo Album
                        requireContext().openA<PhotoAlbumActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "5" -> { //Privacy
                        requireContext().openA<PrivacyLegalActivity> {putExtra(IS_FROM,"1")}
                    }

                    "6" -> { //Rules
                        requireContext().openA<PrivacyLegalActivity> {putExtra(IS_FROM,"2")}
                    }

                    "7" -> {//FeedBack
                        requireContext().openA<FeedbackActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "8" -> { //Change Phone Number
                        requireContext().openA<MobileNumberActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }

                    "9" -> { //Log out
                        callApi(3)
                    }

                    "10" -> { //Pause Search
                        callApi(2)//1=pause, 0=not pause
                    }

                    "11" -> {  // Delete Account

                        (context as BaseActivity).errorDialogComponent = ErrorDialogComponent(
                            requireContext(),
                            ErrorDialogComponent.ErrorDialogFor.CONFIRMING,
                            getString(R.string.about_your_profile),
                            Pref.getStringValue(Pref.PREF_DELETE_POPUP_TEXT,getString(R.string.are_you_sure)).toString(),
                            this
                        ).apply {
                            this.show()
                        }

//                        callApi(4)
                    }
                    "12" -> {  // Notifications
                        requireContext().openA<NotificationActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_MENU)}
                    }
                    "13" -> {  // terms of use
                        requireContext().openA<PrivacyLegalActivity> {putExtra(IS_FROM,"3")}
                    }
                }
            }
            R.id.toggleBtnChatIntroduction -> {

                val pos = view.tag.toString().toInt()
                if (menuAdapter!!.objList[pos].id == "14") {

                    Pref.setBooleanValue(Pref.PREF_CHAT_INTRODUCTION,!Pref.getBooleanValue(Pref.PREF_CHAT_INTRODUCTION,false))
                    menuAdapter!!.notifyItemChanged(pos)
                    chatIntroduction = if (Pref.getBooleanValue(Pref.PREF_CHAT_INTRODUCTION,false)) "1" else "2"
                    callApi(5,false)

                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]

        userViewModel.pauseUnPauseSearchResponse.observe(requireActivity()) {
            Util.showLottieDialog(requireContext(), "done_lottie.json",wrapContent = true)
        }

        userViewModel.logoutApiResponse.observe(requireActivity()) {
            Util.showLottieDialog(requireContext(), "done_lottie.json",wrapContent = true) {
                Util.clearData(requireActivity())
            }
        }
        userViewModel.deleteAccountApiResponse.observe(requireActivity()) {
            Util.showLottieDialog(requireContext(), "done_lottie.json",wrapContent = true) {
                Util.clearData(requireActivity())
            }
        }
        userViewModel.chatIntroSettingApiResponse.observe(requireActivity()) {
            if (it.success == 1 && it.data.isNotEmpty()) {
                Pref.setBooleanValue(Pref.PREF_CHAT_INTRODUCTION,
                    it.data[0].chatIntroduction == "1"
                )
                if (menuAdapter.isNotNull())
                    menuAdapter!!.notifyDataSetChanged()
            }
        }

    }


    private fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(requireContext())) {
            when (tag) {
                2 -> {
                    if (showProgress)
                        Util.showProgress(requireContext())

                    Pref.setStringValue(
                        Pref.PREF_SEARCH_PAUSED,
                        if (Pref.getStringValue(Pref.PREF_SEARCH_PAUSED, "0") == "0") "1" else "0"
                    )
                    DataUtil.menuList(requireContext(), menuAdapter!!)
                    userViewModel.pauseUnPauseSearchApiRequest(
                        requireContext(),
                        Pref.getStringValue(Pref.PREF_SEARCH_PAUSED, "0").toString()
                    ) //1=pause, 0=not pause
                }

                3 -> {
                    if (showProgress)
                        Util.showProgress(requireContext())
                    userViewModel.logoutApiRequest(requireContext())
                }

                4 -> {
                    if (showProgress)
                        Util.showProgress(requireContext())
                    userViewModel.deleteUserApiRequest(requireContext())
                }

                5 -> {
                    if (showProgress)
                        Util.showProgress(requireContext())
                    userViewModel.chatIntroSettingApiRequest(requireContext(),chatIntroduction)
                }
            }
        }
    }

    override fun onItemClick(itemID: String, isFrom: ErrorDialogComponent.ErrorDialogFor?) {
        (requireActivity() as BaseActivity).errorDialogComponent?.dismiss()
        when (itemID) {
            "0" -> {
                callApi(4)
            }
        }
    }
}