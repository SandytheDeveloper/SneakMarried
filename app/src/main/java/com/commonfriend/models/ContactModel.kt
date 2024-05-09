package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ContactModel(val names: String = "",val phone: String = "") {

    var isSelected : Int = 0

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("number")
    var number: String = ""

}