package com.example.android.socialnetwork.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class PostFragment : Fragment() {

    private lateinit var videoPath: String
    private lateinit var videoName: String
    private lateinit var progressBar: ProgressBar
    private lateinit var content: View

    private lateinit var etPostTitle: TextView
    private lateinit var etPostDescription: TextView
    private lateinit var videoViewPost: VideoView
    private lateinit var buttonPost: Button
    private lateinit var videoFile: File

    private var mAuth: FirebaseAuth? = null
    private var mStorageRef: StorageReference? = null
    private val postsCollection = Firebase.firestore.collection("posts")

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
        etPostDescription = view.findViewById(R.id.etPostDescription)
        videoViewPost = view.findViewById(R.id.videoViewPost)
        buttonPost = view.findViewById(R.id.buttonPost)
        progressBar = view.findViewById(R.id.progressBar)
        content = view.findViewById(R.id.content)
        videoFile = File(videoPath)

        mAuth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference

        videoViewPost.setVideoPath(videoPath)
        videoViewPost.setOnCompletionListener { // repeat the playback
            videoViewPost.start()
        }
        videoViewPost.start()

        buttonPost.setOnClickListener {
            upload()
        }
    }

    private fun upload() {
        progressBar.visibility = View.VISIBLE
        content.visibility = View.INVISIBLE

        val userId = Firebase.auth.currentUser!!.uid

        val storageReference = mStorageRef!!.child("videos/$userId/$videoName")
        storageReference.putFile(videoFile.toUri()).addOnSuccessListener {

            val newReference =
                FirebaseStorage.getInstance().getReference("videos/$userId/$videoName")
            // add post to firestore database to be fetched later in feed
            newReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadURL = uri.toString()
                val user = mAuth!!.currentUser!!

                val post = Post(
                    user.uid,
                    downloadURL,
                    etPostTitle.text.toString(),
                    etPostDescription.text.toString()
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
                        Toast.makeText(requireContext(), "an error has occurred", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack()
                    }

            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "an error has occurred",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "an error has occurred", Toast.LENGTH_LONG)
                .show()
            findNavController().popBackStack()
        }
    }
}