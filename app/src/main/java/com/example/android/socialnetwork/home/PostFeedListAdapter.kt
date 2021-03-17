package com.example.android.socialnetwork.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import java.text.SimpleDateFormat
import java.util.*

class PostFeedListAdapter(
    private val onClickUsername: (String) -> Unit = {}
) : ListAdapter<Post, PostFeedListAdapter.ViewHolder>(PostDiffUtil()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(post: Post, onClickUserPhoto: (String) -> Unit) {
            itemView.apply {
                findViewById<TextView>(R.id.tvUsername).apply {
                    text = post.username.replace("\\s".toRegex(), "").toLowerCase()
                    setOnClickListener {
                        onClickUserPhoto(post.userUid)
                    }
                }
                findViewById<TextView>(R.id.tvPostTitle).text = post.title
                findViewById<TextView>(R.id.tvPostDescription).text = post.description
                Glide
                    .with(context)
                    .load(post.userPhotoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById<ImageView>(R.id.imagePostUser))

                val format = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                val dateTime = format.format(Date(post.timeStamp))
                findViewById<TextView>(R.id.tvPostTime).text = dateTime

                findViewById<VideoView>(R.id.videoViewPost).apply {
                    setVideoPath(post.videoUrl)
                    start()
                    setOnCompletionListener { // set video playback repeating
                        start()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.post_feed_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickUsername)
    }
}

class PostDiffUtil : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}