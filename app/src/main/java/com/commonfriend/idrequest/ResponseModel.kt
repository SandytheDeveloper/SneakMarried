package com.commonfriend.idrequest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class ResponseModel<T> {

    @Expose
    @SerializedName("success")
    var success: Int = 0

    @Expose
    @SerializedName("message")
    var msg: String = ""

    @Expose
    @SerializedName("data")
    var data: ArrayList<T> = ArrayList()
}
