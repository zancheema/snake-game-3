package com.example.android.socialnetwork.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Utils {

    @RequiresApi(Build.VERSION_CODES.M)
    fun connectionAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capability =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    fun updateDeviceToken(context: Context, token: String) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            val rootRef: DatabaseReference =
                FirebaseDatabase.getInstance().reference.child("RealTimeChat2")

            val databaseReference: DatabaseReference = rootRef.child(NodeNames.USERS)
                .child(currentUser.uid).child(NodeNames.TOKENS)

            val hashMap: HashMap<String, String> = HashMap()
            //hashMap.put(NodeNames.DEVICE_TOKEN, token)
            hashMap[NodeNames.DEVICE_TOKEN] = token.substringAfter(':')

            databaseReference.setValue(hashMap).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Failed to save device token: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
    /*
    fun sendNotification(context: Context, title: String, message: String, otherUserId: String){
        val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val databaseReference: DatabaseReference = rootRef.child(NodeNames.TOKENS).child(otherUserId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(NodeNames.DEVICE_TOKEN).value != null) {
                }
                val deviceToken: String = snapshot.child(NodeNames.DEVICE_TOKEN).value.toString()

                val notification: JSONObject = JSONObject()
                val notificationData: JSONObject = JSONObject()

                try {
                    notificationData.put(Constants.NOTIFICATION_TITLE, title)
                    notificationData.put(Constants.NOTIFICATION_MESSAGE, message)
                    notificationData.put(Constants.NOTIFICATION_TO, deviceToken)
                    notificationData.put(Constants.NOTIFICATION_DATA, notificationData)

                    val fcmAPIUrl: String = "https://fcm.googleapis.com/fcm/send"

                    val contentType: String = "application/json"

                    val successListener: Response.Listener<JSONObject> =
                        Response.Listener<JSONObject> {
                            Toast.makeText(context, "Notification Successfully Sent", Toast.LENGTH_SHORT).show()
                        }
                    val failureListener: Response.ErrorListener = Response.ErrorListener { error ->
                        Toast.makeText(context, "Failed to Send notification1: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }

                    val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(fcmAPIUrl, notification, successListener, failureListener) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val params: MutableMap<String, String> = HashMap()
                            params["Authorization"] = "key=" + Constants.FIREBASE_KEY
                            params["Sender"] = "id=" + Constants.SENDER_ID
                            params["Content-Type"] = contentType
                            return params
                        }
                    }

                    val requestQueue: RequestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(jsonObjectRequest)

                } catch (e: JSONException) {
                    Toast.makeText(
                        context,
                        "Failed to Send notification2: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to Send notification3: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }


  */
