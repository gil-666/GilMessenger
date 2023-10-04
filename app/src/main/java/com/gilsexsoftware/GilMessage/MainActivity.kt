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
import com.gilsexsoftware.GilMessage.MyFirebaseMessagingService.Companion.OPEN_CHANNEL_FROM_NOTIFICATION_ACTION
import com.gilsexsoftware.GilMessage.databinding.ActivityMainBinding
import com.gilsexsoftware.GilMessage.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory


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
        private const val CID_KEY = "key:cid"
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
        val channelId = intent.getStringExtra("channelId")

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

        Toast.makeText(getApplicationContext(), "Connecting to server 3...", Toast.LENGTH_SHORT)

        val client = ChatClient.instance()
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
                startActivity(MyFirebaseMessagingService.newIntent(this, channel))
            }

            fetchUserName(user.id) { userDisplayName ->
                if (userDisplayName.isNullOrEmpty()) {
                    println("obtained user display name is: $userDisplayName")
                    NewUserRoutine(user.id)

                }
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
                val intent2 = Intent(this, SettingsActivity::class.java)
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

    fun NewUserRoutine (userId: String){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Hola!")
        builder.setMessage("Parece que eres un usuario nuevo, por favor, configura tu perfil")
        builder.setPositiveButton("Continuar"){dialog, _ ->
            val intent2 = Intent(this, EditProfileActivity::class.java)
            intent2.putExtra("passeduserId", userId)
            startActivityForResult(intent2, SettingsActivity.EDIT_PROFILE_REQUEST)
            dialog.dismiss()
        }
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun fetchUserName(userID: String, onComplete: (String?) -> Unit) {
        println("Fetching user display name for userID: $userID")
        val client = ChatClient.instance()

        val request = QueryUsersRequest(
            filter = Filters.eq("id", userID),
            offset = 0,
            limit = 1
        )

        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) {
                val users = result.data()

                if (users.isNotEmpty()) {
                    val user = users.first()
                    val displayName = user.name

                    println("User display name is: $displayName")
                    onComplete(displayName)
                } else {
                    println("No user found for the given ID: $userID")
                    onComplete(null)
                }
            } else {
                println("Error fetching user data: ${result.error()}")
                onComplete(null)
            }
        }
    }
    private fun navigateToChannel(channelId: String?) {
        if (channelId.isNullOrEmpty()) {
            println("channelID is null or incorrect! $channelId")
            return
        }

        // Assuming you have a function to fetch the Channel object based on channelId
        fetchChannelById(channelId) { channel ->
            if (channel != null) {
                val intent = MyFirebaseMessagingService.newIntent(this, channel)
                startActivity(intent)
                finish()
            } else {
                // Handle the case where the channel is not found
                Toast.makeText(applicationContext, "Channel not found", Toast.LENGTH_SHORT).show()
            }
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

