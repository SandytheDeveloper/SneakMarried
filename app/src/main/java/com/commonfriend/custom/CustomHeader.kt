package com.commonfriend.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.commonfriend.R
import com.commonfriend.databinding.ActivityCustomHeaderBinding
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.utils.CATEGORY_ID
import com.commonfriend.utils.LAST_POS
import com.commonfriend.utils.ONBOARDING_SKIP
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.mainObjList
import com.commonfriend.utils.visibleIf
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory

class CustomHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {

     var binding: ActivityCustomHeaderBinding=
        ActivityCustomHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    init {

        skipButtonVisibleIf(
            try {
                if (Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED,"") == "1" || ONBOARDING_SKIP){
                    false
                } else {
                    if (mainObjList.isNotEmpty()) {
                        if (mainObjList[CATEGORY_ID].questionList.isNotEmpty()) {
                            mainObjList[CATEGORY_ID].questionList[LAST_POS].isSkippable == "1"
                        } else false
                    } else false
                }
            } catch (e: Exception) {
                false
            }
        )

        binding.btnLeft.setOnClickListener {
            if (context is AppCompatActivity)
                context.onBackPressedDispatcher.onBackPressed()
        }
        binding.imgCross.setOnClickListener {
            if (context is AppCompatActivity)
                context.onBackPressedDispatcher.onBackPressed()
        }
      /*  binding.txtSkip.setOnClickListener {
            if (context is AppCompatActivity)
                Util.manageTemplate(context)
        }*/

        binding.llSkipAll.setOnClickListener {
            if (context is AppCompatActivity) {
                Util.skipAndOpenHomeScreen(context)
            }
        }


//        binding.imgCustomProfile.setImageURI(Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE,""))
    }


    fun get() : ActivityCustomHeaderBinding{
        return  binding
    }

    fun setBtnLeft(boolean: Boolean){
        binding.btnLeft.visibility = if(boolean) View.VISIBLE else View.GONE
    }
    fun setBtnImageLeft(fromMenu: Boolean){
        binding.btnLeft.setImageResource(if(fromMenu) R.drawable.dr_ic_cross else R.drawable.dr_ic_left)
    }

    fun skipButtonVisibleIf(condition : Boolean) {
        binding.llSkipAll.visibleIf(condition)
    }
}