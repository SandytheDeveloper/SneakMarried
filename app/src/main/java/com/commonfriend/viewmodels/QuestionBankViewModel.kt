package com.commonfriend.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.QuestionBankModel
import com.commonfriend.utils.Util
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.*

class QuestionBankViewModel(private val repository: ApiRepository) : ViewModel() {

    var saveQuestionAnswerResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var getQuestionBankResponse: MutableLiveData<ResponseModel<QuestionBankModel>> = MutableLiveData()
    var hideUnhideResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var questionViewResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var addContributeQuestionsResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    private var job: Job? = null

    @SuppressLint("CheckResult")
    fun getQuestionBankApiRequest(context: Context, opponentId: String="",questionType : String,questionId : String = "") =
        viewModelScope.launch(Dispatchers.IO) {
            repository.getQuestionBankApiRequest(opponentId,questionType,questionId)
                .subscribe {
                    if (it.success == 1) {
                        getQuestionBankResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }


    @SuppressLint("CheckResult")
    fun saveOpinionApiRequest(context: Context, id: String, answer: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .saveOpinionApiRequest(id, answer)
                .subscribe {
                    if (it.success == 1) {
                        saveQuestionAnswerResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }


    @SuppressLint("CheckResult")
    fun questionViewApiRequest(context: Context, id: String,profileId:String,isFromSneakPeak:String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .questionViewApiRequest(id,profileId,isFromSneakPeak)
                .subscribe {
                    if (it.success == 1) {
                        questionViewResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun hideUnhideAnswerApiRequest(context: Context, id: String, isHide: String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .hideUnhideAnswerApiRequest(id,isHide)
                .subscribe {
                    if (it.success == 1) {
                        hideUnhideResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }

    @SuppressLint("CheckResult")
    fun addContributeQuestionsApiRequest(context: Context, question : String,optionA : String,optionB : String) =
        CoroutineScope(Dispatchers.IO).launch {
            repository
                .addContributeQuestionsApiRequest(question,optionA,optionB)
                .subscribe {
                    if (it.success == 1) {
                        addContributeQuestionsResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
        }


    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        compositeDisposable.clear()
    }
}


