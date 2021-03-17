package com.example.android.socialnetwork.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    //variables from xml layout file
    private lateinit var userName: TextView
    private lateinit var profileName: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var email: TextView
    private lateinit var logoutIcon: ImageView
    private lateinit var editProfileButton: Button

    private lateinit var localFileUri: Uri

    private var serverFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userName = view.findViewById(R.id.tvUsername)
        profileName = view.findViewById(R.id.tvProfileName)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        email = view.findViewById(R.id.tvEmail)
        logoutIcon = view.findViewById(R.id.logoutIcon)
        editProfileButton = view.findViewById(R.id.editProfileButton)

        Firebase.auth.currentUser?.let { user ->
            val name = user.displayName?.replace("\\s".toRegex(), "")?.toLowerCase()
            userName.text = "@${name}"
            profileName.text = name
            email.text = user.email
            serverFileUri = user.photoUrl

            if (serverFileUri != null) {
                Glide.with(this)
                    .load(serverFileUri)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(ivProfilePic)
            }
        }

        logoutIcon.setOnClickListener {
            btnLogoutClick()
        }
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    //result of obtaining permission code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                localFileUri = data?.data!!
                ivProfilePic.setImageURI(localFileUri)
            }
        }
    }

    //Request permission to read external storage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 101)
            } else {
                Toast.makeText(requireContext(), "Access Permission Required", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //Log out the user
    private fun btnLogoutClick() {
        Auth.logoutAndNavigateToLogin(requireActivity(), findNavController())
    }
}