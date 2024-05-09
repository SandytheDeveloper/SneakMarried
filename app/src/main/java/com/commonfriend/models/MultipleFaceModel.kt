package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MultipleFaceModel : Serializable {

    @Expose
    @SerializedName("photo_url")
    var photoUrl: String = ""


    @Expose
    @SerializedName("photo_key")
    var photoKey : String = ""

    @Expose
    @SerializedName("is_selected")
    var isSelected: Int = 0

}