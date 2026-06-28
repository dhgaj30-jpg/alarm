package com.example.prayertime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prayertime.notification.NotificationHelper
import com.example.prayertime.ui.PrayerAppScreen
import com.example.prayertime.ui.theme.PrayerTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.ensureChannels(this)
        viewModel = MainViewModel(application)

        setContent {
            PrayerTheme {
                PrayerAppScreen(viewModel)
            }
        }
    }
}
