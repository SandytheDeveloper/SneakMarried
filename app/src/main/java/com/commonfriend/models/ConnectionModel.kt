package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ConnectionModel: Serializable {

    @Expose
    @SerializedName("connection_type")
    var connectionType: Int = 0 //

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("notify_connection")
    var notifyConnection: String = ""

    @Expose
    @SerializedName("font_change")
    var fontChange: String = ""


    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("_id")
    var id: String = ""


    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""


}