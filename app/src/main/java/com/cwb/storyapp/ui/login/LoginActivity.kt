package com.cwb.storyapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cwb.storyapp.ui.main.MainActivity
import com.cwb.storyapp.ui.register.RegisterActivity
import com.cwb.storyapp.data.SessionManager
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.api.LoginResponse
import com.cwb.storyapp.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
        }

        animateViews()

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()
            if (email.isNotEmpty() && password.length >= 8) {
                login(email, password)
            } else {
                Toast.makeText(this, "Email or password is invalid", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Set minimum length validation for password field
        binding.edLoginPassword.setMinLength(8)
    }

    private fun animateViews() {
        binding.edLoginEmail.alpha = 0f
        binding.edLoginPassword.alpha = 0f
        binding.btnLogin.alpha = 0f
        binding.btnRegister.alpha = 0f

        binding.edLoginEmail.animate().alpha(1f).setDuration(1000).start()
        binding.edLoginPassword.animate().alpha(1f).setDuration(1000).setStartDelay(200).start()
        binding.btnLogin.animate().alpha(1f).setDuration(1000).setStartDelay(400).start()
        binding.btnRegister.animate().alpha(1f).setDuration(1000).setStartDelay(600).start()
    }

    private fun login(email: String, password: String) {
        val client = ApiConfig.getApiService("").login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()
                    if (loginResponse?.error == false) {
                        loginResponse.loginResult.let {
                            sessionManager.saveAuthToken(it.token)
                            navigateToMain()
                        }
                    } else {
                        Log.e("LoginActivity", "Login failed: ${response.body()?.message}")
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Login failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@LoginActivity, "Login failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error: ${t.message}")
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
