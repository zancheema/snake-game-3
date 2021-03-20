package com.example.android.socialnetwork.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    val uid: String = "",
    val username: String = "",
    val messagingToken: String = "",
    val photoUrl: String = "",
    val recentMessage: String = "",
    val timestamp: Long = 0L
) : Parcelable
