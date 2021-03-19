package com.example.android.socialnetwork

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.android.socialnetwork.common.Constants
import com.example.android.socialnetwork.notification.NotificationService

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(nManager)
        }
        startService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(nManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.CHANNEL_ID,
            Constants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = Constants.CHANNEL_DESC
        nManager.createNotificationChannel(channel)
    }
}