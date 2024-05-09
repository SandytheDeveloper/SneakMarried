package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class EditDetailsModel: Serializable {

    @Expose
    @SerializedName("id")
    var id: Int = 0

    @Expose
    @SerializedName("edit_item")
    var editItem: String = ""

    @Expose
    @SerializedName("isSelected")
    var isSelected: Int = 0
}