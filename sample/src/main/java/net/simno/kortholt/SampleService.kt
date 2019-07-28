package net.simno.kortholt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

class SampleService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val manager = getSystemService<NotificationManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager?.getNotificationChannel(CHANNEL_ID)
                ?: NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
            manager?.createNotificationChannel(channel)
        }
        val intent = Intent(this, SampleActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kortholt Sample")
            .setContentText("The Kortholt sample is running!")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stat_play)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val CHANNEL_ID = "1337"
    }
}
