package com.gilsexsoftware.GilMessage

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gilsexsoftware.GilMessage.databinding.ActivityPersonalizeActivityBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class PersonalizeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalizeActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalizeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun setLayoutColor(context: Context, envelope: ColorEnvelope) {
        val selectedColor = envelope.color

        val preferences = getSharedPreferences("ChatPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("background_color", selectedColor)
        editor.apply()
    }

    fun setBubbleColor(context: Context, envelope: ColorEnvelope) {
        val selectedColor = envelope.color

        val preferences = getSharedPreferences("ChatPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("bubble_color", selectedColor)
        editor.apply()
    }
    fun openChatColorPicker(view: View) {
        val context = this
//        val preferences: SharedPreferences = context.getSharedPreferences("chatColor", MODE_PRIVATE)
//        val defaultColor = resources.getColor(com.gilsexsoftware.GilMessage.R.color.default_chat_color)
//        val initialColor = preferences.getInt("background_color", defaultColor)

        ColorPickerDialog.Builder(this)
            .setTitle("Cambiar Color del chat")
            .setPreferenceName("ChatColorDialog")
            .setPositiveButton(getString(R.string.ok),
                ColorEnvelopeListener { envelope, fromUser -> setLayoutColor(context, envelope) })
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)

            .show()
    }

    fun openMessageColorPicker(view: View) {
        val context = this
//        val preferences: SharedPreferences = context.getSharedPreferences("chatColor", MODE_PRIVATE)
//        val defaultColor = resources.getColor(com.gilsexsoftware.GilMessage.R.color.default_chat_color)
//        val initialColor = preferences.getInt("background_color", defaultColor)

        ColorPickerDialog.Builder(this)
            .setTitle("Cambiar Color de mensajes")
            .setPreferenceName("ChatColorDialog")
            .setPositiveButton(getString(R.string.ok),
                ColorEnvelopeListener { envelope, fromUser -> setBubbleColor(context, envelope) })
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)

            .show()
    }

    // Click event for the "Select Message Color" button


    private fun getColorPresets(): IntArray {
        // Define your preset colors here
        return intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
    }

}