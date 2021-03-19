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
import java.text.SimpleDateFormat
import java.util.*

class NotificationListAdapter(private val friendRequestListener: FriendRequestListener) :
    ListAdapter<Notification, NotificationListAdapter.ViewHolder>(NotificationDiffUtil()) {

    interface FriendRequestListener {
        fun onAcceptFriendRequest(notification: Notification)
        fun onDeclineFriendRequest(notification: Notification)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(notification: Notification, friendRequestListener: FriendRequestListener) {
            itemView.apply {
                Glide
                    .with(itemView.context)
                    .load(notification.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(findViewById(R.id.ivProfilePic))

                findViewById<TextView>(R.id.tvUsername).text = notification.username

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.notification_friend_request_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), friendRequestListener)
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