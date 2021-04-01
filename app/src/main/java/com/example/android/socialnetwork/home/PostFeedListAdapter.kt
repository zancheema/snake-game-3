package com.example.android.socialnetwork.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import com.example.android.socialnetwork.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class PostFeedListAdapter(
    private val onClickUsername: (String) -> Unit = {},
) : ListAdapter<Post, PostFeedListAdapter.ViewHolder>(PostDiffUtil()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(post: Post, onClickUserName: (String) -> Unit) {
            Firebase.firestore.collection("users").document(post.userUid)
                .get()
                .addOnSuccessListener { snap ->
                    val user = snap.toObject(User::class.java)!!
                    itemView.apply {
                        findViewById<TextView>(R.id.tvUsername).apply {
                            text = user.username.replace("\\s".toRegex(), "").toLowerCase()
                            setOnClickListener {
                                onClickUserName(post.userUid)
                            }
                        }
                        Glide
                            .with(context)
                            .load(user.photoUrl)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .error(R.drawable.ic_baseline_person_24)
                            .into(findViewById(R.id.imagePostUser))
                    }
                }

            itemView.apply {
                findViewById<TextView>(R.id.tvPostTitle).text = post.title
                findViewById<TextView>(R.id.tvPostDescription).text = post.description

                val format = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                val dateTime = format.format(Date(post.timeStamp))
                findViewById<TextView>(R.id.tvPostTime).text = dateTime

                val videoViewPost = findViewById<VideoView>(R.id.videoViewPost)
                videoViewPost.setVideoPath(post.videoUrl)
                val viewPlayVideo = findViewById<View>(R.id.viewPlayVideo)
                videoViewPost.setOnCompletionListener {
                    viewPlayVideo.visibility = View.VISIBLE
                }
                viewPlayVideo.setOnClickListener {
                    videoViewPost.start()
                    it.visibility = View.GONE
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