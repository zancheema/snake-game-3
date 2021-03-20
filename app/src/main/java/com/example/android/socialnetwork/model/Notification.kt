package com.example.android.socialnetwork.model

data class Notification(
    val notificationType: String = "",
    val notificationId: String = "",
    val message: String = "",
    val senderName: String = "",
    val photoUrl: String = "",
    val senderUid: String = "",
    val senderToken: String = "",
    val receiverUid: String = "",
    val receiverToken: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
