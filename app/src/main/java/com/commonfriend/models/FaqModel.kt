package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FaqModel : java.io.Serializable {

    @Expose
    @SerializedName("_id")
    var id: String = ""

    @Expose
    @SerializedName("faqQuestion")
    var faqQuestion = ""

    @Expose
    @SerializedName("faqBody")
    var faqBody = ""


    @Expose
    @SerializedName("faqReference")
    var faqReference = ""


    @Expose
    @SerializedName("name")
    var name = ""

    @Expose
    @SerializedName("isOpen")
    var isOpen: Boolean = false


}
