package com.example.android.socialnetwork.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    //variables from xml layout file
    private lateinit var userName: TextView
    private lateinit var profileName: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var email: TextView
    private lateinit var logoutButton: ImageView
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

        userName = view.findViewById(R.id.userName)
        profileName = view.findViewById(R.id.profileName)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        email = view.findViewById(R.id.email)
        logoutButton = view.findViewById(R.id.logoutButton)
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

        logoutButton.setOnClickListener {
            btnLogoutClick()
        }
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    //result of obtaining permission code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==101){
            if(resultCode== Activity.RESULT_OK){
                localFileUri = data?.data!!
                ivProfilePic.setImageURI(localFileUri)
            }
        }
    }

    //Request permission to read external storage
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==102){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 101)
            }else{
                Toast.makeText(requireContext(), "Access Permission Required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Log out the user
    fun btnLogoutClick() {
        // sign out of firebase
        Firebase.auth.signOut()
        // sign out of google (if needed)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut()

        // show login
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }
}