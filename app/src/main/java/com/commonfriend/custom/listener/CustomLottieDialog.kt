package com.commonfriend.custom.listener

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.commonfriend.R
import com.commonfriend.databinding.CustomLottieViewBinding
import com.commonfriend.utils.Util

class CustomLottieDialog(
    context: Context,
    private var lottieFile: String,
    private var wrapContent: Boolean,
    private var soundType: Int,
    private var isBlackBackground: Int,
    var onComplete: ((Boolean) -> Unit)? = null
) : Dialog(context, R.style.AppLottieDialogTheme) {

    init {
        setCancelable(false)
    }

    lateinit var binding: CustomLottieViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomLottieViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Util.statusBarColor(context, window!!)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.gravity = Gravity.CENTER
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window?.statusBarColor = ContextCompat.getColor(context, R.color.color_black)

//        binding.txtTitle.visibleIf(lottieFile == "done_lottie.json")

        if (wrapContent) {
            binding.rlMainView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isBlackBackground == 1) R.color.color_black else R.color.color_white
                )
            )
        }
        /*else if (soundType == -1)
            binding.rlMainView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.new_red_transparent
                )
            )*/

        val layoutParams = binding.lottieView.layoutParams as ViewGroup.LayoutParams

        if (wrapContent) {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.lottieView.layoutParams = layoutParams

        binding.lottieView.setAnimation(lottieFile)
        binding.lottieView.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.lottieView.playAnimation()

        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                if (soundType != -1)
                    Util.playSound(context, soundType)
                Util.dismissLoading()
            }

            override fun onAnimationEnd(p0: Animator) {
                Util.dismissProgress()
                Util.dismissLottieDialog()
                window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                onComplete?.invoke(true)
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })

    }
}