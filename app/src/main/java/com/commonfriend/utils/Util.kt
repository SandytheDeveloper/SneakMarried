package com.commonfriend.utils

import CustomTypefaceSpan
import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.facebook.drawee.view.SimpleDraweeView
import com.commonfriend.*
import com.commonfriend.custom.CustomDialog
import com.commonfriend.custom.CustomLoading
import com.commonfriend.custom.listener.CustomLottieDialog
import com.commonfriend.idrequest.ApiRepository
import com.commonfriend.models.CategoryModel
import com.commonfriend.models.ContactModel
import com.commonfriend.models.GeneralModel
import com.commonfriend.template.*
import com.commonfriend.viewmodels.QuestionViewModel
import com.commonfriend.viewmodels.ViewModelFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object Util {

    private var customDialog: CustomDialog? = null
    private var customLottieDialog: CustomLottieDialog? = null
    private var customLoading: CustomLoading? = null
    private var professionCategory: CategoryModel? = null
    private var customDialogForAnimation: CustomDialog? = null


    fun print(message: String) {
//        if (com.getmarried.BuildConfig.DEBUG) {

        if (message.length > 1000) {
            val maxLogSize = 1000
            for (i in 0..message.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > message.length) message.length else end
                Log.v("Print ::", message.substring(start, end))
            }
        } else {
            Log.i("Print ::", message)
        }
//        }
    }

    fun setToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Utills", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d(" Utills  TOKEN::", token.toString())
            if (!token.equals(
                    "null",
                    ignoreCase = true
                ) && token.isNotEmpty()
            ) Pref.setStringValue(Pref.PREF_DEVICE_TOKEN, token)
        })
    }

    fun getNameInitials(fullName: String): String {
        val nameWithoutWhitespace = fullName.trim()
        return if (nameWithoutWhitespace.isNotEmpty()) {
            val initials =
                nameWithoutWhitespace.split(" ").mapNotNull { it.firstOrNull()?.toString() }
            initials.take(2).reduce { acc, s -> acc + s }.uppercase()
        } else ""
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission(context: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ), REQUEST_CODE
                )
                false
            }
        } else
            true
    }

    fun getLocationFromLatLong(
        context: Context,
        addressModel: GeneralModel
    ) {
        val coder = Geocoder(context)
        val address: List<Address>?
        try {
            address = coder.getFromLocation(
                addressModel.latitude.toDouble(),
                addressModel.longitude.toDouble(),
                5
            )
            if (address != null && address.isNotEmpty()) {
                addressModel.city = address[0].locality
                addressModel.state = address[0].adminArea
                addressModel.zipcode = address[0].postalCode
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            dismissProgress()
            (context as Activity).setResult(
                Activity.RESULT_OK,
                Intent().putExtra(DATA, addressModel)
            )
            context.finish()
        }
    }


    fun isQuestionSkipable(): Boolean {

//        println("======================== CATEGORY ID :: $CATEGORY_ID")
//        println("======================== LAST_POS :: $LAST_POS")
//        println("======================== mainObjList :: " + mainObjList.size)
//        println("======================== questionList size :: " + mainObjList[CATEGORY_ID].questionList.size)
//        println("======================== SKIPPABLE :: " + mainObjList[CATEGORY_ID].questionList[LAST_POS].skipable)

        return false
        /*return when (mainObjList[CATEGORY_ID].questionList[LAST_POS].skipable) {
            "1" -> {
                Pref.getStringValue(Pref.PREF_USER_ROLE, "") == "1"
            }

            "2" -> {
                Pref.getStringValue(Pref.PREF_USER_ROLE, "") == "2"
            }

            "3" -> {
                true
            }

            else -> {
                false
            }
        }*/

    }


    fun manageBackClick(context: Context) {
        print(">>>>>>>>>>>>>>>>>>>>>>>>>>manageBackClick>>>>>>>>>>>>>>>>>>>> LAST POS IS ON START>>>$LAST_POS ")
        if (LAST_POS > 0) {
            LAST_POS -= 1
            if (
                (RELATIONSHIP_STATUS != "1" && mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains(
                    "kids"
                ))
            ) {
                LAST_POS -= 1
            }
        } else if (LAST_POS == 0) {
            LAST_POS -= 1
//            FROM_BACK_PRESSED = true
            when (mainObjList[CATEGORY_ID].hasQuestion) {
                "0" -> context.openA<SectionBreakerActivity>()
                "1" -> context.openA<ChecklistActivity>()
                "2" -> context.openA<PriorityActivity>()
                "3" -> context.openA<EditProfileActivity>()

            }
        } else {
            (context as Activity).finish()
        }
    }


    fun applyCustomFonts(
        context: Context,
        spannableStringBuilder: SpannableStringBuilder?,
        mainString: String = "",
        word: String,
        color: Int = R.color.color_black
    ): SpannableStringBuilder {
        var word = word
        word = ""

        val spannableString = spannableStringBuilder ?: SpannableStringBuilder(mainString)

        val startIndex = spannableString.indexOf(word)

        if (startIndex != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                spannableString.setSpan(
                    TypefaceSpan(
                        Typeface.createFromAsset(context.assets, "natural_script_bold.ttf")
                    ), startIndex, startIndex + word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                spannableString.setSpan(
                    CustomTypefaceSpan(
                        "",
                        Typeface.createFromAsset(context.assets, "natural_script_bold.ttf")
                    ), startIndex, startIndex + word.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )

            }

            spannableString.setSpan(
                AbsoluteSizeSpan(context.resources.getDimensionPixelSize(if (context is MainActivity) com.intuit.sdp.R.dimen._35sdp else com.intuit.sdp.R.dimen._40sdp)),
                startIndex,
                startIndex + word.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )


            if (color != -1) {
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, color)),
                    startIndex,
                    startIndex + word.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }


        return spannableString
    }

    fun applyCustomFontsAdd(
        context: Context,
        spannableStringBuilder: SpannableStringBuilder?,
        mainString: String = "",
        word: String,
        color: Int = -1
    ): SpannableStringBuilder {

        val spannableString = spannableStringBuilder ?: SpannableStringBuilder(mainString)

        val startIndex = spannableString.indexOf(word)

        if (startIndex != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                spannableString.setSpan(
                    TypefaceSpan(
                        Typeface.createFromAsset(
                            context.assets,
                            "natural_script.ttf"
                        )
                    ), startIndex, startIndex + word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                spannableString.setSpan(
                    CustomTypefaceSpan(
                        "",
                        Typeface.createFromAsset(context.assets, "natural_script.ttf")
                    ), startIndex, startIndex + word.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )

            }

            spannableString.setSpan(
                AbsoluteSizeSpan(context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._20sdp)),
                startIndex,
                startIndex + word.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )


            if (color != -1) {
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, color)),
                    startIndex,
                    startIndex + word.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }


        return spannableString
    }


    fun <T> swap(list: ArrayList<T>, originalIndex: Int, swapIndex: Int): ArrayList<T> {
        val t = list[originalIndex]
        list[originalIndex] = list[swapIndex]
        list[swapIndex] = t
        return list
    }

    fun hideKeyBoard(context: Context, view: View?) {
        if (view == null)
            return
        try {
            val inputMethodManager = context.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun setUnderLine(
        context: Context,
        spannableStringBuilder: SpannableStringBuilder?,
        mainString: String = "",
        text1: String,
        text2: String = "",
        text3: String = "",
        color: Int = -1
    ): SpannableStringBuilder {

        val spannableString = spannableStringBuilder ?: SpannableStringBuilder(mainString)


        val startIndex = spannableString.indexOf(text1)
        val startIndex2 = spannableString.indexOf(text2)
        val startIndex3 = spannableString.indexOf(text3)


        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>startIndex1>>>>>>>>>>>>" + startIndex)
        val text1Span: UnderlineSpan = object : UnderlineSpan() {


            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(context, R.color.color_black)
                ds.isUnderlineText = true
                ds.underlineColor = ContextCompat.getColor(context, color)
            }


        }
        if (startIndex != -1) {
            spannableString.setSpan(
                text1Span,
                startIndex,
                startIndex + text1.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }


        if (text2.isNotEmpty()) {
            var text2Span: UnderlineSpan = object : UnderlineSpan() {


                @RequiresApi(Build.VERSION_CODES.Q)
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(context, R.color.color_black)
                    ds.isUnderlineText = true
                    ds.underlineColor = ContextCompat.getColor(context, R.color.color_black)
                }
            }

            if (startIndex2 != -1) {
                spannableString.setSpan(
                    text2Span,
                    startIndex2,
                    startIndex2 + text2.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        if (text3.isNotEmpty()) {
            var text3Span: UnderlineSpan = object : UnderlineSpan() {


                @RequiresApi(Build.VERSION_CODES.Q)
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(context, R.color.color_black)
                    ds.isUnderlineText = true
                    ds.underlineColor = ContextCompat.getColor(context, R.color.color_black)

                }
            }

            if (startIndex3 != -1) {
                spannableString.setSpan(
                    text3Span,
                    startIndex3,
                    startIndex3 + text3.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannableString
    }


    fun showProgress(context: Context,showLoading : Boolean = false) {
        try {
            if (customDialog != null && customDialog!!.isShowing)
                customDialog!!.dismiss()

            customDialog = CustomDialog(context,showLoading)
            customDialog!!.setCancelable(false)
            customDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showProgressForAnimation(context: Context) {
        try {
            if (customDialogForAnimation != null && customDialogForAnimation!!.isShowing)
                customDialogForAnimation!!.dismiss()

            customDialogForAnimation = CustomDialog(context)
            customDialogForAnimation!!.setCancelable(false)
            customDialogForAnimation!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun dismissAnimationProgress() {
        if (customDialogForAnimation != null && customDialogForAnimation!!.isShowing)
            customDialogForAnimation!!.dismiss()
        customDialogForAnimation = null
    }

    fun showLoading(context: Context) {
        try {
            if (customLoading != null && customLoading!!.isShowing)
                customLoading!!.dismiss()

            customLoading = CustomLoading(context)
            customLoading!!.setCancelable(false)
            customLoading!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun dismissLoading() {
        if (customLoading != null && customLoading!!.isShowing)
            customLoading!!.dismiss()
        customLoading = null
    }

    fun manageTemplate(context: Context, isFrom: ActivityIsFrom = ActivityIsFrom.NORMAL) {
        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>BEFORE ALL>>>>>$isFrom>>>>>$LAST_POS" + ":::" + (mainObjList[CATEGORY_ID].questionList.size - 1 > LAST_POS))
        if (mainObjList[CATEGORY_ID].questionList.size - 1 > LAST_POS) {
//            FROM_BACK_PRESSED = false

            if (WORK_AS == "1" && professionCategory != null && mainObjList[CATEGORY_ID].questionList.isNotEmpty() && LAST_POS != -1 && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName.isNotEmpty() && mainObjList[CATEGORY_ID].questionList[LAST_POS].masterApiName == "get_profession") {
                mainObjList.add(
                    mainObjList.indexOf(mainObjList.find { it.categoryKey == "candidate_finances" }) + 1,
                    professionCategory!!
                )

                print(">>>>>>>>>>>>>>>>>>>>>>CATEGORY SIZE NOT STUDENT IS >>>>>>>>>>>${mainObjList.size}")
            }

            LAST_POS += 1

            if ((RELATIONSHIP_STATUS != "1" && mainObjList[CATEGORY_ID].questionList[LAST_POS].question.contains(
                    "kids"
                ))
            ) {
                LAST_POS += 1
            }


            if (WORK_AS != "1" && mainObjList.any { it.categoryKey == "candidate_professional" }) {
                professionCategory = CategoryModel()
                mainObjList.find { it.categoryKey == "candidate_professional" }
                    ?.let { professionCategory = it }
                mainObjList.removeAt(mainObjList.indexOf(mainObjList.find { it.categoryKey == "candidate_professional" }))
                print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>CATEGORY SIZE IS >>>>>>${mainObjList.size}")
            }
            print(">>>>>>>>>>>>>>>>>>>>>>>>>>WORK AS IS >>>>>>${WORK_AS}")
            print(">>>>>>>>>>>>>>>>>>>>>>LASTPOS FROM MANAGE TEMPLATE>>>>>>>>>>>>>>>>>$LAST_POS")
            print(">>>>>>>>>>>>>>>>>>>>>>LASTPOS FROM MANAGE TEMPLATE>>>>>>>>>>>>>>>>>${mainObjList[CATEGORY_ID].questionList.size}")
            print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CATEGORY ID IS NORMAL>>>>>>>>>>>$CATEGORY_ID")
            if (LAST_POS <= mainObjList[CATEGORY_ID].questionList.size - 1)
                templateMovement(context, isFrom)
            else
                manageTemplate(context, isFrom)
        } else {
            if (mainObjList.size - 1 > CATEGORY_ID) {
                print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CATEGORY ID IS BEFORE CHANGE >>>>>>>>>>>$CATEGORY_ID")
                CATEGORY_ID += 1
                print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>CATEGORY ID IS AFTER CHANGE >>>>>>>>>>>$CATEGORY_ID")
                LAST_POS = -1

                when (mainObjList[CATEGORY_ID].hasQuestion) {
                    "0" ->
                        context.openA<SectionBreakerActivity> {
                            putExtra(IS_FROM, isFrom)
                        }

                    "1" -> {
                        context.openA<ChecklistActivity> {
                            putExtra(IS_FROM, isFrom)
                        }
                    }

                    "2" -> {
                        context.openA<PriorityActivity> {
                            putExtra(IS_FROM, isFrom)
                        }
                    }

                    "3" -> {
                        if (isFrom == ActivityIsFrom.FROM_EDIT_SECTION) {
                            context.openA<StepsActivity> {
                                putExtra(IS_FROM, ActivityIsFrom.NORMAL)
                            }
                        } else
                            context.openA<EditProfileActivity> {
                                putExtra(IS_FROM, isFrom)
                            }
                    }
                }
            } else {
                print(">>>>>>>>>>>>>>>CATEGORY_ID>>>>>$isFrom>>>>>>>${CATEGORY_ID}")
                when (isFrom) {
                    ActivityIsFrom.FROM_EDIT -> {
                        (context as AppCompatActivity).finish()
                    }

                    ActivityIsFrom.FROM_EDIT_SECTION -> {
                        context.openA<StepsActivity> {
                            putExtra(IS_FROM, ActivityIsFrom.NORMAL)
                        }
                    }

                    else -> {
                        //                    Pref.setStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "1")
                        //                    Pref.setStringValue(Pref.PREF_USER_GENDER, "")
                        manageOnBoarding(context)
                    }
                }
            }
        }
    }

    fun templateMovement(context: Context, from: ActivityIsFrom = ActivityIsFrom.NORMAL) {
        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>templateMovement >> $from>>>>>>>>" + mainObjList[CATEGORY_ID].questionList[LAST_POS].templateType)
        when (mainObjList[CATEGORY_ID].questionList[LAST_POS].templateType) {
            0, 1, 19 -> {
                context.openA<FirstTemplateActivity> { putExtra(IS_FROM, from) }
            }

            2 -> {
                context.openA<SecondTemplateActivity> { putExtra(IS_FROM, from) }
            }

            3 -> {
                context.openA<ThirdTemplateActivity> { putExtra(IS_FROM, from) }
            }

            4 -> {
                context.openA<FourTemplateActivity> { putExtra(IS_FROM, from) }
            }

            5 -> {
                context.openA<FiveTemplateActivity> { putExtra(IS_FROM, from) }
            }

            6 -> {
                context.openA<SixTemplateActivity> { putExtra(IS_FROM, from) }
            }

            7 -> {
                context.openA<SevanTemplateActivity> { putExtra(IS_FROM, from) }
            }

            8, 10, 23 -> {
                context.openA<EightTenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            11 -> {
                context.openA<ElevenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            13 -> {
                context.openA<ThirteenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            15 -> {
                context.openA<FifteenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            16 -> {
                context.openA<SixteenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            18 -> {
                context.openA<EightTeenTemplateActivity> { putExtra(IS_FROM, from) }
            }

            20 -> {
                context.openA<SettleLocationTemplate> { putExtra(IS_FROM, from) }
            }
            /*20 -> {
                context.openA<TwentyTemplateActivity> { putExtra(IS_FROM, from) }
            }*/

            21 -> {
                context.openA<TwentyOneTemplateActivity> { putExtra(IS_FROM, from) }
            }

            22 -> {
                context.openA<TwentyTwoTemplateActivity> { putExtra(IS_FROM, from) }
            }

            12 -> {
                context.openA<PhotoAlbumActivity> { putExtra(IS_FROM, from) }
            }

            24 -> {
                context.openA<TwentyFourTemplateActivity> { putExtra(IS_FROM, from) }
            }
        }
    }


    fun dismissProgress() {
        if (customDialog != null && customDialog!!.isShowing)
            customDialog!!.dismiss()
        customDialog = null
    }

    fun checkContactsPermission(context: Context): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("Range")
    fun getMobileContacts(context: Context): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        val cursor = context.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        cursor?.use {
            while (it.moveToNext()) {
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(ContactModel(name, phone))
            }
        }
        return contacts
    }

    fun manageOnBoarding(context: Context) {
        if (Pref.getStringValue(Pref.PREF_PROFILE_CONFIRMATION, "") != "1") {
            context.openA<EditProfileActivity>()
        } else if (Pref.getStringValue(Pref.PREF_CHECK_LIST_PROVIDED, "") != "1") {
            context.openA<ChecklistActivity>()
        } else if (Pref.getStringValue(Pref.PREF_PRIORITY_GIVEN, "") != "1") {
            context.openA<PriorityActivity>()

        } else if ((Pref.getStringValue(Pref.PREF_CHECK_LIST_PROVIDED, "") == "1"
                    && Pref.getStringValue(Pref.PREF_PRIORITY_GIVEN, "") == "1"
                    && Pref.getStringValue(Pref.PREF_PROFILE_CONFIRMATION, "") == "1")
        ) {
            context.openA<MainActivity>()
            (context as Activity).finishAffinity()
        }
    }

    fun checkIsVideoFile(path: String): Boolean {
        var extension = ""
        val i = path.lastIndexOf('.')
        if (i > 0) {
            extension = path.substring(i + 1)
        }
        return extension.equals("MP4", ignoreCase = true)

    }

    fun isEmptyText(view: View?): Boolean {
        return if (view == null)
            true
        else
            getTextValue(view).isEmpty()


    }

    fun getTextValue(view: View): String {
        return (view as? EditText)?.text?.toString()?.trim { it <= ' ' }
            ?: ((view as? TextView)?.text?.toString()?.trim { it <= ' ' }
                ?: "")

    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(
            target
        )
            .matches()
    }


    fun isOnline(context: Context): Boolean {
        return try {
            val conMgr = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = conMgr.activeNetworkInfo
            if (info == null || !info.isConnected) showToastMessage(
                context,
                context.resources.getString(R.string.please_check_internet_connection),
                true
            )
            info != null && info.isConnected
        } catch (e: Exception) {
            showToastMessage(
                context,
                context.resources.getString(R.string.please_check_internet_connection),
                true
            )
            e.printStackTrace()
            false
        }
    }

    fun showToastMessage(context: Context, mgs: String, isShort: Boolean) {
        Toast.makeText(
            context,
            mgs,
            if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_SHORT
        ).show()
    }

    fun getImageUriFromPath(path: String): Uri {
        val file = File(path)
        return Uri.fromFile(file)
    }

    fun saveImageFromUrl(context: Context, imageUrl: String): Uri {
        // Create a URL object from the image URL string.
        val url = URL(imageUrl)

        // Create a bitmap from the image URL.
        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

        // Create a file to save the image to.
        val path = File("${context.externalCacheDir}/getMarried")
        val file = File(path, "temp_image${System.currentTimeMillis()}.png")

        // Save the image to the file.
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        // Get the URI of the saved image.
        val uri = Uri.fromFile(file)

        return uri
    }


    fun millisToDate(millis: Long, myFormat: String): String {
        return SimpleDateFormat(myFormat, Locale.US).format(Date(millis))
    }
//
//
//    fun millisToDateObj(millis: Long, myFormat: String): Date {
//        return SimpleDateFormat(myFormat, Locale.ENGLISH).parse(
//            SimpleDateFormat(
//                myFormat,
//                Locale.ENGLISH
//            ).format(Date(millis))
//        )
//    }
//
//    fun stringToMills(date: String, myFormat: String): Long {
//        return convertStringToDate(date, myFormat).time
//    }
//
//    fun convertDateStringToString(
//        strDate: String,
//        currentFormat: String, parseFormat: String
//    ): String {
//        return try {
//            if (strDate.isNotEmpty() && strDate.isNotBlank())
//                convertDateToString(convertStringToDate(strDate, currentFormat), parseFormat)
//            else ""
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//            ""
//        }
//    }


    fun convertStringToDate(
        strDate: String,
        parseFormat: String
    ): Date {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return SimpleDateFormat(parseFormat, Locale.ENGLISH).parse(strDate)

    }


    private fun convertDateToString(
        objDate: Date, parseFormat: String
    ): String {
        return try {
            SimpleDateFormat(parseFormat, Locale.getDefault()).format(objDate)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun convertDateStringToString(
        strDate: String,
        currentFormat: String, parseFormat: String,
    ): String {
        return try {
            if (strDate.isNotEmpty() && strDate.isNotBlank())
                convertDateToString(
                    convertStringToDate(strDate, currentFormat),
                    parseFormat
                )
            else ""
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun openWebIntent(context: Context, url: String) {
        var mUrl = url
        try {
            if (!mUrl.startsWith("http://") && !mUrl.startsWith("https://")) {
                mUrl = "http://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.resources.getString(R.string.choose_browser)
                )
            )


        } catch (e: ActivityNotFoundException) {
            showToastMessage(
                context,
                context.resources.getString(R.string.no_browser_app_available),
                true
            )
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun closeDialogAfterShortWait(view: View, obj: BottomSheetDialog) {
        view.isClickable = false
        Handler(Looper.myLooper()!!).postDelayed({
            obj!!.dismiss()
            view.isClickable = true
        }, dialogDisplayTimer)
    }

    fun statusBarColor(context: Context, window: Window) {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

//        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    fun statusBarColorDark(window: Window) {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun expandBottomSheet(bottomSheetDialog: BottomSheetDialog) {
        if (bottomSheetDialog.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDialog.behavior.peekHeight =
                Resources.getSystem().displayMetrics.heightPixels
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    @SuppressLint("ServiceCast", "Range")
    fun downloadImage(context: Context, url: String) {

        var msg: String? = ""
        var lastMsg = ""

        val directory = File("${context.externalCacheDir}")

//        if (!directory.exists()) {
//            directory.mkdirs()
//        }

        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalFilesDir(
                    context,
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }


        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    context.run {
                        print("----------------$msg--------------------------------------------")
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }.start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )

            else -> "There's nothing to download"
        }
        return msg
    }

    fun setHeaderProfile(context: MainActivity) {
        with(context.get())
        {
            imgCustomProfile.setActualImageResource(R.color.color_black)

            if (Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, "").toString()
                    .isNotEmpty()
            ) {
                imgCustomProfile.setImageURI(
                    Pref.getStringValue(
                        Pref.PREF_USER_DISPLAY_PICTURE,
                        ""
                    ).toString()
                )
                txtUserInits.visibility = View.GONE
            } else {
                txtUserInits.visibility = View.VISIBLE
                txtUserInits.text = getNameInitials(
                    Pref.getStringValue(
                        Pref.PREF_USER_NAME,
                        ""
                    ).toString()
                )
            }

            val errorOnProfile =
                when {
                    Pref.getStringValue(Pref.PREF_IS_ACCOUNT_BAN, "") == "1" -> true
                    Pref.getStringValue(Pref.PREF_IS_ACCOUNT_LOCKED, "") == "1" -> true
                    Pref.getStringValue(Pref.PREF_AADHAR_VERIFIED, "") == "0" -> true
                    Pref.getStringValue(Pref.PREF_UNDER_REVIEW, "") == "1" -> true
                    else -> false
                }
            imgAadharError.visibleIf(errorOnProfile)
            imgAadharError.setImageResource(
                when {
                    Pref.getStringValue(
                        Pref.PREF_IS_ACCOUNT_BAN,
                        ""
                    ) == "1" -> R.drawable.dr_ic_error_ban

                    Pref.getStringValue(
                        Pref.PREF_IS_ACCOUNT_LOCKED,
                        ""
                    ) == "1" -> R.drawable.dr_ic_error_locked

                    Pref.getStringValue(
                        Pref.PREF_UNDER_REVIEW,
                        ""
                    ) == "1" -> R.drawable.dr_ic_error_review

                    else -> R.drawable.dr_ic_error
                }
            )
            rlCustomProfile.setOnClickListener(
                when {
                    Pref.getStringValue(Pref.PREF_IS_ACCOUNT_BAN, "") == "1" -> null
                    Pref.getStringValue(Pref.PREF_IS_ACCOUNT_LOCKED, "") == "1" -> null
                    Pref.getStringValue(Pref.PREF_UNDER_REVIEW, "") == "1" -> null
                    Pref.getStringValue(Pref.PREF_AADHAR_VERIFIED, "") == "0" -> context
                    else -> null
                }
            )

        }
    }


    fun getDestinationUri(context: Context): Uri? {
        val getImage = context.externalCacheDir
        var finalUri: Uri? = null
        if (getImage != null) {

            val photoFile = File.createTempFile(
                "tempImage",
                ".png",
                context.externalCacheDir
            )//File(getImage.path, "pickImageResult.png")//

            finalUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                FileProvider.getUriForFile(
                    context,
                    "com.example.getmarried",
                    photoFile
                )
            } else {
                Uri.fromFile(photoFile)
            }


        }

        return finalUri
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermissions(context: Activity): Boolean {

        if (ContextCompat.checkSelfPermission(
                context,
                if (PhotoPickerAvailabilityChecker.isPhotoPickerAvailable())
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), REQUEST_CODE
            )

            return false
        }
    }

    object PhotoPickerAvailabilityChecker {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun isPhotoPickerAvailable(): Boolean {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> true
                else -> false
            }
        }
    }

    fun getImagePPI(imageFilePath: String): Float {
        // Decode the image file to get its density information
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, options)

        // Get the density in pixels per inch (PPI)
        val density = options.inDensity

        return density.toFloat()
    }

    fun showLottieDialog(
        context: Context,
        lottieFile: String,
        soundType: Int = 0,
        wrapContent : Boolean = false,
        isBlackBackground : Int = 0,
        onComplete : ((Boolean) -> Unit)? = null)
    {
        try {
            if (customLottieDialog != null && customLottieDialog!!.isShowing)
                customLottieDialog!!.dismiss()

            customLottieDialog = CustomLottieDialog(context, lottieFile, wrapContent,soundType,isBlackBackground) {
                if (it)
                    onComplete?.invoke(true)
            }

            customLottieDialog!!.setCancelable(false)
            customLottieDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun dismissLottieDialog() {
        if (customLottieDialog != null && customLottieDialog!!.isShowing)
            customLottieDialog!!.dismiss()
        customLottieDialog = null
    }

    fun clearNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @SuppressLint("CheckResult")
    fun clearData(activity: Activity) {
        MAIN_PROFILE_LIST.clear()
        mainObjList.clear()
        SELECTED_FILTER = ""
        ONBOARDING_SKIP = false
        Pref.clearAllPref()
        clearNotifications(activity)
        activity.startActivity(Intent(activity, MobileNumberActivity::class.java))
        if (ChatClient.isInitialized)
            ChatClient.instance().disconnect(true)
        activity.finishAffinity()
    }

    fun genderInitalsForFirebase(oppositeGender: String): String {
        val userGender: String =
            Pref.getStringValue(Pref.PREF_USER_GENDER, "").toString().trim().lowercase()

        return when {
            userGender == "male" && oppositeGender.trim().lowercase() == "female" -> {
                return "MtoF"
            }

            userGender == "female" && oppositeGender.trim().lowercase() == "male" -> {
                return "FtoM"
            }

            userGender == "female" && oppositeGender.trim().lowercase() == "female" -> {
                return "FtoF"
            }

            userGender == "male" && oppositeGender.trim().lowercase() == "male" -> {
                return "MtoM"
            }

            else -> ""
        }
    }

    fun startAnimationForAnswer(
        context: Context,
        firstImage : SimpleDraweeView) {

        firstImage.visible()
        firstImage.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
        playSound(context,2)

    }

    fun startAnimationForSameAnswer(
        context: Context,
        firstImage : SimpleDraweeView,
        secondImage : SimpleDraweeView,
        button: MaterialCardView,
        textView: AppCompatTextView) {

        firstImage.gone()
        secondImage.gone()

        Handler(Looper.getMainLooper()).postDelayed({
            firstImage.visible()
            firstImage.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            playSound(context,2)
        },150)

        Handler(Looper.getMainLooper()).postDelayed({
            secondImage.visible()
            secondImage.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            playSound(context,2)
        },450)

        Handler(Looper.getMainLooper()).postDelayed({
            animateCardViewColor(context,button,textView)
        },650)
    }

    fun startAnimationForSingleAnswer(
        context: Context,
        firstImage : SimpleDraweeView,
        secondImage : SimpleDraweeView,
        firstButton: MaterialCardView,
        firstTextView: AppCompatTextView,
        secondButton: MaterialCardView,
        secondTextView: AppCompatTextView) {

        firstImage.gone()
        secondImage.gone()

        Handler(Looper.getMainLooper()).postDelayed({
            firstImage.visible()
            firstImage.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            playSound(context,2)
        },150)

        Handler(Looper.getMainLooper()).postDelayed({
            secondImage.visible()
            secondImage.animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_anim)
            playSound(context,2)
        },450)

        Handler(Looper.getMainLooper()).postDelayed({
            animateCardViewColor(context,firstButton,firstTextView,true)
            animateCardViewColor(context,secondButton,secondTextView,false)
        },650)
    }

    private fun animateCardViewColor(context: Context, button: MaterialCardView, textView: AppCompatTextView,optionA : Boolean = true) {
        val animatorSet = AnimatorSet()
        val firstColor = ContextCompat.getColor(context,if (optionA) R.color.color_white else R.color.color_black)
        val secondColor = ContextCompat.getColor(context,if (optionA) R.color.color_black else R.color.color_white)

        // Background color animation
        val backgroundColorAnimator = ValueAnimator.ofArgb(button.cardBackgroundColor.defaultColor, firstColor)
        backgroundColorAnimator.addUpdateListener { animator ->
            button.setCardBackgroundColor(animator.animatedValue as Int)
        }

        // Text color animation
        val textColorAnimator = ValueAnimator.ofArgb(textView.currentTextColor, secondColor)
        textColorAnimator.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }

        // Stroke color animation
        val strokeColorAnimator = ValueAnimator.ofArgb(button.strokeColor, firstColor)
        strokeColorAnimator.addUpdateListener { animator ->
            button.strokeColor = animator.animatedValue as Int
        }

        // Add all the animators to the animator set
        animatorSet.playTogether(backgroundColorAnimator, textColorAnimator, strokeColorAnimator)

        // Start the animation
        animatorSet.start()
    }

    fun playSound(context: Context,soundType : Int) {

        val mediaPlayer = MediaPlayer()

        val soundFile = Uri.parse( "android.resource://" + context.packageName + "/" + when (soundType){
            1 -> R.raw.sound_unlocked
            2 -> R.raw.sound_sneak_peak
            else -> R.raw.sound_done
        })

        mediaPlayer.setDataSource(context,soundFile)

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)

        mediaPlayer.prepare()

        mediaPlayer.start()
    }


    fun createProgressBarAnimation(
        progressBar: ProgressBar,
        targetProgress: Int,
        fromRight: Boolean,
    ) {
        progressBar.progress = if (fromRight) 100 else 0
        val duration = (if (fromRight) 100-targetProgress else targetProgress) * 8
        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress).apply {
            this.duration = duration.toLong()
            start()
        }

    }

    fun getTextWithIcon(context: Context, text: String, image: Int, textSize: Float) : SpannableStringBuilder {
        val drawable: Drawable =
            ContextCompat.getDrawable(context, image)!!
        val imageSize = textSize.toInt()
        drawable.setBounds(
            0, 0,
            imageSize,
            imageSize
        )

        val spannableString = SpannableStringBuilder()
        spannableString.append("  ")
        spannableString.append(text)
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        spannableString.setSpan(
            imageSpan,
            0,
            1,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }


    fun getChatClient(context: Context): ChatClient {

        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true
            ),
            appContext = context
        )
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                FirebasePushDeviceGenerator(
                    providerName = context.getString(R.string.notification_provider_name)
                )
            )
        )

        return if (ChatClient.isInitialized)
            ChatClient.instance()
        else
            ChatClient.Builder(
                context.getString(R.string.get_stream_api_key),
                context.applicationContext
            )
                .withPlugins(offlinePluginFactory, statePluginFactory)
                .logLevel(ChatLogLevel.ALL)
                .notifications(notificationConfig)
                .build()
    }

    fun connectGetStreamUser(context: Context,onComplete : ((Boolean,ChannelListViewModelFactory?) -> Unit)? = null) {

        try {

            val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)
            val statePluginFactory = StreamStatePluginFactory(
                config = StatePluginConfig(
                    backgroundSyncEnabled = true,
                    userPresence = true
                ),
                appContext = context
            )
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = context.getString(R.string.notification_provider_name)))
            )

//            val client = ChatClient.Builder(
//                context.getString(R.string.get_stream_api_key),
//                context.applicationContext
//            )
//                .withPlugins(offlinePluginFactory, statePluginFactory)
//                .logLevel(ChatLogLevel.ALL)
//                .build()
            val client =
                if (ChatClient.isInitialized)
                    ChatClient.instance()
                else
                    ChatClient.Builder(context.getString(R.string.get_stream_api_key),context.applicationContext)
                        .withPlugins(offlinePluginFactory, statePluginFactory)
                        .logLevel(ChatLogLevel.ALL)
                        .notifications(notificationConfig)
                        .build()

            val user = User(
                id = Pref.getStringValue(Pref.PREF_USER_ID, "").toString(),
                name = Pref.getStringValue(Pref.PREF_USER_NAME, "").toString(),
                image = Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, "").toString(),
                invisible = true
            )

            print("PREPARING TO CONNECT USER")

            client.connectUser(
                user = user,
                token = Pref.getStringValue(Pref.PREF_STREAM_CHAT_TOKEN, "").toString()
            ).enqueue {
                if (it.isSuccess) {
                    print("USER CONNECT SUCCESSFULLY")

                    val filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.`in`("members", listOf(user.id)),
//                        Filters.greaterThan("unread_count", 0)
                    )


                    val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)

                    onComplete?.invoke(true,viewModelFactory)


                } else {
                    print("USER NOT CONNECT")
                    onComplete?.invoke(false,null)
                }

            }
        } catch (e:Exception){
            e.printStackTrace()
        }

    }

    /*fun connectGetStream(context: Context,onComplete : ((Boolean) -> Unit)? = null) {

        try {

            val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)
            val statePluginFactory = StreamStatePluginFactory(
                config = StatePluginConfig(
                    backgroundSyncEnabled = true,
                    userPresence = true
                ),
                appContext = context
            )
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = context.getString(R.string.notification_provider_name)))
            )

            val client =
                if (ChatClient.isInitialized)
                    ChatClient.instance()
                else
                    ChatClient.Builder(context.getString(R.string.get_stream_api_key),context.applicationContext)
                        .withPlugins(offlinePluginFactory, statePluginFactory)
                        .logLevel(ChatLogLevel.ALL)
                        .notifications(notificationConfig)
                        .build()

            val user = User(
                id = Pref.getStringValue(Pref.PREF_USER_ID, "").toString(),
                name = Pref.getStringValue(Pref.PREF_USER_NAME, "").toString(),
                image = Pref.getStringValue(Pref.PREF_USER_DISPLAY_PICTURE, "").toString(),
                invisible = true
            )

            print("PREPARING TO CONNECT USER")

            client.connectUser(
                user = user,
                token = Pref.getStringValue(Pref.PREF_STREAM_CHAT_TOKEN, "").toString()
            ).enqueue {
                if (it.isSuccess) {
                    print("USER CONNECT SUCCESSFULLY")
                    onComplete?.invoke(true)
                } else {
                    print("USER NOT CONNECT")
                }

            }
        } catch (e:Exception){
            e.printStackTrace()
        }

    }*/


    fun getChannelUser(context: Context, members: List<Member>): User {

        val commonFriendId = context.getString(R.string.common_friend_id)
        val currentUserId = Pref.getStringValue(Pref.PREF_USER_ID,"").toString()

        val list =
            members.filter { it.user.id != currentUserId && it.user.id != commonFriendId }
        return if (list.isNotEmpty())
            list[0].user
        else
            User()

    }


    // To Create CommonFriend user in getStream Console
     fun createCommonFriend(context : Context) {

        var userId = "64073c9bb97d2f57c44eb0a7"
        var userName = "Common Friend"
        var userProfilePic = "https://s3.us-east-1.amazonaws.com/getmarried/GetMarried/android_1708005161608.png"
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNjQwNzNjOWJiOTdkMmY1N2M0NGViMGE3In0.SE1dC_3DfWl92udnxSeK0nQ8q_CzDNwpBcthU0gTmKU"

        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true
            ),
            appContext = context
        )


        var client = ChatClient.Builder(context.getString(R.string.get_stream_api_key),context.applicationContext)
                    .withPlugins(offlinePluginFactory, statePluginFactory)
                    .logLevel(ChatLogLevel.ALL)
                    .build()


        val user = User(
            id = userId,
            name = userName,
            image = userProfilePic,
            invisible = true,
        )

        client.connectUser(
            user = user,
            token = token
        ).enqueue {
            if (it.isSuccess){
                Toast.makeText(context, "Common Friend user created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }




    }

    fun skipAndOpenHomeScreen(activity: Activity) {

        showLottieDialog(activity, "done_lottie.json",wrapContent = true) {

            ONBOARDING_SKIP = true
            mainObjList.clear()
            activity.openA<MainActivity>()
            activity.finishAffinity()

        }

        // onBoarding Skip Api
        if (isOnline(activity)) {

            val questionViewModel : QuestionViewModel = ViewModelProvider(
                activity as ViewModelStoreOwner,
                ViewModelFactory(ApiRepository())
            )[QuestionViewModel::class.java]

            questionViewModel.onBoardingSkipApiRequest()

        }
    }


}


