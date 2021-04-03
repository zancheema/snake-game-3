package com.example.android.socialnetwork.profile

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
import com.example.android.socialnetwork.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    //variables from xml layout file
    private lateinit var profileName: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var email: TextView
    private lateinit var buttonLogout: ImageButton
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileName = view.findViewById(R.id.tvProfileName)
        tvUserBio = view.findViewById(R.id.tvUserBio)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        email = view.findViewById(R.id.tvEmail)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        editProfileButton = view.findViewById(R.id.editProfileButton)


        Firebase.firestore.collection("users")
            .document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { snap ->
                val user = snap.toObject(User::class.java)!!
                profileName.text = user.username
                email.text = user.email
                tvUserBio.text = user.bio

                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(ivProfilePic)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "an error has occurred", Toast.LENGTH_SHORT).show()
            }

        buttonLogout.setOnClickListener {
            btnLogoutClick()
        }
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    //Log out the user
    private fun btnLogoutClick() {
        Auth.logoutAndNavigateToLogin(requireActivity(), findNavController())
    }
}