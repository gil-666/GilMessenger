package com.gilsexsoftware.GilMessage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gilsexsoftware.GilMessage.databinding.ActivityEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.ChatClient
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
        println("user is $userId")
        user = User(
            id = userId,
            image = ""

        )

        // Initialize the binding
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userLabel: TextView = findViewById(R.id.textView5)
        userLabel.text = "Your username is: $userId"
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
        fetchUserImage(user) { imageUrl ->
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
        // Set the image using Glide
//        val imageView: ImageView = findViewById(R.id.imageView3)
//        Glide.with(applicationContext).load(MainActivity().user.image).into(imageView)
    }
    fun fetchCurrentUser(): User {
        // Replace this with your actual logic to retrieve the current user
        val userId = intent.getStringExtra("passeduserId").toString()

        // Assuming you have a User class, create and return a User object
        return User(
            id = userId,
            name = "", // You can set the name based on your logic
            image = "" // You can set the image based on your logic
        )
    }
    private fun updateProfileImage(newImage: String) {
        // Update the user's image URL
        user.image = newImage

        // Get the ChatClient instance
        val chatClient = ChatClient.instance()

        // Call updateUser to update the user
        val updatedUser = User(id = user.id, name = user.id, image = newImage)
        chatClient.updateUser(updatedUser).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
                println("User image updated successfully")
                currentFocus?.let { Snackbar.make(it, "Profile picture updated!", Snackbar.LENGTH_LONG) }
            } else {
                // Handle error
                println("Failed to update user image: ${result.error().message}")
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
    private fun fetchUserImage(user: User, onComplete: (String?) -> Unit) {
        val client = ChatClient.instance()

        // Fetch the current user data
        client.fetchCurrentUser().enqueue { result ->
            if (result.isSuccess) {
                val currentUser = result.data()

                // Get the image URL
                val imageUrl = currentUser.image
                println("the sex is: $imageUrl")

                // Call the onComplete callback with the image URL
                onComplete(imageUrl)
            } else {
                // Handle error
                onComplete(null)
                println("the sex has not happened")
                println("current user: ${fetchCurrentUser()}")
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