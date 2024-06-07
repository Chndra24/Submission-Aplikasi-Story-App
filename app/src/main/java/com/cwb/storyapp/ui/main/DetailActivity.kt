package com.cwb.storyapp.ui.main

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.cwb.storyapp.R
import com.cwb.storyapp.data.SessionManager
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.api.DetailStoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        window.sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.move)

        sessionManager = SessionManager(this)
        val token = sessionManager.fetchAuthToken()
        val storyId = intent.getStringExtra("storyId")
        if (token != null && storyId != null) {
            fetchStoryDetail(token, storyId)
        } else {
            Log.e("DetailActivity", "Token or Story ID is null")
        }
    }

    private fun fetchStoryDetail(token: String, storyId: String) {
        val client = ApiConfig.getApiService(token).getStoryDetail(storyId)
        client.enqueue(object : Callback<DetailStoryResponse> {
            override fun onResponse(call: Call<DetailStoryResponse>, response: Response<DetailStoryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val story = response.body()?.story
                    story?.let {
                        findViewById<TextView>(R.id.tv_detail_name).text = it.name
                        findViewById<TextView>(R.id.tv_detail_description).text = it.description
                        val imageView = findViewById<ImageView>(R.id.iv_detail_photo)
                        ViewCompat.setTransitionName(imageView, "sharedElement")
                        Glide.with(this@DetailActivity)
                            .load(it.photoUrl)
                            .into(imageView)
                    }
                } else {
                    Log.e("DetailActivity", "Failed to fetch story detail: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                Log.e("DetailActivity", "Error: ${t.message}")
            }
        })
    }
}
