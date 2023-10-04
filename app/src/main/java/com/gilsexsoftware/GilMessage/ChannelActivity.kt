package com.gilsexsoftware.GilMessage

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode.Normal
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode.Thread
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.State.NavigateUp
import com.gilsexsoftware.GilMessage.databinding.ActivityChannelBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory


class ChannelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelBinding
    private lateinit var sharedPreferences: SharedPreferences
    val apiKey = "cdrk83rwm524"

    var user = User(

        id = "",
        image = ""

    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 0 - inflate binding
        binding = ActivityChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fromNotification = intent.getBooleanExtra(FROM_NOTIFICATION_KEY, false)
        val client = ChatClient.instance()
        val cid = checkNotNull(intent.getStringExtra(CID_KEY)) {
            "Specifying a channel id is required when starting ChannelActivity"
        }

        if (!ChatClient.instance().isSocketConnected() && fromNotification && !cid.isNullOrEmpty()) {
            val apiKey = "$apiKey"
            val offlinePluginFactory = StreamOfflinePluginFactory(
                config = Config(
                    backgroundSyncEnabled = true,
                    userPresence = true,
                    persistenceEnabled = true,
                    uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING
                ),
                appContext = applicationContext
            )
            val chatClient = ChatClient.Builder(apiKey, applicationContext)
                .withPlugin(offlinePluginFactory)
                .build()

            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("username", "").toString()
            val user = User(id = savedUsername)  // Replace with the actual user ID
            val token = client.devToken(savedUsername)  // Replace with the user's token (e.g., obtained from your backend)

            chatClient.connectUser(user, token)
                .enqueue { result ->
                    if (result.isSuccess) {
                        // Connection successful
                        // Handle success cases
                    } else {
                        // Handle connection failure
                        val errorMessage = result.error().message
                        // Handle the error, e.g., display an error message
                    }
                }
        }

        // Step 1 - Create three separate ViewModels for the views so it's easy
        //          to customize them individually
        val factory = MessageListViewModelFactory(cid)
        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }

        // TODO set custom Imgur attachment factory

        // Step 2 - Bind the view and ViewModels, they are loosely coupled so it's easy to customize
        messageListHeaderViewModel.bindView(binding.messageListHeaderView, this)
        messageListViewModel.bindView(binding.messageListView, this)
        messageInputViewModel.bindView(binding.messageInputView, this)

        // Step 3 - Let both MessageListHeaderView and MessageInputView know when we open a thread
        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is Thread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageInputViewModel.setActiveThread(mode.parentMessage)
                }
                Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageInputViewModel.resetThread()
                }
            }
        }

        // Step 4 - Let the message input know when we are editing a message
        binding.messageListView.setMessageEditHandler(messageInputViewModel::postMessageToEdit)

        // Step 5 - Handle navigate up state
        messageListViewModel.state.observe(this) { state ->
            if (state is NavigateUp) {
                finish()
            }
        }

        // Step 6 - Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            if (fromNotification) {
                // Coming from a notification, navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // Finish the ChannelActivity
            } else {
                // Normal back behavior
                messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
            }
        }

        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        onBackPressedDispatcher.addCallback(this) {

            backHandler()
        }
    }

    companion object {
        private const val CID_KEY = "key:cid"
        const val OPEN_CHANNEL_FROM_NOTIFICATION_ACTION = "com.gilsexsoftware.GilMessage.OPEN_CHANNEL_FROM_NOTIFICATION"
        private const val FROM_NOTIFICATION_KEY = "from_notification"
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

    private fun fetchChannelById(channelId: String, callback: (Channel?) -> Unit) {
        println("Fetching channel with id: $channelId")
        val client = ChatClient.instance()

        val request = QueryChannelsRequest(
            Filters.eq("cid", channelId),
            offset = 0,
            limit = 1
        )

        client.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                val channels = result.data()

                if (channels.isNotEmpty()) {
                    val channel = channels[0]
                    println(channel)
                    callback(channel)

                } else {
                    println("No channel found for the given ID: $channelId")
                    callback(null)
                }
            } else {
                println("Error fetching user data: ${result.error()}")
                callback(null)
            }
        }
    }

}