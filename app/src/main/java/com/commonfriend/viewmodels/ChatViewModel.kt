package com.commonfriend.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.ChatModel
import com.commonfriend.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ApiRepository) : ViewModel() {

    var chatDataResponse: MutableLiveData<ResponseModel<ChatModel>> = MutableLiveData()
    var readFirstMessageApiResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var getChatDataResponse: MutableLiveData<ResponseModel<ChatModel>> = MutableLiveData()
    var sendChatDataResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var readChatDataResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var fetchChatDataResponse: MutableLiveData<ResponseModel<ChatModel>> = MutableLiveData()
    var job: Job? = null

    @SuppressLint("CheckResult")
    fun chatDetailApiRequest(context: Context, chatId: String,lastMessageId:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository.chatDetailApiRequest(chatId,lastMessageId)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        chatDataResponse.postValue(it)
                    } else
                        Util.print(it.msg)
                }
        }

    @SuppressLint("CheckResult")
    fun readFirstMessageApiRequest(context: Context, chatId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository.readFirstMessageApiRequest(chatId)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        readFirstMessageApiResponse.postValue(it)
                    } else
                        Util.print(it.msg)
                }
        }

    @SuppressLint("CheckResult")
    fun getChatDetailApiRequest(context: Context, chatId: String,lastMessageId:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository.chatDetailApiRequest(chatId,lastMessageId)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        getChatDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }


    @SuppressLint("CheckResult")
    fun sendMessageApiRequest(context: Context, chatId: String, message: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .sendMessageApiRequest(chatId,message)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        sendChatDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun readChatApiRequest(chatId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .readChatApiRequest(chatId)
                .subscribe {
                    if (it.success == 1) {
                        readChatDataResponse.postValue(it)
                    }
                }
        }

    @SuppressLint("CheckResult")
    fun fetchChatApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .fetchChatApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        fetchChatDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    /*@SuppressLint("CheckResult")
    fun getPriorityApiRequest(context: Context) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .getPriorityApiRequest()
                .subscribe {
                    if (it.success == 1) {
                        priorityListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun saveCheckListApiRequest(context: Context, data: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .saveCheckListApiRequest(data)
                .subscribe {
                    if (it.success == 1) {
                        saveCheckListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun savePriortyListApiRequest(context: Context, data: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .savePriortyListApiRequest(data)
                .subscribe {
                    if (it.success == 1) {
                        saveCheckListDataResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }*/

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}


