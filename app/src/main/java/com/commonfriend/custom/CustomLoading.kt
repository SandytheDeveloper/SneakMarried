package com.commonfriend.custom

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.commonfriend.R
import com.commonfriend.databinding.CustomLoadingBinding


class CustomLoading(context: Context) : Dialog(context,R.style.AppLottieDialogTheme) {

//    private val handler: Handler = Handler(Looper.getMainLooper())
//    private val delay: Long = 1000L
    lateinit var binding : CustomLoadingBinding

    init {
        setCancelable(false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.color_base_grey)))
        window!!.attributes.gravity = Gravity.CENTER
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)



//        handler.postDelayed(this::animateText, delay)

    }

//    private fun animateText() {
//        var txtLoading = findViewById<AppCompatTextView>(R.id.txtLoading)
//        val text = txtLoading.text.toString()
//        val newText = text + "."
//        txtLoading.text = newText
//
//        if (text == "Loading...") {
//            txtLoading.text = "Loading"
//        }
//
//        handler.postDelayed(this::animateText, delay)
//    }
}