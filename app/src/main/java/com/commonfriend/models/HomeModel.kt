package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class HomeModel : Serializable {


    var name: String = ""



    var image = 0

    @Expose
    @SerializedName("lock")
    var lock : ArrayList<HomeModel> = ArrayList()

    @Expose
    @SerializedName("ban")
    var ban : ArrayList<BanModel> = ArrayList()

    @Expose
    @SerializedName("review")
    var review : ArrayList<HomeModel> = ArrayList()

    @Expose
    @SerializedName("your_matches")
    var matchesData: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("is_lock") // 1 - show locked widget
    var isLock : String = ""

    @Expose
    @SerializedName("is_ban") // 1 - account ban
    var isBan : String = ""

    @Expose
    @SerializedName("under_review") // 1 - Account is under Review , show review screen
    var underReviewScreen : String = ""

    @Expose
    @SerializedName("title")
    var title : String = ""

    @Expose
    @SerializedName("description")
    var description : String = ""

    @Expose
    @SerializedName("sub_description")
    var subDescription : String = ""

    @Expose
    @SerializedName("sub_description_2")
    var subDescription2 : String = ""

    @Expose
    @SerializedName("is_under_review")
    var isUnderReview : String = "" // 1 - show text else show button

    @Expose
    @SerializedName("access_code")
    var accessCode : String = ""

    @Expose
    @SerializedName("remaining_time")
    var remainingTime : String = "" // In seconds

    @Expose
    @SerializedName("hint_text")
    var hintText : String = ""


    @Expose
    @SerializedName("user_name")
    var userName: String = ""

    @Expose
    @SerializedName("user_profile_pic")
    var userProfilePic: String = ""

    @Expose
    @SerializedName("user_id")
    var userId: String = ""


    @Expose
    @SerializedName("relation")
    var relation: String = ""

    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""

    @Expose
    @SerializedName("user_role")
    var userRole: String = "" //user_role  : "1" = candidate/prospect, "2" = decision maker, "3"= family, "4" = extended family


    @Expose
    @SerializedName("is_selected")
    var isSelected: String = ""

    @Expose
    @SerializedName("_id") //0 unblock 1 blocked
    var id: String = ""

    @Expose
    @SerializedName("is_pause") //is_pause: 1=yes, 0=not
    var isPause: String = ""

    @Expose
    @SerializedName("weekly_recommendation")
    var weeklyRecommendation: String = ""

    @Expose
    @SerializedName("profile_unlock")
    var profileUnlock: String = ""

    @Expose
    @SerializedName("new_recommendation")
    var newRecommendation: String = ""

    @Expose
    @SerializedName("is_aadhar_verified")
    var isAadharVerified: String = "" // 0 - verified , 1 - not verified

    @Expose
    @SerializedName("message_data")
    var messageData: ArrayList<ChatModel> = ArrayList()

    @Expose
    @SerializedName("associate_reactions")
    var associateReactionsList: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("reference_data")
    var referenceData: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("opinion_data")
    var opinionData: ArrayList<QuestionBankModel> = ArrayList()

    @Expose
    @SerializedName("chat_intrest_received")
    var chatInterestReceived: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("referred_notification")
    var referredNotification: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("send_request")
    var sendRequestList: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("received_request")
    var receivedRequestList: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("candidate_file")
    var candidateFileList: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("sneak_peak_data")
    var sneakPeakData: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("reference_blocker")
    var referenceBlockerData: ArrayList<UserModel> = ArrayList()



    @Expose
    @SerializedName("family_member_details")
    var familyMemberDetails: ArrayList<ConnectionModel> = ArrayList()


    @Expose
    @SerializedName("reminder_data")
    var reminderData: ArrayList<ReminderModel> = ArrayList()

    @Expose
    @SerializedName("mobile_number", alternate = ["number"])
    var mobileNumber: String = ""

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("profile_dot")
    var profileDot: String = ""

    @Expose
    @SerializedName("is_user_exist")
    var isUserExist: Boolean = true

    @Expose
    @SerializedName("chat_dot")
    var chatDot: String = ""

    @Expose
    @SerializedName("delete_popup_text")
    var deletePopupText: String = ""

}