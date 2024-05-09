package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ChatModel : Serializable {


    @Expose
    @SerializedName("chat_data")
    var chatData: ArrayList<ChatModel> = ArrayList()

    @Expose
    @SerializedName("chat_details_data")
    var chatDetailsData: ArrayList<ChatModel> = ArrayList()

    @Expose
    @SerializedName("user_details_data")
    var userDetailsData: ArrayList<ChatModel> = ArrayList()

    @Expose
    @SerializedName("ban")
    var banData: ArrayList<BanModel> = ArrayList()

    @Expose
    @SerializedName("image")
    var image: Int = 0

    @Expose
    @SerializedName("is_banned")
    var isBanned: String = ""

    @Expose
    @SerializedName("user_id")
    var userId: String = ""

    @Expose
    @SerializedName("is_first_message_send")
    var isFirstMessageSend: String = "" // if "1" then call firstMessage api

    @Expose
    @SerializedName("show_animation")
    var showAnimation: String = "" // 1 - do animation

    /*@Expose
    @SerializedName("is_date")
    var isDate: String = "" // 1 yes 0 no
*/
    @Expose
    @SerializedName("is_common_friend")
    var isCommonFriend: String = ""

    @Expose
    @SerializedName("is_typing")
    var isTyping: String = ""

    @Expose
    @SerializedName("is_removed")
    var isRemoved: Boolean = false

    @Expose
    @SerializedName("sender_profile")
    var senderPprofile: Int = 0

    @Expose
    @SerializedName("common_profile")
    var commonProfile: Int = 0

    @Expose
    @SerializedName("day")
    var day: String = ""

    @Expose
    @SerializedName("date")
    var date: String = ""

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("chat_name")
    var chatName: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("address")
    var address: String = ""

    @Expose
    @SerializedName("liked")
    var liked: String = ""

    @Expose
    @SerializedName("number")
    var number: String = ""

    @Expose
    @SerializedName("receiver_message")
    var receiverMessage: String = ""

    @Expose
    @SerializedName("receiveMessage")
    var receiveMessage: String = ""

    @Expose
    @SerializedName("sender_name")
    var senderName: String = ""

    @Expose
    @SerializedName("chat_id")
    var chatId: String = ""

    @Expose
    @SerializedName("common_sender_message")
    var commonSenderMessage: String = ""

    @Expose
    @SerializedName("common_sender_name")
    var commonSenderName: String = ""

    @Expose
    @SerializedName("gender")
    var gender: String = ""


    @Expose
    @SerializedName("stage")
    var stage: String = ""


    @Expose
    @SerializedName("match_status")
    var matchStatus: String = ""

    @Expose
    @SerializedName("sneak_peak_status")
    var sneakPeakStatus: String = ""

    @Expose
    @SerializedName("is_profile_locked")
    var recommendationType: String = ""

    @Expose
    @SerializedName("similar_answer")
    var similarAnswer: String = ""

    @Expose
    @SerializedName("common_question")
    var commonQuestions: String = ""

    @Expose
    @SerializedName("user_status")
    var userStatus: String = ""

    @Expose
    @SerializedName("sender_message")
    var senderMessage: String = ""

    @Expose
    @SerializedName("message")
    var message: String = ""

    @Expose
    @SerializedName("relation_name")
    var relation: String = ""

    @Expose
    @SerializedName("is_system_msg")
    var systemMessage: Int = 0

    @Expose
    @SerializedName("is_first_message")
    var isFirstMessage: String = "" //1 if yes 0 for no

    @Expose
    @SerializedName("is_second_message")
    var isFirstResponseMessage: String = "" //1 if yes 0 for no

    @Expose
    @SerializedName("is_info_msg")
    var infoMessage: Int = 0

    @Expose
    @SerializedName("sender_id")
    var senderId: String = ""

    @Expose
    @SerializedName("message_id")
    var messageId: String = ""

    @Expose
    @SerializedName("status")
    var status: Int = 0


    @Expose
    @SerializedName("profile_image", alternate = ["profile_pic"])
    var senderProfilePic: String = ""

    @Expose
    @SerializedName("message_count")
    var messageCount: Int = 0

    @Expose
    @SerializedName("isgroup")
    var isGroup: Int = 0  //1 Yes , 0 No

    @Expose
    @SerializedName("timePosition")
    var timePosition: Int = 0  // 1 TOP , 2 BOTTOM

    @Expose
    @SerializedName("profile_dot")
    var profileDot: String = ""

    @Expose
    @SerializedName("chat_dot")
    var chatDot: String = ""

    @Expose
    @SerializedName("show_keyboard")
    var showKeyboard: String = "" // 1 - show keyboard
}