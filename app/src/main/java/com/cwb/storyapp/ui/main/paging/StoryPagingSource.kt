package com.cwb.storyapp.ui.main.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cwb.storyapp.api.ApiService
import com.cwb.storyapp.api.Story
import retrofit2.HttpException
import java.io.IOException

class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        val position = params.key ?: 1
        return try {
            val response = apiService.getAllStoriesWithPaging(position, params.loadSize)
            val stories = response.listStory
            LoadResult.Page(
                data = stories,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}


