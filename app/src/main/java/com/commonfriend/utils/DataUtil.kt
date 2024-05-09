package com.commonfriend.utils

import android.annotation.SuppressLint
import android.content.Context
import com.commonfriend.R
import com.commonfriend.adapter.ErrorBtnAdapter
import com.commonfriend.adapter.NavigationMenuAdapter
import com.commonfriend.models.FaqModel
import com.commonfriend.models.GeneralModel

object DataUtil {

    @SuppressLint("NotifyDataSetChanged")
    fun menuList(context: Context, menuListAdapter: NavigationMenuAdapter) {

        menuListAdapter.objList.clear()
        menuListAdapter.objList = ArrayList()

//        var menuObj = GeneralModel()
//        menuObj.name = "Chat Screen"
//        menuObj.id = "22"
//        menuObj.iconImage = R.drawable.ic_chat_selector
//        menuListAdapter.objList.add(menuObj)

        var menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.chat_introductions)
        menuObj.id = "14"
        menuObj.iconImage = R.drawable.ic_menu_chat_intro
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.notifications)
        menuObj.id = "12"
        menuObj.iconImage = R.drawable.ic_menu_notification
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.questions)
        menuObj.id = "0"
        menuObj.iconImage = R.drawable.ic_menu_questions
        menuListAdapter.objList.add(menuObj)

        if (!ONBOARDING_SKIP) { // If questions are skipped then hide this options

            menuObj = GeneralModel()
            menuObj.name = context.resources.getString(R.string.priority)
            menuObj.id = "3"
            menuObj.iconImage = R.drawable.ic_menu_priority
            menuListAdapter.objList.add(menuObj)

            menuObj = GeneralModel()
            menuObj.name = context.resources.getString(R.string.checklist)
            menuObj.id = "2"
            menuObj.iconImage = R.drawable.ic_menu_checklist
            menuListAdapter.objList.add(menuObj)

            menuObj = GeneralModel()
            menuObj.id = "4"
            menuObj.name = context.resources.getString(R.string.photo_album)
            menuObj.iconImage = R.drawable.ic_menu_photo_album
            menuListAdapter.objList.add(menuObj)

            menuObj = GeneralModel()
            menuObj.name = context.resources.getString(R.string.edit_profile)
            menuObj.id = "1"
            menuObj.iconImage = R.drawable.ic_menu_edit
            menuListAdapter.objList.add(menuObj)
        }

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.terms_of_use)
        menuObj.id = "13"
        menuObj.iconImage = R.drawable.ic_menu_feedback
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.privacy)
        menuObj.id = "5"
        menuObj.iconImage = R.drawable.ic_menu_privacy
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.rules)
        menuObj.id = "6"
        menuObj.iconImage = R.drawable.ic_menu_rules
        menuListAdapter.objList.add(menuObj)

//        menuObj = GeneralModel()
//        menuObj.name = context.resources.getString(R.string.feedback)
//        menuObj.id = "7"
//        menuObj.iconImage = R.drawable.ic_menu_feedback
//        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.change_phone_no)
        menuObj.id = "8"
        menuObj.iconImage = R.drawable.ic_menu_changeno
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.logout)
        menuObj.id = "9"
        menuObj.iconImage = R.drawable.ic_menu_logout
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = if (Pref.getStringValue(Pref.PREF_SEARCH_PAUSED, "0") == "0")
            context.resources.getString(R.string.pause_search) else
            context.resources.getString(R.string.unpause_search)
        menuObj.id = "10"
        menuObj.iconImage = if (Pref.getStringValue(Pref.PREF_SEARCH_PAUSED, "0") == "0")
            R.drawable.ic_menu_pause_search else R.drawable.ic_menu_unpause_search
        menuListAdapter.objList.add(menuObj)

        menuObj = GeneralModel()
        menuObj.name = context.resources.getString(R.string.delete_account)
        menuObj.id = "11"
        menuObj.iconImage = R.drawable.ic_menu_delete
        menuListAdapter.objList.add(menuObj)


        menuListAdapter.addData(menuListAdapter.objList)
    }


    fun confirmationList(
        context: Context,
        btnListAdapter: ErrorBtnAdapter?,
        positiveBtnName: String = "",
        negativeBtnName: String = ""
    ) {

        var btnObj = FaqModel()
        btnObj.name = positiveBtnName.ifEmpty { context.resources.getString(R.string.yes) }
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = negativeBtnName.ifEmpty { context.resources.getString(R.string.no) }
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)

    }

    fun rejectReportList(context: Context, btnListAdapter: ErrorBtnAdapter?) {

        var btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.reject)
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.reject_and_report)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.cancel)
        btnObj.id = "2"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)

    }

    fun suretyList(context: Context, btnListAdapter: ErrorBtnAdapter?) {

        val btnObj = FaqModel().apply {
            this.name = context.resources.getString(R.string.okay)
            this.id = "0"
        }

        btnListAdapter?.objList?.add(btnObj)
        btnListAdapter?.addData(btnListAdapter.objList)

    }

    fun photoAlbumErrorList(context: Context, btnListAdapter: ErrorBtnAdapter?) {
        var btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.make_display_picture)
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.change_picture)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

//        btnObj = FaqModel()
//        btnObj.name = context.resources.getString(R.string.edit_picture)
//        btnObj.id = "2"
//        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)
    }

    fun chatIntroductionList(context: Context, btnListAdapter: ErrorBtnAdapter?) {
        var btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.get_introduced)
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.getString(R.string.dont_need_an_introduction)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.cancel)
        btnObj.id = "2"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)
    }

    fun onChangeQuestionList(context: Context, btnListAdapter: ErrorBtnAdapter?, hideQuestion : Boolean) {

        var btnObj : FaqModel

        if (hideQuestion) {
            btnObj = FaqModel()
            btnObj.name = context.resources.getString(R.string.hide_question)
            btnObj.id = "0"
            btnListAdapter?.objList?.add(btnObj)
        }

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.cancel)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)
    }

    fun photoAlbumPendingList(context: Context, btnListAdapter: ErrorBtnAdapter?) {

        var btnObj = FaqModel()
        btnObj.name = context.getString(R.string.add_photos_)
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)


        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.later)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)
    }


    fun changeConfirmationList(
        context: Context,
        btnListAdapter: ErrorBtnAdapter?
    ) {

        var btnObj = FaqModel()
        btnObj.name = context.getString(R.string.change_now)
        btnObj.id = "0"
        btnListAdapter?.objList?.add(btnObj)

        btnObj = FaqModel()
        btnObj.name = context.resources.getString(R.string.later)
        btnObj.id = "1"
        btnListAdapter?.objList?.add(btnObj)

        btnListAdapter?.addData(btnListAdapter.objList)

    }


}