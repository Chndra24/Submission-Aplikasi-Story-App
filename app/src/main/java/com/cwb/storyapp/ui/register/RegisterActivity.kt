package com.cwb.storyapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cwb.storyapp.data.SessionManager
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.api.RegisterResponse
import com.cwb.storyapp.databinding.ActivityRegisterBinding
import com.cwb.storyapp.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        animateViews()

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()
            if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 8) {
                register(name, email, password)
            } else {
                Toast.makeText(this, "All fields are required and password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            }
        }

        binding.edRegisterPassword.setMinLength(8)
    }

    private fun animateViews() {
        binding.edRegisterName.alpha = 0f
        binding.edRegisterEmail.alpha = 0f
        binding.edRegisterPassword.alpha = 0f
        binding.btnRegister.alpha = 0f

        binding.edRegisterName.animate().alpha(1f).setDuration(1000).start()
        binding.edRegisterEmail.animate().alpha(1f).setDuration(1000).setStartDelay(200).start()
        binding.edRegisterPassword.animate().alpha(1f).setDuration(1000).setStartDelay(400).start()
        binding.btnRegister.animate().alpha(1f).setDuration(1000).setStartDelay(600).start()
    }

    private fun register(name: String, email: String, password: String) {
        val client = ApiConfig.getApiService("").register(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@RegisterActivity, "Register successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Log.e("RegisterActivity", "Register failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@RegisterActivity, "Register failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("RegisterActivity", "Error: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

