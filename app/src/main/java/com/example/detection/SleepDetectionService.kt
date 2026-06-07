package com.example.detection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class SleepDetectionService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Solace")
            .setContentText("Solace is monitoring sleep")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Placeholder icon
            .setOngoing(true)
            .build()
        
        // In Android 14+ we should use ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        // For simplicity we just use standard startForeground
        startForeground(NOTIFICATION_ID, notification)
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ch_detection"
        const val NOTIFICATION_ID = 1
    }
}
