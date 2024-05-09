package com.commonfriend.custom

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager


class VerticalViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    init {
        setPageTransformer(true, VerticalPageTransFormer())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        val width = measuredHeight
        val height = measuredWidth
        setMeasuredDimension(width, height)
    }

}