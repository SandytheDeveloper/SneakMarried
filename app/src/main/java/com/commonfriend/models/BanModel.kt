package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BanModel {

    @Expose
    @SerializedName("is_ban")
    var isBan: String = ""

    @Expose
    @SerializedName("title")
    var title: String = ""

    @Expose
    @SerializedName("sneak_peak_status")
    var sneakPeakStatus: String = ""

    @Expose
    @SerializedName("description")
    var description: String = ""

    @Expose
    @SerializedName("sub_description")
    var subDescription: String = ""
}