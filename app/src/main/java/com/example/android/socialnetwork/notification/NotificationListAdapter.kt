package com.example.android.socialnetwork.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Notification
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class NotificationListAdapter(private val friendRequestListener: FriendRequestListener) :
    ListAdapter<Notification, NotificationListAdapter.ViewHolder>(NotificationDiffUtil()) {

    interface FriendRequestListener {
        fun onAcceptFriendRequest(notification: Notification)
        fun onDeclineFriendRequest(notification: Notification)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class FriendRequestSentViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(notification: Notification, friendRequestListener: FriendRequestListener) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(notification.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvUsername).text = notification.title

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                val time = timeFormat.format(Date(notification.timestamp))
                findViewById<TextView>(R.id.tvTime).text = time

                findViewById<Button>(R.id.buttonAccept).setOnClickListener {
                    friendRequestListener.onAcceptFriendRequest(notification)
                }
                findViewById<Button>(R.id.buttonDecline).setOnClickListener {
                    friendRequestListener.onDeclineFriendRequest(notification)
                }
            }
        }
    }

    class FriendRequestAcceptedViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(notification: Notification) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(notification.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvMessage).text =
                    "${notification.title} accepted your friend request"

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                val time = timeFormat.format(Date(notification.timestamp))
                findViewById<TextView>(R.id.tvTime).text = time
            }
        }
    }

    class ChatMessageSentViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(notification: Notification) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(notification.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvUsername).text = notification.title

                findViewById<TextView>(R.id.tvMessage).text = notification.body

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                val time = timeFormat.format(Date(notification.timestamp))
                findViewById<TextView>(R.id.tvTime).text = time
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val itemView =
                    inflater.inflate(R.layout.notification_friend_request_sent_item, parent, false)
                FriendRequestSentViewHolder(itemView)
            }
            1 -> {
                val itemView = inflater.inflate(
                    R.layout.notification_friend_request_accepted_item,
                    parent,
                    false
                )
                FriendRequestAcceptedViewHolder(itemView)
            }
            2 -> {
                val itemView = inflater.inflate(
                    R.layout.notification_new_chat_message_item,
                    parent,
                    false
                )
                ChatMessageSentViewHolder(itemView)
            }
            else -> throw Exception("Invalid Notification View Holder")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val viewType = getItemViewType(position)
        if (viewType == 0) {
            (holder as FriendRequestSentViewHolder).bind(item, friendRequestListener)
        } else if (viewType == 1) {
            (holder as FriendRequestAcceptedViewHolder).bind(item)
        } else if (viewType == 2) {
            (holder as ChatMessageSentViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).notificationType) {
            "friendRequestSent" -> 0
            "friendRequestAccepted" -> 1
            "chatMessageSent" -> 2
            else -> -1
        }
    }
}

class NotificationDiffUtil : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}