package com.silas.fake.move

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MockLocationService : Service() {

    private val CHANNEL_ID = "MockLocationChannel"
    private var locationJob: Job? = null
    private val provider = "gps"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startMockLocationUpdates()
        return START_STICKY
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
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mock Location Service")
            .setContentText("Providing mock GPS location...")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                ),
            )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("WrongConstant")
    private fun startMockLocationUpdates() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                locationManager.addTestProvider(
                    provider,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    3,
                    1
                )
                locationManager.setTestProviderEnabled(provider, true)
                val list = MockLocations.list.map { it }.toList()
                val gapTime = System.currentTimeMillis() - list[0].time
                var index = 0
                var currentLocation = list[index]
                while (isActive) {
                    val item = currentLocation
                    val mockLocation = Location(provider).apply {
                        latitude = item.latitude
                        longitude = item.longitude
                        accuracy = item.accuracy
                        speed = item.speed
                        bearing = item.bearing
                        altitude = item.altitude
                        time = System.currentTimeMillis()
                        elapsedRealtimeNanos = System.nanoTime()
                    }
                    locationManager.setTestProviderLocation(provider, mockLocation)
                    println("send mockLocation:${item}")
                    delay(500)
                    if (System.currentTimeMillis() - gapTime > currentLocation.time) {
                        index++
                    }
                    if (index >= list.size - 1) {
                        break;
                    }
                    currentLocation = list[index]
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            } finally {
                locationManager.removeTestProvider(provider)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
