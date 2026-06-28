package com.example.prayertime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.prayertime.AppSettings
import com.example.prayertime.PrayerAlarmItem
import com.example.prayertime.displayTitle

class AlarmScheduler(private val context: Context) {

    fun scheduleAlarm(alarm: PrayerAlarmItem, triggerAtMillis: Long, settings: AppSettings) {
        scheduleInternal(alarm, triggerAtMillis, settings, variant = 0)
    }

    fun scheduleSnooze(alarm: PrayerAlarmItem, triggerAtMillis: Long, settings: AppSettings) {
        scheduleInternal(alarm, triggerAtMillis, settings, variant = 1)
    }

    fun cancelAlarm(alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(alarmId, variant = 0, alarmTitle = null, triggerAtMillis = 0L, ringtoneUri = null, repeatDaily = false, snoozeMinutes = 0))
        alarmManager.cancel(buildPendingIntent(alarmId, variant = 1, alarmTitle = null, triggerAtMillis = 0L, ringtoneUri = null, repeatDaily = false, snoozeMinutes = 0))
    }

    private fun scheduleInternal(
        alarm: PrayerAlarmItem,
        triggerAtMillis: Long,
        settings: AppSettings,
        variant: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = buildPendingIntent(
            alarmId = alarm.id,
            variant = variant,
            alarmTitle = alarm.displayTitle(),
            triggerAtMillis = triggerAtMillis,
            ringtoneUri = settings.selectedToneUri,
            repeatDaily = alarm.repeatDaily,
            snoozeMinutes = alarm.snoozeMinutes
        )

        val showIntent = PendingIntent.getActivity(
            context,
            requestCode(alarm.id, variant) + 10_000,
            Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarm.id)
                putExtra(EXTRA_ALARM_TITLE, alarm.displayTitle())
                putExtra(EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                operation
            )
            return
        } catch (_: Throwable) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun buildPendingIntent(
        alarmId: String,
        variant: Int,
        alarmTitle: String?,
        triggerAtMillis: Long,
        ringtoneUri: String?,
        repeatDaily: Boolean,
        snoozeMinutes: Int
    ): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            alarmTitle?.let { putExtra(EXTRA_ALARM_TITLE, it) }
            putExtra(EXTRA_TRIGGER_AT_MILLIS, triggerAtMillis)
            ringtoneUri?.let { putExtra(EXTRA_RINGTONE_URI, it) }
            putExtra(EXTRA_REPEAT_DAILY, repeatDaily)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(alarmId, variant),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCode(alarmId: String, variant: Int): Int {
        return alarmId.hashCode() * 31 + variant
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val EXTRA_REPEAT_DAILY = "extra_repeat_daily"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_TRIGGER_AT_MILLIS = "extra_trigger_at_millis"
    }
}
