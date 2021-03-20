package com.example.android.socialnetwork.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Chat
import java.text.SimpleDateFormat
import java.util.*

class ChatsListAdapter(
    private val onClick: (Chat) -> Unit = {}
) : ListAdapter<Chat, ChatsListAdapter.ViewHolder>(ChatDiffUtil()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chat: Chat, onClick: (Chat) -> Unit) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(chat.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvUsername).text = chat.username

                if (chat.timestamp != 0L) {
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val time = timeFormat.format(Date(chat.timestamp))
                    findViewById<TextView>(R.id.tvTime).text = time
                }

                setOnClickListener { onClick(chat) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.chats_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class ChatDiffUtil : DiffUtil.ItemCallback<Chat>() {
    override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem == newItem
    }
}