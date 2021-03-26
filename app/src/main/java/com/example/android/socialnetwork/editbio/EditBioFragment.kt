package com.example.android.socialnetwork.editbio

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditBioFragment : Fragment(R.layout.fragment_edit_bio) {

    private lateinit var user: User

    private lateinit var etUserBio: TextInputEditText
    private lateinit var buttonSaveChanges: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = requireArguments().getParcelable("user")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserBio = view.findViewById(R.id.etUserBio)
        buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges)

        etUserBio.setText(user.bio)

        buttonSaveChanges.setOnClickListener {
            val bioArgs = mapOf(
                "bio" to etUserBio.text.toString()
            )

            Firebase.firestore.collection("users")
                .document(user.email)
                .update(bioArgs)
                .addOnSuccessListener {
                    Toast.makeText(context, "Bio updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to updated bio: $it", Toast.LENGTH_SHORT).show()
                }
        }
    }
}