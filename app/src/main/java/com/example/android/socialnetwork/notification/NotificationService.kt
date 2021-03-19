package com.example.android.socialnetwork.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.android.socialnetwork.MainActivity
import com.example.android.socialnetwork.R
import com.example.android.socialnetwork.common.Constants
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Once a token is generated, we subscribe to topic.
        FirebaseMessaging.getInstance()
            .subscribeToTopic("pushNotifications")

        getSharedPreferences("MAIN", Context.MODE_PRIVATE).edit().putString("token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = remoteMessage.notification ?: return
        val title = notification.title ?: return
        showNotification(title)
    }

    private fun showNotification(title: String) {
        val nManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.notificationFragment)
            .createPendingIntent()

        val nBuilder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
        nBuilder.apply {
            setSmallIcon(R.drawable.app_logo)
            color = ContextCompat.getColor(this@NotificationService, R.color.theme_blue)
            setContentTitle(title)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
        }

        nManager.notify(999, nBuilder.build())
    }
}