package com.commonfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityMainBinding
import com.commonfriend.databinding.CustomReviewPopupBinding
import com.commonfriend.fragment.ChatScreenFragment
import com.commonfriend.fragment.HomeFragment
import com.commonfriend.fragment.MenuFragment
import com.commonfriend.fragment.ProfileFragment
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.BanModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.MAIN_PROFILE_LIST
import com.commonfriend.utils.ONBOARDING_SKIP
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Pref.PREF_SELECTED_POS
import com.commonfriend.utils.Util
import com.commonfriend.utils.activityOnBackPressed
import com.commonfriend.utils.gone
import com.commonfriend.utils.isNotNull
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.UserViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.dynamicLinks

class MainActivity : BaseActivity(), OnClickListener, AccountLocked {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    lateinit var currentFragment: Fragment // 0 Home , 1 - Menu , 2 - Profile , 3 - Chat
    lateinit var ft: FragmentTransaction
    var selectedPosition: Int = 0
    var profileId=""

    fun get(): ActivityMainBinding {
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!ONBOARDING_SKIP) {
            Pref.setStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "1")
            Pref.setStringValue(Pref.PREF_PROFILE_CONFIRMATION, "1")
        }

        initialization()
    }

    lateinit var mListener: AccountLocked

    private fun initialization() {

        mListener = this
        selectedPosition = intent.getIntExtra(DATA, 0)
         profileId = if (intent.hasExtra(ID)) intent.getStringExtra(ID)!! else ""
        Util.statusBarColor(this@MainActivity, window)
        if (Pref.getStringValue(Pref.PREF_IS_ACCOUNT_BAN, "") == "1") {
            Util.setHeaderProfile(this)
            setBanView()
        }


        manageDynamicLink()
        changeFragment(selectedPosition, profileId)

//        binding.rlCustomProfile.setOnClickListener(this)
        binding.btnDeleteAccount.setOnClickListener(this)
        binding.btnEditProfile.setOnClickListener(this)
        binding.imgOptions.setOnClickListener(this)

        allApiResponses()

        activityOnBackPressed(this,this) {
            onBackPress()
        }
    }

    private fun manageDynamicLink() {

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pdlink ->

                var deepLink : Uri? = null

                if (pdlink.isNotNull()){
                    deepLink = pdlink.link
                }

                if (deepLink.isNotNull()){
                    // type = 1 - Home , 2 - Profile , 3 - ChatScreen , 4 - EditProfile
                    val type = deepLink?.getQueryParameter("type").toString()
                    Util.print("type ---- $type ")

                    if (type.isNotEmpty()){
                        // selectedPosition = 0 HomeFragment , 1 - MenuFragment , 2 - Profile , 3 - Chat

                        if (type == "4"){

                            openA<EditProfileActivity> {putExtra(IS_FROM,ActivityIsFrom.FROM_HOME)}

                        } else {

                            selectedPosition = when(type) {
                                "2" -> 2
                                "3" -> 3
                                else -> 0
                            }
                            changeFragment(selectedPosition, profileId)
                        }

                    }

                }

            }

    }

    private fun allApiResponses() {
        userViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiRepository()))[UserViewModel::class.java]

        userViewModel.deleteAccountApiResponse.observe(this) {
            Util.showLottieDialog(this, "done_lottie.json", wrapContent = true) {
                clearData()
            }
        }

        userViewModel.logoutApiResponse.observe(this) {
            Util.showLottieDialog(this, "done_lottie.json", wrapContent = true) {
                clearData()
            }
        }
    }

    private fun clearData() {
        Pref.clearAllPref()
        startActivity(Intent(this, MobileNumberActivity::class.java))
        finishAffinity()
    }


    private fun onBackPress() {

        finishAffinity()

    }

    override fun onClick(view: View) {
        when (view.id) {
//            R.id.rlCustomProfile -> {
//                callApi(1, true)
//            }

            /*R.id.txtAddNew -> {
                startActivity(
                    Intent(this, NineteenTemplateActivity::class.java).putExtra(
                        IS_FROM, ActivityIsFrom.FROM_CREATE_PROFILE
                    )
                )
            }*/

            R.id.rlCustomProfile -> {

                if (Pref.getStringValue(Pref.PREF_AADHAR_VERIFIED, "").toString() == "0") {
                    startActivity(Intent(this, AadharVerificationActivity::class.java))
                }
            }

            R.id.btnDeleteAccount -> {
                callApi(3)
            }

            R.id.btnEditProfile -> {
                startActivity(
                    Intent(this, EditProfileActivity::class.java)
                        .putExtra(IS_FROM, ActivityIsFrom.LOCKED_ACCOUNT)
                )
            }

            R.id.imgOptions -> {
                popUpDialog(view)
            }
        }
    }


    private fun popUpDialog(view: View) {
        // Create an instance of PopupWindow
        val customReviewPopupBinding: CustomReviewPopupBinding =
            CustomReviewPopupBinding.inflate(layoutInflater)
        val popupView: View = customReviewPopupBinding.root

        val popupWindow = PopupWindow(
            popupView,
            resources.getDimension(com.intuit.sdp.R.dimen._240sdp).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background drawable for the popup window
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        customReviewPopupBinding.btnWithDraw.setOnClickListener {
            popupWindow.dismiss()
            callApi(3)

        }
        customReviewPopupBinding.btnLogout.setOnClickListener {
            popupWindow.dismiss()
            callApi(4)

        }

        customReviewPopupBinding.rlClose.setOnClickListener {
            popupWindow.dismiss()
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val offsetX = location[0]
        val offsetY = location[1]

        // Show the popup window at the clicked location
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, offsetX, offsetY)
    }

    fun callApi(tag: Int, showProgress: Boolean = true) {
        if (Util.isOnline(this)) {
            when (tag) {

                3 -> { // Delete Account
                    if (showProgress)
                        Util.showProgress(this)
                    userViewModel.deleteUserApiRequest(this)
                }

                4 -> {
                    if (showProgress)
                        Util.showProgress(this)
                    userViewModel.logoutApiRequest(this)
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    fun changeFragment(pos: Int, userId: String = "") {
        selectedPosition = pos
        profileId = userId
        Pref.setIntValue(PREF_SELECTED_POS, selectedPosition)
        binding.customFooter.changeBackground(selectedPosition)
        binding.rvFilter.visibility = View.GONE
        binding.llProgress.visibility = View.GONE
        binding.llProgress.removeAllViews()
        if (intent.hasExtra(IS_FROM) && pos == 2) { // clearing MainProfileList if profile list is not up to date
            val isFrom = intent.getSerializableExtra(IS_FROM) as ActivityIsFrom
            if (isFrom == ActivityIsFrom.NOTIFICATION){
                intent.removeExtra(IS_FROM)
                MAIN_PROFILE_LIST.clear()
            }
        }

        Util.setHeaderProfile(this)

        @Suppress("IMPLICIT_CAST_TO_ANY")
        currentFragment =
            when (pos) {
                1 -> MenuFragment()
                2 -> ProfileFragment()
                3 -> ChatScreenFragment()
                else -> HomeFragment()
            }

//        binding.imgAadharError.visibility =
//            if (Pref.getStringValue(Pref.PREF_AADHAR_VERIFIED, "")
//                    .toString() == "0"
//            ) View.VISIBLE else View.GONE

        ft = supportFragmentManager.beginTransaction()

        val existingFragment = supportFragmentManager.findFragmentByTag(pos.toString())
        if (existingFragment != null) {
            if (pos == 2){
                ft.remove(existingFragment)
                ft.add(R.id.fragmentLayout, currentFragment,pos.toString())
            } else
                ft.show(existingFragment)

        } else {
            ft.add(R.id.fragmentLayout, currentFragment,pos.toString())
//            ft.addToBackStack(null) // Optional: Add to back stack for back navigation
        }


        for (i in 0 until 4){
            val fragments = supportFragmentManager.findFragmentByTag(i.toString())
            if (fragments != null && pos != i){
                if (i == 2){
                    ft.remove(fragments)
                } else
                    ft.hide(fragments)
            }
        }

//        ft.replace(R.id.fragmentLayout, currentFragment)
        ft.commit()
    }

    override fun showLockedView(banModel: BanModel) {
        // on receive
        Pref.setStringValue(Pref.PREF_IS_ACCOUNT_BAN, banModel.isBan)
        Pref.setStringValue(Pref.PREF_BAN_TITLE, banModel.title)
        Pref.setStringValue(Pref.PREF_BAN_DESCRIPTION, banModel.description)
        setBanView()
    }

    private fun setBanView() {

        val isBan = Pref.getStringValue(Pref.PREF_IS_ACCOUNT_BAN, "")

        if (isBan == "1")
            Util.clearNotifications(this)

        binding.fragmentLayout.visibleIf(isBan != "1")
        binding.customFooter.visibleIf(isBan != "1")
//        if (isBan == "1"){
//            binding.rlReviewView.gone()
//        }
        binding.rlBanView.visibleIf(isBan == "1")
        binding.rlMain.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isBan == "1") R.color.color_red else R.color.color_white
            )
        )

        binding.txtBanTitle.text = Pref.getStringValue(Pref.PREF_BAN_TITLE, "")
//        binding.txtBanDescription.text = Pref.getStringValue(Pref.PREF_BAN_DESCRIPTION, "")

        binding.txtBanDescription.text =
            Util.getTextWithIcon(
                this,
                Pref.getStringValue(Pref.PREF_BAN_DESCRIPTION, "").toString(),
                R.drawable.dr_ic_white_reminder,
                binding.txtBanDescription.textSize)

    }


    override fun showNormalView() {

        binding.topView.visible()
        binding.fragmentLayout.visible()
        binding.customFooter.visible()
//        binding.rlReviewView.gone()
        binding.rlBanView.gone()

        binding.rlMain.setBackgroundColor(ContextCompat.getColor(this, R.color.color_white))

    }

}

interface AccountLocked {
    fun showLockedView(banModel: BanModel)

//    fun showUnderReviewView(reviewModel: HomeModel)

    fun showNormalView()
}