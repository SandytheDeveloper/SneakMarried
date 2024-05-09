package com.commonfriend.custom

import android.text.InputFilter
import android.text.Spanned
import com.vdurmont.emoji.EmojiParser


class EmojiInputFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {

        if (source.isNullOrEmpty()) {
            return null
        }

        if (source.matches("[^a-zA-Z]+".toRegex())) {
            return "" // Don't allow invalid characters
        }
        return null // Allow all other characters

        /*if (source.isNullOrEmpty()) {
            return null
        }

        // Remove emojis from the source text using emoji-java
        val filteredSource = EmojiParser.removeAllEmojis(source.toString())

        // Return the filtered source text
        return if (filteredSource == source.toString()) {
            null  // No emojis were removed, so allow the input
        } else {
            filteredSource  // Return the text with emojis removed
        }*/
    }
}
