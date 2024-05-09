package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class NotificationModel:Serializable {

    @Expose
    @SerializedName("user_id")
    var userId: String = ""

    @Expose
    @SerializedName("all_noti")
    var allNotifications: String = ""

    @Expose
    @SerializedName("question_noti")
    var questionsNotifications: String = ""

    @Expose
    @SerializedName("connection_noti")
    var connectionNotifications: String = ""

    @Expose
    @SerializedName("recommendation_noti")
    var recommendationNotifications: String = ""

    @Expose
    @SerializedName("message_noti")
    var messageotifications: String = ""

}