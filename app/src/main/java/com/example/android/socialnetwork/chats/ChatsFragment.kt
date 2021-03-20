package com.example.android.socialnetwork.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Chat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatsFragment : Fragment() {

    private lateinit var chatsList: RecyclerView

    private val chatsCollection = Firebase.firestore
        .collection("users")
        .document(Firebase.auth.currentUser!!.email)
        .collection("chats")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatsList = view.findViewById(R.id.chatsList)
        val chatsListAdapter = ChatsListAdapter { chat ->
            val args = bundleOf(
                "chat" to chat
            )
            findNavController().navigate(R.id.action_chatsFragment_to_chatFragment, args)
        }
        chatsList.adapter = chatsListAdapter

        chatsCollection.get()
            .addOnSuccessListener { snap ->
                val chats = mutableListOf<Chat>()
                for (doc in snap.documents) {
                    chats.add(doc.toObject(Chat::class.java)!!)
                }
                chatsListAdapter.submitList(chats)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load chats: $it", Toast.LENGTH_SHORT).show()
            }
    }
}