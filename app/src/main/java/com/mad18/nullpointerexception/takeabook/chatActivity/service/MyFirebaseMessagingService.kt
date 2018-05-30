package com.mad18.nullpointerexception.takeabook.chatActivity.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null ) {
            //TODO: Show notification
            Log.d("FCM-Messaging", "FCM message received!")
        }
    }
}