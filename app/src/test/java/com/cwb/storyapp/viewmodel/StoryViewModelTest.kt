package com.cwb.storyapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.cwb.storyapp.api.Story
import com.cwb.storyapp.ui.maps.StoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class StoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: StoryViewModel
    private val repository: StoryRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testScope.cancel()
    }

    @Test
    fun `when fetching stories, ensure data is not null`() = testScope.runTest {
        val storyList = listOf(
            Story("1", "Story 1", "Description 1", "photoUrl", "createdAt", 0.0, 0.0),
            Story("2", "Story 2", "Description 2", "photoUrl", "createdAt", 0.0, 0.0)
        )

        coEvery { repository.getStories() } returns flowOf(PagingData.from(storyList))

        val differ = AsyncPagingDataDiffer(
            diffCallback = STORY_COMPARATOR,
            updateCallback = NOOP_LIST_UPDATE_CALLBACK,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val job = launch {
            viewModel.getStories().collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        job.cancelAndJoin()

        assertNotNull(differ.snapshot())
        assertEquals(storyList.size, differ.snapshot().size)
        assertEquals(storyList[0], differ.snapshot()[0])
    }

    @Test
    fun `when no stories available, ensure data size is zero`() = testScope.runTest {
        val storyList = emptyList<Story>()

        coEvery { repository.getStories() } returns flowOf(PagingData.from(storyList))

        val differ = AsyncPagingDataDiffer(
            diffCallback = STORY_COMPARATOR,
            updateCallback = NOOP_LIST_UPDATE_CALLBACK,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val job = launch {
            viewModel.getStories().collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        job.cancelAndJoin()

        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    companion object {
        private val STORY_COMPARATOR = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean =
                oldItem == newItem
        }

        private val NOOP_LIST_UPDATE_CALLBACK = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }
}