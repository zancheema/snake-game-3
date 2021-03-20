package com.example.android.socialnetwork.model

import java.util.*

data class ChatMessage(
    val photoUrl: String = "",
    val message: String = "",
    val mine: Boolean = true,
    val uid: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
)
