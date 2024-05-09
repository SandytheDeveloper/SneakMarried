package com.commonfriend.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.commonfriend.databinding.BottomViewBinding

class CustomButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {


    private var binding: BottomViewBinding? = null

//    private val btnSkip: AppCompatTextView = findViewById(R.id.btnSkip)
//    private val btnContinue: AppCompatButton = findViewById(R.id.btnContinue)

    private val debounceInterval = 1000L // milliseconds

    init {


    }

  /*  override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        btnSkip.setOnClickListener {
            // Do something when the skip button is clicked
        }

        btnContinue.setOnClickListener {
            // Do something when the continue button is clicked
        }

        btnContinue.debouncedClick(debounceInterval) {
            // Do something when the continue button is clicked after debounce
            // For example, you can navigate to the next screen
        }
    }
*/}