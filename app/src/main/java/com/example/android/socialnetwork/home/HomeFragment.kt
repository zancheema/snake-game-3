package com.example.android.socialnetwork.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import com.example.android.socialnetwork.model.TotalUnreadMessages
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var postFeed: RecyclerView
    private lateinit var openChat: View
    private lateinit var tvUnreadMessagesCount: TextView

    private val postsCollection = Firebase.firestore.collection("posts")
    private lateinit var unreadMessagesDoc: DocumentReference

    init {
        Firebase.auth.currentUser?.let { user ->
            unreadMessagesDoc = Firebase.firestore
                .collection("users")
                .document(user.uid)
                .collection("chatFunctions")
                .document("unreadMessages")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUnreadMessagesCount = view.findViewById(R.id.tvUnreadMessagesCount)
        postFeed = view.findViewById(R.id.postFeed)
        openChat = view.findViewById(R.id.openChat)

        setUpUnreadMessages()
        setUpPostFeed()

        openChat.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatsFragment)
        }
    }

    private fun setUpPostFeed() {
        val postFeedAdapter = PostFeedListAdapter { uid ->
            if (uid == Firebase.auth.currentUser!!.uid) {
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                    .selectedItemId = R.id.profileFragment
            } else {
                val args = bundleOf(
                    "userUid" to uid
                )
                findNavController().navigate(R.id.action_homeFragment_to_otherProfileFragment, args)
            }
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
                Toast.makeText(context, exc.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setUpUnreadMessages() {
        if (!::unreadMessagesDoc.isInitialized) return
        unreadMessagesDoc.apply {
            get()
                .addOnSuccessListener {
                    processUnreadMessagesSnapshot(it)
                }
                .addOnFailureListener {
                    Log.d(TAG, "setUpUnreadMessages: failure: $it")
                }

            addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    processUnreadMessagesSnapshot(value)
                }
            }
        }
    }

    private fun processUnreadMessagesSnapshot(it: DocumentSnapshot) {
        it.toObject(TotalUnreadMessages::class.java)?.let { messages ->
            tvUnreadMessagesCount.apply {
                text = messages.totalCount.toString()
                visibility = if (messages.totalCount > 0) VISIBLE else GONE
            }
        }
    }
}