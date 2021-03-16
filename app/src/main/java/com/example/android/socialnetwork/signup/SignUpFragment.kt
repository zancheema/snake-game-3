package com.example.android.socialnetwork.signup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.NodeNames
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SignUpFragment : Fragment() {

    //variables from xml layout file
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var ivProfilePic: ImageView

    //variables for checking info & send info to firebase
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var btnSignUp: Button

    //firebase variables
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private val usersCollection = Firebase.firestore.collection("users")
    private lateinit var fileStorage: StorageReference

    ///TODO removed lateinit, initialize this with null ...
    private var localFileUri: Uri? = null
    private lateinit var serverFileUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = view.findViewById(R.id.etPostTitle)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        btnSignUp = view.findViewById(R.id.btnSignup)

        btnSignUp.setOnClickListener {
            btnSignupClick()
        }

        mAuth = FirebaseAuth.getInstance()

        fileStorage = FirebaseStorage.getInstance().reference

        view.findViewById<TextView>(R.id.loginText).setOnClickListener {
            findNavController().popBackStack()
        }

        ivProfilePic.setOnClickListener {
            pickProfilePicture()
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

    //When the profile picture is being set
    fun pickProfilePicture() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
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

    //When Sign up button is clicked
    fun btnSignupClick() {
        username = etUsername.text.toString().trim()
        email = etEmail.text.toString().trim()
        password = etPassword.text.toString().trim()
        confirmPassword = etConfirmPassword.text.toString().trim()

        if (username == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_username), Toast.LENGTH_SHORT)
                .show()
        } else if (email == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_email), Toast.LENGTH_SHORT)
                .show()
        } else if (password == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT)
                .show()
        } else if (confirmPassword == "") {
            Toast.makeText(
                requireContext(),
                getString(R.string.confirm_password),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.enter_correct_email),
                Toast.LENGTH_SHORT
            ).show()
        } else if (password != confirmPassword) {
            Toast.makeText(
                requireContext(),
                getString(R.string.password_mismatch),
                Toast.LENGTH_SHORT
            ).show()
        } else {
//            progressBar.visibility = View.VISIBLE

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
//                    progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        firebaseUser = mAuth.currentUser!!

                        if (localFileUri != null) {
                            updateNameAndProfilePhoto()
                        } else {
                            updateOnlyName()
                        }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Fail to create user: ${exception.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

    }

    //Save username and profile photo information to firebase
    private fun updateNameAndProfilePhoto() {
        val strFileName: String = firebaseUser.uid + ".jpg"

        val fileReference: StorageReference = fileStorage.child("images/" + strFileName)

//        progressBar.visibility = View.VISIBLE
        fileReference.putFile(localFileUri!!)
            .addOnCompleteListener { task ->
//                progressBar.visibility = View.GONE
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
                                        val userID: String = firebaseUser.uid

                                        // add user in firestore users collection
                                        val user = mapOf(
                                            NodeNames.USERNAME to etUsername.text.toString().trim(),
                                            NodeNames.EMAIL to etEmail.text.toString().trim(),
                                            NodeNames.PHOTO to serverFileUri.path.toString(),
                                            NodeNames.ONLINE to "true"
                                        )
                                        usersCollection.document(userID).set(user)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "User Successfully Created!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    findNavController().popBackStack()
                                                }
                                            }
                                            .addOnFailureListener { error ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to create user: ${error.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                    val userID: String = firebaseUser.uid

                    // save user to firestore users collection
                    val user = mapOf(
                        NodeNames.USERNAME to etUsername.text.toString().trim(),
                        NodeNames.EMAIL to etEmail.text.toString().trim(),
                        NodeNames.PHOTO to "",
                        NodeNames.ONLINE to "true"
                    )
                    usersCollection.document(userID).set(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "User Successfully Created!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to create user: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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