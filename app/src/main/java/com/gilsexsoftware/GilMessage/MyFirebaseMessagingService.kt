package com.gilsexsoftware.GilMessage;
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
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

            // Open ChannelActivity with the channel ID
        } catch (exception: IllegalStateException) {
            println("se mio ${exception.message}")
        }
    }

    companion object {
        private const val CID_KEY = "key:cid"
        const val OPEN_CHANNEL_FROM_NOTIFICATION_ACTION = "com.gilsexsoftware.GilMessage.OPEN_CHANNEL_FROM_NOTIFICATION"
        private const val FROM_NOTIFICATION_KEY = "from_notification"
        private lateinit var sharedPreferences: SharedPreferences
        fun newIntent(context: Context, channel: Channel): Intent =
            Intent(context, ChannelActivity::class.java).putExtra(CID_KEY, channel.cid)
        fun newIntentFromNotification(context: Context, channelId: String?, fromNotification: Boolean): Intent {
            val intent = Intent(context, ChannelActivity::class.java)
            intent.action = OPEN_CHANNEL_FROM_NOTIFICATION_ACTION
            intent.putExtra(CID_KEY, channelId)
            intent.putExtra(FROM_NOTIFICATION_KEY, fromNotification)
            return intent
        }
    }
}
