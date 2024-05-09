package com.commonfriend.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.idrequest.ResponseModel
import com.commonfriend.idrequest.ResponseServiceModel
import com.commonfriend.models.*
import com.commonfriend.utils.CATEGORY_ID
import com.commonfriend.utils.LAST_POS
import com.commonfriend.utils.Util
import com.commonfriend.utils.mainObjList
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class QuestionViewModel(private val repository: ApiRepository) : ViewModel() {

    var questionOptionsListResponse: MutableLiveData<ResponseModel<GeneralModel>> =
        MutableLiveData()
    var saveAnswerResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var sendRequestResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var addCandidateResponse: MutableLiveData<ResponseModel<UserModel>> = MutableLiveData()
    var referContactResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()
    var getQuestionsResponse: MutableLiveData<ResponseModel<QuestionsModel>> = MutableLiveData()
    var getSingleQuestionsResponse: MutableLiveData<ResponseModel<CategoryModel>> =
        MutableLiveData()
    var getSuggestedLocationsResponse: MutableLiveData<ResponseModel<GeneralModel>> =
        MutableLiveData()
    var getCategoryListResponse: MutableLiveData<ResponseModel<CategoryModel>> = MutableLiveData()
    var onBoardingSkipResponse: MutableLiveData<ResponseServiceModel> = MutableLiveData()

    private var job: Job? = null

    private val compositeDisposable = CompositeDisposable()
    private var questionOptionsDisposable: Disposable? = null

    @SuppressLint("CheckResult")
    fun categoryListApiRequest(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .categoryListApiRequest()
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        mainObjList = ArrayList()
                        mainObjList = it.data
                        CATEGORY_ID = 0
                        LAST_POS = -1
                        getCategoryListResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }


    @SuppressLint("CheckResult")
    fun getQuestionListApiRequest(context: Context, categoryId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .getQuestionListApiRequest(categoryId)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        mainObjList[CATEGORY_ID].questionList = ArrayList()
                        mainObjList[CATEGORY_ID].questionList = it.data
                        LAST_POS = -1
                        getQuestionsResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun getSingleQuestionListApiRequest(
        context: Context,
        categoryId: String = "",
        questionId: String = "",
        generalId: String = ""
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .getSingleQuestionListApiRequest(categoryId, questionId, generalId)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        mainObjList = ArrayList()
                        mainObjList = it.data
                        Util.print(">>>>DATAT IS${Gson().toJson(mainObjList)}")
                        LAST_POS = -1
                        getSingleQuestionsResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun questionOptionsListApiRequest(
        context: Context,
        searchKey: String = "",
        pageNo: Int = 0,
        generalId: String = "",
        apiName: String,
        currentLat: Double? = null,
        currentLong: Double? = null,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            questionOptionsDisposable?.dispose() // Dispose the previous subscription if it exists
            questionOptionsDisposable = repository
                .generalApiRequest(searchKey, pageNo, generalId, apiName,currentLat,currentLong)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        questionOptionsListResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun relationOptionsListApiRequest(
        context: Context,
        searchKey: String = "",
        pageNo: Int = 0,
        generalId: String = "",
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .relationshipListRequest(searchKey, pageNo, generalId)
                .subscribe {
                    if (it.success == 1) {
                        questionOptionsListResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun sendAssociateRequestApi(
        context: Context,
        mainObj: JSONObject
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .sendAssociateRequestApi(mainObj)
                .subscribe {
                    sendRequestResponse.postValue(it)
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun addCandidateRequestApi(
        context: Context,
        mainObj: JSONObject
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .addCandidateRequestApi(mainObj)
                .subscribe {
                    addCandidateResponse.postValue(it)
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }


    @SuppressLint("CheckResult")
    fun referContactApi(
        context: Context,
        mainObj: JSONObject
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .referContactsRequestApi(mainObj)
                .subscribe {
                    referContactResponse.postValue(it)
                    if (it.success != 1)
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun questionAnswerSaveApiRequest(
        context: Context,
        answers: JSONObject? = null,
        arrayData: JSONArray? = null
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.generalSaveDataApiRequest(answers, arrayData)
                .subscribe {
                    Util.dismissProgress()
                    if (it.success == 1) {
                        saveAnswerResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    @SuppressLint("CheckResult")
    fun suggestedLocationsListApiRequest(context: Context, type: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.suggestedLocationsListApiRequest(type)
                .subscribe {
                    if (it.success == 1) {
                        getSuggestedLocationsResponse.postValue(it)
                    } else
                        Util.showToastMessage(context, it.msg, true)
                }
                .addTo(compositeDisposable)
        }

    fun onBoardingSkipApiRequest() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.onBoardingSkipApiRequest()
                .subscribe {
                    if (it.success == 1) {
                        onBoardingSkipResponse.postValue(it)
                    } else
                        Util.print(it.msg)
                }
                .addTo(compositeDisposable)
        }


    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        compositeDisposable.clear()
    }
}


