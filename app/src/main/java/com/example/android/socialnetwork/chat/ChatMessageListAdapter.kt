package com.example.android.socialnetwork.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.ChatMessage

private const val TAG = "ChatMessageListAdapter"

class ChatMessageListAdapter :
    ListAdapter<ChatMessage, ChatMessageListAdapter.ViewHolder>(ChatMessageDiffUtil()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatMessage: ChatMessage) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(chatMessage.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvChatMessage).text = chatMessage.message
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = when (viewType) {
            0 -> inflater.inflate(R.layout.chat_message_mine, parent, false)
            else -> inflater.inflate(R.layout.chat_message_other, parent, false)
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = getItem(position)
        Log.d(TAG, "onBindViewHolder: $chatMessage")
        holder.bind(chatMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).mine) 0 else 1
    }
}

class ChatMessageDiffUtil : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}