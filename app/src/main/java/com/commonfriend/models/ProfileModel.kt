package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProfileModel : Serializable {

    @Expose
    @SerializedName("filter_data")
    var filterData: ArrayList<FilterModel> = ArrayList()

    @Expose
    @SerializedName("profile_data")
    var profileData: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("id")
    var id: String = ""

    @Expose
    @SerializedName("user_status")
    var userStatus: String = ""

    @Expose
    @SerializedName("name")
    var name: String = ""

    @Expose
    @SerializedName("age")
    var age: Int = 0

    @Expose
    @SerializedName("current_location")
    var currentLocation: String = ""

    @Expose
    @SerializedName("locality")
    var locality: String = ""

    @Expose
    @SerializedName("height")
    var height: String = ""

    @Expose
    @SerializedName("dob")
    var dob: String = ""

    @Expose
    @SerializedName("gender")
    var gender: String = ""

    @Expose
    @SerializedName("disability")
    var disability: String = ""

    @Expose
    @SerializedName("relationship_status")
    var relationshipStatus: String = ""

    @Expose
    @SerializedName("professions")
    var professions: String = ""

    @Expose
    @SerializedName("religions")
    var religions: String = ""

    @Expose
    @SerializedName("culture")
    var culture: String = ""

    @Expose
    @SerializedName("caste")
    var caste: String = ""

    @Expose
    @SerializedName("languages")
    var languages: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("status")
    var status: String = "" //1 = new status, 2 = introduces

    @Expose
    @SerializedName("status_text")
    var statusText: String = ""

    @Expose
    @SerializedName("status_dot")
    var statusDot: String = "" // 1 - visible , 0 - gone

    @Expose
    @SerializedName("designation")
    var designation: String = ""

    @Expose
    @SerializedName("office")
    var office: String = ""

    @Expose
    @SerializedName("work_place_title")
    var workPlaceTitle: String = ""

    @Expose
    @SerializedName("industry")
    var industry: String = ""

    @Expose
    @SerializedName("eating_habit")
    var eatingHabit: String = ""

    @Expose
    @SerializedName("is_aadhar_verified")
    var isAadharVerified: String = ""

    @Expose
    @SerializedName("birth_place")
    var birthPlace: String = ""

    @Expose
    @SerializedName("birth_time")
    var birthTime: String = ""

    @Expose
    @SerializedName("income")
    var income: String = ""

    @Expose
    @SerializedName("settle_location")
    var settleLocation: String = ""

    @Expose
    @SerializedName("personality_traits")
    var personalityTraits: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("album_list")
    var albumList: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("album_screen_type")
    var albumScreenType: String = "" // 1 - FourPhoto , 2 - Single Photo

    @Expose
    @SerializedName("finance")
    var finance: String = ""

    @Expose
    @SerializedName("is_like")
    var isLike: Int =0

    @Expose
    @SerializedName("is_pending_questions")
    var isPendingQuestions: String = "" // 0 = nothing is pending, 1 = edit profile, 2 = Question bank pending, 3 = album pending

    @Expose
    @SerializedName("exchange_button")
    var exchangeButton: String = ""
//    1 = normal button, 2 = ! mark button when user pending onboarding questions and photo album,
//    3 = Request Received but mandatory questions are pending, 4 = Chat request received, 5 = sent request and disable button, 6 = Like button For Family and Extended Family

    @Expose
    @SerializedName("gunn_match")
    var gunnMatch: String = ""

    @Expose
    @SerializedName("maanglik")
    var maanglik: String = ""

    @Expose
    @SerializedName("nadi")
    var nadi: String = ""


    @Expose
    @SerializedName("common_question")
    var commonQuestions: String = ""

    @Expose
    @SerializedName("is_opinions")
    var isOpinions: String = "" // 0 - Not Enable Button , 1 :  Show Enable Button

    @Expose
    @SerializedName("opinion_view")
    var opinionView: String = "" // 1 to 6

    @Expose
    @SerializedName("similar_answer")
    var similarAnswer: String = "0"

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""

    @Expose
    @SerializedName("like_by")
    var likeBy: ArrayList<SuggestionModel> = ArrayList()

    @Expose
    @SerializedName("in_touch")
    var inTouch: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("request_send_users")
    var requestSendUsers: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("is_sender") // 1 sender , 2 receiver
    var isSender: String = ""

    @Expose
    @SerializedName("sneak_peak_dot") // 1 visible , 0 hide
    var sneakPeakDot: String = ""

    @Expose
    @SerializedName("sneak_peak_text") // 1 sender , 2 receiver
    var sneakPeakText: String = ""

    @Expose
    @SerializedName("sneak_peak_button") // 1 sender , 2 receiver
    var sneakPeakButton: String = "" // Four buttons and keys 1 to 4

    @Expose
    @SerializedName("sneak_peak_viewed") // 1 yes , 0 no
    var sneakPeakViewed: String = ""

    @Expose
    @SerializedName("sneak_peak_status")
    var sneakPeakStatus: String = ""

    @Expose
    @SerializedName("sneak_peak_view_by") // 1 sender , 2 receiver
    var sneakPeakViewBy: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("education")
    var educationList: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("from_user_profile")
    var fromUserProfile: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("to_user_profile")
    var toUserProfile: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("course_name")
    var courseName: String = ""

    @Expose
    @SerializedName("higher_education_name")
    var higherEducationName: String = ""

    @Expose
    @SerializedName("college_name")
    var collegeName: String = ""

    @Expose
    @SerializedName("is_reference")
    var isReferences: String = "" // 0 - notFound , 1 - Reference found

    @Expose
    @SerializedName("is_contact_require")
    var isContactRequire: String = "" // 0 - Not Required, 1 - Contact Required

    @Expose
    @SerializedName("is_profile_lock")
    var isProfileLocked: String = "" // 1 - locked , 0 - visible

    @Expose
    @SerializedName("reference")
    var reference: String = ""

    @Expose
    @SerializedName("chat_id")
    var chatId: String = ""

    @Expose
    @SerializedName("cid")
    var cid: String = ""

    @Expose
    @SerializedName("zodiac")
    var zodiac: String = ""

    @Expose
    @SerializedName("progress_dot")
    var progressDot : Boolean = false // true - visible , false - gone

    @Expose
    @SerializedName("button_type")
    var buttonType : Boolean = false

    @Expose
    @SerializedName("filter_list")
    var filterList: ArrayList<ProfileModel> = ArrayList()

    @Expose
    @SerializedName("mobile_no")
    var mobileNo: String = ""

    @Expose
    @SerializedName("sneak_available")
    var sneakAvailable: String = "" // 1 - show animation , else - do nothing

    @Expose
    @SerializedName("question_left")
    var questionLeft: String = ""

    @Expose
    @SerializedName("show_opinions")
    var showOpinions: String = "" // 1 - not show opinion and set "0" to opinionProgressBar

    @Expose
    @SerializedName("profile_dot")
    var profileDot: String = ""

    @Expose
    @SerializedName("chat_dot")
    var chatDot: String = ""

    @Expose
    @SerializedName("is_first_request")
    var isFirstRequest: String = "" // 1 = yes, 2 = no

    @Expose
    @SerializedName("is_lock")
    var isLock: String = ""

    @Expose
    @SerializedName("show_error")
    var showError: String = "" // 1 - show error

    @Expose
    @SerializedName("title")
    var title: String = "" // 1 - show error

    @Expose
    @SerializedName("description")
    var description: String = "" // 1 - show error

    @Expose
    @SerializedName("is_banned")
    var isBanned: String = "" // 1 - show Ban animation

}