package com.gilsexsoftware.GilMessage

//import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.gilsexsoftware.GilMessage.databinding.ActivityMainBinding
import com.gilsexsoftware.GilMessage.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatClient: ChatClient // Declare chatClient
    private lateinit var sharedPreferences: SharedPreferences
    val apiKey = "cdrk83rwm524"
//    private lateinit var user: User

    companion object {
        private const val EDIT_PROFILE_REQUEST = 123 // You can use any unique integer value
        private const val LOGIN_REQUEST = 124
    }
    var user = User(

        id = "",
        image = ""

    )
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("logout", false)) {
            startLoginActivity()
            return
        }
        if (!isUserAuthenticated()) {
            startLoginActivity()
            println("no auth, user is ${user.id}")
        } else {
            val username = intent.getStringExtra("username").toString()

            user.id = username



//            updateUserName(username)
            println("user is $username")
            setupChat()
        }


    }

    private fun setupChat(){
        // Get the username passed from LoginActivity

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "sera agregado despues, pendejo", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // Step 0 - inflate binding
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        // Step 1 - Set up the OfflinePlugin for offline storage
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
        val client = ChatClient.Builder("$apiKey", applicationContext)
            .withPlugin(offlinePluginFactory)
            .notifications(notificationConfig)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        Toast.makeText(getApplicationContext(), "Connecting to server 3...", Toast.LENGTH_SHORT)


        val token = client.devToken(user.id)
        client.connectUser(
            user = user,
            token = token
        ).enqueue{ result ->
            if (result.isSuccess)
                Toast.makeText(getApplicationContext(), "Login exitoso, bienvenido "+user.id+"!", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(getApplicationContext(), "Login error, pinche idiota", Toast.LENGTH_SHORT).show()
                startLoginActivity()
            }
            // Step 4 - Set the channel list filter and order
            // This can be read as requiring only channels whose "type" is "messaging" AND
            // whose "members" include our "user.id"
            val filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(user.id))
            )

            addUserToChannel("mierda_b7694213-3f39-4ee8-9dd8-0049bf2e0f9e", user.id,
                onSuccess = {
                    println("User added to the channel successfully")
                },
                onError = { errorMessage ->
                    println("Error: $errorMessage")
                }
            )
            val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
            val viewModel: ChannelListViewModel by viewModels { viewModelFactory }
            // Step 5 - Connect the ChannelListViewModel to the ChannelListView, loose
            //          coupling makes it easy to customize
            viewModel.bindView(binding.channelListView, this)
            binding.channelListView.setChannelItemClickListener { channel ->
                startActivity(ChannelActivity.newIntent(this, channel))
            }

    }}
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> {
                val intent2 = Intent(this, EditProfileActivity::class.java)
                intent2.putExtra("passeduserId", user.id)
                startActivityForResult(intent2, EDIT_PROFILE_REQUEST)
                true
            }
            R.id.action_news ->{
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Novedades de Version ${getString(R.string.version)}")
                alertDialogBuilder.setMessage("${getString(R.string.news)}")  // You can replace this with the actual version
                alertDialogBuilder.create()
                alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                alertDialogBuilder.show()
                true
            }
            R.id.action_logout -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("${getString(R.string.action_log_out)}")
                alertDialogBuilder.setMessage("${getString(R.string.logout_confirmation)}")  // You can replace this with the actual version
                alertDialogBuilder.create()
                alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                    logOut()
                    dialog.dismiss()
                }
                alertDialogBuilder.setNegativeButton("Cancelar") {dialog, _ -> dialog.dismiss()}
                alertDialogBuilder.show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // User has successfully logged in
                // Retrieve the username from the intent
                val username = data?.getStringExtra("username")

                // Handle the case where the username is not provided
                if (!username.isNullOrEmpty()) {
                    user.id = username
//                    updateUserName(username)
                    setupChat()
                } else {
                    Toast.makeText(this, "Username not provided", Toast.LENGTH_SHORT).show()
                    finish() // Close the app if username is not provided (or implement your logic)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Handle login cancellation if needed
                Toast.makeText(this, "Login canceled", Toast.LENGTH_SHORT).show()
                finish() // Close the app if login is canceled (or implement your logic)
            }
        } else if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            val updatedImage = data?.getStringExtra("updatedImage")
            if (!updatedImage.isNullOrEmpty()) {
                // Update the user's profile image
                user.image = updatedImage
                val imageView: ImageView = findViewById(R.id.imageView3)
                Glide.with(applicationContext).load(updatedImage).into(imageView)
            }
        }
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
//            val updatedImage = data?.getStringExtra("updatedImage")
//            if (!updatedImage.isNullOrEmpty()) {
//                // Update the user's profile image
//                user.image = updatedImage
//                val imageView: ImageView = findViewById(R.id.imageView3)
//                Glide.with(applicationContext).load(updatedImage).into(imageView)
//            }
//        }
//    }

    private fun isUserAuthenticated(): Boolean {
        // Implement your authentication logic here, return true if authenticated
        // For simplicity, assuming the user is authenticated if the user ID is not empty
        return !user.id.isNullOrEmpty()
    }
    private fun logOut() {
        // Clear the saved username
        sharedPreferences.edit().remove("username").apply()

        // Navigate back to LoginActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("logout", true)
        startActivity(intent)
        finish() // Close MainActivity
    }
    private fun startLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.putExtra("username", user.id)
        startActivityForResult(loginIntent, LOGIN_REQUEST)
    }

    private fun updateUserName(newName: String) {
        // Ensure the user is authenticated and you have a valid chatClient instance

        // Construct the updated user object with the new name
        val updatedUser = User(id = user.id, extraData = mutableMapOf("name" to newName))

        // Call the updateUser function
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Use the chatClient to update the user
                val updatedUserResponse = chatClient.updateUser(updatedUser).execute()
                if (updatedUserResponse.isSuccess) {
                    // Successfully updated user
                    val updatedUser = updatedUserResponse.data()
                    println("User name updated successfully to: ${updatedUser.name}")
                } else {
                    // Handle update failure
                    println("Error updating user name: ${updatedUserResponse.error().message}")
                }
            } catch (e: Exception) {
                // Handle any exceptions
                println("Error updating user name: ${e.message}")
            }
        }
    }
    private fun fetchUserName(userId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val client = ChatClient.instance()
        client.fetchCurrentUser().enqueue { result ->
            if (result.isSuccess) {
                val user: User? = result.data()
                val userName = user?.name ?: "Unknown" // Extract user's name (or use a default value)

                // Call the success callback with the user's name
                onSuccess(userName)
            } else {
                // Call the error callback with the error message
                result.error().message?.let { onError(it) }
            }
        }
    }
    fun addUserToChannel(channelId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Assuming you have a ChatClient instance
        val chatClient = ChatClient.instance()

        // Get the specified channel
        val channel = chatClient.channel("messaging", channelId)

        // Add the user to the channel
        channel.addMembers(listOf(userId))
            .enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                    onSuccess()
                } else {
                    // Handle error
                    onError("Failed to add user to the channel: ${result.error().message}")
                }
            }
    }
}

