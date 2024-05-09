package com.commonfriend.utils

import android.content.Context
import android.content.SharedPreferences
import com.commonfriend.MainApplication


object Pref {

    private var sharedPreferences: SharedPreferences? = null

    val PREF_FILE: String = "FINAL_PROJECT_PREF"
    private fun openPreference() {

        sharedPreferences =
            MainApplication.instance.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    }

    /*For Integer Value*/

    fun setIntValue(key: String, value: Int) {
        openPreference()
        val prefsPrivateEditor: SharedPreferences.Editor? = sharedPreferences!!.edit()
        prefsPrivateEditor!!.putInt(key, value)
        prefsPrivateEditor.apply()
        sharedPreferences = null
    }

    fun getIntValue(key: String, defaultValue: Int): Int {
        openPreference()
        val result = sharedPreferences!!.getInt(key, defaultValue)
        sharedPreferences = null
        return result
    }


    /*Fore String Value Store*/
    fun getStringValue(key: String, defaultValue: String): String? {
        openPreference()
        val result = sharedPreferences!!.getString(key, defaultValue)
        sharedPreferences = null
        return result
    }

    /*For Remove variable from pref*/
     fun remove(key: String) {
        openPreference()
        val prefsPrivateEditor = sharedPreferences!!.edit()
        prefsPrivateEditor.remove(key)
        prefsPrivateEditor.apply()
        sharedPreferences = null
    }

    /*For Remove variable from pref*/
    fun clearAllPref() {
        remove(PREF_USER_NAME)
        remove(PREF_USER_REGISTERED)
        remove(PREF_USER_PROFILE_COMPLETED)
        remove(PREF_PROFILE_CONFIRMATION)
        remove(PREF_USER_APPROVED)
        remove(PREF_USER_DISPLAY_PICTURE)
        remove(PREF_USER_ID)
        remove(PREF_USER_GENDER)
        remove(PREF_MOBILE_NUMBER)
        remove(PREF_DEVICE_TOKEN)
        remove(PREF_LANGUAGE)
        remove(PREF_AUTH_TOKEN)
        remove(PREF_LOGIN)
        remove(PREF_CURRENT_LAT)
        remove(PREF_CURRENT_LNG)
        remove(PREF_USER_CURRENT_CITY)
        remove(PREF_USER_CURRENT_COUNTRY)
        remove(PREF_PRIORITY_GIVEN)
        remove(PREF_CHECK_LIST_PROVIDED)
        remove(PREF_SELECTED_POS)
        remove(PREF_SEARCH_PAUSED)
        remove(PREF_AADHAR_VERIFIED)
        remove(PREF_IS_ACCOUNT_LOCKED)
        remove(PREF_IS_ACCOUNT_BAN)
        remove(PREF_BAN_TITLE)
        remove(PREF_BAN_DESCRIPTION)
        remove(PREF_STREAM_CHAT_TOKEN)
        remove(PREF_PROFILE_FIRST_TIME_DAILOG)
        remove(PREF_UNDER_REVIEW)
//        remove(PREF_REVIEW_TITLE)
//        remove(PREF_REVIEW_DESCRIPTION)
//        remove(PREF_REVIEW_SUB_DESCRIPTION)
//        remove(PREF_IS_UNDER_REVIEW)
        remove(PREF_RESOLUTION)
        remove(PREF_RATIO_1)
        remove(PREF_RATIO_2)
        remove(PREF_CHAT_INTRODUCTION)
        remove(PREF_ALBUM_SCREEN_TYPE)
    }

    fun setStringValue(key: String, value: String) {
        openPreference()
        val prefsPrivateEditor: SharedPreferences.Editor? = sharedPreferences!!.edit()
        prefsPrivateEditor!!.putString(key, value)
        prefsPrivateEditor.apply()
        sharedPreferences = null
    }


    /*For boolean Value Store*/

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        openPreference()
        val result = sharedPreferences!!.getBoolean(key, defaultValue)
        sharedPreferences = null
        return result
    }

    fun setBooleanValue(key: String, value: Boolean) {
        openPreference()
        val prefsPrivateEditor: SharedPreferences.Editor? = sharedPreferences!!.edit()
        prefsPrivateEditor!!.putBoolean(key, value)
        prefsPrivateEditor.apply()
        sharedPreferences = null
    }

    const val PREF_USER_NAME: String = "PREF_USER_NAME"
    const val PREF_PROFILE_CONFIRMATION: String = "PREF_PROFILE_CONFIRMATION"
    const val PREF_USER_REGISTERED: String = "PREF_USER_REGISTERED"
    const val PREF_USER_PROFILE_COMPLETED: String = "PREF_USER_PROFILE_COMPLETED"
    const val PREF_USER_APPROVED: String = "PREF_USER_APPROVED"
    const val PREF_USER_DISPLAY_PICTURE: String = "PREF_USER_DISPLAY_PICTURE"
    const val PREF_USER_ID: String = "PREF_USER_ID"
    const val PREF_USER_GENDER: String = "PREF_USER_GENDER"
    const val PREF_MOBILE_NUMBER: String = "PREF_MOBILE_NUMBER"
    const val PREF_CURRENT_LAT: String = "PREF_CURRENT_LAT"
    const val PREF_CURRENT_LNG: String = "PREF_CURRENT_LNG"
    const val PREF_USER_CURRENT_CITY: String = "PREF_USER_CURRENT_CITY"
    const val PREF_USER_CURRENT_COUNTRY: String = "PREF_USER_CURRENT_COUNTRY"
    const val PREF_DEVICE_TOKEN: String = "PREF_DEVICE_TOKEN"
    const val PREF_LANGUAGE: String = "PREF_LANGUAGE"
    const val PREF_AUTH_TOKEN: String = "PREF_AUTH_TOKEN"
    const val PREF_LOGIN: String = "PREF_LOGIN"
    const val PREF_PRIORITY_GIVEN: String = "PREF_PRIORITY_GIVEN"
    const val PREF_CHECK_LIST_PROVIDED: String = "PREF_CHECK_LIST_PROVIDED"
    const val PREF_SELECTED_POS: String = "PREF_SELECTED_POS"
    const val PREF_SEARCH_PAUSED: String = "PREF_SEARCH_PAUSED"
    const val PREF_AADHAR_VERIFIED: String = "PREF_AADHAR_VERIFIED" // 0 - not verified , 1 - verified, 2 - Locked, 3 - Ban
    const val PREF_IS_ACCOUNT_LOCKED: String = "PREF_IS_ACCOUNT_LOCKED" // 1 - show Locked Widget
    const val PREF_IS_ACCOUNT_BAN: String = "PREF_IS_ACCOUNT_BAN" // 1 - show Ban View
    const val PREF_BAN_TITLE: String = "PREF_BAN_TITLE"
    const val PREF_BAN_DESCRIPTION: String = "PREF_BAN_DESCRIPTION"
    const val PREF_STREAM_CHAT_TOKEN: String = "PREF_STREAM_CHAT_TOKEN"
    const val PREF_PROFILE_FIRST_TIME_DAILOG: String = "PREF_PROFILE_FIRST_TIME_DAILOG"
    const val PREF_DELETE_POPUP_TEXT: String = "PREF_DELETE_POPUP_TEXT"
    const val PREF_CHAT_INTRODUCTION: String = "PREF_CHAT_INTRODUCTION"
    const val PREF_ALBUM_SCREEN_TYPE: String = "PREF_ALBUM_SCREEN_TYPE" // true - FourPhoto , false - Single Photo

    // For Under Review Screen
    const val PREF_UNDER_REVIEW: String = "PREF_UNDER_REVIEW" // 1 - show review View
//    const val PREF_REVIEW_TITLE: String = "PREF_REVIEW_TITLE"
//    const val PREF_REVIEW_DESCRIPTION: String = "PREF_REVIEW_DESCRIPTION"
//    const val PREF_REVIEW_SUB_DESCRIPTION: String = "PREF_REVIEW_SUB_DESCRIPTION"
//    const val PREF_IS_UNDER_REVIEW: String = "PREF_IS_UNDER_REVIEW" // 0 - show button else hide


    // For photoAlbum
    const val PREF_RESOLUTION: String = "PREF_RESOLUTION"
    const val PREF_RATIO_1: String = "PREF_RATIO_1"
    const val PREF_RATIO_2: String = "PREF_RATIO_2"

}