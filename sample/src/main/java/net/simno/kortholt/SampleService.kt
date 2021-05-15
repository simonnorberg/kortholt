package net.simno.kortholt

import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SampleService : Service() {

    override fun onCreate() {
        super.onCreate()
        Kortholt.create(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        Kortholt.destroy()
        stopForeground(true)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_LOW)
                .setName("Playback")
                .build()
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, SampleActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kortholt Sample")
            .setContentText("The Kortholt sample is running!")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stat_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val CHANNEL_ID = "1337"

        fun intent(context: Context): Intent = Intent(context, SampleService::class.java)
    }
}
