package com.example.android.socialnetwork.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    val username: String = "",
    val userEmail: String = "",
    val messagingToken: String = "",
    val photoUrl: String = "",
    val recentMessage: String = "",
    val timestamp: Long = 0L
) : Parcelable
