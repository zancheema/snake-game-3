package com.example.android.socialnetwork.editprofile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Auth
import com.example.android.socialnetwork.common.NodeNames
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileFragment : Fragment() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var ivProfilePic: ImageView
    private lateinit var logoutIcon: ImageView
    private lateinit var changePictureButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var content: View

    //firebase variables
    private lateinit var mAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var fileStorage: StorageReference
    private lateinit var localFileUri: Uri

    private val usersCollection = Firebase.firestore.collection("users")

    ////TODO removed lateinit ...
    private var serverFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.etPostTitle)
        etEmail = view.findViewById(R.id.etEmail)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        logoutIcon = view.findViewById(R.id.logoutIcon)
        changePictureButton = view.findViewById(R.id.buttonChangePicture)
        saveChangesButton = view.findViewById(R.id.buttonSaveChanges)
        progressBar = view.findViewById(R.id.progressBar)
        content = view.findViewById(R.id.content)

        mAuth = FirebaseAuth.getInstance()

        firebaseUser = mAuth.currentUser!!

        fileStorage = FirebaseStorage.getInstance().reference

        etUsername.setText(firebaseUser.displayName)
        etEmail.setText(firebaseUser.email)
        serverFileUri = firebaseUser.photoUrl

        if (serverFileUri != null) {
            Glide.with(this)
                .load(serverFileUri)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(ivProfilePic)
        }

        logoutIcon.setOnClickListener {
            Auth.logoutAndNavigateToLogin(requireActivity(), findNavController())
        }
        changePictureButton.setOnClickListener {
            changeImage(it)
        }
        saveChangesButton.setOnClickListener {
            saveChanges()
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

    //save changes to the profile page
    private fun saveChanges() {
        if (etUsername.text.toString().trim() == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_username), Toast.LENGTH_SHORT)
                .show()
        } else {
            if (!this::localFileUri.isInitialized) {
                updateOnlyName()
            } else {
                updateNameAndProfilePhoto()
            }
        }
    }

    //change the profile image when the profile icon is clicked
    fun changeImage(v: View) {
        if (serverFileUri == null) {
            pickProfilePicture()
        } else {
            val popupMenu = PopupMenu(requireContext(), v)
            popupMenu.menuInflater.inflate(R.menu.menu_picture, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                val id: Int = it.itemId

                if (id == R.id.mnuChangePicture) {
                    pickProfilePicture()
                } else if (id == R.id.mnuRemovePicture) {
                    removeProfilePicture()
                }

                return@setOnMenuItemClickListener false
            }
            popupMenu.show()
        }
    }

    //Choose a new profile picture
    private fun pickProfilePicture() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 101)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                102
            )
        }
    }

    //Remove the profile picture
    private fun removeProfilePicture() {
        progressBar.visibility = View.VISIBLE
        content.visibility = View.GONE

        val request: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(etUsername.text.toString().trim())
            .setPhotoUri(null)
            .build()

        firebaseUser.updateProfile(request)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                content.visibility = View.VISIBLE

                if (task.isSuccessful) {

                    val map = mapOf(
                        NodeNames.PHOTO to ""
                    )
                    usersCollection.document(firebaseUser.email)
                        .update(map)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Profile Photo Successfully Removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                }
            }
    }

    //Save username and profile photo information to firebase
    private fun updateNameAndProfilePhoto() {
        val strFileName: String = firebaseUser.uid + ".jpg"

        val fileReference: StorageReference = fileStorage.child("images/$strFileName")

        progressBar.visibility = View.VISIBLE
        content.visibility = View.GONE

        fileReference.putFile(localFileUri)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                content.visibility = View.VISIBLE

                if (task.isSuccessful) {
                    fileReference.downloadUrl
                        .addOnSuccessListener {
                            serverFileUri = it

                            val request: UserProfileChangeRequest =
                                UserProfileChangeRequest.Builder()
                                    .setDisplayName(etUsername.text.toString().trim())
                                    .setPhotoUri(serverFileUri)
                                    .build()

                            firebaseUser.updateProfile(request)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val map = mapOf(
                                            NodeNames.USERNAME to etUsername.text.toString().trim(),
                                            NodeNames.PHOTO to serverFileUri.toString()
                                        )
                                        usersCollection.document(firebaseUser.email)
                                            .update(map)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    findNavController().popBackStack()
                                                }
                                            }
                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Fail to update profile: ${exception.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                }
            }
    }

    //Save only username info to firebase
    private fun updateOnlyName() {
//        progressBar.visibility = View.VISIBLE

        val request: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(etUsername.text.toString().trim())
            .build()

        firebaseUser.updateProfile(request)
            .addOnCompleteListener { task ->
//                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val map = mapOf(
                        NodeNames.USERNAME to etUsername.text.toString().trim()
                    )
                    usersCollection.document(firebaseUser.email)
                        .update(map)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                findNavController().popBackStack()
                            }
                        }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Fail to update profile: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}