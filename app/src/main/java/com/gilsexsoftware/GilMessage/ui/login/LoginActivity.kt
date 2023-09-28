package com.gilsexsoftware.GilMessage.ui.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val videoView: VideoView = findViewById(R.id.videoView2)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.splash
        val buttonVersion: Button = findViewById(R.id.button2)

        buttonVersion.setOnClickListener {
            // Create and configure the alert dialog
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Gracias <3")
            alertDialogBuilder.setMessage("gracias por probar mi aplicacion, \ncon solo usarla ya me ayudas mucho a continuar el desarollo\nte debo una torta\n\n-gil \n\nVersion: ${getString(R.string.version)} \nFecha de compilación: 2023-9-28 3:10AM")  // You can replace this with the actual version
            alertDialogBuilder.create()
            alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            alertDialogBuilder.show()
        }
        videoView.run {
            setVideoURI(Uri.parse(videoPath))
            start()
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


