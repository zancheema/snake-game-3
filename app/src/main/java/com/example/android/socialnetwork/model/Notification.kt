package com.example.android.socialnetwork.model

data class Notification(
    val notificationType: String = "",
    val notificationId: String = "",
    val title: String = "",
    val body: String = "",
    val photoUrl: String = "",
    val senderEmail: String = "",
    val senderToken: String = "",
    val receiverEmail: String = "",
    val receiverToken: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
