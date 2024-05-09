package com.commonfriend.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.commonfriend.TutorialActivity
import com.commonfriend.databinding.FragmentIntroductionBinding
import com.commonfriend.models.IntroDataClass
import com.commonfriend.utils.gone
import com.commonfriend.utils.onAnimationCompleted
import com.commonfriend.utils.visible

@SuppressLint("NotConstructor")
class IntroductionFragment() : Fragment() {

    private lateinit var binding: FragmentIntroductionBinding
    var value1 = 0

    var item : IntroDataClass? = null

    constructor(item: IntroDataClass) : this() {
        this.item = item
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false)
        binding.txtTitle.text = item!!.title
        binding.txtSubTitle.text = item!!.subTitle
        binding.story.setAnimation(item!!.lottieFile)
        binding.story.onAnimationCompleted {
            if (item!!.title == (requireActivity() as TutorialActivity).lottieList[(requireActivity() as TutorialActivity).binding.viewPager.currentItem].title) {
                (requireActivity() as TutorialActivity).currentItem =
                    (requireActivity() as TutorialActivity).currentItem + 1
                if ((requireActivity() as TutorialActivity).currentItem <= (requireActivity() as TutorialActivity).lottieList.size - 1) {

                    changePageWithAnimation((requireActivity() as TutorialActivity).currentItem) // using animation for duration

                } else {
                    try {
                        (requireActivity() as TutorialActivity).binding.viewPager.currentItem = 0
                    } catch (e:Exception){
                        (requireActivity() as TutorialActivity).binding.viewPager.setCurrentItem(0,false)
                    }
                }
            }
        }

        return binding.root
    }

    private fun changePageWithAnimation(desiredPage: Int) {

        val distanceToMove = (requireActivity() as TutorialActivity).binding.viewPager.width

        val animator = ValueAnimator.ofInt(0, distanceToMove)
        animator.addUpdateListener { valueAnimator ->

            val value =
                (valueAnimator.animatedValue as Int) + if (desiredPage > 1) distanceToMove * (desiredPage - 1) else 0

            (requireActivity() as TutorialActivity).binding.viewPager.scrollTo(value, 0)
        }
        animator.interpolator =
            DecelerateInterpolator() // You can change the interpolator as needed
        animator.duration = 500

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {

                (requireActivity() as TutorialActivity).binding.viewPager.setCurrentItem(
                    desiredPage,
                    false
                )
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    override fun onResume() {
        super.onResume()
        binding.story.visible()
        binding.story.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.story.gone()
    }

    override fun onStop() {
        super.onStop()
        binding.story.removeAllAnimatorListeners()
    }

}