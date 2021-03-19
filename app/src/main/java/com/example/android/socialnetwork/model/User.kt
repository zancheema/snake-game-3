package com.example.android.socialnetwork.model

data class User(
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val messagingToken: String,
    val online: Boolean = true
)
