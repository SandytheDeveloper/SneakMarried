package com.commonfriend

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivitySplashBinding
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Screens
import com.commonfriend.utils.Util
import com.commonfriend.utils.openA
import com.commonfriend.utils.visible
import io.branch.referral.Branch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable : Runnable

    override fun onStart() {
        super.onStart()

        Branch.sessionBuilder(this).withCallback { branchUniversalObject, linkProperties, error ->
            if (error != null) {
                Log.e("BranchSDK_Tester", "branch init failed. Caused by -" + error.message)
            } else {
                Log.i("BranchSDK_Tester", "branch init complete!")
                if (branchUniversalObject != null) {
                    Log.i("BranchSDK_Tester", "title " + branchUniversalObject.title)
                    Log.i("BranchSDK_Tester", "CanonicalIdentifier " + branchUniversalObject.canonicalIdentifier)
                    Log.i("BranchSDK_Tester", "metadata " + branchUniversalObject.contentMetadata.convertToJson())
                }
                if (linkProperties != null) {
                    Log.i("BranchSDK_Tester", "Channel " + linkProperties.channel)
                    Log.i("BranchSDK_Tester", "control params " + linkProperties.controlParams)
                }
            }
        }.withData(this.intent.data).init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logForCurrentScreen(Screens.SPLASH_SCREEN.screenType, Screens.SPLASH_SCREEN.screenName)
        Util.setToken()
    }

    override fun onResume() {
        super.onResume()
//        Handler(Looper.myLooper()!!).postDelayed({
//            manageLogin()
//        }, 2000)

        runnable = Runnable {
            manageLogin()
            handler.removeCallbacks(runnable) // Remove itself after execution
        }


        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator) {

            }
            override fun onAnimationEnd(p0: Animator) {
                binding.shimmerEffect.visible()
                binding.shimmerEffect.alpha = 0f
                binding.shimmerEffect.animate().alpha(1f).setDuration(200).start()
                handler.postDelayed(runnable, 1500)
            }
            override fun onAnimationCancel(p0: Animator) {
            }
            override fun onAnimationRepeat(p0: Animator) {
            }
        })

    }

    private fun manageLogin() {
        if (Pref.getBooleanValue(Pref.PREF_LOGIN, false)) {

            Util.print(
                ">>>>>>>>>>>>>PREF_USER_PROFILE_COMPLETED>>>>>>${
                    Pref.getStringValue(
                        Pref.PREF_USER_PROFILE_COMPLETED,
                        ""
                    )
                }"
            )
            if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "") == "1") {
                Util.manageOnBoarding(this@SplashActivity)
                finish()
            } else {
                openA<StepsActivity>()
                finishAffinity()
            }
        } else {
            openA<TutorialActivity>()
            finish()
        }
    }
}
