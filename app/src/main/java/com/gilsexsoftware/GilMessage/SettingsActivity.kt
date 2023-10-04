package com.gilsexsoftware.GilMessage

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.gilsexsoftware.GilMessage.databinding.ActivitySettingsBinding
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.models.User

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    companion object {
        const val EDIT_PROFILE_REQUEST = 123 // You can use any unique integer value
        private const val LOGIN_REQUEST = 124
    }
    var user = User(

        id = "",
        image = ""

    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        val settingsListView = findViewById<ListView>(R.id.settingsListView)
        val settingsOptions = arrayOf("Perfil", "Personalización")
        val userId = intent.getStringExtra("passeduserId").toString()
        settingsListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, settingsOptions)

        settingsListView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    // Profile option selected, navigate to EditProfileActivity
                    val intent2 = Intent(this, EditProfileActivity::class.java)
                    intent2.putExtra("passeduserId", userId)
                    startActivityForResult(intent2, EDIT_PROFILE_REQUEST)
                    true
                }
                // Add more cases for other settings and corresponding activities
                1 -> { Snackbar.make(settingsListView, "Pronto :)", Snackbar.LENGTH_SHORT).show()
                    true // Return true to indicate the event is handled }
            }
        }
    }

}
}