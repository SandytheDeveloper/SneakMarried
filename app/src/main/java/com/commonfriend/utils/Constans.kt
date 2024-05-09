package com.commonfriend.utils

import android.net.Uri
import com.commonfriend.models.CategoryModel
import com.commonfriend.models.ProfileModel
import io.getstream.chat.android.client.ChatClient

//const val BASE_URL: String = "http://107.23.138.139:5000/"
//Live url
const val BASE_URL: String = "https://api.commonfriend.com/"
const val API_HOST: String = "api/v2/"
const val PRIVACY_URL: String = "http://107.23.138.139:5000/"
const val LEGAL_URL: String = "http://107.23.138.139:5000/"
const val TERMS_AND_CONDITION_URL: String = "http://107.23.138.139:5000/"

var MAIN_PROFILE_LIST: ArrayList<ProfileModel> = ArrayList()

//var SHOW_ANIM : ArrayList<String> = ArrayList()
//var SHOWANIMA = false

const val ID: String = "ID"
const val QUESTION_ID: String = "QUESTION_ID"
const val GENDER: String = "GENDER"
const val STAGE: String = "STAGE"
const val USER_ID: String = "USER   _ID"
const val CHAT_ID: String = "CHAT_ID"
const val DATA: String = "DATA"
const val URLS: String = "URLS"
const val IS_FROM: String = "IS_FROM"
const val PUSH_NOTIFICATION = "PUSH_NOTIFICATION"
const val PAUSE_SEARCH = "PAUSE_SEARCH"
const val STATUS = "STATUS"
const val CHANNEL_ID = "CHANNEL_ID"
var FOR_FIRST_TIME = true // for floating button
var LAST_POS = -1
var ALBUM_LAST_POS = -1
var FROM_BACK_PRESSED = false
var FOR_SCREEN = 1
var RELATIONSHIP_STATUS = "1"
var WORK_AS = "1" //to remove category of professional
var CHAT_ID_FOR_NOTIFICATION = ""
var CID_FOR_NOTIFICATION = ""
var ONBOARDING_SKIP = false
const val HASH_SEPERATOR: String = "###"
const val COMMA_SPACE_SEPERATOR: String = ", "
const val DEVICE_TYPE: String = "1"     // 1=Android 2=iOS
const val EN: String = "en"
const val API_KEY: String = "AIzaSyCwokoXzd5Ox8rjwk1LkQQLzIEnrXxhtN8"
const val GET_ADDRESS = "getaddress"
var SELECTED_FILTER = "" // Selected Filter for Profile Screen
const val REQUEST_CODE = 50
const val REQUEST_CODE_FOR_SINGLE_IMAGE = 51
const val REQUEST_CODE_2 = 200
const val REQUEST_CODE_1 = 100


enum class API_RESULT {
    SUCCESS, FAIL
}

const val DEFAULT_LANGUAGE: String = EN // Default : EN

/*AWS PHOTO UPLOADING*/
const val BUCKET_REGION = "us-east-1"
const val BUCKET_FOLDER_NAME = "GetMarried"
const val BUCKET_NAME = "getmarried"
const val COGNITO_POOL_ID = "us-east-1:08b1f93a-e430-4105-a5fb-f3516faf5597"
const val dialogDisplayTimer: Long = 500


const val ABLY_KEY: String = "8g8nEg.VtPnEg:TF4VC2fN5f972kAT2QcTO36m0oSqMj1rl_XlACgSEco"
const val CHAT_CHANNEL: String = "chat_channel"


/*const val BUCKET_REGION = "us-east-1"
const val BUCKET_FOLDER_NAME = "GetMarried"
const val BUCKET_NAME = "getmarried"
const val COGNITO_POOL_ID = "us-east-1:1befb098-e1d6-442d-a2b3-a0dfef6e2d3d"*/


var mainObjList: ArrayList<CategoryModel> = ArrayList()
var PROFILE_IMAGE: Uri? = null
var REQUEST_IMAGE: Int = 0
var SELECTED_IMAGE: String = ""
var EDIT_CAPTION2: Array<String> = arrayOf("", "", "", "")
var CATEGORY_ID: Int = 0

enum class FaqIsFrom {
    CONTACTS_PERMISSION,
    LOCATION_PERMISSION,
    OTHER,
    PROFILE_LOCKED,
    REFERENCE,
    OPINIONS_AND_INTEREST,
}

enum class ActivityIsFrom {
    NORMAL,
    FROM_EDIT,
    FROM_CHECKLIST,
    FROM_HOME,
    FROM_MENU,
    FROM_CREATE_PROFILE,
    FROM_ADD_ASSOCIATE,
    REFER_NOW,
    QUESTION_LIST,
    FILTERED_QUESTION_LIST,
    QUESTION_OF_THE_DAY,
    SNEAK_PEAK,
    PROFILE,
    CANDIDATE_ALBUM,
    ASSOCIATE_ALBUM,
    NOTIFICATION,
    CHAT_SCREEN,
    LOCKED_ACCOUNT,
    FROM_EDIT_SECTION
}

enum class DiscardIsFrom {
    PROFILE,
    CHAT
}

enum class AlbumIsFrom {
    GROUP,
    CROP
}

enum class ChatKeys(val key: String) {
    USER_ID("user_id"),
    SENDER_ID("sender_id"),
    CHAT_ID("chat_id"),
    STATUS("status"),
    IS_TYPING("is_typing"),
    IS_COMMON_FRIEND("is_common_friend")
}


enum class Screens(val screenName: String, val screenType: String) {
    SPLASH_SCREEN("splash", "splash"),
    INTRO_SCREEN("intro_screen", "intro"),
    MOBILE_NO_SCREEN("login_screen", "login"),
    OTP_SCREEN("otp_screen", "login"),
    VERIFICATION_SCREEN("verification_done_screen", "login"),
    SECTION_BREAKER_SCREEN("", "section_breaker"),
    QUESTION_SCREEN("", "question"),
    PHOTO_ALBUM_SCREEN("photo_album_screen", "photo_album"),
    EDIT_PROFILE_CONF_SCREEN("edit_profile_confirmation", "edit_profile"),
    CHECKLIST_SCREEN("checklist_screen", "filters"),
    PRIORITY_SCREEN("priority_screen", "filters"),
    HOME_SCREEN("home_screen", "home"),
    PROFILE_SCREEN("profile_screen", "profile"),
    CHAT_LIST_SCREEN("chat_list_screen", "chat"),
    CHAT_SCREEN("chat_screen", "chat"),
    MENU_SCREEN("menu_screen", "menu"),
    SNEAK_PEAK_SCREEN("sneak_peak_screen", "sneak_peak"),
    NOTIFICATION_SCREEN("notification_screen", "notification"),
    QUESTION_BANK_SCREEN("questions_bank_screen", "sneak_peak"),
    DISCARD_SCREEN("discard_screen", "discard"),
    EDIT_PROFILE_SCREEN("edit_profile_screen", "edit_profile"),
    TERMS_SCREEN("", "terms"),
    CHANGE_MOB_NO_SCREEN("change_number_screen", "login"),
    AADHAR_SCREEN("", "aadhar"),
    STEPS_SCREEN("steps", "steps"),

}
