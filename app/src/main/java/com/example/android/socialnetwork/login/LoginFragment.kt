package com.example.android.socialnetwork.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    //variables from xml layout file
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    //variables for checking info & send info to firebase
    private lateinit var email: String
    private lateinit var password: String

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)

        mAuth = FirebaseAuth.getInstance()

        view.findViewById<TextView>(R.id.signUpText).setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }
        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            btnLoginClick()
        }
    }

    //When log in button is clicked
    fun btnLoginClick() {
        email = etEmail.text.toString().trim()
        password = etPassword.text.toString().trim()

        if(email == ""){
            Toast.makeText(requireContext(), getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
        }else if(password == ""){
            Toast.makeText(requireContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
        }else{
            val utils = Utils()
            if(utils.connectionAvailable(requireContext())) {

//                progressBar.visibility = View.VISIBLE




                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
//                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

//                            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceResult ->
//                                val utils = Utils()
//
//                                utils.updateDeviceToken(requireContext(), instanceResult.token)
//
//                                Log.e(
//                                    "token",
//                                    "token instance id :  ${instanceResult.token.substringAfter(':')}"
//                                )
//
//
//                            }

//                            startActivity(Intent(applicationContext, MainActivity::class.java))
//                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Login failed: ${exception.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }else{
//                startActivity(Intent(applicationContext, NoInternetMessageActivity::class.java))
            }
        }
    }
}