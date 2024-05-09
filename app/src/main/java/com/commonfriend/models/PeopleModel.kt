package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PeopleModel : Serializable {





    @Expose
    @SerializedName("image")
    var image: Int = 0

    @Expose
    @SerializedName("for_screen")
    var forScreen : String = "" // 1 for suggestion 2 for profile

    @Expose
    @SerializedName("time", alternate = ["expire_time"])
    var time = ""
    @Expose
    @SerializedName("time_left")
    var timeLeft = ""

    @Expose
    @SerializedName("date")
    var date = ""

    @Expose
    @SerializedName("scheduled")
    var scheduled = ""

    @Expose
    @SerializedName("info")
    var info = ""

    @Expose
    @SerializedName("name")
    var name = ""

    @Expose
    @SerializedName("id")
    var id:String = ""

    @Expose
    @SerializedName("tag_line")
    var tagLine = ""

    @Expose
    @SerializedName("refer_id", alternate = ["_id"])
    var referId = ""

    @Expose
    @SerializedName("user_id")
    var userId = ""

    @Expose
    @SerializedName("title")
    var title = ""

    @Expose
    @SerializedName("message")
    var message = ""


    @Expose
    @SerializedName("is_locked")
    var isLocked = "" //1 for yes 0 for no

    var isClicked :Boolean = false

    @Expose
    @SerializedName("sender_profile_pic")
    var senderProfilePic = ""


    @Expose
    @SerializedName("sender_name")
    var senderName = ""


    @Expose
    @SerializedName("profile_pic")
    var profilePic = ""

    @Expose
    @SerializedName("liked_count")
    var likedCount = ""

    @Expose
    @SerializedName("status")
    var status = ""

    @Expose
    @SerializedName("status_no")
    var statusNo =
        "" //1 for incomplete 2 for reminder has been  send     3 for send remainder 4 for claimed

    @Expose
    @SerializedName("location")
    var location = ""

    @Expose
    @SerializedName("age")
    var age = ""

    @Expose
    @SerializedName("is_introduced")
    var isIntroduced = "" // 1 - introduced , 0 - not introduced

}