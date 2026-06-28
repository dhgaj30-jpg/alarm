package com.example.prayertime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayertime.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return@launch
                val alarmTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "المنبه"
                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, alarmTitle)
                    putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI))
                    putExtra(AlarmScheduler.EXTRA_REPEAT_DAILY, intent.getBooleanExtra(AlarmScheduler.EXTRA_REPEAT_DAILY, true))
                    putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 5))
                }
                context.startForegroundService(serviceIntent)

                val repo = SettingsRepository(context)
                val settings = repo.settingsFlow.first()
                val alarm = repo.findAlarmById(alarmId)
                if (alarm != null) {
                    if (alarm.repeatDaily && alarm.enabled) {
                        PrayerAlarmPlanner(context).previewNextTrigger(settings, alarm)?.let { next ->
                            AlarmScheduler(context).scheduleAlarm(alarm, next.toEpochMilli(), settings)
                        }
                    } else if (!alarm.repeatDaily) {
                        repo.upsertAlarm(alarm.copy(enabled = false))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
