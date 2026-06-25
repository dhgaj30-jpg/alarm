package com.example.prayertime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayertime.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 5)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.stopService(Intent(context, AlarmSoundService::class.java))

                if (action == ACTION_SNOOZE_2 || action == ACTION_SNOOZE_5) {
                    val repo = SettingsRepository(context)
                    val settings = repo.settingsFlow.first()
                    val alarm = repo.findAlarmById(alarmId) ?: return@launch
                    val minutes = if (action == ACTION_SNOOZE_2) 2 else 5
                    val trigger = Instant.now().plus(Duration.ofMinutes(minutes.toLong())).toEpochMilli()
                    AlarmScheduler(context).scheduleSnooze(alarm, trigger, settings)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_STOP = "com.example.prayertime.action.STOP"
        const val ACTION_SNOOZE_2 = "com.example.prayertime.action.SNOOZE_2"
        const val ACTION_SNOOZE_5 = "com.example.prayertime.action.SNOOZE_5"
    }
}
