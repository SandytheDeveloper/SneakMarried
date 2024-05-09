package com.commonfriend.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.BanModel
import com.commonfriend.models.ProfileModel
import com.commonfriend.utils.Util
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ApiRepository) : ViewModel() {

    var profileListApiResponse: MutableLiveData<ResponseModel<ProfileModel>> = MutableLiveData()
    var singleProfileListApiResponse: MutableLiveData<ResponseModel<ProfileModel>> = MutableLiveData()
    var readExchangedProfileResponse: MutableLiveData<ResponseModel<BanModel>> = MutableLiveData()
    var sendProfileExchangeApiResponse: MutableLiveData<ResponseModel<ProfileModel>> = MutableLiveData()
    var saveProfileExchangeApiResponse: MutableLiveData<ResponseModel<ProfileModel>> = MutableLiveData()
    var readReferencesApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var readSneakPeakApiResponse: MutableLiveData<ResponseModel<BanModel>> = MutableLiveData()
    var likeDislikeResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()

    private var job: Job? = null

    private val compositeDisposable = CompositeDisposable()

    fun profileListApiRequest(context: Context,filter : String = "",profileId : String = "") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .profileListApiRequest(filter,profileId)
                .subscribe {

                    Util.dismissProgress()
                    profileListApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }


    fun singleProfileListApiRequest(context: Context,profileId : String = "") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .singleProfileListApiRequest(profileId)
                .subscribe {

                    Util.dismissProgress()
                    singleProfileListApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun readExchangedProfileApiRequest(context: Context,
                                       profileId: String = "",
                                       isBanned: String = "") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .readExchangedProfileApiRequest(profileId,isBanned)
                .subscribe {

                    Util.dismissProgress()
                    readExchangedProfileResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun sendProfileExchangeApiRequest(context: Context,
                                      profileId: String = "") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .sendProfileExchangeApiRequest(profileId)
                .subscribe {

                    Util.dismissProgress()
                    sendProfileExchangeApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun saveProfileExchangeApiRequest(context: Context,
                                      receiverId: String = "",
                                      status: String = "",
                                      reason: String = "",
                                      otherReason: String = "",
                                      channelId: String = "") =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .saveProfileExchangeApiRequest(receiverId,status,reason,otherReason,channelId)
                .subscribe {

                    Util.dismissProgress()
                    saveProfileExchangeApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun readReferencesApiRequest(context: Context,
                                 profileId: String,
                                 refNumber: String,
                                 fromCandidateId: String,
                                 toCandidateId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .readReferencesApiRequest(
                    profileId,
                    refNumber,
                    fromCandidateId,
                    toCandidateId)
                .subscribe {

                    Util.dismissProgress()
                    readReferencesApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun readSneakPeakApiRequest(context: Context,profileId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .readSneakPeakApiRequest(profileId)
                .subscribe {

                    Util.dismissProgress()
                    readSneakPeakApiResponse.postValue(it)
                    if (it.success != 1){
                        Util.showToastMessage(context, it.msg, true)
                    }
                }
        }

    fun likeDislikeApiRequest(context: Context,
                              profileId: String = "") =
        viewModelScope.launch(Dispatchers.IO) {
            repository.likeDislikeApiRequest( profileId)
                .subscribe {

                    Util.dismissProgress()
                    if (it.success == 1) {
                        likeDislikeResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        compositeDisposable.clear()
    }
}


