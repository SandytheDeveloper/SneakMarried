package com.commonfriend.utils


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.commonfriend.MainApplication
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


///////////////////////////////////////// CONTEXT ////////////////////////////////////


/**
 * Util Function for startActivity
 * open<BooksDetailActivity> {
 * putExtra("IntentKey","DATA")
 * putExtra("IntenKey@2", "DATA@2")
 * }
 * or
 * open<BooksDetailActivity>()
 * */
inline fun <reified T> Context.openA(extras: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.extras()
    startActivity(intent)
}


fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}


fun Context.toastL(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}


fun Fragment.toast(msg: String) {
    Toast.makeText(this.activity, msg, Toast.LENGTH_LONG).show()
}


fun Context.parseResColor(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}


fun Fragment.hideKeyboard() {
    activity?.hideKeyboard(view ?: View(activity))
}


fun Activity.hideKeyboard() {
    if (currentFocus == null) View(this) else currentFocus?.let { hideKeyboard(it) }
}


fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


fun Context.showAlert(
    message: String?,
    cancelable: Boolean = true,
    showPositiveButton: Boolean = true,
    showNegativeButton: Boolean = false,
    work: () -> Unit = { }
) {


    if (message.isNullOrEmpty()) return


    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
    } else {
        AlertDialog.Builder(this)
    }


    builder.setMessage(message).setCancelable(cancelable)


    if (showPositiveButton) {
        builder.setPositiveButton("No") { dialog, id ->
            dialog.dismiss()
        }
    }


    if (showNegativeButton) {
        builder.setNegativeButton("Ok") { dialog, id ->
            work.invoke()
            dialog.dismiss()
        }
    }


    val alert = builder.create()
    if (alert.getButton(Dialog.BUTTON_NEGATIVE).isNotNull())
        alert.getButton(Dialog.BUTTON_NEGATIVE).isAllCaps = false
    if (alert.getButton(Dialog.BUTTON_POSITIVE).isNotNull())
        alert.getButton(Dialog.BUTTON_POSITIVE).isAllCaps = false
    alert.show()
}


fun Context.showSignOutAlert(
    message: String?,
    cancelable: Boolean = true,
    showPositiveButton: Boolean = true,
    showNegativeButton: Boolean = false,
    work: () -> Unit = { }
) {


    if (message.isNullOrEmpty()) return


    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
    } else {
        AlertDialog.Builder(this)
    }


    builder.setMessage(message).setCancelable(cancelable)


    if (showPositiveButton) {
        builder.setPositiveButton("No") { dialog, id ->
            dialog.dismiss()
        }
    }


    if (showNegativeButton) {
        builder.setNegativeButton("SignOut") { dialog, id ->
            work.invoke()
            dialog.dismiss()
        }
    }


    val alert = builder.create()
    if (alert.getButton(Dialog.BUTTON_NEGATIVE).isNotNull())
        alert.getButton(Dialog.BUTTON_NEGATIVE).isAllCaps = false
    if (alert.getButton(Dialog.BUTTON_POSITIVE).isNotNull())
        alert.getButton(Dialog.BUTTON_POSITIVE).isAllCaps = false
    alert.show()
}


fun Context.showConfirmAlert(
    message: String?,
    positiveText: String?,
    negativeText: String?,
    onConfirmed: () -> Unit = {},
    onCancel: () -> Unit = { }
) {


    if (message.isNullOrEmpty()) return


    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
    } else {
        AlertDialog.Builder(this)
    }




    builder.setMessage(message)
        .setCancelable(false)
        .setPositiveButton(positiveText) { dialog, _ ->
            onConfirmed.invoke()
            dialog.dismiss()
        }
        .setNegativeButton(negativeText) { dialog, _ ->
            onCancel.invoke()
            dialog.dismiss()
        }


    val alert = builder.create()
    alert.getButton(Dialog.BUTTON_NEGATIVE).isAllCaps = false
    alert.getButton(Dialog.BUTTON_POSITIVE).isAllCaps = false
    alert.show()
}


fun String.capitalizedFirstLetter(): String {
    return if (this.isEmpty())
        ""
    else this.substring(0, 1).toUpperCase(Locale.getDefault()) + this.substring(1)
}

fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()


fun Bitmap.toFile(): File {
// Get the context wrapper
    val wrapper = ContextWrapper(MainApplication.instance)


// Initialize a new file instance to save bitmap object
    var file = wrapper.cacheDir
    file = File(file, "${UUID.randomUUID()}.jpg")


    try {
// Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(file)
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }


// Return the saved bitmap uri
    return file
}


///////////////////////////////////////// VIEW ////////////////////////////////////


inline fun EditText.observeTextChange(
    debounceDelay: Long = 0L,
    crossinline body: (String) -> Unit
) {
    val handler = Handler(Looper.getMainLooper())
    var searchRunnable: Runnable? = null


    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            searchRunnable?.let {
                handler.removeCallbacks(it)
            }


            val searchText = p0.toString().trim()
            if (searchText.isNotEmpty() && debounceDelay != 0L) {
                searchRunnable = Runnable {
                    body(searchText)
                }
                handler.postDelayed(searchRunnable!!, debounceDelay)
            } else {
                body(searchText)
            }
        }
    }


    addTextChangedListener(textWatcher)


// Clean up the handler and remove the text watcher when the EditText is detached
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {


        override fun onViewAttachedToWindow(p0: View) {


        }


        override fun onViewDetachedFromWindow(p0: View) {
            if (searchRunnable != null) {
                handler.removeCallbacks(searchRunnable!!)
            }
            removeTextChangedListener(textWatcher)
        }
    })
}


fun View.debouncedClick(debounceInterval: Long, action: () -> Unit) {


    var lastClickTime = System.currentTimeMillis()


    setOnClickListener {
        val currentTime = System.currentTimeMillis()


        if (currentTime - lastClickTime < debounceInterval) {
            return@setOnClickListener
        }


        lastClickTime = currentTime
        action()
    }
}


fun String.lengthTrimming(length: Int): String {
    return if (length > length) {


        substring(0, length)
    } else
        this
}


fun View.animateX(value: Float) {
    with(ObjectAnimator.ofFloat(this, View.TRANSLATION_X, value)) {
        duration = 3500
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        start()
    }
}


fun View.animateY(value: Float) {
    with(ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, value)) {
        duration = 3500
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        start()
    }
}


infix fun ViewGroup.inflate(@LayoutRes view: Int): View {
    return LayoutInflater.from(context).inflate(view, this, false)
}


fun Int.inflate(viewGroup: ViewGroup): View {
    return LayoutInflater.from(viewGroup.context).inflate(this, viewGroup, false)
}


fun View.visible() {
    this.visibility = View.VISIBLE
}


fun View.visibleIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.invisibleIf(condition: Boolean) {
    this.visibility = if (condition) View.INVISIBLE else View.VISIBLE
}


fun View.gone() {
    this.visibility = View.GONE
}


fun View.invisible() {
    this.visibility = View.INVISIBLE
}


fun View.toggleVisibility() {
    when (this.visibility) {
        View.VISIBLE -> this.gone()
        View.INVISIBLE -> this.visible()
        View.GONE -> this.visible()
    }
}


fun View.alphaGone() {
    animate()
        .alpha(0f)
        .setDuration(500)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@alphaGone.gone()
            }
        })
}


fun View.alphaVisible() {
    visible()
    animate()
        .alpha(0f)
        .setDuration(500)
        .interpolator = AccelerateDecelerateInterpolator()
}


///////////////////////////////////////// COMMON ////////////////////////////////////


inline fun <T> T.executeSafe(body: () -> Unit) {
    try {
        body.invoke()
    } catch (e: Exception) {


    }
}


fun <T> T.isNull(): Boolean {
    return this == null
}


fun <T> T.isNotNull(): Boolean {
    return this != null
}


inline infix operator fun Int.times(action: (Int) -> Unit) {
    var i = 0
    while (i < this) {
        action(i)
        i++
    }
}


fun String.remove(vararg value: String): String {
    var removeString = this
    value.forEach {
        removeString = removeString.replace(it, "")
    }
    return removeString
}


/*
fun makeRequired(context: Context, text: String): SpannableString {
val spannableString = SpannableString(text)
// Apply color formatting
val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.ayp_red))
spannableString.setSpan(
colorSpan,
text.length - 2,
text.length,
SpannableString.SPAN_INCLUSIVE_INCLUSIVE
)


return spannableString
}
*/




fun getCurrentTimeString(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}


fun getCurrentDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd ").format(Date())
}


fun checkNotificationIntent(bundle: Bundle): String {
    val data = bundle.getString("data")
    var type = ""
    if (data != null) {
        val dataJson = JSONObject(data)
        type = dataJson.getString("type")
    }


    return type
}


fun String.toRequestBody(): RequestBody {
    return RequestBody.create(MultipartBody.FORM, this)
}


fun Int.toRequestBody(): RequestBody {
    return RequestBody.create(MultipartBody.FORM, this.toString())
}


@SuppressLint("SimpleDateFormat")
fun getDate(timeStamp: Long): String {
//val time = java.util.Date(timeStamp as Long * 1000)
    val sdf1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S")
    val valueFromDB = timeStamp.toString()
    val d1 = sdf1.parse(valueFromDB)
    val sdf = SimpleDateFormat("dd-MM-yyyy")
    val dateWithoutTime = sdf.format(d1)
    println("sdf.format(d) $dateWithoutTime")
    return valueFromDB
}


fun String.toDisplayAlarm(): CharSequence? {


    val sdf1 = SimpleDateFormat("HH:mm")


    val d1 = sdf1.parse(this)
    val sdf = SimpleDateFormat("hh:mm aa")
    val dateWithoutTime = sdf.format(d1)


    return dateWithoutTime.toUpperCase()
}


fun Long.getDateDifference(): String {
    var different = Calendar.getInstance().time.time - this


    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val monthInMilli = hoursInMilli * 24 * 30
    val yearInMilli = hoursInMilli * 24 * 30 * 12


    val years = different / yearInMilli
    different %= yearInMilli


    val months = different / monthInMilli
    different %= monthInMilli


    val days = different / daysInMilli
    different %= daysInMilli


    val hours = different / hoursInMilli
    different %= hoursInMilli


    val minutes = different / minutesInMilli


    return when {
        years > 1 -> "$years years ago"
        years == 1.toLong() -> "a year ago"
        months > 1 -> "$months months ago"
        months == 1.toLong() -> "a month ago"
        days > 1 -> "$days days ago"
        days == 1.toLong() -> "a day ago"
        hours > 1 -> "$hours hours ago"
        hours == 1.toLong() -> "a hour ago"
        minutes > 1 -> "$minutes min ago"
        else -> "just now"
    }
}


fun getDateFromTimeStamp(timeStamp: Long): String {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    return formatter.format(Date(timeStamp))
}


fun getTimeStamp(DateString: String): Long {
    val date1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault(Locale.Category.FORMAT)
        ).parse(DateString)
    } else {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(DateString)
    }
    return date1.time
}


fun getDate(dateString: String, dateFormat: String = "yyyy-MM-dd"): Date? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(dateFormat, Locale.getDefault(Locale.Category.FORMAT)).parse(dateString)
    } else {
        SimpleDateFormat(dateFormat, Locale.getDefault()).parse(dateString)
    }
}


/** String MONOGO_DB_UTC= "yyyy-MM-dd'T'HH:mm:ss.SS'Z'"; */


fun getCustomDate(
    string: String, oldDateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
    newDateFormat: String = "yyyy"
): String {
    return try {
        val format = SimpleDateFormat(oldDateFormat, Locale.ENGLISH)
        val date: Date? = format.parse(string)
        val sdf = SimpleDateFormat(newDateFormat, Locale.ENGLISH)
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        string
    }
}


fun isDateInDefaultMongoUTC(
    string: String, oldDateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
    newDateFormat: String = "yyyy"
): String {
    return try {
        val format = SimpleDateFormat(oldDateFormat, Locale.ENGLISH)
        val date: Date? = format.parse(string)
        val sdf = SimpleDateFormat(newDateFormat, Locale.ENGLISH)
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


fun getMemorialDateFormat(string: String, newDateFormat: String = ""): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH)
        val date: Date? = format.parse(string)
        val sdf = SimpleDateFormat(newDateFormat.ifEmpty { "MMM dd, yyyy" }, Locale.ENGLISH)
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        string
    }
}


fun getProfileDateFormat(string: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH)
        val date: Date? = format.parse(string)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        string
    }
}


fun String.withCurrency(): String {
    return "KSH $this"
}


fun String.withDiscountCurrency(): String {
    return "KSH -$this"
}


/* When API got 403 401 code */
fun handleUnauthorized(response: Response<*>?): Boolean {
    /* if (response != null && (response.code() == GlobalConstants.RESPONSE_CODES.BLOCKED_403 || response.code() == GlobalConstants.RESPONSE_CODES.UNAUTHORIZED_401)) {
    DialogClass().logoutWithMessage(ApiResponseErrorHandling().getErrorMessage(response))
    return true
    }*/
    return false
}


fun String.isYoutubeUrl(): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val youtubePattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+"
    pattern = Pattern.compile(youtubePattern)
    matcher = pattern.matcher(this)
    return matcher.matches()
}


fun String.validateEmail(): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val EMAILPATTERN =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    pattern = Pattern.compile(EMAILPATTERN)
    matcher = pattern.matcher(this)
    return matcher.matches()
}


/**
 * Explanation:


^ # start-of-string
(?=.*[0-9]) # a digit must occur at least once
(?=.*[a-z]) # a lower case letter must occur at least once
(?=.*[A-Z]) # an upper case letter must occur at least once
(?=.*[@#$%^&+=]) # a special character must occur at least once
(?=\S+$) # no whitespace allowed in the entire string
.{8,} # anything, at least eight places though
$ # end-of-string


 */


fun String.validatePassword(): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val PASSWORD_PATTERN =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$"
    pattern = Pattern.compile(PASSWORD_PATTERN)
    matcher = pattern.matcher(this)
    return matcher.matches()
}


fun Long.toTime(dateFormat: String = "HH:mm:ss"): String {
    val mDate = if (this.toString().length == 10) Date(this * 1000) else Date(this)
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    return formatter.format(mDate)
}


fun Long.toDisplayDate(): String {
    val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale("en"))
// Create a calendar object that will convert the date and time value in
// milliseconds to date.
    val calendar1 = Calendar.getInstance()
    if (this.toString().length == 10) calendar1.timeInMillis =
        this * 1000 else calendar1.timeInMillis = this


    val calendar2 = Calendar.getInstance()
    calendar2.timeInMillis = Date().time
    if (calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR) && calendar1.get(
            Calendar.YEAR
        ) == calendar2.get(Calendar.YEAR)
    ) {
        return "Today"
    }
    return formatter.format(calendar1.time)
}


fun Long.toWeekday(): String {
    val displayDate = this.toDisplayDate()
    val getTime = this.toTime("hh:mm a")
    if (displayDate == "Today") {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun, $getTime"
            Calendar.MONDAY -> "Mon, $getTime"
            Calendar.TUESDAY -> "Tue, $getTime"
            Calendar.WEDNESDAY -> "Wed, $getTime"
            Calendar.THURSDAY -> "Thurs, $getTime"
            Calendar.FRIDAY -> "Fri, $getTime"
            Calendar.SATURDAY -> "Sat, $getTime"
            else -> "Unknown"
        }
    }
    return displayDate // If not "Today", return the original display date
}


fun Long.greaterThan20MB(): Boolean {
    val sizeInKB = this / 1024
    val sizeInMB = sizeInKB / 1024
    return sizeInMB > 20
}


fun printHashKey(pContext: Context) {
    try {
        val info = pContext.packageManager.getPackageInfo(
            pContext.packageName,
            PackageManager.GET_SIGNATURES
        )
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("TAG", "printHashKey()", e)
    } catch (e: NoSuchAlgorithmException) {
        Log.e("TAG", "printHashKey()", e)
    }
}


fun checkResolution(): Boolean {
    val bitmap = BitmapFactory.decodeFile(SELECTED_IMAGE)
    val resolution = 720
    var width = bitmap.width
    var height = bitmap.height

    if (width >= resolution && height >= resolution) {
        if (width > height) {
            width = (width * resolution) / height
            height = resolution
        } else {
            height = (height * resolution) / width
            width = resolution
        }

        SELECTED_IMAGE = Bitmap.createScaledBitmap(bitmap, width, height, false).toFile().toString()

        return true

    } else {
        return false
    }
}


/*TEXT VIEWS*/
fun TextView.adjustFontSize(
    maxLength: Int = 10,
    originalFontSize: Float = 16f,
    isBiggerScale: Boolean = false
) {

//        val maxLength = 10  // Maximum length for which you want to set a font size
//        val originalFontSize = 16f  // Original font size in dp
    val scaleFactor =
        if (isBiggerScale) 0.8f else 0.65f  // Scaling factor to reduce the font size

    val text = this.text.toString()
    val textSize = if (text.length <= maxLength) {
        originalFontSize
    } else {
        val scaledFontSize = originalFontSize * scaleFactor
        scaledFontSize.coerceAtLeast(12f)  // Minimum font size to prevent it from becoming too small
    }

    this.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
}


fun JSONObject.toBundle(): Bundle {
    val bundle = Bundle()

    try {
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()

            when (val value = this.get(key)) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                // Add more data types as needed

                // You can also handle nested JSON objects or arrays recursively
            }
        }

    } catch (e: JSONException) {
        e.printStackTrace()
        // Handle the exception according to your needs
    }

    return bundle
}


fun String.removeString(text: String="candidate_"): String {
    return if (this.contains(text)) {
        this.remove(text)
    } else
        this
}


fun LottieAnimationView.onAnimationCompleted(onCompleted: () -> Unit) {
    addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            // Optional: Do something on animation start if needed
        }

        override fun onAnimationEnd(animation: Animator) {
            onCompleted.invoke()
        }

        override fun onAnimationCancel(animation: Animator) {
            // Optional: Do something on animation cancel if needed
        }

        override fun onAnimationRepeat(animation: Animator) {
            // Optional: Do something on animation repeat if needed
        }
    })
}



fun RecyclerView.scrollToPositionWithOffsetSmooth(position: Int, offset: Int) {
    val layoutManager = this.layoutManager as LinearLayoutManager
    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START // You can customize this based on your requirements
        }

        override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
            return boxStart - viewStart + offset // Calculate the distance to scroll
        }
    }

    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}


fun Activity.activityOnBackPressed( // OnBackPressed is Deprecated
    lifecycleOwner: LifecycleOwner,
    componentActivity: ComponentActivity,
    onBackPress: ((Boolean) -> Unit)? = null
) {

    componentActivity.onBackPressedDispatcher.addCallback(lifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress!!.invoke(true)
            }
        })
}


