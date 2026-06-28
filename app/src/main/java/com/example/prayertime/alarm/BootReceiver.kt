package com.example.prayertime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayertime.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = SettingsRepository(context)
                val settings = repo.settingsFlow.first()
                val alarms = repo.alarmsFlow.first()
                PrayerAlarmPlanner(context).scheduleAll(settings, alarms)
            } finally {
                pending.finish()
            }
        }
    }
}
