package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SuggestionModel : Serializable {

    @Expose
    @SerializedName("id", alternate = ["user_id"])
    var id: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("family_type")
    var familyType: String = "" //0 : family  , 1 : Extended Family ,  2 : Decision Maker,-1:Candidate or Associate(Login User)

    @Expose
    @SerializedName("age")
    var age: Int = 0

    @Expose
    @SerializedName("current_location")
    var currentLocation: String = ""

    @Expose
    @SerializedName("height")
    var height: String = ""

    @Expose
    @SerializedName("dob")
    var dob: String = ""

    @Expose
    @SerializedName("disability")
    var disability: String = ""

    @Expose
    @SerializedName("relationship_status")
    var relationshipStatus: String = ""

    @Expose
    @SerializedName("professions")
    var professions: String = ""

    @Expose
    @SerializedName("religions")
    var religions: String = ""

    @Expose
    @SerializedName("culture")
    var culture: String = ""

    @Expose
    @SerializedName("caste")
    var caste: String = ""

    @Expose
    @SerializedName("status")
    var status: String = ""

    @Expose
    @SerializedName("status_dot")
    var statusDot: String = "" // 1 - visible , 0 - gone

    @Expose
    @SerializedName("settle_location")
    var settleLocation: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("finance")
    var finance: String = ""

    @Expose
    @SerializedName("is_like")
    var isLike: Int =0

    @Expose
    @SerializedName("exchange_profile")
    var exchangeProfile: String = ""
//    1 = normal button, 2 = ! mark button when user pending onboaring questions and photo album,
//    3 = receive request button, 4 = sent request and disable button, 5 = like button
    @Expose
    @SerializedName("gunn_match")
    var gunnMatch: String = ""

    @Expose
    @SerializedName("maanglik")
    var maanglik: String = ""

    @Expose
    @SerializedName("nadi")
    var nadi: String = ""

    @Expose
    @SerializedName("is_opinions")
    var isOpinions: String = "" // 0 - Enable Button

    @Expose
    @SerializedName("similar_answer")
    var similarAnswer: String = ""

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""

    @Expose
    @SerializedName("like_by")
    var likeBy: ArrayList<SuggestionModel> = ArrayList()

    @Expose
    @SerializedName("is_reference")
    var isReferences: String = "" // 0 - notFound , 1 - Reference found

    @Expose
    @SerializedName("is_ref_dot")
    var isRefDot: String = "" // 1 - show dot , 0 - hide dot

    @Expose
    @SerializedName("is_locked")
    var isProfileLocked: String = "" // 1 - locked , 0 - visible

    @Expose
    @SerializedName("reference")
    var reference: String = ""

    @Expose
    @SerializedName("progress_dot")
    var progressDot : Boolean = false // true - visible , false - gone

}