package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReasonsModel : Serializable {

    @Expose
    @SerializedName("_id")
    var id: String = ""

    @Expose
    @SerializedName("is_other")
    var isOther: Int = 0

    @Expose
    @SerializedName("reason")
    var reason: String = ""

    @Expose
    @SerializedName("is_selected")
    var isSelected: Int = 0


}