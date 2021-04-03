package com.example.android.socialnetwork.otherprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Auth
import com.example.android.socialnetwork.model.Notification
import com.example.android.socialnetwork.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OtherProfileFragment : Fragment() {

    private lateinit var buttonLogout: ImageButton
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var buttonAddFriend: Button

    private lateinit var content: View
    private lateinit var progress: View


    private lateinit var otherUserUid: String
    private lateinit var otherUser: User
    private lateinit var currentUser: User

    private val firebaseUser = Firebase.auth.currentUser!!
    private val usersCollection = Firebase.firestore.collection("users")
    private lateinit var notificationCollection: CollectionReference

    private val notificationId = "friend-request-${firebaseUser.uid}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        otherUserUid = arguments?.getString("userUid")!!
        notificationCollection = usersCollection.document(otherUserUid).collection("notifications")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonLogout = view.findViewById(R.id.buttonLogout)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvUserBio = view.findViewById(R.id.tvUserBio)
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend)

        content = view.findViewById(R.id.content)
        progress = view.findViewById(R.id.progressBar)

        showProgress()

        notificationCollection
            .document(notificationId)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.data != null) {
                    buttonAddFriend.text = "Friend Request Sent"
                    showContent()
                } else {
                    usersCollection
                        .document(firebaseUser.uid)
                        .collection("chats")
                        .document(otherUserUid)
                        .get()
                        .addOnSuccessListener { snap ->
                            if (snap.data != null) {
                                buttonAddFriend.text = "Friends"
                                showContent()
                            } else {
                                showContent()
                                buttonAddFriend.setOnClickListener {

                                    val notification = Notification(
                                        "friendRequestSent",
                                        notificationId,
                                        "${currentUser.username} has requested to friend you",
                                        "",
                                        currentUser.photoUrl,
                                        currentUser.uid,
                                        currentUser.messagingToken,
                                        otherUser.uid,
                                        otherUser.messagingToken
                                    )
                                    notificationCollection.document(notification.notificationId)
                                        .set(notification)
                                        .addOnSuccessListener {
                                            buttonAddFriend.text = "Friend Request Sent"
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "an error has occurred",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        }
                }
            }

        usersCollection.document(firebaseUser.uid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load current user", Toast.LENGTH_SHORT).show()
            }

        usersCollection.document(otherUserUid).get()
            .addOnSuccessListener {
                otherUser = it.toObject(User::class.java)!!

                tvUserBio.text = otherUser.bio
                Glide
                    .with(requireContext())
                    .load(otherUser.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(ivProfilePic)
                tvProfileName.text = otherUser.username
                tvEmail.text = otherUser.email
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        buttonLogout.setOnClickListener {
            Auth.logoutAndNavigateToLogin(
                requireActivity(),
                findNavController()
            )
        }
    }

    private fun showContent() {
        progress.visibility = View.GONE
        content.visibility = View.VISIBLE
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
        content.visibility = View.GONE
    }
}