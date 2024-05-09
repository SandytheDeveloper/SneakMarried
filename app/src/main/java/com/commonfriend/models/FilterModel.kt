package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FilterModel : java.io.Serializable {

    @Expose
    @SerializedName("id")
    var id: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("is_selected")
    var isSelected: Int = 0

}