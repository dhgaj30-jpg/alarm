package com.example.prayertime.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.prayertime.ui.theme.PrayerTheme

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepScreenOnForAlarm()

        setContent {
            PrayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmRingingScreen(
                        alarmTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "المنبه",
                        snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 5),
                        onStop = { stopAlarmAndFinish() },
                        onSnooze2 = { snooze(2) },
                        onSnooze5 = { snooze(5) }
                    )
                }
            }
        }
    }

    private fun keepScreenOnForAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun stopAlarmAndFinish() {
        sendBroadcast(Intent(this, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_STOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID))
        })
        finishAndRemoveTask()
    }

    private fun snooze(minutes: Int) {
        sendBroadcast(Intent(this, AlarmActionReceiver::class.java).apply {
            action = if (minutes <= 2) AlarmActionReceiver.ACTION_SNOOZE_2 else AlarmActionReceiver.ACTION_SNOOZE_5
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, minutes)
        })
        finishAndRemoveTask()
    }
}

@Composable
private fun AlarmRingingScreen(
    alarmTitle: String,
    snoozeMinutes: Int,
    onStop: () -> Unit,
    onSnooze2: () -> Unit,
    onSnooze5: () -> Unit
) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF060E18), Color(0xFF101F33), Color(0xFF13243C)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlarmLogo(modifier = Modifier.size(180.dp))
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12233B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF324B6F))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFD6A74A))
                    Text("حان وقت $alarmTitle", style = MaterialTheme.typography.headlineSmall)
                    Text("حان وقت الصلاة الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("الغفوة الافتراضية: ${snoozeMinutes} دقائق", color = Color(0xFFE9C97B))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6A74A), contentColor = Color(0xFF1A1507))
            ) {
                Icon(Icons.Filled.Alarm, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("إيقاف")
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSnooze2,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2D49), contentColor = Color.White)
                ) {
                    Text("غفوة 2 دقائق")
                }
                Button(
                    onClick = onSnooze5,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2D49), contentColor = Color.White)
                ) {
                    Text("غفوة 5 دقائق")
                }
            }
        }
    }
}

@Composable
private fun AlarmLogo(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1422)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6A74A).copy(alpha = 0.55f))
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gold = Color(0xFFD6A74A)
                val gold2 = Color(0xFFE9C97B)
                val dark = Color(0xFF0B1320)

                drawCircle(color = gold.copy(alpha = 0.10f), radius = w * 0.43f, center = Offset(w / 2f, h / 2f))
                drawCircle(color = gold2, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.20f))

                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.18f, h * 0.30f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.74f, h * 0.30f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                )

                drawCircle(color = gold, radius = w * 0.24f, center = Offset(w * 0.5f, h * 0.56f))
                drawRect(color = gold, topLeft = Offset(w * 0.26f, h * 0.58f), size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.18f))
                drawRoundRect(
                    color = gold,
                    topLeft = Offset(w * 0.18f, h * 0.69f),
                    size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.11f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f)
                )
                drawCircle(color = dark, radius = w * 0.13f, center = Offset(w / 2f, h * 0.67f))
                drawCircle(color = gold2, radius = w * 0.13f, center = Offset(w / 2f, h * 0.67f), style = Stroke(width = 6f))
                drawLine(color = gold2, start = Offset(w / 2f, h * 0.67f), end = Offset(w / 2f, h * 0.60f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(color = gold2, start = Offset(w / 2f, h * 0.67f), end = Offset(w * 0.60f, h * 0.70f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.20f, h * 0.84f),
                    size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.06f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )
            }
        }
    }
}
