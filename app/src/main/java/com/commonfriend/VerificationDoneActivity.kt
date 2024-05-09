package com.commonfriend

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.base.BaseActivity

import com.commonfriend.databinding.ActivityVerificationDoneBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class VerificationDoneActivity : BaseActivity() {

    private lateinit var binding: ActivityVerificationDoneBinding
    private var isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL

    private lateinit var questionViewModel: QuestionViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerificationDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFrom =if(intent.hasExtra(IS_FROM)) intent.getSerializableExtra(IS_FROM) as ActivityIsFrom else ActivityIsFrom.NORMAL

        allApiResponses()

        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator) {
                Util.playSound(this@VerificationDoneActivity,0)
            }

            override fun onAnimationEnd(p0: Animator) {

                Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>${IS_FROM}")
                if (isFrom == ActivityIsFrom.FROM_EDIT ) {
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this@VerificationDoneActivity, EditProfileActivity::class.java
                            ))
                        finishAffinity()
                    }, 1000)
                } else if (isFrom == ActivityIsFrom.FROM_MENU ) {
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this@VerificationDoneActivity, MainActivity::class.java
                            ))
                        finishAffinity()
                    }, 1000)
                }
                else {
                    if (Pref.getStringValue(Pref.PREF_USER_REGISTERED, "0") == "0") {
                        callApi()
                    } else manageLogin()
                }



            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }

        })

    }

    private fun allApiResponses() {
        questionViewModel = ViewModelProvider(
            this, ViewModelFactory(ApiRepository())
        )[QuestionViewModel::class.java]
        questionViewModel.getCategoryListResponse.observe(/* owner = */ this@VerificationDoneActivity) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(
                        this@VerificationDoneActivity,
                        SectionBreakerActivity::class.java
                    ).putExtra(IS_FROM, isFrom)
                )
                finishAffinity()


            }, 1000)
        }
    }

    @SuppressLint("CheckResult")
    private fun callApi() {
        if (Util.isOnline(this)) {
            questionViewModel.categoryListApiRequest(this@VerificationDoneActivity)
        }
    }

    private fun manageLogin() {
        if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, " ") == "1") {
            Util.manageOnBoarding(this@VerificationDoneActivity)
            finishAffinity()

        } else {
            startActivity(Intent(this@VerificationDoneActivity, StepsActivity::class.java))
            finishAffinity()
        }
    }
}