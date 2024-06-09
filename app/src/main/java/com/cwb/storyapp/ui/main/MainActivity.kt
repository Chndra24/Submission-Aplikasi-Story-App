package com.cwb.storyapp.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwb.storyapp.R
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.data.SessionManager
import com.cwb.storyapp.ui.addStory.AddStoryActivity
import com.cwb.storyapp.ui.login.LoginActivity
import com.cwb.storyapp.ui.main.paging.StoryPagingAdapter
import com.cwb.storyapp.ui.main.paging.StoryPagingSource
import com.cwb.storyapp.ui.maps.MapsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var storyPagingAdapter: StoryPagingAdapter
    private lateinit var recyclerView: RecyclerView
    private var isPagingInitialized = false

    private val addStoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            storyPagingAdapter.refresh()
        }
    }

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
            addStoryLauncher.launch(intent)
        }

        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            initPaging(token)
        } else {
            navigateToLogin()
        }
    }

    private fun initPaging(token: String) {
        if (isPagingInitialized) return
        isPagingInitialized = true

        storyPagingAdapter = StoryPagingAdapter { story, imageView ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("storyId", story.id)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                ViewCompat.getTransitionName(imageView) ?: "sharedElement"
            )
            startActivity(intent, options.toBundle())
        }
        recyclerView.adapter = storyPagingAdapter

        lifecycleScope.launch {
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { StoryPagingSource(ApiConfig.getApiService(token)) }
            ).flow.cachedIn(lifecycleScope).collectLatest { pagingData ->
                storyPagingAdapter.submitData(pagingData)
            }
        }
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
            R.id.action_map -> {
                val token = sessionManager.fetchAuthToken()
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("TOKEN", token)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}



