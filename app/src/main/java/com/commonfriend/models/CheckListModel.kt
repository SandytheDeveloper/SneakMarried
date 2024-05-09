package com.commonfriend.models

import com.commonfriend.adapter.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckListModel {
    var title = ""
    var religion = ""
    var surName = ""
    var surNameSuggestion = ""
    var location = ""
    var age = ""

    @Expose
    @SerializedName("is_selected")
    var isSelected = 0

//    @Expose
//    @SerializedName("user_onboarding_data")
//    var userOnboardingSelectedId = ""

    @Expose
    @SerializedName("is_enable")
    var isEnabled = ""

    @Expose
    @SerializedName("is_any")
    var isAny = ""

    @Expose
    @SerializedName("selected_age")
    var selectedAge = ""

    @Expose
    @SerializedName("selected_height")
    var selectedHeight = ""

    @Expose
    @SerializedName("category_id")
    var categoryId = ""

    @Expose
    @SerializedName("general_id")
    var generalId = ""

    @Expose
    @SerializedName("question_id")
    var questionId = ""

    var type = 0
    var year = 0

    @Expose
    @SerializedName("id", alternate = ["_id"])
    var id: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("latitude")
    var latitude: String = ""

    @Expose
    @SerializedName("longitude")
    var longitude: String = ""

    @Expose
    @SerializedName("has_subsidary")
    var hasSubsidary: String = "" //1 for yes 0 for no

    @Expose
    @SerializedName("gotra_data")
    var gotraData: ArrayList<CheckListModel> = ArrayList()


    @Expose
    @SerializedName("selected_maglik")
    var selectedMaglik: ArrayList<CheckListModel> = ArrayList()


    @Expose
    @SerializedName("selected_dosh")
    var selectedDosh: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("selected_direct_reference")
    var selectedDirectReference: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("selected_disability")
    var selectedDisability: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("seleted_surname_yes")
    var seletedSurnameYes: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("seleted_surname_no")
    var selectedSurnameNo: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("relegion_data")
    var religionList: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("array_data")
    var data: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("castes_data")
    var castesList: ArrayList<CheckListModel> = ArrayList()

//    @Expose
//    @SerializedName("culture_data")
//    var cultureList: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("relation_data")
    var relationshipList: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("settle_location_data")
    var settleLocationData: ArrayList<GeneralModel> = ArrayList()

    @Expose
    @SerializedName("habitseating_data")
    var eatingHabitsList: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("ideal_age")
    var idealAge: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("ideal_height")
    var idealHeight: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("professions_data")
    var professionsList: ArrayList<CheckListModel> = ArrayList()

    @Expose
    @SerializedName("suggestion_list_surname")
    var suggestedSurnameList: ArrayList<CheckListModel> = ArrayList()



}