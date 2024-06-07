package com.cwb.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwb.storyapp.R
import com.cwb.storyapp.data.SessionManager
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.api.GetAllStoriesResponse
import com.cwb.storyapp.ui.adapter.StoryAdapter
import com.cwb.storyapp.ui.addStory.AddStoryActivity
import com.cwb.storyapp.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.button_add_story).setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            fetchStories(token)
        } else {
            navigateToLogin()
        }
    }

    private fun fetchStories(token: String) {
        val client = ApiConfig.getApiService(token).getAllStories()
        client.enqueue(object : Callback<GetAllStoriesResponse> {
            override fun onResponse(call: Call<GetAllStoriesResponse>, response: Response<GetAllStoriesResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stories = response.body()?.listStory ?: emptyList()
                    storyAdapter = StoryAdapter(stories) { story, imageView ->
                        val intent = Intent(this@MainActivity, DetailActivity::class.java)
                        intent.putExtra("storyId", story.id)

                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@MainActivity,
                            imageView,
                            ViewCompat.getTransitionName(imageView) ?: "sharedElement"
                        )
                        startActivity(intent, options.toBundle())
                    }
                    recyclerView.adapter = storyAdapter
                } else {
                    Log.e("MainActivity", "Failed to fetch stories: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetAllStoriesResponse>, t: Throwable) {
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            fetchStories(token)
        }
    }
}
