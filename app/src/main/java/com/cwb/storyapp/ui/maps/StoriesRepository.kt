package com.cwb.storyapp.ui.maps

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cwb.storyapp.api.ApiService
import com.cwb.storyapp.api.Story
import com.cwb.storyapp.ui.main.paging.StoryPagingSource
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val apiService: ApiService) {
    suspend fun getStoriesWithLocation(): List<Story> {
        return try {
            val response = apiService.getStoriesWithLocation()
            if (!response.error) {
                response.listStory
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStories(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).flow
    }
}