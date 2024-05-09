package com.commonfriend.custom

import android.view.View
import androidx.viewpager.widget.ViewPager

class VerticalPageTransFormer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> page.alpha = 0f
            position > 1 -> page.alpha = 0f
            position < 0 -> {
                page.alpha = 1 + position
                page.translationX = pageWidth * -position
                page.translationY = 0f
            }
            else -> {
                page.alpha = 1 - position
                page.translationX = pageWidth * -position
                page.translationY = pageHeight * position
            }
        }
    }
}

