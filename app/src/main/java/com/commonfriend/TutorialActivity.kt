package com.commonfriend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.commonfriend.adapter.ViewPagerAdapter
import com.commonfriend.base.BaseActivity
import com.commonfriend.databinding.ActivityIntroductionChooseBinding
import com.commonfriend.fragment.IntroductionFragment
import com.commonfriend.models.IntroDataClass
import com.commonfriend.utils.Screens

class TutorialActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityIntroductionChooseBinding
    var currentItem = 0
    var lottieList = listOf(
        IntroDataClass(
            "1. Create a profile",
            "Boring, but mandatory.\nA complete profile is necessary.",
            "frame01_CreateProfie.json"
        ),
        IntroDataClass(
            "2. Tell me your type",
            "Tell me your priorities.\nWhat matters and what doesn't.",
            "frame02_YourType02.json"
        ),
        IntroDataClass(
            "3. Discover people",
            "Weekly recommendations every Sunday.\nPrivate and meaningful.",
            "frame03_DiscoverPeople.json"
        ),
        IntroDataClass(
            "4. Get introduced",
            "Like someone? Let me know,\nand I'll introduce you.",
            "frame04_GetIntroduced.json"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroductionChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        logForCurrentScreen(Screens.INTRO_SCREEN.screenType, Screens.INTRO_SCREEN.screenName)
        initialization()
    }

    override fun onResume() {
        super.onResume()
        setData()
    }

    private fun setData() {
        currentItem = 0
        animSlide()

        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(
            IntroductionFragment(lottieList[0])
        )
        adapter.addFragment(
            IntroductionFragment(lottieList[1])
        )
        adapter.addFragment(
            IntroductionFragment(lottieList[2])
        )
        adapter.addFragment(
            IntroductionFragment(lottieList[3])
        )
        binding.viewPager.adapter = adapter
        /*Slider*/
        try {

            binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    currentItem = position
                    if (currentItem <= lottieList.size - 1) {
                        animSlide()
                    } else {
                        currentItem = 0
                        animSlide()
                    }
                    binding.viewPager.currentItem = currentItem
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
            binding.viewPager.currentItem = 0

        } catch (ex: Exception) {
            Log.e("", ex.message.toString())
        }

    }

    private fun initialization() {
        binding.btnNext.setOnClickListener(this)


    }


    fun animSlide() {
        when (currentItem) {
            0 -> {
                binding.imgStepOne.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_fill_dots
                )
                binding.imgStepTwo.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepThree.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepFour.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
            }

            1 -> {
                binding.imgStepOne.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepTwo.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_fill_dots
                )
                binding.imgStepThree.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )

                binding.imgStepFour.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
            }

            2 -> {
                binding.imgStepOne.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepTwo.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepThree.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_fill_dots
                )

                binding.imgStepFour.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
            }

            else -> {
                binding.imgStepOne.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepTwo.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
                binding.imgStepFour.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_fill_dots
                )
                binding.imgStepThree.background = ContextCompat.getDrawable(
                    this@TutorialActivity, R.drawable.dr_unfill_dots_white
                )
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnNext -> {
                startActivity(Intent(this@TutorialActivity, MobileNumberActivity::class.java))
                finishAffinity()
            }
        }
    }

}