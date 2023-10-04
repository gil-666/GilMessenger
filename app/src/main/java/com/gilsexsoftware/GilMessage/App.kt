package com.gilsexsoftware.GilMessage
import android.app.Application
import com.gilsexsoftware.GilMessage.ChannelActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiKey = "cdrk83rwm524"
        val context = applicationContext

        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
            ),
            appContext = applicationContext,
        )
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = "FB"))
        )
        // Step 2 - Set up the client for API calls with the plugin for offline storage
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = applicationContext,
            newMessageIntent = { messageId: String, channelType: String, channelId: String ->
                // Create an Intent to open ChannelActivity and pass the channelId as an extra
                val intent = MyFirebaseMessagingService.newIntentFromNotification(this, "$channelType:$channelId", true)

                intent
            }
        )
        val client = ChatClient.Builder("$apiKey", applicationContext)
            .withPlugin(offlinePluginFactory)
            .notifications(notificationConfig, notificationHandler)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
    }
}