package com.example.android.socialnetwork.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Chat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatsFragment : Fragment() {

    private lateinit var chatsList: RecyclerView

    private val chatsCollection = Firebase.firestore
        .collection("users")
        .document(Firebase.auth.currentUser!!.uid)
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
                val chats = getChats(snap)
                chatsListAdapter.submitList(chats)
            }
            .addOnFailureListener {
                Toast.makeText(context, "an error has occurred", Toast.LENGTH_SHORT).show()
            }

        // observe for changes to chats collection
        chatsCollection.addSnapshotListener { snap, error ->
            if (error != null) {
                Toast.makeText(context, "an error has occurred", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            chatsListAdapter.submitList(getChats(snap!!))
        }

        view.findViewById<ImageButton>(R.id.openChat).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun getChats(snap: QuerySnapshot) = snap.documents
        .map { it.toObject(Chat::class.java) }
        .sortedByDescending { it?.timestamp }
}