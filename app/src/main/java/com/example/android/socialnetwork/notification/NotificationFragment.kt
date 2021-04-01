package com.example.android.socialnetwork.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Auth
import com.example.android.socialnetwork.model.Chat
import com.example.android.socialnetwork.model.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class NotificationFragment : Fragment(), NotificationListAdapter.FriendRequestListener {

    private lateinit var notificationList: RecyclerView
    private lateinit var buttonLogout: ImageButton

    private val firebaseUser = Firebase.auth.currentUser!!
    private val usersCollection = Firebase.firestore.collection("users")
    private val notificationCollection =
        usersCollection.document(firebaseUser.uid)
            .collection("notifications")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationList = view.findViewById(R.id.notificationList)
        buttonLogout = view.findViewById(R.id.buttonLogout)

        val notificationListAdapter = NotificationListAdapter(this)
        notificationList.adapter = notificationListAdapter
        refreshNotifications(notificationListAdapter)

        buttonLogout.setOnClickListener {
            Auth.logoutAndNavigateToLogin(requireActivity(), findNavController())
        }
    }

    override fun onAcceptFriendRequest(notification: Notification) {
        val newNotification = Notification(
            "friendRequestAccepted",
            UUID.randomUUID().toString(),
            firebaseUser.displayName,
            "${firebaseUser.displayName} accepted your friend request",
            firebaseUser.photoUrl?.toString() ?: "",
            notification.receiverUid,
            notification.receiverToken,
            notification.senderUid,
            notification.senderToken
        )
        usersCollection.document(notification.senderUid).collection("notifications")
            .document(newNotification.receiverUid)
            .set(newNotification)
            .addOnSuccessListener {
                val chat = Chat(
                    newNotification.title,
                    newNotification.senderUid,
                    newNotification.senderToken,
                    newNotification.photoUrl
                )
                usersCollection.document(newNotification.receiverUid)
                    .collection("chats")
                    .document(chat.userUid)
                    .set(chat)
                    .addOnSuccessListener {
                        val chat = Chat(
                            notification.title,
                            notification.senderUid,
                            notification.senderToken,
                            notification.photoUrl
                        )
                        usersCollection.document(notification.receiverUid)
                            .collection("chats")
                            .document(chat.userUid)
                            .set(chat)
                            .addOnSuccessListener {
                                deleteNotification(notification.notificationId)
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Task failed: $it", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    override fun onDeclineFriendRequest(notification: Notification) {
        deleteNotification(notification.notificationId)
    }

    private fun deleteNotification(notificationId: String) {
        notificationCollection.document(notificationId).delete()
        refreshNotifications(notificationList.adapter as NotificationListAdapter)
    }

    private fun refreshNotifications(notificationListAdapter: NotificationListAdapter) {
        notificationCollection.get()
            .addOnSuccessListener { snap ->
                val notifications = mutableListOf<Notification>()
                for (doc in snap.documents) {
                    notifications.add(doc.toObject(Notification::class.java)!!)
                }
                notifications.sortByDescending { it.timestamp }
                notificationListAdapter.submitList(notifications)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading notification: $e", Toast.LENGTH_SHORT).show()
            }
    }
}