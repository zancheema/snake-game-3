package com.example.android.socialnetwork.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificationFragment : Fragment(), NotificationListAdapter.FriendRequestListener {

    private lateinit var notificationList: RecyclerView

    private val firebaseUser = Firebase.auth.currentUser
    private val notificationCollection =
        Firebase.firestore.collection("users").document(firebaseUser.uid)
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
        val notificationListAdapter = NotificationListAdapter(this)
        notificationList.adapter = notificationListAdapter

        notificationCollection.get()
            .addOnSuccessListener { snap ->
                val notifications = mutableListOf<Notification>()
                for (doc in snap.documents) {
                    notifications.add(doc.toObject(Notification::class.java)!!)
                }
                notificationListAdapter.submitList(notifications)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading notification: $e", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onAcceptFriendRequest(notification: Notification) {
        TODO("Not yet implemented")
    }

    override fun onDeclineFriendRequest(notification: Notification) {
        TODO("Not yet implemented")
    }
}