package com.commonfriend.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.FaqModel
import com.commonfriend.models.HomeModel
import com.commonfriend.models.ImageModel
import com.commonfriend.models.NotificationModel
import com.commonfriend.models.PhotoModel
import com.commonfriend.models.ReasonsModel
import com.commonfriend.models.UserModel
import com.commonfriend.models.UserProfileModel
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

class UserViewModel @Inject constructor(private val repository: ApiRepository) : ViewModel() {

    var serviceDataResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var referReadResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var userDataResponse: MutableLiveData<ResponseModel<UserModel>> = MutableLiveData()
    var homeDataResponse: MutableLiveData<ResponseModel<HomeModel>> = MutableLiveData()
    var getHomeDataResponse: MutableLiveData<ResponseModel<HomeModel>> = MutableLiveData()
    var sendReminderResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var userProfileResponse: MutableLiveData<ResponseModel<UserProfileModel>> = MutableLiveData()
    var sendEditProfileApiResponse: MutableLiveData<ResponseModel<UserProfileModel>> = MutableLiveData()
    var pauseUnPauseSearchResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var photoAlbumResponse: MutableLiveData<ResponseModel<PhotoModel>> = MutableLiveData()
    var getPhotoAlbumResponse: MutableLiveData<ResponseModel<ImageModel>> = MutableLiveData()
    var saveUploadPhotoResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var logoutApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var notifciationResponse: MutableLiveData<ResponseModel<NotificationModel>> = MutableLiveData()
    var notifciationSetResponse: MutableLiveData<ResponseModel<NotificationModel>> = MutableLiveData()
    var deleteAccountApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var privacyLegalApiResponse: MutableLiveData<ResponseModel<FaqModel>> = MutableLiveData()
    var sendFeedbackApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var getDiscardReasonApiResponse: MutableLiveData<ResponseModel<ReasonsModel>> = MutableLiveData()
    var claimProfileApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var getStreamChatTokenApiResponse: MutableLiveData<ResponseModel<UserModel>> = MutableLiveData()
    var getAadharOtpApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var verifyAadharOtpApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var chatIntroSettingApiResponse: MutableLiveData<ResponseModel<UserModel>> = MutableLiveData()
    var accessCodeApplyApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()

    var job: Job? = null

    @SuppressLint("CheckResult")
    fun sendOtpApiRequest(context: Context, countryCode: String, mobileNo: String,inTouchOnWhatsapp : Int,versionName : String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .registerApiRequest(countryCode, mobileNo,inTouchOnWhatsapp,versionName)
                .subscribe {
                    Util.dismissProgress()
                    userDataResponse.postValue(it)
//                    if (it.success == 1) {
//                        Util.showToastMessage(context,it.data[0].otp,false)
                        //serviceDataResponse.postValue(it)
//
//                    } else
//                        Util.showToastMessage(context, it.msg, true)
                }
        }


    @SuppressLint("CheckResult")
    fun changeNumberApiRequest(context: Context, countryCode: String, mobileNo: String,inTouchOnWhatsapp : Int) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .changeNumberApiRequest(countryCode, mobileNo,inTouchOnWhatsapp)
                .subscribe {
                    Util.dismissProgress()
                    userDataResponse.postValue(it)
//                    if (it.success == 1) {
//                        Util.showToastMessage(context,it.data[0].otp,false)
//                    } else
//                        Util.showToastMessage(context, it.msg, true)
                }
        }


    @SuppressLint("CheckResult")
    fun sendFeedbackApiRequest(context: Context,feedback:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .sendFeedbackApiRequest(feedback)
                .subscribe {
                    Util.dismissProgress()
                    Util.showToastMessage(context, it.msg, true)
                    if (it.success == 1) {
                        sendFeedbackApiResponse.postValue(it)
                    }
                }
        }


    @SuppressLint("CheckResult")//type = 1-privacy,  2-legal, 3 -terms of use
    fun privacyLegalRequestApi(context: Context,type:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .privacyLegalRequestApi(type)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        privacyLegalApiResponse.postValue(it)
                    }else{
                        Util.showToastMessage(context, it.msg, true)}
                }
        }





    @SuppressLint("CheckResult") //  // 1 = resend otp form change number, 0 = resend otp from Register
    fun resendOtpApiRequest(context: Context,isChangeNumber:String="0") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .resendOtpApiRequest(isChangeNumber)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        serviceDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun homePageApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .homePageApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        homeDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun getHomePageApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .homePageApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        getHomeDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun readReferredApiRequest(context: Context,id:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .readReferredApiRequest(id)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        referReadResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun pauseUnPauseSearchApiRequest(context: Context,status:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .pauseUnPauseSearchApiRequest(status)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        pauseUnPauseSearchResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun uploadPhotoApiRequest(context: Context,photoUrl:String,isProfilePic:String,fromGroupPic:String = "0") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .uploadPhotoApiRequest(photoUrl,isProfilePic,fromGroupPic)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)

                    photoAlbumResponse.postValue(it)


                }
        }

    @SuppressLint("CheckResult")
    fun getPhotoAlbumApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getPhotoAlbumApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)

                    getPhotoAlbumResponse.postValue(it)

                }
        }

    @SuppressLint("CheckResult")
    fun saveUploadPhotoApiRequest(context: Context,imageArray: JSONArray) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .saveUploadPhotoApiRequest(imageArray)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)

                    saveUploadPhotoResponse.postValue(it)

                }
        }



    @SuppressLint("CheckResult")
    fun getProfileApiRequest(context: Context,isEditAble:String="0") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getProfileApiRequest(isEditAble)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        userProfileResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun logoutApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .logoutApiRequest()
                .subscribe {
                    logoutApiResponse.postValue(it)
                    if (it.success != 1) {
                        Util.showToastMessage(context, it.msg, true)
                    }
                } }

    @SuppressLint("CheckResult")
    fun getNotificationsApi(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getNotificationsApi()
                .subscribe {
                    Util.dismissProgress()
                    notifciationResponse.postValue(it)
                    if (it.success != 1) {
                        Util.showToastMessage(context, it.msg, true)
                    }
                } }

    @SuppressLint("CheckResult")
    fun setNotificationsApi(context: Context,notificationModel: NotificationModel) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .setNotificationsApi(notificationModel)
                .subscribe {
                    Util.dismissProgress()
                    notifciationSetResponse.postValue(it)
                    if (it.success != 1) {
                        Util.showToastMessage(context, it.msg, true)
                    }
                } }

    @SuppressLint("CheckResult")
    fun deleteUserApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .deleteUserApiRequest()
                .subscribe {
                    if (it.success == 1) {
                        deleteAccountApiResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }




    @SuppressLint("CheckResult")
    fun verifyOtpApiRequest(context: Context, otp: String, mobileNo: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .verifyOtpApiRequest(otp, mobileNo)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        if(it.data.isNotEmpty()) {
                            Pref.setStringValue(Pref.PREF_AUTH_TOKEN, "Bearer ${it.data[0].token}")
                            Pref.setStringValue(Pref.PREF_USER_ID, it.data[0].userId)

                            Pref.setStringValue(
                                Pref.PREF_USER_DISPLAY_PICTURE,
                                it.data[0].profilePic
                            )

                            Pref.setStringValue(
                                Pref.PREF_USER_DISPLAY_PICTURE,
                                it.data[0].profilePic
                            )
                            Pref.setStringValue(Pref.PREF_USER_REGISTERED, it.data[0].isRegistered)
                            Pref.setStringValue(
                                Pref.PREF_USER_APPROVED,it.data[0].isProfileApproved)
                            Pref.setStringValue(
                                Pref.PREF_USER_PROFILE_COMPLETED,
                                it.data[0].isProfileCompleted
                            )

                                 Pref.setBooleanValue(Pref.PREF_LOGIN, true)

//                            Util.setToken()
                            Pref.setStringValue(Pref.PREF_PRIORITY_GIVEN, it.data[0].is_priority_given)
                            Pref.setStringValue(Pref.PREF_CHECK_LIST_PROVIDED,it.data[0].is_check_list_provided)
                            Pref.setStringValue(Pref.PREF_USER_NAME, it.data[0].userName)
                            Pref.setStringValue(Pref.PREF_USER_GENDER, it.data[0].gender)
                            Pref.setStringValue(Pref.PREF_PROFILE_CONFIRMATION, it.data[0].isProfileConfirmed)
                            Pref.setStringValue(Pref.PREF_STREAM_CHAT_TOKEN, it.data[0].chatToken)
                        }
                        userDataResponse.postValue(it)
                    }
                    else
                        Util.showToastMessage(context, it.msg, true)
                }
        }


    @SuppressLint("CheckResult")
    fun changeNumberVerifyOtpApiRequest(context: Context, otp: String, mobileNo: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .changeNumberVerifyOtpApiRequest(otp, mobileNo)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {

                        userDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }
    fun getDiscardReasonApiRequest(context: Context,profileId : String,type : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getDiscardReasonApiRequest(profileId,type)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1){
                        getDiscardReasonApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun getStreamChatTokenApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getStreamChatTokenApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1){
                        getStreamChatTokenApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun getAadharOtpApiRequest(context: Context,aadharNumber : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getAadharOtpApiRequest(aadharNumber)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1){
                        getAadharOtpApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun verifyAadharOtpApiRequest(context: Context,otp : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .verifyAadharOtpApiRequest(otp)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1){
                        verifyAadharOtpApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun chatIntroSettingApiRequest(context: Context,chatIntroduction : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .chatIntroSettingApiRequest(chatIntroduction) // 1 for yes, 2 for no
                .subscribe {
//                    Util.dismissProgress()
                    if (it.success == 1){
                        chatIntroSettingApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun accessCodeApplyApiRequest(context: Context,accessCode : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .accessCodeApplyApiRequest(accessCode) // 1 for yes, 2 for no
                .subscribe {
//                    Util.dismissProgress()
                    if (it.success == 1){
                        accessCodeApplyApiResponse.postValue(it)
                    } else {
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }


    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}


