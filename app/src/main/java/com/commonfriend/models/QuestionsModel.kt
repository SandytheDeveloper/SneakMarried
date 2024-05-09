package com.commonfriend.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class QuestionsModel : java.io.Serializable {


    @Expose
    @SerializedName("question")
    var question = "" // add question

    @Expose
    @SerializedName("is_special_certificate")
    var isSpecialCertificate = ""


    @Expose
    @SerializedName("has_subsidary")
    var hasSubsidary = "" // add question


    @Expose
    @SerializedName("has_gotra")
    var hasGotra = "" // add question



    @Expose
    @SerializedName("location_delay")
    var locationDelay = "" // add question


    @Expose
    @SerializedName("_id", alternate = ["id"])
    var id = "" // question id


    @Expose
    @SerializedName("placeholder_text")
    var searchTitle = "" // rlspinner title

    @Expose
    @SerializedName("default_text")
    var defaultText = ""

    @Expose
    @SerializedName("is_lock")
    var isLock = ""


    var otherTitle = "" // select other option visible other title in dialoge


    @Expose
    @SerializedName("dailog_placeholder_text")
    var dialogTitle = "" // dialoge box title in header


    @Expose
    @SerializedName("suggested_text")
    var suggestionTitle = "" // suggetion title in dialoge box


    var religionTitle = "" // select text in dialoge box add in religion title


    @Expose
    @SerializedName("answer")
    var answer = ""

    @Expose
    @SerializedName("event_name")
    var eventName = ""


    @Expose
    @SerializedName("skippable_by")
    var skipable = "" //0 for none,1 for candidate,2 for associate,3 for both


    @Expose
    @SerializedName("location_answer")
    var locationAnswer: ArrayList<GeneralModel> = ArrayList()


    var answerString = "" // replace select answer string in dialoge


    /*FOR ADAPTERS*/


    @Expose
    @SerializedName("name", alternate = ["course_name"])
    var optionName = "" //objlist option name


    var isSelected = 0


    @Expose
    @SerializedName("template_code")
    var templateType: Int = 0

    @Expose
    @SerializedName("is_skippable")
    var isSkippable: String = ""

    @Expose
    @SerializedName("is_any")
    var isAny: String = ""


    @Expose
    @SerializedName("longitude")
    var longitude: String = ""


    @Expose
    @SerializedName("latitude")
    var latitude: String = ""


    @Expose
    @SerializedName("description")
    var description: String = ""


    @Expose
    @SerializedName("general_id")
    var generalId: String = ""


    @Expose
    @SerializedName("question_key")
    var questionKey: String = ""


    @Expose
    @SerializedName("priority")
    var priority: String = ""


    @Expose
    @SerializedName("is_master_required")
    var isMasterRequired: String = ""


    @Expose
    @SerializedName("master_api_name")
    var masterApiName: String = ""


    @Expose
    @SerializedName("hand_written_texts", alternate = ["hand_written_text"])
    var handWrittenTexts: ArrayList<String> = ArrayList()


    @Expose
    @SerializedName("info_message")
    var infoMessage: String = ""


    @Expose
    @SerializedName("message")
    var message: String = ""


}

