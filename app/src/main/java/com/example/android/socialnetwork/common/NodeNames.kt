package com.example.android.socialnetwork.common

//Class for making branches in firebase
class NodeNames {

    companion object {

        const val RECEIVER_ID: String = "ReceiverId"
        const val LAST_MESSAGE: String = "LastMessage"
        const val LAST_MESSAGE_TIME: String = "LastMessageTime"
        const val CHAT_TIME: String = "ChatTimeStamp"


        const val USERS: String = "Users"
        const val CHATS: String = "Chats"
        const val FRIEND_REQUESTS: String = "FriendRequests"
        const val MESSAGES: String = "Messages"

        const val USERNAME: String = "username"
        const val EMAIL: String = "email"
        const val TOKENS: String = "tokens"

        //PHOTO FILE NAME
        const val PHOTO: String = "photo"
        const val ONLINE: String = "online"

        const val REQUEST_TYPE: String = "request_type"

        val TIME_STAMP: String = "timestamp"

        const val MESSAGE_ID: String = "MessageId"
        const val MESSAGETXT: String = "MessageTxt"
        const val MESSAGEIMG: String = "MessageImg"
        const val MESSAGE_TYPE: String = "MessageType"
        const val MESSAGE_FROM: String = "MessageFrom"
        const val MESSAGE_TIME: String = "MessageTime"

        const val MESSAGE_TYPE_TEXT: String = "text"

        const val DEVICE_TOKEN: String = "device_token"

    }
}