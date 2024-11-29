package com.silas.fake.move

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*

object SharedNotifyMessage {
    val notifyMessage = MutableLiveData<String>()
}

class MockLocationService : LifecycleService() {
    private val NOTIFY_ID = 1
    private val CHANNEL_ID = "MockLocationChannel"
    private var locationJob: Job? = null
    private var currentMessage: String? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SharedNotifyMessage.notifyMessage.observe(this) { content ->
            currentMessage = content
            startForegroundService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundService()
        return START_STICKY
    }

    private fun createNotification(message: String): Notification {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mock Location Service")
            .setContentText("Providing mock GPS location\n$message")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                ),
            )
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mock Location Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification: Notification = createNotification(currentMessage ?: "1")
        startForeground(NOTIFY_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        stopSelf()
    }

}
