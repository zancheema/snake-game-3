package com.example.android.socialnetwork.editprofile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Auth
import com.example.android.socialnetwork.common.NodeNames
import com.example.android.socialnetwork.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "EditProfileFragment"

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var etUserBio: TextInputEditText
    private lateinit var ivProfilePic: ImageView
    private lateinit var buttonLogout: ImageButton
    private lateinit var changePictureButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var buttonShowPassword: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var content: View

    private lateinit var user: User

    private var password: String? = null

    //firebase variables
    private lateinit var mAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var fileStorage: StorageReference
    private lateinit var localFileUri: Uri

    private val usersCollection = Firebase.firestore.collection("users")

    ////TODO removed lateinit ...
    private var serverFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            password = it.getString("password")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.etPostTitle)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        etUserBio = view.findViewById(R.id.etUserBio)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        changePictureButton = view.findViewById(R.id.buttonChangePicture)
        saveChangesButton = view.findViewById(R.id.buttonSaveChanges)
        buttonShowPassword = view.findViewById(R.id.buttonShowPassword)
        progressBar = view.findViewById(R.id.progressBar)
        content = view.findViewById(R.id.content)

        showDataLoading()

        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth.currentUser!!
        fileStorage = FirebaseStorage.getInstance().reference

        Firebase.firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { snap ->
                user = snap.toObject(User::class.java)!!

                serverFileUri = Uri.parse(user.photoUrl)
                if (serverFileUri != null) {
                    Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .into(ivProfilePic)
                }

                etUsername.setText(user.username)
                etEmail.setText(user.email)
                etUserBio.setText(user.bio)

                buttonLogout.setOnClickListener {
                    Auth.logoutAndNavigateToLogin(requireActivity(), findNavController())
                }
                changePictureButton.setOnClickListener {
                    changeImage(it)
                }
                saveChangesButton.setOnClickListener {
                    saveChanges()
                }
                buttonShowPassword.setOnClickListener {
                    if (user.email.isNotBlank()) {
                        findNavController().navigate(R.id.action_editProfileFragment_to_reAuthFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Password can be only changed for email login",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                etUserBio.setOnClickListener {
                    val args = bundleOf(
                        "user" to user
                    )
                    findNavController().navigate(
                        R.id.action_editProfileFragment_to_editBioFragment,
                        args
                    )
                }
                showContent()
            }
            .addOnFailureListener {
                showContent()
                Toast.makeText(
                    requireContext(),
                    "Failed to load user data: $it",
                    Toast.LENGTH_SHORT
                ).show()
            }

        if (password != null) {
            etPassword.isEnabled = true
            etPassword.setText(password)
            etConfirmPassword.setText(password)
            etConfirmPassword.isEnabled = true

            buttonShowPassword.visibility = View.GONE
        } else {
            etPassword.setText("******")
            etConfirmPassword.setText("******")
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
        showDataLoading()

        val request: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(etUsername.text.toString().trim())
            .setPhotoUri(null)
            .build()

        firebaseUser.updateProfile(request)
            .addOnCompleteListener { task ->
                showContent()

                if (task.isSuccessful) {

                    val map = mapOf(
                        NodeNames.PHOTO to ""
                    )
                    usersCollection.document(firebaseUser.uid)
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
        Log.d(TAG, "updateNameAndProfilePhoto: called")

        val strFileName: String = firebaseUser.uid + ".jpg"

        val fileReference: StorageReference = fileStorage.child("images/$strFileName")

        showDataLoading()

        fileReference.putFile(localFileUri)
            .addOnCompleteListener { task ->
                showContent()

                if (task.isSuccessful) {
                    updatePasswordIfRequired()

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
                                        usersCollection.document(firebaseUser.uid)
                                            .update(map)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "The update was successful",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    findNavController().popBackStack()
                                                }
                                            }
                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Update Failed: ${exception.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                }
            }
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        content.visibility = View.VISIBLE
    }

    private fun showDataLoading() {
        progressBar.visibility = View.VISIBLE
        content.visibility = View.GONE
    }

    //Save only username info to firebase
    private fun updateOnlyName() {
        Log.d(TAG, "updateOnlyName: called")
//        progressBar.visibility = View.VISIBLE

        val request: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(etUsername.text.toString().trim())
            .build()

        firebaseUser.updateProfile(request)
            .addOnSuccessListener {
//                progressBar.visibility = View.GONE

                updatePasswordIfRequired()

                val map = mapOf(
                    NodeNames.USERNAME to etUsername.text.toString().trim()
                )
                usersCollection.document(firebaseUser.uid)
                    .update(map)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
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

    private fun updatePasswordIfRequired() {

        val pass1 = etPassword.text.toString()
        val pass2 = etConfirmPassword.text.toString()
        Log.d(TAG, "updatePassword: called: $pass1, $pass2")

        if (pass1 != password && pass1 == pass2) {
            firebaseUser.updatePassword(pass1)
        } else {
            Toast.makeText(
                requireContext(),
                "The passwords did not match and could not be updated",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}