package com.example.prayertime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFD6A74A)
private val GoldSoft = Color(0xFFE3C06D)
private val Night = Color(0xFF08111E)
private val Night2 = Color(0xFF0E1A2C)
private val Night3 = Color(0xFF13243C)
private val Surface = Color(0xFF13233A)
private val Surface2 = Color(0xFF1A2D49)
private val Mint = Color(0xFF4CC08A)
private val Text = Color(0xFFF8F4EA)

val PrayerDarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1B1506),
    secondary = GoldSoft,
    onSecondary = Color(0xFF1B1506),
    tertiary = Mint,
    onTertiary = Color(0xFF082015),
    background = Night,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = Surface2,
    onSurfaceVariant = Text,
    outline = Color(0xFF8C7647),
    outlineVariant = Color(0xFF2B3E5D),
    inverseOnSurface = Night,
    inverseSurface = Text,
    scrim = Color(0xAA000000)
)

@Composable
fun PrayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PrayerDarkColorScheme,
        content = content
    )
}
