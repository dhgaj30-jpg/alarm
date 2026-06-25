package com.example.prayertime.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.prayertime.notification.NotificationHelper

class AlarmSoundService : Service() {
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: ""
        val alarmTitle = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "المنبه"
        val ringtoneUri = intent?.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)
        val snoozeMinutes = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 5) ?: 5

        startInForeground(alarmId, alarmTitle, snoozeMinutes)
        playTone(ringtoneUri)

        return START_STICKY
    }

    private fun startInForeground(alarmId: String, alarmTitle: String, snoozeMinutes: Int) {
        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, alarmTitle)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_STOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val snooze2Intent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE_2
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snooze5Intent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE_5
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }

        val notification = NotificationCompat.Builder(this, NotificationHelper.ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان وقت $alarmTitle")
            .setContentText("يفتح المنبّه الآن بملء الشاشة")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "إيقاف",
                PendingIntent.getBroadcast(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            .addAction(
                android.R.drawable.ic_media_rew,
                "غفوة 2",
                PendingIntent.getBroadcast(this, 2, snooze2Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            .addAction(
                android.R.drawable.ic_media_ff,
                "غفوة 5",
                PendingIntent.getBroadcast(this, 3, snooze5Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            .build()

        ServiceCompat.startForeground(
            this,
            1001,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    private fun playTone(uriString: String?) {
        stopTone()

        val uri = uriString?.let(Uri::parse) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        player = MediaPlayer().apply {
            val afd = contentResolver.openAssetFileDescriptor(uri, "r")
            if (afd != null) {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                setDataSource(this@AlarmSoundService, uri)
            }
            isLooping = true
            prepare()
            start()
        }
    }

    private fun stopTone() {
        try {
            player?.stop()
        } catch (_: Throwable) {
        }
        try {
            player?.release()
        } catch (_: Throwable) {
        }
        player = null
    }

    override fun onDestroy() {
        stopTone()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
