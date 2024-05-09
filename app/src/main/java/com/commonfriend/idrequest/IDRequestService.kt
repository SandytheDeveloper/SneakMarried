package com.commonfriend.idrequest

import com.commonfriend.models.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*


interface IDRequestService {

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.REGISTER_API)
    fun registerApiRequest(
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.CHANGE_NUMBER_API)
    fun changeNumberApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.VERIFY_OTP_API)
    fun verifyOtpApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.CHANGE_NUMBER_VERIFY_OTP_API)
    fun changeNumberVerifyOtpApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.RESEND_OTP_API)
    fun resendOtpApiRequest(
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.QUESTION_CATEGORY_LIST_API)
    fun categoryListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<CategoryModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_QUESTION_LIST_API)
    fun getQuestionListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<QuestionsModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_SINGLE_QUESTION_LIST_API)
    fun getSingleQuestionListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<CategoryModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.QUESTION_LIST_API)
    fun questionListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<CategoryModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.CHECK_LIST_API)
    fun checkListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<CheckListModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.PRIORITY_LIST_API)
    fun getPriorityApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<String>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.HOME_SCREEN_API)
    fun homePageApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<HomeModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_REFER_ME_API)
    fun readReferredApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_PROFILE_API)
    fun getProfileApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserProfileModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_CHECK_LIST_API)
    fun saveCheckListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_OPINION_API)
    fun saveOpinionApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.QUESTION_VIEW_API)
    fun questionViewApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.HIDE_UNHIDE_ANSWER_API)
    fun hideUnhideAnswerApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SEND_MESSAGE_API)
    fun sendMessageApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.DELETE_ACCOUNT_API)
    fun deleteUserApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.CHAT_DETAIL_API)
    fun chatDetailApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ChatModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_FIRST_MESSAGE_API)
    fun readFirstMessageApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_QUESTION_BANK_API)
    fun getQuestionBankApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<QuestionBankModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.FETCH_CHAT_API)
    fun fetchChatApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ChatModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_PRIORITY_LIST_API)
    fun savePriortyListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SUGGESTED_LOCATION_LIST_API)
    fun suggestedLocationsListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<GeneralModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SUGGESTION_LIST_API)
    fun suggestionListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<SuggestionModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.PROFILE_LIST_API)
    fun profileListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ProfileModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.PROFILE_LIST_API)
    fun singleProfileListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ProfileModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_PROFILE_API)
    fun readProfileApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_EXCHANGED_PROFILE_API)
    fun readExchangedProfileApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<BanModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.LIKE_DISLIKE_API)
    fun likeDislikeApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SEND_EXCHANGE_API)
    fun sendExchangeApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_EXCHANGE_API)
    fun saveExchangeApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SEND_PROFILE_EXCHANGE_REQUEST_API)
    fun sendProfileExchangeApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ProfileModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_PROFILE_EXCHANGE_REQUEST_API)
    fun saveProfileExchangeApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String,
    ): Single<Response<ResponseModel<ProfileModel>>>

    @Headers("Content-Type: application/json")
    @POST
    fun generalApiRequest(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<GeneralModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.RELATIONSHIP_LIST_API)
    fun relationShipListApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<GeneralModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SEND_ASSOCIATE_REQUEST_API)
    fun sendAssociateRequestApi(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.ADD_CANDIDATE_API)
    fun addCandidateRequestApi(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.REFER_CONTACTS_API)
    fun referContactsRequestApi(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST
    fun generalSaveDataApiRequest(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.PAUSE_UNPAUSE_SEARCH_API)
    fun pauseUnPauseSearchApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.LOGOUT_API)
    fun logoutApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_SET_NOTIFICATIONS_API)
    fun getNotificationSettingsApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<NotificationModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_SET_NOTIFICATIONS_API)
    fun setNotificationSettingsApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<NotificationModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.PRIVACY_LEGAL_API)
    fun privacyLegalRequestApi(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<FaqModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SEND_FEEDBACK_API)
    fun sendFeedbackApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_REFERENCES_API)
    fun readReferencesApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_DISCARD_REASON_API)
    fun getDiscardReasonApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<ReasonsModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_SNEAK_PEAK_API)
    fun readSneakPeakApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<BanModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.READ_CHAT_API)
    fun readChatApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.UPLOAD_PHOTO_ALBUM_API)
    fun uploadPhotoApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<PhotoModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_PHOTO_ALBUM_API)
    fun getPhotoAlbumApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<ImageModel>>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.SAVE_UPLOAD_PHOTO_API)
    fun saveUploadPhotoApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>


    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_STREAM_CHAT_TOKEN)
    fun getStreamChatTokenApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.GET_AADHAR_OTP_API)
    fun getAadharOtpApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.VERIFY_AADHAR_OTP_API)
    fun verifyAadharOtpApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.ADD_CONTRIBUTE_QUESTIONS_API)
    fun addContributeQuestionsApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.CHAT_INTRO_SETTING_API)
    fun chatIntroSettingApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseModel<UserModel>>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.ACCESS_CODE_APPLY_API)
    fun accessCodeApplyApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

    @Headers("Content-Type: application/json")
    @POST(IDRequestBuilder.ON_BOARDING_SKIP_API)
    fun onBoardingSkipApiRequest(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Single<Response<ResponseServiceModel>>

}