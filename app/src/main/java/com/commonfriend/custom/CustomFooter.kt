package com.commonfriend.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.commonfriend.MainActivity
import com.commonfriend.R
import com.commonfriend.databinding.CustomFooterBinding
import com.commonfriend.utils.Pref
import com.commonfriend.utils.visibleIf


class CustomFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {
//    private var pager: ViewPager? = null

    var binding: CustomFooterBinding =
        CustomFooterBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.btnHome.setOnClickListener(this)
        binding.btnAccount.setOnClickListener(this)
        binding.btnChat.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)

        changeBackground(0)
    }

    /*fun setPager(pager: ViewPager) {
        this.pager = pager
    }*/

    fun changeBackground(position: Int /*isChangePager: Boolean = true*/) {

        binding.imgHome.setImageResource(if (position == 0) R.drawable.ic_home_selected else R.drawable.ic_home_unselected)
        binding.imgSettings.setImageResource(if (position == 1) R.drawable.ic_settings_selected else R.drawable.ic_setting_idle)
        binding.imgAccount.setImageResource(if (position == 2) R.drawable.ic_profile_selected else R.drawable.ic_profile_unselected)
        binding.imgChat.setImageResource(if (position == 3) R.drawable.ic_chat_selected else R.drawable.ic_chat_unselected)
        binding.imgAccount.alpha = if (binding.btnAccount.isEnabled) 1.0f else 0.1f
        binding.imgChat.alpha = if (binding.btnChat.isEnabled) 1.0f else 0.1f
        /*  if (pager != null && isChangePager)

              pager!!.setCurrentItem(position, true)*/

    }

    fun bottomRedDot() {
        binding.imgAccountDot.visibleIf(true)
        binding.imgChatDot.visibleIf(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnHome -> {
                if (Pref.getIntValue(Pref.PREF_SELECTED_POS, 0) != 0)
                    (context as MainActivity).changeFragment(0)
            }

            R.id.btnSettings -> {
                if (Pref.getIntValue(Pref.PREF_SELECTED_POS, 0) != 1)
                    (context as MainActivity).changeFragment(1)
            }

            R.id.btnChat -> {
                if (Pref.getIntValue(Pref.PREF_SELECTED_POS, 0) != 3)
                    (context as MainActivity).changeFragment(3)
            }

            R.id.btnAccount -> {
                if (Pref.getIntValue(Pref.PREF_SELECTED_POS, 0) != 2)
                    (context as MainActivity).changeFragment(2)
            }
        }
    }

}
