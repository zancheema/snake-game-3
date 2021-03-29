package com.example.android.socialnetwork.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Chat
import com.example.android.socialnetwork.model.ChatMessage
import com.example.android.socialnetwork.model.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var otherChatDoc: DocumentReference
    private lateinit var myChatDoc: DocumentReference
    private lateinit var messageList: RecyclerView
    private lateinit var chat: Chat
    private lateinit var otherChatMessagesCollection: CollectionReference
    private lateinit var myChatMessagesCollection: CollectionReference
    private lateinit var notificationsCollection: CollectionReference
    private lateinit var sendMessage: View
    private lateinit var etMessage: EditText
    private lateinit var tvUsername: TextView
    private lateinit var backIcon: View

    private val firebaseUser = Firebase.auth.currentUser!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chat = requireArguments().getParcelable("chat")!!
        myChatDoc = Firebase.firestore
            .collection("users")
            .document(firebaseUser.uid)
            .collection("chats")
            .document(chat.userUid)

        otherChatDoc = Firebase.firestore
            .collection("users")
            .document(chat.userUid)
            .collection("chats")
            .document(firebaseUser.uid)

        myChatMessagesCollection = myChatDoc
            .collection("messages")
        otherChatMessagesCollection = otherChatDoc
            .collection("messages")

        notificationsCollection = Firebase.firestore
            .collection("users")
            .document(chat.userUid)
            .collection("notifications")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendMessage = view.findViewById(R.id.tvSendMessage)
        etMessage = view.findViewById(R.id.etMessage)
        tvUsername = view.findViewById(R.id.tvUsername)
        backIcon = view.findViewById(R.id.backIcon)

        tvUsername.text = chat.username

        messageList = view.findViewById(R.id.messageList)
        val messageListAdapter = ChatMessageListAdapter()
        messageList.adapter = messageListAdapter

        refreshChatMessages(messageListAdapter)

        sendMessage.setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isBlank()) return@setOnClickListener

            val chatMessage = ChatMessage(firebaseUser.photoUrl?.toString() ?: "", message)

            // send chat message
            myChatMessagesCollection.document(chatMessage.uid)
                .set(chatMessage)
            otherChatMessagesCollection.document(chatMessage.uid)
                .set(chatMessage.copy(mine = false))

            // update recent message
            myChatDoc.update(
                mapOf(
                    "recentMessage" to chatMessage.message,
                    "timestamp" to chatMessage.timestamp
                )
            )
            otherChatDoc.update(
                mapOf(
                    "recentMessage" to chatMessage.message,
                    "timestamp" to chatMessage.timestamp
                )
            )

            // send chat message notifcation
            val notification = Notification(
                "chatMessageSent",
                UUID.randomUUID().toString(),
                firebaseUser.displayName ?: "",
                message,
                firebaseUser.photoUrl?.toString() ?: "",
                firebaseUser.uid,
                "",
                chat.userUid,
                chat.messagingToken
            )
            notificationsCollection.add(notification)

            // listen to new messages
            myChatMessagesCollection.addSnapshotListener { snap, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load chat: $error", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }
                val messages = getChatMessages(snap!!)
                messageListAdapter.submitList(messages)
                if (messageListAdapter.itemCount > 0) {
                    messageList.smoothScrollToPosition(messageListAdapter.itemCount - 1)
                }
            }

            refreshChatMessages(messageListAdapter)
            etMessage.text.clear()
        }

        backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun refreshChatMessages(messageListAdapter: ChatMessageListAdapter) {
        myChatMessagesCollection.get()
            .addOnSuccessListener { snap ->
                val messages = getChatMessages(snap)
                messageListAdapter.submitList(messages)
                if (messageListAdapter.itemCount > 0) {
                    messageList.smoothScrollToPosition(messageListAdapter.itemCount - 1)
                }
            }
    }

    private fun getChatMessages(snap: QuerySnapshot) = snap.documents
        .map { it.toObject(ChatMessage::class.java) }
        .sortedBy { it?.timestamp }
}