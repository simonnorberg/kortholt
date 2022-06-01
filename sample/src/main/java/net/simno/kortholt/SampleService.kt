package net.simno.kortholt

import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

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
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_LOW)
            .setName("Playback")
            .build()
        NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val bitmap = ContextCompat.getDrawable(this, R.drawable.ic_notification)?.toBitmap()
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, "The Kortholt sample is running!")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Kortholt Sample")
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
            .build()

        val mediaSession = MediaSessionCompat(this, "SampleService")
        mediaSession.setMetadata(metadata)
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)

        val intent = Intent(this, SampleActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_music_note)
            .setStyle(mediaStyle)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val CHANNEL_ID = "1337"

        fun intent(context: Context): Intent = Intent(context, SampleService::class.java)
    }
}
