package com.commonfriend.base

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.commonfriend.MainActivity
import com.commonfriend.MainApplication
import com.commonfriend.R
import com.commonfriend.custom.ErrorDialogComponent
import com.commonfriend.utils.EN
import com.commonfriend.utils.Pref
import com.commonfriend.utils.Util
import com.commonfriend.utils.visibleIf
import com.google.firebase.analytics.FirebaseAnalytics
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import java.util.Locale


open class BaseActivity : AppCompatActivity() {


    //to control backpress in question activites
    var backPressedOnce: Boolean = true

    var bundle: Bundle? = null
//    var liveChatList = MutableLiveData<List<Channel>>()

    private var firebaseAnalytics:FirebaseAnalytics? =null


    var errorDialogComponent: ErrorDialogComponent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Util.statusBarColor(this, window)
        Locale.setDefault(Locale(EN))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        firebaseAnalytics = (application as MainApplication).getFirebaseAnalytics()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


    }

    /*private fun setChatList() {

        Util.connectGetStream(this) {

            val userId = ChatClient.instance().getCurrentUser()?.id

            val filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(userId!!))
            )

            val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)


            val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

            viewModel.state.observe(this) { state ->
                try {
                    if (state.isLoading) {

                        Util.print("---HOME---state.isLoading-----------------")

                    } else {

                        liveChatList.postValue(state.channels)

                    }

                } catch (e:Exception){
                    e.printStackTrace()
                }
            }

        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        Util.dismissProgress()
    }

    override fun onStop() {
        super.onStop()
        backPressedOnce = true
    }

    fun logForCurrentScreen(screenType: String?, screenName: String?) {
        firebaseAnalytics?.logEvent("custom_screen_view", Bundle().apply {
            putString("screen_type", screenType)
            putString("custom_screen_name", screenName)
            putString("cf_id", Pref.getStringValue(Pref.PREF_USER_ID, ""))
        })
    }

    fun firebaseEventLog(eventName: String?, data: Bundle?) {
        firebaseAnalytics?.logEvent(eventName!!, data)
    Util.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>eventName: $eventName , Bundle: $data")
    }
}
