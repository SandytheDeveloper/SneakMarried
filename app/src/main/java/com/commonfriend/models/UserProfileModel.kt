package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserProfileModel : Serializable {

    @Expose
    @SerializedName("profile_pic")
    var profilePic: String = ""

    @Expose
    @SerializedName("firstName")
    var firstName: String = ""

    @Expose
    @SerializedName("lastName")
    var lastName: String = ""

    @Expose
    @SerializedName("age")
    var age: String = ""


    @Expose
    @SerializedName("born_place")
    var bornPlace: String = ""

    @Expose
    @SerializedName("birth_time")
    var birthTime: String = ""

    @Expose
    @SerializedName("culture")
    var culture: String = ""

    @Expose
    @SerializedName("income")
    var income: String = ""

    @Expose
    @SerializedName("networth")
    var networth: String = ""

    @Expose
    @SerializedName("genderName")
    var gender: String = ""

    @Expose
    @SerializedName("settle_location")
    var settleLocation: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("title")
    var title : String = ""

    @Expose
    @SerializedName("workplace")
    var workplace: String = ""

    @Expose
    @SerializedName("industry")
    var industry: String = ""

    @Expose
    @SerializedName("native_place")
    var nativePlace: String = ""

    @Expose
    @SerializedName("education")
    var education: ArrayList<ProfileModel> = ArrayList()

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
    @SerializedName("currentLocation")
    var currentLocation: String = ""

    @Expose
    @SerializedName("dob")
    var dob: String = ""

    @Expose
    @SerializedName("height")
    var height: String = ""

    @Expose
    @SerializedName("disability")
    var disability: String = ""

    @Expose
    @SerializedName("professionName")
    var professionName: String = ""

    @Expose
    @SerializedName("designation")
    var designation: String = ""

    @Expose
    @SerializedName("religion")
    var religion: String = ""

    @Expose
    @SerializedName("personalitytraitsName")
    var personalityTraitsName: ArrayList<String> = ArrayList()


    @Expose
    @SerializedName("language")
    var language: ArrayList<String> = ArrayList()

    @Expose
    @SerializedName("relation")
    var relation: String = ""

    @Expose
    @SerializedName("caste")
    var caste: String = ""

    @Expose
    @SerializedName("has_subsidary")
    var hasSubsidary: String = ""

    @Expose
    @SerializedName("is_editable")
    var isEditable: String = "" // 0 - no , 1 - yes , 2 - already requested

    @Expose
    @SerializedName("habitEating")
    var habitEating: String = ""

    @Expose
    @SerializedName("horoscope")
    var horoscope: String = ""

    @Expose
    @SerializedName("category_id")
    var categoryId: String = ""

    @Expose
    @SerializedName("question_id")
    var questionId: String = ""

    @Expose
    @SerializedName("general_id")
    var generalId: String = ""

    @Expose
    @SerializedName("manglik")
    var manglik: String = ""

    @Expose
    @SerializedName("gunn_match")
    var gunnMatch: String = ""

    @Expose
    @SerializedName("nadi")
    var nadi: String = ""

    @Expose
    @SerializedName("zodiac_sign")
    var zodiacSign: String = ""

    @Expose
    @SerializedName("livinglocation")
    var livinglocation: String = ""

    @Expose
    @SerializedName("localitylocation")
    var localityLocation: String = ""

    @Expose
    @SerializedName("album_list")
    var albumList: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("album_screen_type")
    var albumScreenType: String = "" // 1 - FourPhoto , 2 - Single Photo

    @Expose
    @SerializedName("image")
    var imgUrl: String = ""

    @Expose
    @SerializedName("is_lock")
    var isLock: String = ""

    @Expose
    @SerializedName("is_changed")
    var isChanged: String = ""



    @Expose
    @SerializedName("candidate_profile_pic")
    var candidateProfilePic: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_name")
    var candidateName: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_age")
    var candidateAge: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_gender")
    var candidateGender: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_gotra")
    var candidateGotra: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_current_location")
    var candidateCurrentLocation: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_dob")
    var candidateDob: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_height")
    var candidateHeight: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_disability")
    var candidateDisability: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_relationship")
    var candidateRelationship: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_profession")
    var candidateProfession: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_personality")
    var candidatePersonality: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_religion")
    var candidateReligion: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_caste")
    var candidateCaste: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_eating_habit")
    var candidateEatingHabit: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_birthplace")
    var candidateBirthPlace: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_birthtime")
    var candidateBirthTime: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_designation")
    var candidateDesignation: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_culture")
    var candidateCulture: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_earning")
    var candidateEarning: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_networth")
    var candidateNetWorth: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_settle_location")
    var candidateSettleLocation: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_work_industry")
    var candidateWorkIndustry: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_native")
    var candidateNative: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_education")
    var candidateEducation: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_horoscope")
    var candidateHoroscope: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_zodiac")
    var candidateZodiac: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_living_location")
    var candidateLivingLocation: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("candidate_locality")
    var candidateLocality: ArrayList<UserProfileModel> = ArrayList()

    @Expose
    @SerializedName("error_list")
    var errorList: ArrayList<ErrorModel> = ArrayList()



}