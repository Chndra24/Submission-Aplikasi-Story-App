package com.cwb.storyapp.ui.main.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cwb.storyapp.R
import com.cwb.storyapp.api.Story

class StoryPagingAdapter(private val onItemClick: (Story, ImageView) -> Unit) :
    PagingDataAdapter<Story, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_item_name)
        val photo: ImageView = itemView.findViewById(R.id.iv_item_photo)

        fun bind(story: Story) {
            name.text = story.name
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(photo)

            itemView.setOnClickListener { onItemClick(story, photo) }
            ViewCompat.setTransitionName(photo, "sharedElement_${story.id}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        story?.let { holder.bind(it) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean =
                oldItem == newItem
        }
    }
}
