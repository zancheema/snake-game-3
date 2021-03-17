package com.example.android.socialnetwork.otherprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Auth
import com.example.android.socialnetwork.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OtherProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var logoutIcon: View
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvEmail: TextView

    private lateinit var userUid: String
    private lateinit var user: User
    private val usersCollection = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userUid = arguments?.getString("userUid")!!
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

        usersCollection.document(userUid).get()
            .addOnSuccessListener {
                user = it.toObject(User::class.java)!!

                val username = user.username.replace("\\s".toRegex(), "").toLowerCase()
                tvUsername.text = "@$username"
                Glide
                    .with(requireContext())
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(ivProfilePic)
                tvProfileName.text = username
                tvEmail.text = user.email
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        logoutIcon.setOnClickListener {
            Auth.logoutAndNavigateToLogin(
                requireActivity(),
                findNavController()
            )
        }
    }
}