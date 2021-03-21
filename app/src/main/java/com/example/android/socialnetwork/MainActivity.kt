package com.example.android.socialnetwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_FiverrOrder3)
        setContentView(R.layout.activity_main)

        /** setup the bottom nav view */
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        /** show login if not logged in */
        if (Firebase.auth.currentUser == null) {
            navController.navigate(R.id.action_global_loginFragment)
        }

        /** observe currently showing fragment */
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                // show or hide bottom navigation with respect to currently displayed fragment
                if (destination.id == R.id.homeFragment ||
                    destination.id == R.id.notificationFragment ||
                    destination.id == R.id.profileFragment
                ) {
                    bottomNav.visibility = View.VISIBLE
                } else {
                    bottomNav.visibility = View.GONE
                }
            }
        }
    }
}