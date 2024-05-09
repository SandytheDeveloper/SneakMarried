package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class CountryCodeModel : Serializable {

    @Expose
    @SerializedName("name")
    var name: String= ""

    @Expose
    @SerializedName("dial_code")
    var dialCode: String= ""

    @Expose
    @SerializedName("code")
    var code: String= ""


    var isSelected = 0



}


