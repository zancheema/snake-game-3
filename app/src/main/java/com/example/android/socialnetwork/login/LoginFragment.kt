package com.example.android.socialnetwork.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.NodeNames
import com.example.android.socialnetwork.common.Utils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "LoginFragment"
private const val RC_SIGN_IN: Int = 0

class LoginFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var loginManager: LoginManager

    //variables from xml layout file
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var buttonGoogleSignIn: FloatingActionButton
    private lateinit var buttonFacebookLogin: FloatingActionButton

    //variables for checking info & send info to firebase
    private lateinit var email: String
    private lateinit var password: String

    private lateinit var auth: FirebaseAuth
    private val usersCollections = Firebase.firestore.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        loginManager = LoginManager.getInstance()

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        buttonGoogleSignIn = view.findViewById(R.id.buttonGoogleLogin)
        buttonFacebookLogin = view.findViewById(R.id.buttonFacebookLogin)

        auth = Firebase.auth

        // initialize google sign in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        view.findViewById<TextView>(R.id.signUpText).setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }
        view.findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            btnLoginClick()
        }
        buttonGoogleSignIn.setOnClickListener {
            googleSignIn()
        }
        loginManager.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
        buttonFacebookLogin.setOnClickListener {
            Log.d(TAG, "facebook login button: clicked")
            loginManager.logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        Log.d(TAG, "onActivityResult: called")
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    saveUserDataAndOpenFeed()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserDataAndOpenFeed()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Login failed: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Store signed in user to database
     *
     * Sign up is not needed with Google Login in
     * So, all sign up credentials are stored at login
     */
    private fun saveUserDataAndOpenFeed() {
        auth.currentUser.let { user ->
            val userMap = mapOf(
                NodeNames.USERNAME to user.displayName.trim(),
                NodeNames.EMAIL to user.email.trim(),
                NodeNames.PHOTO to user.photoUrl.toString(),
                NodeNames.ONLINE to "true"
            )
            usersCollections.document(user.uid)
                .set(userMap)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //When log in button is clicked
    private fun btnLoginClick() {
        email = etEmail.text.toString().trim()
        password = etPassword.text.toString().trim()

        if (email == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_email), Toast.LENGTH_SHORT)
                .show()
        } else if (password == "") {
            Toast.makeText(requireContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT)
                .show()
        } else {
            val utils = Utils()
            if (utils.connectionAvailable(requireContext())) {

//                progressBar.visibility = View.VISIBLE


                auth.signInWithEmailAndPassword(email, password)
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
            } else {
//                startActivity(Intent(applicationContext, NoInternetMessageActivity::class.java))
            }
        }
    }
}