package com.commonfriend.idrequest

import com.commonfriend.utils.API_HOST
import com.commonfriend.utils.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object IDRequestBuilder {
    const val NETWORK_CALL_TIMEOUT = 300
    const val API_URL: String = BASE_URL + API_HOST

    const val REGISTER_API: String = "register"
    const val CHANGE_NUMBER_API: String = "change_number"
    const val VERIFY_OTP_API: String = "verify_otp"
    const val CHANGE_NUMBER_VERIFY_OTP_API: String = "change_number_verify_otp"
    const val RESEND_OTP_API: String = "resend_otp"
    const val QUESTION_LIST_API: String = "question_list"
    const val CHECK_LIST_API: String = "checklist"
    const val SAVE_CHECK_LIST_API: String = "save_preferences"
    const val SEND_MESSAGE_API: String = "send_message"
    const val PRIORITY_LIST_API: String = "get_priority"
    const val SAVE_PRIORITY_LIST_API: String = "save_priority"
    const val HOME_SCREEN_API: String = "home_page"
    const val READ_REFER_ME_API: String = "read_refer_me"
    const val SAVE_OPINION_API: String = "save_opinions"
    const val QUESTION_VIEW_API: String = "question_view"
    const val HIDE_UNHIDE_ANSWER_API: String = "hide_unhide_answer"
    const val CHAT_DETAIL_API: String = "chat_details"
    const val GET_QUESTION_BANK_API: String = "get_question_bank"
    const val FETCH_CHAT_API: String = "fetch_chat"
    const val DELETE_ACCOUNT_API: String = "account_delete"
    const val GET_PROFILE_API: String = "get_profile"
    const val QUESTION_CATEGORY_LIST_API: String = "get_categories"
    const val GET_QUESTION_LIST_API: String = "get_questions_list"
    const val GET_SINGLE_QUESTION_LIST_API: String = "get_single_question"
    const val SUGGESTED_LOCATION_LIST_API: String = "suggested_locations"
    const val SUGGESTION_LIST_API: String = "suggestion_list"
    const val PROFILE_LIST_API: String = "profile_list"
    const val READ_PROFILE_API: String = "read_profile"
    const val READ_EXCHANGED_PROFILE_API: String = "read_exchanged_profile"
    const val LIKE_DISLIKE_API: String = "like_dislike"
    const val SEND_EXCHANGE_API: String = "send_exchange_request"
    const val SAVE_EXCHANGE_API: String = "save_exchange_request"
    const val SEND_PROFILE_EXCHANGE_REQUEST_API: String = "send_profile_exchange_request"
    const val SAVE_PROFILE_EXCHANGE_REQUEST_API: String = "save_profile_exchange_request"
    const val RELATIONSHIP_LIST_API: String = "get_associate_relation"
    const val SEND_ASSOCIATE_REQUEST_API: String = "send_inner_circle_req"
    const val ADD_CANDIDATE_API: String = "candidate_register"
    const val FAQ_LIST_API: String = "faq_list"
    const val REFER_CONTACTS_API: String = "refer_me"
    const val PAUSE_UNPAUSE_SEARCH_API: String = "change_is_pause"
    const val LOGOUT_API: String = "logout"
    const val PRIVACY_LEGAL_API: String = "privacy_legal"
    const val READ_REFERENCES_API: String = "read_references"
    const val SEND_FEEDBACK_API: String = "send_feedback"
    const val GET_DISCARD_REASON_API: String = "get_discard_reason"
    const val READ_SNEAK_PEAK_API: String = "read_sneak_peak"
    const val READ_CHAT_API: String = "read_chat"
    const val UPLOAD_PHOTO_ALBUM_API: String = "upload_photo_album"
    const val GET_PHOTO_ALBUM_API: String = "get_photo_album"
    const val SAVE_UPLOAD_PHOTO_API: String = "save_upload_photo"
    const val GET_SET_NOTIFICATIONS_API: String = "notification"
    const val GET_STREAM_CHAT_TOKEN: String = "get_stream_chat_token"
    const val GET_AADHAR_OTP_API: String = "get_aadhar_otp"
    const val VERIFY_AADHAR_OTP_API: String = "verify_aadhar_otp"
    const val ADD_CONTRIBUTE_QUESTIONS_API: String = "add_contribute_questions"
    const val CHAT_INTRO_SETTING_API: String = "chat_intro_setting"
    const val READ_FIRST_MESSAGE_API: String = "read_first_message"
    const val ACCESS_CODE_APPLY_API: String = "access_code_apply"
    const val ON_BOARDING_SKIP_API: String = "onboarding_skip"



    private var ipRequestService: IDRequestService? = null


    fun getRequestBuilder(): IDRequestService {

        if (ipRequestService == null)
            ipRequestService = create(API_URL)
        return ipRequestService!!
    }


    private fun create(baseUrl: String): IDRequestService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                OkHttpClient.Builder()
//                    .cache(Cache(cacheDir, 10 * 1024 * 1024 ))// 10MB
                    .addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    )

                    .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .callTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build().create(IDRequestService::class.java)
    }
}