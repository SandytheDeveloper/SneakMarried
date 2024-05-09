package com.commonfriend.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.CheckListModel
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CheckListViewModel(private val repository: ApiRepository) : ViewModel() {

    var checkListDataResponse: MutableLiveData<ResponseModel<CheckListModel>> = MutableLiveData()
    var priorityListDataResponse: MutableLiveData<ResponseModel<String>> = MutableLiveData()
    var saveCheckListDataResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var job: Job? = null

    @SuppressLint("CheckResult")
    fun checkListApiRequest(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .checkListApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        checkListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }
    @SuppressLint("CheckResult")
    fun getPriorityApiRequest(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .getPriorityApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        priorityListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }
    @SuppressLint("CheckResult")
    fun saveCheckListApiRequest(context: Context,data:String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .saveCheckListApiRequest(data)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        saveCheckListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }
    @SuppressLint("CheckResult")
    fun savePriortyListApiRequest(context: Context,data:String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .savePriortyListApiRequest(data)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
//                        Pref.setStringValue(Pref.PREF_USER_PROFILE_COMPLETED,"1")
                        Pref.setStringValue(Pref.PREF_PRIORITY_GIVEN,"1")
                        saveCheckListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }



}


