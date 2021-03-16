package com.example.android.socialnetwork.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class PostFragment : Fragment() {

    private lateinit var videoPath: String
    private lateinit var videoName: String

    private lateinit var etPostTitle: TextView
    private lateinit var postThumbnail: ImageView
    private lateinit var buttonPost: Button
    private lateinit var videoFile: File

    private val postsCollection = Firebase.firestore.collection("posts")

    var mAuth: FirebaseAuth? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null
    var mStorageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoPath = arguments?.getString("videoPath") ?: ""
        videoName = arguments?.getString("videoName") ?: System.currentTimeMillis().toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPostTitle = view.findViewById(R.id.etPostTitle)
        postThumbnail = view.findViewById(R.id.postVideoThumbnail)
        buttonPost = view.findViewById(R.id.buttonPost)
        videoFile = File(videoPath)

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        myRef = firebaseDatabase!!.reference
        mStorageRef = FirebaseStorage.getInstance().reference

        Glide
            .with(requireContext())
            .load(videoFile.toUri())
            .thumbnail(0.1f)
            .placeholder(R.drawable.empty_image_thumbnail)
            .error(R.drawable.empty_image_thumbnail)
            .into(postThumbnail)

        buttonPost.setOnClickListener {
            upload()
        }
    }

    private fun upload() {
        val userId = Firebase.auth.currentUser.uid

        val storageReference = mStorageRef!!.child("videos/$userId/$videoName")
        storageReference.putFile(videoFile.toUri()).addOnSuccessListener { taskSnapshot ->

            val newReference = FirebaseStorage.getInstance().getReference("videos/$userId/$videoName")
            // add post to firestore database to be fetched later in feed
            newReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadURL = uri.toString()
                val user = mAuth!!.currentUser

                val post = Post(
                    user.displayName!!,
                    user.email!!,
                    etPostTitle.text.toString(),
                    downloadURL
                )
                postsCollection.add(post)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Post added successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add post", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack()
                    }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to Post: ${it.message}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        }.addOnFailureListener { exception ->
            if (exception != null) {
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG)
                    .show()
                findNavController().popBackStack()
            }
        }
    }
}