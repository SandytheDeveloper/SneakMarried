package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AssociateProfileModel : Serializable {

    @Expose
    @SerializedName("category_id")
    var categoryId: String = ""

    @Expose
    @SerializedName("question_id")
    var questionId: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("profile")
    var profile: String = ""

    @Expose
    @SerializedName("currentLocation")
    var currentLocation: String = ""

    @Expose
    @SerializedName("professionName")
    var professionName: String = ""

    @Expose
    @SerializedName("relation")
    var relation: String = ""

    @Expose
    @SerializedName("associate_name")
    var associateName: ArrayList<AssociateProfileModel> = ArrayList()

    @Expose
    @SerializedName("associate_profile")
    var associateProfile: ArrayList<AssociateProfileModel> = ArrayList()

    @Expose
    @SerializedName("associate_current_location")
    var associateCurrentLocation: ArrayList<AssociateProfileModel> = ArrayList()

    @Expose
    @SerializedName("associate_profession")
    var associateProfession: ArrayList<AssociateProfileModel> = ArrayList()

    @SerializedName("associate_relation")
    var associateRelation: ArrayList<AssociateProfileModel> = ArrayList()


}