package com.example.android.socialnetwork.model

data class Notification(
    val notificationType: String,
    val notificationId: String,
    val message: String,
    val username: String,
    val photoUrl: String,
    val timestamp: Long
)
