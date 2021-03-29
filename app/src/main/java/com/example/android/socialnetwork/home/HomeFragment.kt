package com.example.android.socialnetwork.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var postFeed: RecyclerView
    private lateinit var openChat: View
    private val postsCollection = Firebase.firestore.collection("posts")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postFeed = view.findViewById(R.id.postFeed)
        openChat = view.findViewById(R.id.openChat)
        val postFeedAdapter = PostFeedListAdapter { uid ->
            val args = bundleOf(
                "userUid" to uid
            )
            findNavController().navigate(R.id.action_homeFragment_to_otherProfileFragment, args)
        }
        postFeed.adapter = postFeedAdapter

        postsCollection
            .get()
            .addOnSuccessListener { snap ->
                val posts = snap.documents
                    .map { it.toObject(Post::class.java) }
                    .sortedByDescending { it?.timeStamp }
                postFeedAdapter.submitList(posts)
            }
            .addOnFailureListener { exc ->
                Toast.makeText(context, "Error Loading feed: $exc", Toast.LENGTH_SHORT).show()
            }

        openChat.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatsFragment)
        }
    }
}