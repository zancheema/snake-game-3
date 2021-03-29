package com.example.android.socialnetwork.common

import android.app.Activity
import androidx.navigation.NavController
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Auth {
    fun logoutAndNavigateToLogin(
        activity: Activity,
        navController: NavController
    ) {
        logout(activity) {
            navController.navigate(R.id.action_global_loginFragment)
        }
    }

    //Log out the user
    fun logout(
        activity: Activity,
        onLoggedOut: () -> Unit = {}
    ) {
        // update user status in database
        val userDoc =
            Firebase.firestore.collection("users").document(Firebase.auth.currentUser.uid)
        userDoc.get()
            .addOnSuccessListener { snap ->
                val user =
                    snap.toObject(User::class.java)!!.copy(online = false, messagingToken = "")
                userDoc.set(user)
            }

        // sign out of firebase
        Firebase.auth.signOut()
        // sign out of google (if needed)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut()

        // show login
        if (Firebase.auth.currentUser == null) {
            onLoggedOut()
        }
    }
}