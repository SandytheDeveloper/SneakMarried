package com.commonfriend.idrequest


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.commonfriend.models.*
import com.commonfriend.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ApiRepository : ViewModel() {
    private val ipRequestService: IDRequestService = IDRequestBuilder.getRequestBuilder()


    companion object {
        val apiRepository: ApiRepository = ApiRepository()
    }

    private var jsonObj: JSONObject? = JSONObject()



    @SuppressLint("CheckResult")
    suspend fun registerApiRequest(
        countryCode: String,
        mobileNo: String,
        inTouchOnWhatsapp: Int,
        versionName: String
    ): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("country_code", countryCode)
        jsonObj!!.put("mobile_no", mobileNo)
        jsonObj!!.put("in_touch_on_whatsapp", inTouchOnWhatsapp)
        jsonObj!!.put("version_number", versionName)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        jsonObj!!.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.registerApiRequest(jsonObj.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    suspend fun changeNumberApiRequest(
        countryCode: String,
        mobileNo: String,
        inTouchOnWhatsapp: Int
    ): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("country_code", countryCode)
        jsonObj!!.put("mobile_no", mobileNo)
        jsonObj!!.put("in_touch_on_whatsapp", inTouchOnWhatsapp)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        jsonObj!!.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.changeNumberApiRequest(Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun verifyOtpApiRequest(otp: String, mobileNo: String): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("mobile_no", mobileNo)
        jsonObj!!.put("otp", otp)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        jsonObj!!.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.verifyOtpApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }else{
                            Util.dismissProgress()
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }



    fun changeNumberVerifyOtpApiRequest(otp: String, mobileNo: String): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("mobile_no", mobileNo)
        jsonObj!!.put("otp", otp)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        jsonObj!!.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.changeNumberVerifyOtpApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun resendOtpApiRequest(isChangeNumber :String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put(
            "mobile_no",
            Pref.getStringValue(Pref.PREF_MOBILE_NUMBER, "").toString()
        )
        jsonObj!!.put(
                "is_change_number",isChangeNumber
        )
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put(
            "device_token",
            Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
        )
        jsonObj!!.put(
            "language",
            Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
        )
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.resendOtpApiRequest(
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun questionListApiRequest(): Observable<ResponseModel<CategoryModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put(
                "device_token",
                Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
            )
            jsonObj!!.put(
                "language",
                Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
            )
            jsonObj!!.put("timezone", TimeZone.getDefault().id)

            ipRequestService.questionListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun categoryListApiRequest(): Observable<ResponseModel<CategoryModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString()) // Login User Id
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put(
                "device_token",
                Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString()
            )
            jsonObj!!.put(
                "language",
                Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString()
            )
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.categoryListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun pauseUnPauseSearchApiRequest(status: String): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("status", status)//1=pause, 0=not pause
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())//loggedin user id
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.pauseUnPauseSearchApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun getQuestionListApiRequest(
        categoryId: String
    ): Observable<ResponseModel<QuestionsModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("category_id", categoryId)
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString()) // Login User Id
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.getQuestionListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun getSingleQuestionListApiRequest(
        categoryId: String,
        questionId: String,
        generalId: String
    ): Observable<ResponseModel<CategoryModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())//loggedin user id
            jsonObj!!.put("category_id", categoryId)
            jsonObj!!.put("question_id", questionId)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
//            jsonObj!!.put("general_id", generalId)
            ipRequestService.getSingleQuestionListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun checkListApiRequest(): Observable<ResponseModel<CheckListModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put(
                "user_id",
                Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
            )
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.checkListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun getPriorityApiRequest(): Observable<ResponseModel<String>> {
        return Observable.create { emitter ->

            jsonObj = JSONObject()
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)

            ipRequestService.getPriorityApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun homePageApiRequest(): Observable<ResponseModel<HomeModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.homePageApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj!!.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun readReferredApiRequest(
        id: String
    ): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("refer_id", id)
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.readReferredApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj!!.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }



    @SuppressLint("CheckResult")
    fun getProfileApiRequest(isEditable:String="0"): Observable<ResponseModel<UserProfileModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("is_edited", isEditable)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.getProfileApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun logoutApiRequest(): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.logoutApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }
    @SuppressLint("CheckResult")
    fun getNotificationsApi(): Observable<ResponseModel<NotificationModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.getNotificationSettingsApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }
    @SuppressLint("CheckResult")
    fun setNotificationsApi(notificationModel:NotificationModel): Observable<ResponseModel<NotificationModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("all_noti", notificationModel.allNotifications)
            jsonObj!!.put("question_noti", notificationModel.questionsNotifications)
            jsonObj!!.put("connection_noti", notificationModel.connectionNotifications)
            jsonObj!!.put("recommendation_noti", notificationModel.recommendationNotifications)
            jsonObj!!.put("message_noti", notificationModel.messageotifications)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.getNotificationSettingsApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun chatDetailApiRequest(chatId: String,lastMessageId:String): Observable<ResponseModel<ChatModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("chat_id", chatId)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("last_message_id", lastMessageId)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.chatDetailApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }


    @SuppressLint("CheckResult")
    fun readFirstMessageApiRequest(chatId: String): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("chat_id", chatId)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.readFirstMessageApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun fetchChatApiRequest(): Observable<ResponseModel<ChatModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)


            ipRequestService.fetchChatApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun saveCheckListApiRequest(data: String?): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.saveCheckListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), data ?: jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun saveOpinionApiRequest(id: String, opinion: String): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("id", id)
            jsonObj!!.put("answer", opinion)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.saveOpinionApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun questionViewApiRequest(id: String,profileId:String,isFromSneakPeak:String): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,""))
            jsonObj!!.put("profile_id", profileId)
            jsonObj!!.put("opinions_id", id)
            jsonObj!!.put("is_from_sneak_peak", isFromSneakPeak)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.questionViewApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun hideUnhideAnswerApiRequest(id: String, isHide: String): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("opinions_id", id)
            jsonObj!!.put("is_hide", isHide)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.hideUnhideAnswerApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun sendMessageApiRequest(chatId: String, message: String?): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("chat_id", chatId)
            jsonObj!!.put("message", message)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)

            ipRequestService.sendMessageApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString() ?: ""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun deleteUserApiRequest(): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.deleteUserApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString() ?: ""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun savePriortyListApiRequest(data: String?): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->

            jsonObj = JSONObject()
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.savePriortyListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), data ?: jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun suggestedLocationsListApiRequest(type: String): Observable<ResponseModel<GeneralModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("latitude", Pref.getStringValue(Pref.PREF_CURRENT_LAT, ""))
            jsonObj!!.put("longitude", Pref.getStringValue(Pref.PREF_CURRENT_LNG, ""))
            jsonObj!!.put("type", type) // 1 for locality 2 for country
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.suggestedLocationsListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }

    @SuppressLint("CheckResult")
    fun generalApiRequest(
        searchKey: String = "",
        pageNo: Int = 0,
        generalId: String = "",
        apiName: String,
        currentLat: Double? = null,
        currentLong: Double? = null,
    ): Observable<ResponseModel<GeneralModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("search_key", searchKey)
        jsonObj!!.put("page", pageNo)
        jsonObj!!.put("general_id", generalId)
        currentLong?.let {
            jsonObj!!.put("longitude", it)
        }
        currentLat?.let {
            jsonObj!!.put("latitude", it)
        }
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString()) // Login User Id
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.generalApiRequest(
                BASE_URL + API_HOST + apiName,
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun relationshipListRequest(
        searchKey: String = "",
        pageNo: Int = 0,
        generalId: String = "",
    ): Observable<ResponseModel<GeneralModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("search_key", searchKey)
        jsonObj!!.put("page", pageNo)
        jsonObj!!.put("general_id", generalId)
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString())
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.relationShipListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun sendAssociateRequestApi(
        mainObj: JSONObject
    ): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            mainObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
            mainObj.put("device_type", DEVICE_TYPE)
            mainObj.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            mainObj.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            mainObj.put("timezone", TimeZone.getDefault().id)
            ipRequestService.sendAssociateRequestApi(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), mainObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun addCandidateRequestApi(
        mainObj: JSONObject,
    ): Observable<ResponseModel<UserModel>> {
       jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("candidate_basic", mainObj)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)

        return Observable.create { emitter ->
            ipRequestService.addCandidateRequestApi(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun referContactsRequestApi(
        mainObj: JSONObject
    ): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            mainObj!!.put("device_type", DEVICE_TYPE)
            mainObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            mainObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            mainObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.referContactsRequestApi(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), mainObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun suggestionListApiRequest(
    ): Observable<ResponseModel<SuggestionModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put(
            "user_id",
            Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
        )
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.suggestionListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun profileListApiRequest(
        filter : String,
        profileId : String
    ): Observable<ResponseModel<ProfileModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("match_id", profileId)
        jsonObj!!.put("filter",filter)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.profileListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .retry(1)
                .observeOn(AndroidSchedulers.mainThread())
                .retry(1)
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun singleProfileListApiRequest(
        profileId : String
    ): Observable<ResponseModel<ProfileModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("match_id", profileId)
        jsonObj!!.put("filter","")
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.singleProfileListApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .retry(1)
                .observeOn(AndroidSchedulers.mainThread())
                .retry(1)
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun readReferencesApiRequest(
        profileId: String,
        refNumber: String,
        fromCandidateId: String,
        toCandidateId: String
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("profile_id", profileId)
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("ref_number", refNumber)
        jsonObj!!.put("from_candidate_id", fromCandidateId)
        jsonObj!!.put("to_candidate_id", toCandidateId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.readReferencesApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun readProfileApiRequest(
        profileId: String = ""
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put(
            "user_id",
            Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
        )
        jsonObj!!.put("profile_id", profileId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.readProfileApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun readExchangedProfileApiRequest(
        profileId: String = "",
        isBanned : String = ""
    ): Observable<ResponseModel<BanModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("match_id", profileId)
        jsonObj!!.put("is_banned", isBanned)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.readExchangedProfileApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun likeDislikeApiRequest(
        profileId: String = "",
        isLike: String = "1"
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("is_like", isLike)
        jsonObj!!.put("_id", profileId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.likeDislikeApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun sendExchangeApiRequest(
        profileId: String = ""
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put(
            "user_id",
            Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
        )
        jsonObj!!.put("reciver_id", profileId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.sendExchangeApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun saveExchangeApiRequest(
        receiverId: String = "",
        status: String = ""
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put(
            "user_id",
            Pref.getStringValue(Pref.PREF_USER_ID, "").toString()
        )  //user id (based on selected profile top right)
        jsonObj!!.put("reciver_id", receiverId)//suggestion profile id
        jsonObj!!.put(
            "status",
            status
        ) // 1 - Accept Request, 2 - Reject Request, 3 - Discard Profile
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.saveExchangeApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun sendProfileExchangeApiRequest(
        profileId: String = ""
    ): Observable<ResponseModel<ProfileModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("match_id", profileId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.sendProfileExchangeApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun saveProfileExchangeApiRequest(
        receiverId: String = "",
        status: String = "",
        reason: String = "",
        otherReason: String = "",
        channelId: String = ""
    ): Observable<ResponseModel<ProfileModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "0").toString())
        jsonObj!!.put("match_id", receiverId)
        jsonObj!!.put("status", status) //1=accept, 2=reject, 3 = discard request, 4 - discard and Report
        jsonObj!!.put("reason", reason) // reason to reject or discard
        jsonObj!!.put("other_reason", otherReason) // other reason text
        jsonObj!!.put("channel_id", channelId) // channelId
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.saveProfileExchangeApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun generalSaveDataApiRequest(
        data: JSONObject? = null,
        arrayData: JSONArray? = null,
    ): Observable<ResponseServiceModel> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, "").toString()) // Login User Id
            jsonObj!!.put(mainObjList[CATEGORY_ID].categoryKey,data ?: arrayData.toString())
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)

            ipRequestService.generalSaveDataApiRequest(
                BASE_URL + API_HOST + mainObjList[CATEGORY_ID].categoryApiName,
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj!!.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun getQuestionBankApiRequest(
        opponentId: String,
        questionType: String,
        questionId: String
    ): Observable<ResponseModel<QuestionBankModel>> {
        return Observable.create { emitter ->
            jsonObj = JSONObject()
            jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
            jsonObj!!.put("opponent_id", opponentId)
            jsonObj!!.put("question_type", questionType)
            jsonObj!!.put("question_id", questionId)
            jsonObj!!.put("device_type", DEVICE_TYPE)
            jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
            jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
            jsonObj!!.put("timezone", TimeZone.getDefault().id)
            ipRequestService.getQuestionBankApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(),
                jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                            // Util.handleRemoveUser(it.body()!!.success, it.body()!!.msg)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })

        }
    }


    @SuppressLint("CheckResult")
    fun privacyLegalRequestApi(
        type:String
    ): Observable<ResponseModel<FaqModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("type",type) // 1-privacy,  2-legal
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.privacyLegalRequestApi(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun sendFeedbackApiRequest(
        feedback:String
    ): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("feedback",feedback)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.sendFeedbackApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun getDiscardReasonApiRequest(profileId : String,type : String): Observable<ResponseModel<ReasonsModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("type", type) // 1 for reject and 2 for reject and report
        jsonObj!!.put("match_id", profileId) // Profile ID
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.getDiscardReasonApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }


    @SuppressLint("CheckResult")
    fun readSneakPeakApiRequest(profileId: String): Observable<ResponseModel<BanModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("match_id", profileId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.readSneakPeakApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }



    @SuppressLint("CheckResult")
    fun readChatApiRequest(chatId:String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("chat_id", chatId)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            ipRequestService.readChatApiRequest(
                Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.body() != null) {
                            emitter.onNext(it.body()!!)
                        }
                    },
                    {
                        Util.dismissProgress()
                        Util.print(emitter.hashCode().toString())
                        it.printStackTrace()
                    })
        }
    }

    fun uploadPhotoApiRequest(photoUrl:String,isProfilePic:String,fromGroupPic:String = "0"): Observable<ResponseModel<PhotoModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("photo_url", photoUrl)
        jsonObj!!.put("is_profile_pic", isProfilePic) // "1"  //if profile pic then 1 otherwise 0
        jsonObj!!.put("from_group_pic", fromGroupPic)    // 1 from group pic , 0 group pic // To not check group pic
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = uploadPhotoApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }



    fun getPhotoAlbumApiRequest(): Observable<ResponseModel<ImageModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = getPhotoAlbumApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun saveUploadPhotoApiRequest(imageArray: JSONArray): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        jsonObj!!.put("images", imageArray)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = saveUploadPhotoApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun getStreamChatTokenApiRequest(): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = getStreamChatTokenApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun getAadharOtpApiRequest(aadharNumber : String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("aadhar_num", aadharNumber)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = getAadharOtpApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun verifyAadharOtpApiRequest(otp : String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("otp", otp)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = verifyAadharOtpApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun addContributeQuestionsApiRequest(question : String,optionA : String,optionB : String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("question", question)
        jsonObj!!.put("option_a", optionA)
        jsonObj!!.put("option_b", optionB)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = addContributeQuestionsApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun chatIntroSettingApiRequest(chatIntroduction : String): Observable<ResponseModel<UserModel>> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("chat_introduction", chatIntroduction) // 1 for yes, 2 for no
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = chatIntroSettingApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
//                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun accessCodeApplyApiRequest(accessCode : String): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("access_code", accessCode)
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = accessCodeApplyApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
//                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }

    fun onBoardingSkipApiRequest(): Observable<ResponseServiceModel> {
        jsonObj = JSONObject()
        jsonObj!!.put("user_id", Pref.getStringValue(Pref.PREF_USER_ID,"").toString())
        jsonObj!!.put("device_type", DEVICE_TYPE)
        jsonObj!!.put("device_token",Pref.getStringValue(Pref.PREF_DEVICE_TOKEN, "").toString() )
        jsonObj!!.put("language",Pref.getStringValue(Pref.PREF_LANGUAGE, DEFAULT_LANGUAGE).toString())
        jsonObj!!.put("timezone", TimeZone.getDefault().id)
        return Observable.create { emitter ->
            with(ipRequestService) {
                val subscribe = onBoardingSkipApiRequest(
                    Pref.getStringValue(Pref.PREF_AUTH_TOKEN, "").toString(), jsonObj.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            if (it.body() != null) {
                                emitter.onNext(it.body()!!)
                            }
                        },
                        {
//                            Util.dismissProgress()
                            Util.print(emitter.hashCode().toString())
                            it.printStackTrace()
                        })
            }
        }
    }


}
