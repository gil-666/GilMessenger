package com.gilsexsoftware.GilMessage;
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.pushprovider.firebase.FirebaseMessagingDelegate

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var chatClient: ChatClient
    val apiKey = "cdrk83rwm524"
    override fun onNewToken(token: String) {
        // Update device's token on Stream backend
        try {
            FirebaseMessagingDelegate.registerFirebaseToken(token, "FB")

        } catch (exception: IllegalStateException) {
            println("se mio token ${exception.message}")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {

            if (FirebaseMessagingDelegate.handleRemoteMessage(message)) {
                println("message was handled by Stream ${message.data}")
                // RemoteMessage was from Stream and it is already processed
            } else {
                println("message not handle from Stream ${message.data}")
            }
        } catch (exception: IllegalStateException) {
            println("se mio ${exception.message}")
        }
    }
}
