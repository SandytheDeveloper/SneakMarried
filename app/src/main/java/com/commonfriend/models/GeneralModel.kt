package com.commonfriend.models

import com.commonfriend.adapter.InnerAdapterAdapter
import com.commonfriend.models.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GeneralModel : Serializable {

    @Expose
    @SerializedName("gender_name", alternate = ["name", "associate_relation"])
    var name: String = ""

    @Expose
    @SerializedName("_id", alternate = ["id"])
    var id: String = ""

    @Expose
    @SerializedName("is_special_certificate")
    var isSpecialCertificate: String = "" // 1 for yes 0 for no


    @Expose
    @SerializedName("is_add_from_designation")
    var isAddFromDesignation: String = ""

    @Expose
    @SerializedName("has_subsidary")
    var hasSubsidary: String = ""

    @Expose
    @SerializedName("has_gotra")
    var hasGotra = ""  // add question

    @Expose
    @SerializedName("description")
    var description: String = ""

    @Expose
    @SerializedName("level")
    var level: Int = 0

    @Expose
    @SerializedName("suggestion_list")
    var suggestestionList: ArrayList<QuestionsModel> = ArrayList()

    @Expose
    @SerializedName("array_data")
    var arrayData: ArrayList<GeneralModel> = ArrayList()

    @Expose
    @SerializedName("selected_data_array")
    var selectedDataArray: ArrayList<GeneralModel>? = ArrayList()


    var isSelected: Int = 0
    var iconImage: Int = 0


    @Expose
    @SerializedName("is_enable")
    var isEnabled = ""

    @Expose
    @SerializedName("category_id")
    var categoryId = ""


    @Expose
    @SerializedName("question_id")
    var questionId = ""

    @Expose
    @SerializedName("general_id")
    var generalId = ""

//For Education Detail

    @Expose
    @SerializedName("candidate_education_level")
    var candidateEducationLevel: String = ""

    @Expose
    @SerializedName("candidate_college_name")
    var candidateCollegeId: String = ""

    @Expose
    @SerializedName("other_name")
    var otherName: String = ""

    @Expose
    @SerializedName("coure_name")
    var courseName: String = ""

    @Expose
    @SerializedName("candidate_college_type")
    var candidateCollegeType: String = ""

    @Expose
    @SerializedName("is_any")
    var isAny: String = ""

    @Expose
    @SerializedName("is_add_other")
    var isAddOther: String = ""


    /*FOR LOCATION (ADDRESS MODEL)*/

    @Expose
    @SerializedName("place_id", alternate = ["placeId"])
    var placeId: String = ""


    var isSuggested: Boolean = false

    var type: String = "" // 1 home 2 work 3 others

    @Expose
    @SerializedName("address")
    var address: String = ""

    @Expose
    @SerializedName("latitude")
    var latitude: String = ""

    @Expose
    @SerializedName("longitude")
    var longitude: String = ""

    @Expose
    @SerializedName("city")
    var city: String = ""

    @Expose
    @SerializedName("area")
    var area: String = ""

    @Expose
    @SerializedName("zipcode")
    var zipcode: String = ""

    @Expose
    @SerializedName("state")
    var state: String = ""

    @Expose
    @SerializedName("country")
    var country: String = ""

    @Expose
    @SerializedName("isCountry")
    var isCountry: Boolean = false

    @Expose
    @SerializedName("isState")
    var isState: Boolean = false

    @Expose
    @SerializedName("isCity")
    var isCity: Boolean = false

    @Expose
    @SerializedName("isArea")
    var isArea: Boolean = false

    @Expose
    @SerializedName("sub_array_data")
    var subArrayData: ArrayList<GeneralModel> = ArrayList()

    var value: String = ""

     var qualificationListAdapter: InnerAdapterAdapter?=null
//    var fourTemplateAdapter: FourTemplateAdapter? = null

}