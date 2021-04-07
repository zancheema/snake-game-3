package com.example.android.socialnetwork.forgotpassword

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private lateinit var etEmail: TextInputEditText
    private lateinit var buttonSendEmail: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var content: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEmail = view.findViewById(R.id.etEmail)
        buttonSendEmail = view.findViewById(R.id.buttonSendEmail)
        progressBar = view.findViewById(R.id.progressBar)
        content = view.findViewById(R.id.content)

        buttonSendEmail.setOnClickListener {
            val email = etEmail.text.toString()
            progressBar.visibility = View.VISIBLE
            content.visibility = View.GONE
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    Toast.makeText(context, "Email sent successfully.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    content.visibility = View.VISIBLE
                    Toast.makeText(
                        context,
                        it.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}