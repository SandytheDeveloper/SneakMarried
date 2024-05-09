package com.commonfriend


import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.performance.BugsnagPerformance
import com.commonfriend.utils.Pref
import com.facebook.drawee.backends.pipeline.Fresco
import com.commonfriend.utils.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.branch.referral.Branch
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import java.util.Locale


class MainApplication : Application() {


//    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    companion object {
        lateinit var instance: MainApplication
        lateinit var dataBaseRoot: DatabaseReference
        lateinit var  firebaseAnalytics:FirebaseAnalytics
//        lateinit var  ablyRealtime: AblyRealtime
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        dataBaseRoot = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path_key))

        FirebaseApp.initializeApp(instance)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Fresco.initialize(this)
        Bugsnag.start(instance)
        BugsnagPerformance.start(instance)
        Util.setToken()
        Locale.setDefault(Locale.ENGLISH)

        // Branch logging for debugging
        Branch.enableLogging()
        Branch.getAutoInstance(this)
    }

    @JvmName("getFirebaseAnalytics")
    fun getFirebaseAnalytics(): FirebaseAnalytics {
        /*if(firebaseAnalytics==null)
        {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        }*/
        return firebaseAnalytics
    }


    /*private fun buildChatClient() {

        try {

            val offlinePluginFactory = StreamOfflinePluginFactory(appContext = this)
            val statePluginFactory = StreamStatePluginFactory(
                config = StatePluginConfig(
                    backgroundSyncEnabled = true,
                    userPresence = true
                ),
                appContext = this
            )
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = getString(R.string.notification_provider_name)))
            )

            ChatClient.Builder(
                getString(R.string.get_stream_api_key),
                applicationContext
            )
                .withPlugins(offlinePluginFactory, statePluginFactory)
                .logLevel(ChatLogLevel.ALL)
                .notifications(notificationConfig)
                .build()

            Util.print("PREPARING TO CONNECT STREAM CHAT")

        } catch (e:Exception){
            e.printStackTrace()
        }

    }*/

}