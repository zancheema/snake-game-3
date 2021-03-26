package com.example.android.socialnetwork.model

data class Post(
    val userEmail: String = "",
    val videoUrl: String = "",
    val title: String = "",
    val description: String = "",
    val timeStamp: Long = System.currentTimeMillis()
)
