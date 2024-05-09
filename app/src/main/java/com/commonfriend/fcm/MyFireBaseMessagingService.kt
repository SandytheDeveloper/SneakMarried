package com.commonfriend.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.commonfriend.ChannelActivity
import com.commonfriend.EditProfileActivity
import com.commonfriend.MainActivity
import com.commonfriend.MainApplication.Companion.firebaseAnalytics
import com.commonfriend.QuestionListActivity
import com.commonfriend.R
import com.commonfriend.SplashActivity
import com.commonfriend.models.MessageNotificationModel
import com.commonfriend.utils.ActivityIsFrom
import com.commonfriend.utils.CHAT_ID_FOR_NOTIFICATION
import com.commonfriend.utils.CID_FOR_NOTIFICATION
import com.commonfriend.utils.DATA
import com.commonfriend.utils.ID
import com.commonfriend.utils.IS_FROM
import com.commonfriend.utils.PUSH_NOTIFICATION
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.isNotNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Objects
import java.util.Random
import androidx.core.app.Person
import com.commonfriend.ProfileViewActivity

class MyFireBaseMessagingService : FirebaseMessagingService() {

    private val tag = "MyFirebaseMsgService"
    lateinit var notificationManager: NotificationManager
    private lateinit var handler: Handler
    private var bannerUrl = ""


    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) { //        MediaPlayer mMediaPlayer = MediaPlayer.create(this, R.raw.pushnotification);
        Log.d(
            tag,
            "remoteMessage: " + remoteMessage.data.toString()
        )
        Log.d(tag, "From: " + remoteMessage.from)
        handler = Handler(Looper.getMainLooper())
        handler.post {
            if (remoteMessage.data.isNotEmpty()) {

                if (remoteMessage.data.containsKey("body")) {
                    try { //                        {"body":"[{\"msg\":\"Kane Williomson Liked your Post\",\"ref_id\":38,\"user_id\":\"5\",\"tag\":\"9\"}]"}
                        val jsonArray =
                            JSONArray(Objects.requireNonNull(remoteMessage.data["body"]))

                        sendNotificationForOther(jsonArray.optJSONObject(0))
                    } catch (e: JSONException) {
                        e.printStackTrace() // ignore this error ,this error not show if user come from notification   otherwise its shows always
                    }
                } else {

                    if (Pref.getStringValue(Pref.PREF_IS_ACCOUNT_BAN, "") == "1")
                        return@post

                    val dataMap = remoteMessage.data
                    val jsonObject = (dataMap as Map<*, *>?)?.let { JSONObject(it) }

                    if (jsonObject != null) {

                        val model = MessageNotificationModel()

                        model.receiverId = jsonObject.optString("receiver_id").toString()

                        if (model.receiverId == Pref.getStringValue(Pref.PREF_USER_ID,"")){

                            model.cid = jsonObject.optString("cid").toString()
                            model.messageId = jsonObject.optString("message_id").toString()
                            model.type = jsonObject.optString("type").toString()

                            if (model.cid == CID_FOR_NOTIFICATION)
                                return@post

                            var chatClient : ChatClient

                            (if (ChatClient.isInitialized) {
                                chatClient = ChatClient.instance()
                                buildAndSend(chatClient,model)
                            }
                            else {
                                Util.connectGetStreamUser(this) { isConnected, _ ->
                                    if (isConnected){
                                        chatClient = ChatClient.instance()
                                        buildAndSend(chatClient,model)
                                    }
                                }
                            })

                        }

                    }
                }
            }
        }

        val message = Message()
        message.obj = remoteMessage
        handler.sendMessage(message)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun buildAndSend(chatClient : ChatClient, model: MessageNotificationModel) {


        chatClient.channel(model.cid).getMessage(model.messageId)
            .enqueue { result ->
                if (result.isSuccess) {
                    result.onSuccess {

                        model.message = it.text
                        model.name = it.user.name
                        sendNotificationForChat(model)

                    }
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotificationForOther(jsonObject: JSONObject) {

        if (!Pref.getBooleanValue(Pref.PREF_LOGIN, false)){
            return
        }

        //  {body=[{"title":"Raj Patel","msg":"ccv","type":"12","ref_id":"64d9f4a11b9e92bbc2ff8dd8"}]}

        //  TYPE :: 1 = que bank, 2=edit profile request, 3=new recommendation(instant), 4=new recommendation (weekly),
        //  5=reference chain found, 6=new profile alert, 7=associate request received, 8=candidate file,
        //  9=associate reaction, 10=profile interest received, 11=chat interest received, 12=msg received,
        //  13=you have been referred, 14= sneak peak, 15= profile unlock,20 =Lock user profile from admin , 21 = profile unlocked (LockBan)

        val title =
            jsonObject.optString("title").ifBlank { resources.getString(R.string.app_name) }

        val message =
            jsonObject.optString("msg")

        val type =
            jsonObject.optString("type")

        val refId =
            jsonObject.optString("ref_id")

        val matchStatus =
            jsonObject.optString("status_text")

        val similarAnswer =
            jsonObject.optString("similar_answer")

        val commonQuestion =
            jsonObject.optString("common_question")

        val sneakPeakStatus =
            jsonObject.optString("sneak_peak_status")

        val userStatus =
            jsonObject.optString("user_status")

        val recommendationType =
            jsonObject.optString("is_profile_lock")

        val oppositeGender =
            jsonObject.optString("opponent_gender")

        val sentRecommendation =
            jsonObject.optInt("sent_recommendation")

        val availableRecommendation =
            jsonObject.optInt("available_recommendation")

        if (type == "12") {
            if (refId == CHAT_ID_FOR_NOTIFICATION)
                return
        }


        // for refreshing activites without navigating yto other activites
        val pushNotification = Intent(PUSH_NOTIFICATION)
        pushNotification.putExtra("NOTIFICATION_TAG", jsonObject.optString("type"))
        LocalBroadcastManager.getInstance(this@MyFireBaseMessagingService)
            .sendBroadcast(pushNotification)

        //  TYPE :: 1 = que bank, 2=edit profile request, 3=new recommendation(instant), 4=new recommendation (weekly),
        //  5=reference chain found, 6=new profile alert, 7=associate request received, 8=candidate file,
        //  9=associate reaction, 10=profile interest received, 11=chat interest received, 12=msg received,
        //  13=you have been referred, 14= sneak peak, 15= profile unlock,20 =Lock user profile from admin , 21 = profile unlocked (LockBan)
        //  16=single profile screen

        if (type == "11") {
            val bundle = Bundle().apply {
                putString("request_from", refId)
                putString("request_to", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                putString("gen_from_to", Util.genderInitalsForFirebase(oppositeGender))
                putString("client_id", "")
                putString("match_status", matchStatus)
                putString("similar_percentage",similarAnswer)
                putString("common_questions",commonQuestion)
                putString("opinion_status",sneakPeakStatus)
                putString("user_status",userStatus)
                putString(
                    "recommendation_type",
                    if (recommendationType == "1") "Locked" else "Unlocked"
                )
            }
            firebaseAnalytics.logEvent("chat_req_received", bundle)
        }

        val resultIntent =
            Intent(
                this,
                if (Pref.getBooleanValue(Pref.PREF_LOGIN, false)
                    && Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "") == "1"
                ) {
                    when (type) {
                        "1" -> QuestionListActivity::class.java
                        "2" -> EditProfileActivity::class.java
                        "3", "4", "10", "11", "14", "15" ->
                            MainActivity::class.java // Profile Screen
                        "12" -> MainActivity::class.java // Chat Screen
                        "16" -> ProfileViewActivity::class.java
                        else -> MainActivity::class.java
                    }
                } else
                    SplashActivity::class.java
            )

        if (type in listOf("3", "4", "10", "11", "14", "15", "16")) {
            resultIntent.putExtra(DATA, 2) // Profile Screen
                .putExtra(ID, refId)
        } else if (type == "12") { // Chat Screen
            resultIntent.putExtra(DATA, 3) // ChatFragment Screen
                .putExtra(ID, refId) // chat Id
        }

        resultIntent.putExtra(IS_FROM, ActivityIsFrom.NOTIFICATION)

        Util.print("-------type--$type-------refId---$refId--------------------------------")



        val channelId = "default_channel"
        val channelName = "Default Channel"
        val notificationId = Random().nextInt(80 - 65) + 65

        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun sendNotificationForChat(model : MessageNotificationModel) {

        val title = model.name.ifBlank { resources.getString(R.string.app_name) }

        val message = model.message.ifBlank { " " }

        val type = model.type.ifBlank { "1" }


        //for refreshing activites without navigating yto other activites
        val pushNotification = Intent(PUSH_NOTIFICATION)
        pushNotification.putExtra("NOTIFICATION_TAG", type)
        LocalBroadcastManager.getInstance(this@MyFireBaseMessagingService)
            .sendBroadcast(pushNotification)


        val channelId = "default_channel"
        val channelName = "Default Channel"
        val notificationId = Random().nextInt(80 - 65) + 65

        val resultIntent =
            if (Pref.getBooleanValue(Pref.PREF_LOGIN, false)
                && Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "") == "1"
            )
                ChannelActivity.newIntent(this, model.cid)
            else
                Intent(this,SplashActivity::class.java)

        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    // before 14 Feb 24
    /*@RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(jsonObject: JSONObject) {

        if (!Pref.getBooleanValue(Pref.PREF_LOGIN, false)){
            return
        }

        //  {body=[{"title":"Raj Patel","msg":"ccv","type":"12","ref_id":"64d9f4a11b9e92bbc2ff8dd8"}]}

        //  TYPE :: 1 = que bank, 2=edit profile request, 3=new recommendation(instant), 4=new recommendation (weekly),
        //  5=reference chain found, 6=new profile alert, 7=associate request received, 8=candidate file,
        //  9=associate reaction, 10=profile interest received, 11=chat interest received, 12=msg received,
        //  13=you have been referred, 14= sneak peak, 15= profile unlock,20 =Lock user profile from admin , 21 = profile unlocked (LockBan)

        val title =
            jsonObject.optString("title").ifBlank { resources.getString(R.string.app_name) }

        val message =
            jsonObject.optString("msg")

        val type =
            jsonObject.optString("type")

        val refId =
            jsonObject.optString("ref_id")

        val matchStatus =
            jsonObject.optString("status_text")

        val similarAnswer =
            jsonObject.optString("similar_answer")

        val commonQuestion =
            jsonObject.optString("common_question")

        val sneakPeakStatus =
            jsonObject.optString("sneak_peak_status")

        val userStatus =
            jsonObject.optString("user_status")

        val recommendationType =
            jsonObject.optString("is_profile_lock")

        val oppositeGender =
            jsonObject.optString("opponent_gender")

        val sentRecommendation =
            jsonObject.optInt("sent_recommendation")

        val availableRecommendation =
            jsonObject.optInt("available_recommendation")

        if (type == "12") {
            if (refId == CHAT_ID_FOR_NOTIFICATION)
                return
        }

//        val activity = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val runningTasks = activity.getRunningTasks(1)
//        if (runningTasks.isNotEmpty()){
//            val currentActivity = runningTasks[0].topActivity
//        }

        //for refreshing activites without navigating yto other activites
        val pushNotification = Intent(PUSH_NOTIFICATION)
        pushNotification.putExtra("NOTIFICATION_TAG", jsonObject.optString("type"))
        LocalBroadcastManager.getInstance(this@MyFireBaseMessagingService)
            .sendBroadcast(pushNotification)


        val notificationBuilder: NotificationCompat.Builder
        lateinit var pendingIntent: PendingIntent
        val notificationCount = Random().nextInt(80 - 65) + 65

        //  TYPE :: 1 = que bank, 2=edit profile request, 3=new recommendation(instant), 4=new recommendation (weekly),
        //  5=reference chain found, 6=new profile alert, 7=associate request received, 8=candidate file,
        //  9=associate reaction, 10=profile interest received, 11=chat interest received, 12=msg received,
        //  13=you have been referred, 14= sneak peak, 15= profile unlock,20 =Lock user profile from admin , 21 = profile unlocked (LockBan)

        if (type == "11") {
            val bundle = Bundle().apply {
                putString("request_from", refId)
                putString("request_to", Pref.getStringValue(Pref.PREF_USER_ID, ""))
                putString("gen_from_to", Util.genderInitalsForFirebase(oppositeGender))
                putString("client_id", "")
                putString("match_status", matchStatus)
                putString("similar_percentage",similarAnswer)
                putString("common_questions",commonQuestion)
                putString("opinion_status",sneakPeakStatus)
                putString("user_status",userStatus)
                putString(
                    "recommendation_type",
                    if (recommendationType == "1") "Locked" else "Unlocked"
                )
            }
            firebaseAnalytics.logEvent("chat_req_received", bundle)
        }

        val resultIntent =
            Intent(
                this,
                if (Pref.getBooleanValue(Pref.PREF_LOGIN, false)
                    && Pref.getStringValue(Pref.PREF_USER_PROFILE_COMPLETED, "") == "1"
                ) {
                    when (type) {
                        "1" -> QuestionListActivity::class.java
                        "2" -> EditProfileActivity::class.java
                        "3", "4", "10", "11", "14", "15" ->
                            MainActivity::class.java // Profile Screen
                        "12" -> MainActivity::class.java // Chat Screen
//                        "12" -> ChatDetailsActivity::class.java // Chat Screen
                        else -> MainActivity::class.java
                    }
                } else
                    SplashActivity::class.java
            )

        if (type in listOf("3", "4", "10", "11", "14", "15")) {
            resultIntent.putExtra(DATA, 2) // Profile Screen
                .putExtra(ID, refId)
        } else if (type == "12") { // Chat Screen
            resultIntent.putExtra(DATA, 3) // ChatFragment Screen
                .putExtra(ID, refId) // chat Id
        }

        resultIntent.putExtra(IS_FROM, ActivityIsFrom.NOTIFICATION)

        Util.print("-------type--$type-------refId---$refId--------------------------------")


        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        val defaultSoundUri =
            if (jsonObject.optString("noti_type").equals(""))
                Uri.parse(
                    "android.resource://" +
                            applicationContext.packageName +
                            "/" +
                            R.raw.emergency_alert
                )
            else
            RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                notificationCount.toString(),
                message,
                NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.description = message
            mChannel.lightColor = Color.CYAN
//            mChannel.canShowBadge()
//            mChannel.setBadgeIconType(BADGE_ICON_SMALL)
            mChannel.setShowBadge(true)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            mChannel.setSound(defaultSoundUri, audioAttributes)
            notificationBuilder = NotificationCompat.Builder(this, notificationCount.toString())
            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
//                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_common_logo))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setStyle(
                    if (bannerUrl.isNotEmpty())
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(doInBackground(jsonObject.optString("banner_url"))) else NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setChannelId(notificationCount.toString())
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            notificationManager.createNotificationChannel(mChannel)
        } else {
            notificationBuilder = NotificationCompat.Builder(this, notificationCount.toString())
            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
//                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_common_logo))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri, AudioManager.STREAM_NOTIFICATION)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }
        notificationManager.notify(
            notificationCount,
            notificationBuilder.build()
        )
        val pm =
            this.getSystemService(POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "BankerDilSe: tag"
        )
        wl.acquire(6000)

//        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        val cn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(am.appTasks!=null && am.appTasks.size>0)
//            am.appTasks[0].taskInfo.topActivity!!.className
//        } else {
//            am.getRunningTasks(1)[0].topActivity!!.className
//        }


    }
    */

    /*protected fun doInBackground(vararg params: String): Bitmap? {
        val `in`: InputStream
        bannerUrl = params[0] + params[1]
        try {
            val url = URL(params[2])
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            `in` = connection.getInputStream()
            return BitmapFactory.decodeStream(`in`)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }*/


    /*private fun getNotificationIcon(notificationBuilder: NotificationCompat.Builder): Int {
//        notificationBuilder.color = ContextCompat.getColor(this,R.color.black)
        return R.drawable.dr_ic_notification_icon
//        return R.mipmap.ic_launcher
    }*/

    private fun getNotificationIcon(): Int {
        return R.drawable.dr_ic_notification_icon
    }

}