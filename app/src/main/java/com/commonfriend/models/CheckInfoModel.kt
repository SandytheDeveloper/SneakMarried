package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CheckInfoModel : Serializable {

    @Expose
    @SerializedName("available_time")
    var avilableTiming: String = ""

    @Expose
    @SerializedName("img_id")
    var imgId: String = ""

    @Expose
    @SerializedName("is_show_medal")
    var isShowMedal: Int = 0


    // @Expose
    @SerializedName("is_advice")
    var isAdvice: Int = 0//1 for yes 0 for no


    @Expose
    @SerializedName("id")
    var id: String = ""

    @Expose
    @SerializedName("image")
    var image: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    var name1: String = ""

    var name2: String = ""

    var address: String = ""

    var time: String = ""

    @Expose
    @SerializedName("is_completed")
    var isCompleted = 0

    @Expose
    @SerializedName(value = "imgUrl", alternate = ["image", "img_url"])
    var imgUrl: String = ""

    @Expose
    @SerializedName("isSelected")
    var isSelected: Int = 0

    var image1: Int = 0

}
