package com.cwb.storyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cwb.storyapp.api.Story
import com.cwb.storyapp.ui.maps.StoryRepository
import kotlinx.coroutines.flow.Flow

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories(): Flow<PagingData<Story>> {
        return repository.getStories().cachedIn(viewModelScope)
    }
}