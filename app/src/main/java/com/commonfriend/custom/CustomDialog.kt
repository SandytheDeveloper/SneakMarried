package com.commonfriend.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import androidx.core.content.ContextCompat
import com.commonfriend.R
import com.commonfriend.databinding.CustomProgressBarBinding
import com.commonfriend.utils.visibleIf


class CustomDialog(context: Context, private var showLoading: Boolean = false) :
        Dialog(context, if (showLoading) R.style.AppBottomSheetDialogDimTheme else R.style.AppBottomSheetDialogTheme) {


    init {
        setCancelable(false)
    }

    lateinit var binding : CustomProgressBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CustomProgressBarBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.progressBar.visibleIf(showLoading)

//        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.attributes.gravity = Gravity.CENTER

    }
}