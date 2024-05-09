package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CategoryModel : java.io.Serializable {

    @Expose
    @SerializedName("question_array")
    var questionList: ArrayList<QuestionsModel> = ArrayList()

    @Expose
    @SerializedName("category_name")
    var categoryName: String = ""

    @Expose
    @SerializedName("_id")
    var categoryId: String = ""

    var currentSelected = false

    @Expose
    @SerializedName("priority")
    var priority: String = ""

    @Expose
    @SerializedName("bottom_tag_line")
    var bottomTagLine: String = ""

    @Expose
    @SerializedName("is_hidden")
    var isHidden: String = ""

    @Expose
    @SerializedName("tag_line")
    var tagLine: String = ""

    @Expose
    @SerializedName("info_message")
    var infoMessage: String = ""

    @Expose
    @SerializedName("is_skippable")
    var isSkippable: String = ""

    @Expose
    @SerializedName("is_user_exist")
    var isUserExist: Boolean = true // if false then logout the USER

    @Expose
    @SerializedName("category_key")
    var categoryKey: String = ""

    @Expose
    @SerializedName("category_api_name")
    var categoryApiName: String = ""

    @Expose
    @SerializedName("middle_tag_line")
    var middleTagLine: String = ""

    @Expose
    @SerializedName("hand_written_texts")
    var handWrittenTexts: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("is_completed")
    var isCompleted: Int = 0


    /*For faq*/
    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("has_question")
    var hasQuestion: String = "" // 0 for questionList 1 for checklist 2 for priority


    /*FARZI*/

    var question = ""  // add question
    var type = ""  // add screen in type list
    var title = "" // add title in selected text
    var searchTitle = "" // rlspinner title
    var otherTitle = "" // select other option visible other title in dialoge
    var suggestionTitle = ""  // suggetion title in dialoge box
    var answer = -1 //
    // 3 = open collageListAdapter 4 = rowAdapter 5 = open locationListAdapter


}