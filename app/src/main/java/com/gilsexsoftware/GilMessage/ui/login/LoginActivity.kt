package com.gilsexsoftware.GilMessage.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gilsexsoftware.GilMessage.R
import com.gilsexsoftware.GilMessage.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val videoView: VideoView = findViewById(R.id.videoView2)
        val purpleBackground = findViewById<View>(R.id.purpleBackround)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.splash
        val buttonVersion: Button = findViewById(R.id.button2)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            // Hide VideoView
            videoView.visibility = View.GONE

            // Show purple background
            purpleBackground.visibility = View.VISIBLE
            purpleBackground.setBackgroundColor(Color.parseColor("#4527A0")) // Solid purple color
        } else {
            videoView.run {
                setVideoURI(Uri.parse(videoPath))
                start()
            }
        }
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", "")
        binding.username.setText(savedUsername)

        if (savedUsername != null) {
            if(savedUsername.isNotEmpty()){
                loading.visibility = View.VISIBLE

                // Initialize loginViewModel
                loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                    .get(LoginViewModel::class.java)

                // Observe loginViewModel loginResult
                loginViewModel.loginResult.observe(this@LoginActivity, Observer {
                    val loginResult = it ?: return@Observer

                    loading.visibility = View.GONE
                    if (loginResult.error != null) {
                        showLoginFailed(loginResult.error)
                    }
                    if (loginResult.success != null) {
                        updateUiWithUser(loginResult.success)
                    }
                    setResult(Activity.RESULT_OK)

                    //Complete and destroy login activity once successful
                    finish()
                })

                // Call login
                loginViewModel.login(savedUsername.toString(), "")
            }
        }

        buttonVersion.setOnClickListener {
            // Create and configure the alert dialog
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Gracias <3")
            alertDialogBuilder.setMessage("gracias por probar mi aplicacion, \ncon solo usarla ya me ayudas mucho a continuar el desarollo\nte debo una torta\n\n-gil \n\nVersion: ${getString(R.string.version)} \nFecha de compilación: ${getString(R.string.compilationDate)}")  // You can replace this with the actual version
            alertDialogBuilder.create()
            alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            alertDialogBuilder.show()
        }



//        videoView.setOnCompletionListener { mediaPlayer ->
//            // Rewind the video to the beginning
//            mediaPlayer.seekTo(0)
//            // Start playing again
//            videoView.start()
//        }
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
//            if (loginState.passwordError != null) {
//                password.error = getString(loginState.passwordError)
//            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
//        val displayName = model.displayName
        val username = binding.username.text.toString()
        sharedPreferences.edit().putString("username", username).apply()
        println("user is $username")

        val intent = Intent()
        intent.putExtra("username", username)
        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}


