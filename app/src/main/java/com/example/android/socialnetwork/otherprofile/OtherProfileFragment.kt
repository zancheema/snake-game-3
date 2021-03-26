package com.example.android.socialnetwork.otherprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.util.*

class OtherProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var logoutIcon: View
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var buttonAddFriend: Button

    private lateinit var userEmail: String
    private lateinit var otherUser: User
    private lateinit var currentUser: User

    private val firebaseUser = Firebase.auth.currentUser!!
    private val usersCollection = Firebase.firestore.collection("users")
    private lateinit var notificationCollection: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userEmail = arguments?.getString("userEmail")!!
        notificationCollection = usersCollection.document(userEmail).collection("notifications")
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

        tvUsername = view.findViewById(R.id.tvUsername)
        logoutIcon = view.findViewById(R.id.logoutIcon)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvUserBio = view.findViewById(R.id.tvUserBio)
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend)

        usersCollection.document(firebaseUser.email).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load current user", Toast.LENGTH_SHORT).show()
            }

        usersCollection.document(userEmail).get()
            .addOnSuccessListener {
                otherUser = it.toObject(User::class.java)!!

                val username = otherUser.username.replace("\\s".toRegex(), "").toLowerCase()
                tvUsername.text = "@$username"

                tvUserBio.text = otherUser.bio

                Glide
                    .with(requireContext())
                    .load(otherUser.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(ivProfilePic)
                tvProfileName.text = username
                tvEmail.text = otherUser.email
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        usersCollection.document(userEmail).get()
            .addOnSuccessListener {
                otherUser = it.toObject(User::class.java)!!
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load current user", Toast.LENGTH_SHORT).show()
            }

        buttonAddFriend.setOnClickListener {

            val notification = Notification(
                "friendRequestSent",
                UUID.randomUUID().toString(),
                currentUser.username,
                "Let's be friends",
                currentUser.photoUrl,
                currentUser.email,
                currentUser.messagingToken,
                otherUser.email,
                otherUser.messagingToken
            )
            notificationCollection.document(notification.notificationId).set(notification)
                .addOnSuccessListener {
                    Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error sending request: $it", Toast.LENGTH_SHORT).show()
                }
        }

        logoutIcon.setOnClickListener {
            Auth.logoutAndNavigateToLogin(
                requireActivity(),
                findNavController()
            )
        }
    }
}