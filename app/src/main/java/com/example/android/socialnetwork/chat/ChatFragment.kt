package com.example.android.socialnetwork.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Chat
import com.example.android.socialnetwork.model.ChatMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private lateinit var messageList: RecyclerView
    private lateinit var chat: Chat
    private lateinit var otherChatMessagesCollection: CollectionReference
    private lateinit var myChatMessagesCollection: CollectionReference
    private lateinit var sendMessage: View
    private lateinit var etMessage: EditText
    private lateinit var tvUsername: TextView
    private lateinit var backIcon: View

    private val firebaseUser = Firebase.auth.currentUser!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chat = requireArguments().getParcelable("chat")!!
        myChatMessagesCollection = Firebase.firestore
            .collection("users")
            .document(firebaseUser.email!!)
            .collection("chats")
            .document(chat.userEmail)
            .collection("messages")
        otherChatMessagesCollection = Firebase.firestore
            .collection("users")
            .document(chat.userEmail)
            .collection("chats")
            .document(firebaseUser.email!!)
            .collection("messages")
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

            myChatMessagesCollection.document(chatMessage.uid)
                .set(chatMessage)

            otherChatMessagesCollection.document(chatMessage.uid)
                .set(chatMessage.copy(mine = false))

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
                val messages = mutableListOf<ChatMessage>()
                for (doc in snap.documents) {
                    messages.add(doc.toObject(ChatMessage::class.java)!!)
                }
                messages.sortBy { it.timestamp }
                messageListAdapter.submitList(messages)
                messageList.scrollToPosition(messageListAdapter.itemCount - 1)
            }
    }
}