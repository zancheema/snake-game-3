package com.example.android.socialnetwork.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ReAuthFragment : Fragment(R.layout.fragment_re_auth) {

    private lateinit var etPassword: TextInputEditText
    private lateinit var buttonConfirm: Button

    private val firebaseUser = Firebase.auth.currentUser!!
    private val email: String = firebaseUser.email!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPassword = view.findViewById(R.id.etPassword)
        buttonConfirm = view.findViewById(R.id.buttonConfirm)

        buttonConfirm.setOnClickListener {
            val password = etPassword.text.toString()

            val credential = EmailAuthProvider.getCredential(email, password)
            firebaseUser.reauthenticate(credential)
                .addOnSuccessListener {
                    val args = bundleOf(
                        "password" to password
                    )
                    findNavController().navigate(
                        R.id.action_reAuthFragment_to_editProfileFragment,
                        args
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Authentication failed: $it", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                }
        }
    }
}