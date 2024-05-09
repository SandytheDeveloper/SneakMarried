package com.commonfriend.models

import com.commonfriend.models.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class QualificationModel : Serializable {

    @Expose
    @SerializedName("degree_name")
    var degreeName: String = ""

    @Expose
    @SerializedName("candidate_education_level")
    var degreeId: String = ""

    @Expose
    @SerializedName("college_id")
    var collegeId: String = ""

    @Expose
    @SerializedName("is_add_from_designation")
    var isAddFromDesignation: String = ""


    @Expose
    @SerializedName("is_special_certificate")
    var isSpecialCertificate: String = "" // 1 for yes 0 for no

    @Expose
    @SerializedName("is_add_other")
    var isAddOther: String = ""

    @Expose
    @SerializedName("college_name")
    var otherName: String = ""

    @Expose
    @SerializedName("candidate_college_name")
    var collegeName: String = ""

    @Expose
    @SerializedName("candidate_college_type")
    var typeName: String = ""

    @Expose
    @SerializedName("type_id")
    var typeId: String = ""


    var isSelected: Int = 0
}