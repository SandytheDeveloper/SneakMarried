package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ErrorModel {

    @Expose
    @SerializedName("error")
    var error: String = ""

}