package com.commonfriend.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.commonfriend.databinding.CustomSubHeaderBinding

class CustomSubHeader@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): RelativeLayout(context, attrs, defStyleAttr)
{
     var binding : CustomSubHeaderBinding =
        CustomSubHeaderBinding.inflate(LayoutInflater.from(context),this,true)

    init {

        binding.btnLeft.setOnClickListener {
            if (context is AppCompatActivity)
                context.finish()
        }
    }
    fun get() : CustomSubHeaderBinding {
        return  binding
    }



    fun setBtnLeft(boolean: Boolean){
        binding.btnLeft.visibility = if(boolean) View.VISIBLE else View.GONE
    }
}