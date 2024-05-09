package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class QuestionBankModel : java.io.Serializable {

    @Expose
    @SerializedName("_id", alternate = ["id"])
    var id = ""  // question id

    @Expose
    @SerializedName("question_title")
    var questionTitle = ""

    @Expose
    @SerializedName("question")
    var question = ""  // add question


    @Expose
    @SerializedName("question_number")
    var questionNumber: String = ""

    @Expose
    @SerializedName("card_color")
    var cardColor: String = "" // #0B99FF


    @Expose
    @SerializedName("doButtonAnimation")
    var doButtonAnimation: Boolean = false


    @Expose
    @SerializedName("is_contributed")
    var isContributed: String = "" // 1 - show , 0 - hide

    @Expose
    @SerializedName("option_a")
    var optionA : String = ""

    @Expose
    @SerializedName("option_b")
    var optionB : String = ""



    @Expose
    @SerializedName("info_message")
    var infoMessage: String = ""


    @Expose
    @SerializedName("hidden_privacy_message")
    var hiddenPrivacyMessage : String = "" // Show only answered question is hidden


    @Expose
    @SerializedName("is_shield")
    var shield : String = "" // 1 : Shield Question , 0: Not Shield


    @Expose
    @SerializedName("rating")
    var rating : String ="" // between 1 to 3

    @Expose
    @SerializedName("is_mandatory")
    var isMandatory : String = "" // 1 : Mandatory , 0: Not mandatory

    @Expose
    @SerializedName("status")
    var status : String = "" // 0: not answer, 1 : answered , 2 : Answer with hidden


    @Expose
    @SerializedName("is_random")
    var isRandom = 0 // Question Filter type set as random

    @Expose
    @SerializedName("mandatory_count")
    var mandatoryCount : String = "0" // Count of How many Question Mandatory Required



    @Expose
    @SerializedName("my_selected_answer")
    var mySelectedAnswer : String = "" // Selected(Given) Answered by User

    @Expose
    @SerializedName("other_selected_answer")
    var otherSelectedAnswer : String = "" // Selected(Given) Answered by Opposite User

    @Expose
    @SerializedName("is_hidden")
    var isHidden : Int = 0 // 0: not Hidden, 1 : Hidden

    @Expose
    @SerializedName("sneak_available")
    var sneakAvailable : String = "0" // 1 - show popup animation















}