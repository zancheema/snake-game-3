package com.example.android.socialnetwork.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val messagingToken: String = "",
    val bio: String = "",
    val online: Boolean = true
) : Parcelable
