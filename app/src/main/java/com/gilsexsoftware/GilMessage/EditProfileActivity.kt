package com.gilsexsoftware.GilMessage

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gilsexsoftware.GilMessage.databinding.ActivityEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class EditProfileActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private val IMGUR_CLIENT_ID = "96c7899f98191b5"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var user: User
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { it ->
            it?.let { galleryUri ->
                handleSelectedImage(galleryUri)
            }
        }
    private var imageLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("passeduserId").toString()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        println("user is $userId")
        user = User(
            id = userId,
            image = ""

        )

        // Initialize the binding
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userLabel: TextView = findViewById(R.id.profileTextView)
        val idLabel: TextView = findViewById(R.id.profileIDview)
        idLabel.text = "@$userId"
        fetchUserName(userId){userDisplayName ->
            if (userDisplayName != null){
                runOnUiThread{
                    userLabel.text = userDisplayName
                }
            }else{
                println("Could not obtain updated user display name")
            }
        }
        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.profileimgbutton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        fetchUserImage(userId) { imageUrl ->
            if (imageUrl != null) {
                // Image URL fetched successfully, load it into ImageView
                runOnUiThread {
                    val imageView: ImageView = findViewById(R.id.imageView3)
                    Glide.with(this).load(imageUrl).into(imageView)
                }
            } else {
                // Handle the case where image URL couldn't be fetched
                println("Failed to fetch profile image")
            }
        }
        userLabel.setOnClickListener {
            // Create an AlertDialog to prompt the user for a string
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Nombre de pantalla")
            builder.setMessage("Este es tu nombre que aparece en el chat, es diferente a tu nombre de usuario (el que usas para iniciar sesión)\n\nIngresa un nuevo nombre de pantalla:")

            // Set up the input
            val input = EditText(this)
            builder.setView(input)

            // Set up the OK button
            builder.setPositiveButton("OK") { dialog, _ ->
                val userName = input.text.toString()
                // Call the updateUserID function with the provided userId

                updateProfileName(userName)
                userLabel.text =userName
                dialog.dismiss()
            }

            // Set up the Cancel button
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }
        idLabel.setOnClickListener {
            // Create an AlertDialog to prompt the user for a string
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Nombre de usuario")
            builder.setMessage("Este es tu ID unico para iniciar sesión, este valor no puede ser cambiado")
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // Set the image using Glide
//        val imageView: ImageView = findViewById(R.id.imageView3)
//        Glide.with(applicationContext).load(MainActivity().user.image).into(imageView)
    }
    fun fetchCurrentUser(userID: String) {
        // Replace this with your actual logic to retrieve the current user
        val client = ChatClient.instance()

        val request = QueryUsersRequest(
            filter = Filters.`in`("id", userID),
            offset = 0,
            limit = 3,
        )

        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) {
                val user = result.data()
                println("fetchcurrentuser result: $user")
            } else {
                println("could not fetch user data")
            }
        }

    }
    private fun updateProfileImage(newImage: String) {
        // Update the user's image URL
        user.image = newImage

        // Get the ChatClient instance
        val chatClient = ChatClient.instance()

        fetchUserName(user.id) { displayName ->
            if (displayName != null) {
                // Display name fetched successfully, update the user with the new image URL and display name
                val updatedUser = User(id = user.id, name = displayName, image = newImage)
                // Call updateUser to update the user
                chatClient.updateUser(updatedUser).enqueue { result ->
                    if (result.isSuccess) {
                        // Handle success
                        println("User image updated successfully")
                        val rootView: View = findViewById(android.R.id.content)
                        Snackbar.make(rootView, "Se actualizó la foto de perfil", Snackbar.LENGTH_LONG).show()
                    } else {
                        // Handle error
                        println("Failed to update user image: ${result.error().message}")
                    }
                }
            } else {
                // Handle the case where display name couldn't be fetched
                println("Failed to fetch display name")
            }
        }

    }

    private fun updateProfileName(newID: String) {
        // Update the user's image URL


        // Get the ChatClient instance
        val chatClient = ChatClient.instance()
        user.name = newID
        // Call updateUser to update the user
        fetchUserImage(user.id) { imageUrl ->
            if (imageUrl != null) {
                // Image URL fetched successfully, update the user with the new display name and image URL
                val updatedUser = User(id = user.id, name = newID, image = imageUrl)
                // Call updateUser to update the user
                chatClient.updateUser(updatedUser).enqueue { result ->
                    println("API Response: $result") // Print the API response
                    if (result.isSuccess) {
                        // Handle success
                        println("Display Name updated successfully")
                        val rootView: View = findViewById(android.R.id.content)
                        Snackbar.make(rootView, "Se actualizó el nombre de pantalla", Snackbar.LENGTH_LONG).show()

                    } else {
                        // Handle error
                        println("Failed to update display name: ${result.error().message}")
                    }
                }
            } else {
                // Handle the case where image URL couldn't be fetched
                println("Failed to fetch image URL")
            }
        }

    }



    private fun handleSelectedImage(imageUri: android.net.Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        if (inputStream != null) {
            val imageBitmap = BitmapFactory.decodeStream(inputStream)
            val imageByteArray = convertBitmapToByteArray(imageBitmap)
            uploadImageToImgur(imageByteArray)
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun writeByteArrayToFile(file: File, byteArray: ByteArray) {
        file.outputStream().use { it.write(byteArray) }
    }

    private fun showImgurResponseDialog(response: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Imgur Response")
            .setMessage(response)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        alertDialog.show()
    }

    private fun extractImageLink(responseString: String): String? {
        return try {
            val jsonResponse = JSONObject(responseString)
            val dataObject = jsonResponse.getJSONObject("data")
            dataObject.getString("link")
        } catch (e: JSONException) {
            null
        }
    }
    private fun fetchUserImage(userID: String, onComplete: (String?) -> Unit) {
        val client = ChatClient.instance()

        // Fetch the user data based on userID
        val request = QueryUsersRequest(
            filter = Filters.`in`("id", userID),
            offset = 0,
            limit = 1
        )

        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) {
                val currentUser = result.data().firstOrNull()

                // Check if a user was found and has an image
                val imageUrl = currentUser?.image
                println("User image URL: $imageUrl")

                // Call the onComplete callback with the image URL
                onComplete(imageUrl)
            } else {
                // Handle error
                onComplete(null)
                println("Error fetching user data: ${result.error()}")
            }
        }
    }

    private fun fetchUserName(userID: String, onComplete: (String?) -> Unit) {
        println("Fetching user display name for userID: $userID")
        val client = ChatClient.instance()

        // Fetch the user data based on userID
        val request = QueryUsersRequest(
            filter = Filters.`in`("id", userID),
            offset = 0,
            limit = 1
        )

        client.queryUsers(request).enqueue { result ->
            println("API Response: $result") // Print the API response
            if (result.isSuccess) {
                val currentUser = result.data().firstOrNull()

                // Check if a user was found and has an image
                val displayName = currentUser?.name
                println("User display name is: $displayName")

                // Call the onComplete callback with the image URL
                onComplete(displayName)
            } else {
                // Handle error
                onComplete(null)
                println("Error fetching user data: ${result.error()}")
            }
        }
    }

    private fun uploadImageToImgur(imageData: ByteArray) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://api.imgur.com/3/image")
            val connection = url.openConnection() as HttpURLConnection

            // Set the necessary HTTP request headers
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Client-ID $IMGUR_CLIENT_ID")
            connection.doOutput = true

            val archivo = File(cacheDir, "temp_image.jpg")
            val dataOutputStream = connection.outputStream
            writeByteArrayToFile(archivo, imageData)
            dataOutputStream.write(imageData)
            dataOutputStream.flush()
            dataOutputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseInputStream: InputStream = connection.inputStream
                val responseString = responseInputStream.bufferedReader().use { it.readText() }

                // Parse the response to get the image link
                // Extract the image URL from the response (imgurResponse)
                // ...
                imageLink = extractImageLink(responseString)

                runOnUiThread {
                    if (imageLink != null) {
                        // Update the user image in Stream Chat
                        user.image = imageLink.toString()
                        updateProfileImage(user.image)
                        val imageView: ImageView = findViewById(R.id.imageView3)
                        Glide.with(applicationContext).load(imageLink).into(imageView)
                        // Update the user image locally in MainActivity
                    }
                    // Rest of your code
                }
            }
        }
    }
}