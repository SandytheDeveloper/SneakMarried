package com.commonfriend.fcm

import com.commonfriend.R
import com.commonfriend.utils.Util
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.android.push.firebase.FirebaseMessagingDelegate

class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Update device's token on Stream backend
        try {
            FirebaseMessagingDelegate.registerFirebaseToken(token, getString(R.string.notification_provider_name))
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            if (FirebaseMessagingDelegate.handleRemoteMessage(message)) {
                // RemoteMessage was from Stream and it is already processed
                Util.print("---------received-------------------------")
            } else {
                // RemoteMessage wasn't sent from Stream and it needs to be handled by you
                Util.print("---------failed-------------------------")
            }
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }
}