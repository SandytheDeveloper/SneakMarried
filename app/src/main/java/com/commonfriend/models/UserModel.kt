package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserModel : Serializable {

  /*  @Expose
    @SerializedName("mobile_no")
    var mobileNo: String = ""*/


    @Expose
    @SerializedName("name")
    var name: String = ""

    var countryCode: String = ""

    @Expose
    @SerializedName("user_name")
    var userName: String = ""

    @Expose
    @SerializedName("gender")
    var gender: String = ""

    @Expose
    @SerializedName("is_profile_edited")
    var isProfileConfirmed: String = ""

    @Expose
    @SerializedName("chat_token")
    var chatToken: String = ""

    var image = 0

//    @Expose
//    @SerializedName("OTP")
//    var otp: String = ""

    @Expose
    @SerializedName("relation", alternate = ["relation_name"])
    var relation: String = ""



    @Expose
    @SerializedName("relation_data")
    var relationData: GeneralModel = GeneralModel()

    @Expose
    @SerializedName("relation_data_list")
    var relationDataList: ArrayList<GeneralModel> = ArrayList<GeneralModel>()

    @Expose
    @SerializedName("user_id")
    var userId: String = ""

    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""

    @Expose
    @SerializedName("reference_id")
    var referenceId: String = ""

    @Expose
    @SerializedName("phone_book_id")
    var phoneBookId: String = ""

    @Expose
    @SerializedName("is_registered")
    var isRegistered: String = ""

    @Expose
    @SerializedName("is_claimed")
    var isClaimed: String = ""

    @Expose
    @SerializedName("add_claimed_blocker")
    var addClaimedBlocker: Int = 0 // 1 : Need To Claim Profile , 0 : No need to Claim Profile


    @Expose
    @SerializedName("user_claimed_data")
    var userClaimedData: UserModel?=null

    @Expose
    @SerializedName("candidate_relation") //ID
    var candidateRelation: String = ""

    @Expose
    @SerializedName("tag_line") //
    var tagLine: String = ""

    @Expose
    @SerializedName("hand_written_texts", alternate = ["hand_written_text"])
    var handWrittenTexts: ArrayList<String> = ArrayList()





    @Expose
    @SerializedName("register_for") // User Role 1: Candidate , 2 : Associate
    var userRole: String = ""

    @Expose
    @SerializedName("is_selected")
    var isSelected: String = ""

    @Expose
    @SerializedName("_id" ,alternate = ["id"])
    var id: String = ""

    @Expose
    @SerializedName("candidate_id")
    var candidateId: String = ""

    @Expose
    @SerializedName("is_block") //0 unblock 1 blocked
    var isBlocked: String = ""

    @Expose
    @SerializedName("is_safe") // safe , unsafe , unknown
    var isSafe: String = ""

    @Expose
    @SerializedName("is_profile_approved")
    var isProfileApproved: String = ""

    @Expose
    @SerializedName("is_profile_completed")
    var isProfileCompleted: String = ""

    @Expose
    @SerializedName("token")
    var token: String = ""

    @Expose
    @SerializedName("message_data")
    var messageData: ArrayList<ChatModel> = ArrayList()

    @Expose
    @SerializedName("profile_alert")
    var profileAlertList: ArrayList<PeopleModel> = ArrayList()

    @Expose
    @SerializedName("opinion_data")
    var opinionData: ArrayList<QuestionBankModel> = ArrayList()

    @Expose
    @SerializedName("family_data")
    var familyData: ArrayList<UserModel> = ArrayList()

    @Expose
    @SerializedName("contact_data")
    var contactDetail: ContactModel = ContactModel()

    @Expose
    @SerializedName("connection_data")
    var connectionData: ArrayList<ConnectionModel> = ArrayList()

    @Expose
    @SerializedName("reminder_data")
    var reminderData: ArrayList<ReminderModel> = ArrayList()

    @Expose
    @SerializedName("status") //pending 0 accept 1 ignore 2 cancel 3
    var requestType: String = ""

    @Expose
    @SerializedName("mobile_number", alternate = ["number","mobile_no"])
    var mobileNumber: String = ""

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("family_type")
    var familyType: String = "" //0=family, 1= extended_family, 2=decision maker

    @Expose
    @SerializedName("is_uploaded_contact")
    var is_uploaded_contact: String = ""

    @Expose
    @SerializedName("is_priority_given")
    var is_priority_given: String = ""

    @Expose
    @SerializedName("is_check_list_provided")
    var is_check_list_provided: String = ""

    @Expose
    @SerializedName("is_ban")
    var isBan: String = ""

    @Expose
    @SerializedName("under_review")
    var underReview: String = ""

    @Expose
    @SerializedName("chat_introduction")
    var chatIntroduction: String = ""

}