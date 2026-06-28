package com.example.prayertime.ui

import androidx.compose.ui.text.input.KeyboardOptions
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prayertime.AlarmPreviewInfo
import com.example.prayertime.AlarmType
import com.example.prayertime.AppSettings
import com.example.prayertime.CountryMode
import com.example.prayertime.LocationPreset
import com.example.prayertime.MainViewModel
import com.example.prayertime.OffsetMode
import com.example.prayertime.PrayerAlarmItem
import com.example.prayertime.PrayerName
import com.example.prayertime.PrayerUiState
import com.example.prayertime.defaultLocationPresetFor
import com.example.prayertime.displayTitle
import com.example.prayertime.label
import com.example.prayertime.locationPresetsFor
import com.example.prayertime.parseLocationPreset
import com.example.prayertime.summary
import com.example.prayertime.ui.theme.PrayerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val AppBackground = Brush.verticalGradient(
    listOf(Color(0xFF06101C), Color(0xFF0B1728), Color(0xFF101F33))
)

private val CardBrush = Brush.linearGradient(
    listOf(Color(0xFF14253D), Color(0xFF101D30))
)

private val Gold = Color(0xFFD6A74A)
private val GoldSoft = Color(0xFFE9C97B)
private val CardEdge = Color(0xFF2C4261)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerAppScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableIntStateOf(0) }
    var showAddAlarm by rememberSaveable { mutableStateOf(false) }
    var editAlarm by remember { mutableStateOf<PrayerAlarmItem?>(null) }
    val context = LocalContext.current

    val tonePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.chooseTone(it.toString()) }
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("منبّه الصلاة الذكي", fontWeight = FontWeight.Bold)
                        Text(
                            "مواقيت الصلاة والتنبيه والغفوة",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF07111D),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF091523)) {
                NavigationBarItem(
                    selected = screen == 0,
                    onClick = { screen = 0 },
                    icon = { Icon(Icons.Filled.Alarm, contentDescription = null) },
                    label = { Text("المنبّهات") }
                )
                NavigationBarItem(
                    selected = screen == 1,
                    onClick = { screen = 1 },
                    icon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
                    label = { Text("أوقات الصلاة") }
                )
                NavigationBarItem(
                    selected = screen == 2,
                    onClick = { screen = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("الإعدادات") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.statusMessage.isNotBlank()) {
                    StatusBanner(text = state.statusMessage)
                }

                when (screen) {
                    0 -> HomeScreen(
                        state = state,
                        onRefresh = viewModel::refreshPrayerTimes,
                        onAddAlarm = { showAddAlarm = true },
                        onEditAlarm = { editAlarm = it },
                        onToggleAlarm = { alarm, enabled -> viewModel.setAlarmEnabled(alarm, enabled) },
                        onDeleteAlarm = viewModel::deleteAlarm
                    )
                    1 -> PrayerTimesScreen(
                        state = state,
                        onRefresh = viewModel::refreshPrayerTimes
                    )
                    2 -> SettingsScreen(
                        state = state,
                        onCountryChange = { country ->
                            viewModel.updateSettings { current ->
                                val nextPreset = when (country) {
                                    CountryMode.AUTO -> null
                                    CountryMode.JORDAN -> current.locationPreset?.let { parseLocationPreset(it) }?.takeIf { it.countryMode == CountryMode.JORDAN }?.name
                                        ?: defaultLocationPresetFor(country)?.name
                                    CountryMode.SYRIA -> current.locationPreset?.let { parseLocationPreset(it) }?.takeIf { it.countryMode == CountryMode.SYRIA }?.name
                                        ?: defaultLocationPresetFor(country)?.name
                                }
                                current.copy(countryMode = country, locationPreset = nextPreset)
                            }
                        },
                        onPresetChange = { preset ->
                            viewModel.updateSettings { it.copy(locationPreset = preset?.name) }
                        },
                        onPickTone = { tonePicker.launch("audio/*") },
                        onFetchLocation = { viewModel.fetchAutoLocation() },
                        onRequestExactAlarmAccess = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                exactAlarmLauncher.launch(intent)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddAlarm) {
        AlarmEditorDialog(
            title = "إضافة منبه",
            initial = null,
            onDismiss = { showAddAlarm = false },
            onSave = {
                viewModel.addAlarm(it)
                showAddAlarm = false
            }
        )
    }

    editAlarm?.let { alarm ->
        AlarmEditorDialog(
            title = "تعديل منبه",
            initial = alarm,
            onDismiss = { editAlarm = null },
            onSave = {
                viewModel.updateAlarm(it)
                editAlarm = null
            }
        )
    }
}

@Composable
private fun StatusBanner(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16253B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2F4563))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = GoldSoft)
            Spacer(Modifier.width(10.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PrayerTimesScreen(
    state: PrayerUiState,
    onRefresh: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        item {
            SectionHeader(
                title = "أوقات الصلاة",
                action = {
                    TextButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("تحديث")
                    }
                }
            )
        }

        item {
            PrayerTimesSummaryCard(state = state)
        }

        item {
            EmptyCard(
                title = "المنبّه القادم",
                subtitle = state.schedule.nextAlarm
            )
        }
    }
}

@Composable
private fun HomeScreen(
    state: PrayerUiState,
    onRefresh: () -> Unit,
    onAddAlarm: () -> Unit,
    onEditAlarm: (PrayerAlarmItem) -> Unit,
    onToggleAlarm: (PrayerAlarmItem, Boolean) -> Unit,
    onDeleteAlarm: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        item {
            HeroCard(state = state, onRefresh = onRefresh)
        }

        item {
            SectionHeader(
                title = "أوقات الصلاة اليوم",
                action = {
                    TextButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("تحديث")
                    }
                }
            )
        }

        item {
            PrayerTimesSummaryCard(state = state)
        }

        item {
            SectionHeader(
                title = "المنبّهات",
                action = {
                    TextButton(onClick = onAddAlarm) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة")
                    }
                }
            )
        }

        if (state.alarms.isEmpty()) {
            item {
                EmptyCard(
                    title = "لا توجد منبّهات بعد",
                    subtitle = "أضف منبّهًا عاديًا أو مرتبطًا بصلاة، وسيظهر هنا مع الغفوة والتنبيه."
                )
            }
        } else {
            items(state.alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    preview = state.alarmPreviews[alarm.id],
                    onToggle = { onToggleAlarm(alarm, it) },
                    onEdit = { onEditAlarm(alarm) },
                    onDelete = { onDeleteAlarm(alarm.id) }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(state: PrayerUiState, onRefresh: () -> Unit) {
    val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar")))
    val locationText = currentLocationText(state.settings)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBrush)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MosqueBadge(modifier = Modifier.size(124.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("منبّه الصلاة الذكي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("تطبيق منبّه بمواقيت ذكية وإشعارات صوتية", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        color = Color(0xFF1B2D46),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C4261))
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("أوقات الصلاة", fontWeight = FontWeight.SemiBold, color = GoldSoft)
                            Text(dateText)
                            Text(locationText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = onRefresh,
                        colors = buttonColors(containerColor = Gold, contentColor = Color(0xFF1B1506))
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("تحديث الآن")
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0A1525),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.55f))
    ) {
        Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gold = Gold
                val gold2 = GoldSoft
                val dark = Color(0xFF0B1320)

                // background glow
                drawCircle(color = gold.copy(alpha = 0.10f), radius = w * 0.42f, center = Offset(w / 2f, h / 2f))

                // moon
                drawCircle(color = gold2, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.20f))

                // minarets
                val minaretW = w * 0.08f
                val minaretH = h * 0.42f
                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.18f, h * 0.30f),
                    size = androidx.compose.ui.geometry.Size(minaretW, minaretH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(minaretW / 2f, minaretW / 2f)
                )
                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.74f, h * 0.30f),
                    size = androidx.compose.ui.geometry.Size(minaretW, minaretH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(minaretW / 2f, minaretW / 2f)
                )
                drawCircle(color = gold2, radius = w * 0.035f, center = Offset(w * 0.22f, h * 0.29f))
                drawCircle(color = gold2, radius = w * 0.035f, center = Offset(w * 0.78f, h * 0.29f))

                // dome
                drawCircle(
                    color = gold,
                    radius = w * 0.24f,
                    center = Offset(w * 0.5f, h * 0.56f)
                )
                drawRect(
                    color = gold,
                    topLeft = Offset(w * 0.26f, h * 0.58f),
                    size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.18f)
                )
                drawRoundRect(
                    color = gold,
                    topLeft = Offset(w * 0.18f, h * 0.69f),
                    size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.11f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f)
                )

                // central clock
                drawCircle(color = dark, radius = w * 0.13f, center = Offset(w / 2f, h * 0.67f))
                drawCircle(color = gold2, radius = w * 0.13f, center = Offset(w / 2f, h * 0.67f), style = Stroke(width = 6f))
                drawLine(color = gold2, start = Offset(w / 2f, h * 0.67f), end = Offset(w / 2f, h * 0.60f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(color = gold2, start = Offset(w / 2f, h * 0.67f), end = Offset(w * 0.60f, h * 0.70f), strokeWidth = 5f, cap = StrokeCap.Round)

                // base line
                drawRoundRect(
                    color = gold2,
                    topLeft = Offset(w * 0.20f, h * 0.84f),
                    size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.06f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )

                // small side marks
                drawCircle(color = gold2, radius = w * 0.020f, center = Offset(w * 0.12f, h * 0.77f))
                drawCircle(color = gold2, radius = w * 0.020f, center = Offset(w * 0.88f, h * 0.77f))
            }
        }
    }
}

@Composable
private fun PrayerTimesSummaryCard(state: PrayerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("مواعيد اليوم", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("المنبّه القادم: ${state.schedule.nextAlarm}", color = GoldSoft)

            listOf(
                "الفجر" to state.schedule.fajr,
                "الظهر" to state.schedule.dhuhr,
                "العصر" to state.schedule.asr,
                "المغرب" to state.schedule.maghrib,
                "العشاء" to state.schedule.isha,
            ).forEachIndexed { index, (name, time) ->
                if (index > 0) HorizontalDivider(color = Color(0xFF29415D))
                PrayerTimeRow(name = name, time = time)
            }
        }
    }
}

@Composable
private fun PrayerTimeRow(name: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF17283E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = GoldSoft)
            }
            Spacer(Modifier.width(10.dp))
            Text(name, fontWeight = FontWeight.Medium)
        }
        Text(time, color = GoldSoft, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AlarmCard(
    alarm: PrayerAlarmItem,
    preview: AlarmPreviewInfo?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF17283E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AccessAlarm, contentDescription = null, tint = GoldSoft)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(alarm.displayTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(alarm.summary(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF17283E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C4261))
                ) {
                    Text(
                        text = "يرن: ${preview?.exactTimeLabel ?: "غير محدد"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF17283E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C4261))
                ) {
                    Text(
                        text = "متبقي: ${preview?.countdownLabel ?: "غير محدد"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = GoldSoft
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit) { Text("تعديل") }
                OutlinedButton(onClick = onDelete) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: PrayerUiState,
    onCountryChange: (CountryMode) -> Unit,
    onPresetChange: (LocationPreset?) -> Unit,
    onPickTone: () -> Unit,
    onFetchLocation: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            SectionHeader(title = "الإعدادات", action = null)
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Filled.LocationOn,
                    title = "الموقع",
                    subtitle = currentLocationText(state.settings)
                )
                Spacer(Modifier.height(8.dp))
                DropdownRow(
                    label = "الدولة / الطريقة",
                    current = state.settings.countryMode.label(),
                    options = CountryMode.entries.map { it.label() to it.name },
                    onSelected = { onCountryChange(CountryMode.valueOf(it)) }
                )
                Spacer(Modifier.height(8.dp))
                if (state.settings.countryMode != CountryMode.AUTO) {
                    val presetOptions = locationPresetsFor(state.settings.countryMode)
                    val currentPreset = parseLocationPreset(state.settings.locationPreset)
                    DropdownRow(
                        label = "المدينة / المنطقة",
                        current = currentPreset?.label() ?: defaultLocationPresetFor(state.settings.countryMode)?.label() ?: "اختر",
                        options = presetOptions.map { it.label() to it.name },
                        onSelected = { onPresetChange(LocationPreset.valueOf(it)) }
                    )
                }
            }
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Filled.GpsFixed,
                    title = "تحديد تلقائي",
                    subtitle = "جلب الموقع الحالي عبر GPS"
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onFetchLocation) { Text("تحديث الموقع عبر GPS") }
            }
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Filled.Notifications,
                    title = "نغمة التنبيه",
                    subtitle = state.settings.selectedToneUri ?: "اختر نغمة من الجهاز"
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onPickTone) { Text("اختيار نغمة المنبه") }
            }
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Filled.Tune,
                    title = "دقة التنبيه",
                    subtitle = "منح صلاحية المنبّه الدقيق في أندرويد 12 فأعلى"
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onRequestExactAlarmAccess) { Text("تحسين دقة المنبه") }
            }
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = Icons.Filled.Schedule,
                    title = "إعادة التشغيل",
                    subtitle = "المنبّهات تعاد جدولة نفسها بعد إعادة تشغيل الهاتف"
                )
            }
        }

        item {
            SettingsCard {
                SettingRow(
                    icon = if (isNightMode()) Icons.Filled.DarkMode else Icons.Filled.WbSunny,
                    title = "الوضع",
                    subtitle = "مظهر داكن ذهبي"
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF17283E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GoldSoft)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBrush)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: (@Composable () -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        action?.invoke()
    }
}

@Composable
private fun AlarmEditorDialog(
    title: String,
    initial: PrayerAlarmItem?,
    onDismiss: () -> Unit,
    onSave: (PrayerAlarmItem) -> Unit
) {
    var alarmTitle by rememberSaveable(initial?.id) { mutableStateOf(initial?.title ?: "") }
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: AlarmType.PRAYER) }
    var hourText by rememberSaveable(initial?.id) { mutableStateOf(initial?.manualHour?.toString() ?: "5") }
    var minuteText by rememberSaveable(initial?.id) { mutableStateOf(initial?.manualMinute?.toString() ?: "0") }
    var prayer by rememberSaveable(initial?.id) { mutableStateOf(initial?.prayerName ?: PrayerName.FAJR) }
    var offsetMode by rememberSaveable(initial?.id) { mutableStateOf(initial?.offsetMode ?: OffsetMode.BEFORE) }
    var offsetMinutes by rememberSaveable(initial?.id) { mutableStateOf((initial?.offsetMinutes ?: 2).toFloat()) }
    var repeatDaily by rememberSaveable(initial?.id) { mutableStateOf(initial?.repeatDaily ?: true) }
    var enabled by rememberSaveable(initial?.id) { mutableStateOf(initial?.enabled ?: true) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0E1B2E),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardEdge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = alarmTitle,
                    onValueChange = { alarmTitle = it },
                    label = { Text("اسم المنبه") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF14253D),
                        unfocusedContainerColor = Color(0xFF14253D),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                DropdownRow(
                    label = "نوع المنبه",
                    current = type.label(),
                    options = AlarmType.entries.map { it.label() to it.name },
                    onSelected = { type = AlarmType.valueOf(it) }
                )

                if (type == AlarmType.MANUAL) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                            label = { Text("الساعة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF14253D),
                                unfocusedContainerColor = Color(0xFF14253D),
                            )
                        )
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                            label = { Text("الدقيقة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF14253D),
                                unfocusedContainerColor = Color(0xFF14253D),
                            )
                        )
                    }
                } else {
                    DropdownRow(
                        label = "الصلاة",
                        current = prayer.label(),
                        options = PrayerName.entries.map { it.label() to it.name },
                        onSelected = { prayer = PrayerName.valueOf(it) }
                    )
                    DropdownRow(
                        label = "قبل / بعد",
                        current = offsetMode.label(),
                        options = OffsetMode.entries.map { it.label() to it.name },
                        onSelected = { offsetMode = OffsetMode.valueOf(it) }
                    )
                    Text("عدد الدقائق: ${offsetMinutes.toInt()}", color = GoldSoft)
                    Slider(
                        value = offsetMinutes,
                        onValueChange = { offsetMinutes = it },
                        valueRange = 1f..60f,
                        steps = 58
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = repeatDaily, onCheckedChange = { repeatDaily = it })
                    Text("تكرار يومي")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                    Text("تفعيل المنبه")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            val safeTitle = alarmTitle.trim()
                            val newAlarm = if (type == AlarmType.MANUAL) {
                                PrayerAlarmItem(
                                    id = initial?.id ?: PrayerAlarmItem().id,
                                    title = safeTitle,
                                    enabled = enabled,
                                    type = AlarmType.MANUAL,
                                    manualHour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 5,
                                    manualMinute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0,
                                    repeatDaily = repeatDaily,
                                    snoozeMinutes = initial?.snoozeMinutes ?: 5
                                )
                            } else {
                                PrayerAlarmItem(
                                    id = initial?.id ?: PrayerAlarmItem().id,
                                    title = safeTitle,
                                    enabled = enabled,
                                    type = AlarmType.PRAYER,
                                    prayerName = prayer,
                                    offsetMode = offsetMode,
                                    offsetMinutes = offsetMinutes.toInt().coerceIn(1, 60),
                                    repeatDaily = repeatDaily,
                                    snoozeMinutes = initial?.snoozeMinutes ?: 5
                                )
                            }
                            onSave(newAlarm)
                        },
                        colors = buttonColors(containerColor = Gold, contentColor = Color(0xFF1B1506))
                    ) { Text("حفظ") }
                }
            }
        }
    }
}

@Composable
private fun DropdownRow(
    label: String,
    current: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(current)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (display, value) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = {
                            expanded = false
                            onSelected(value)
                        }
                    )
                }
            }
        }
    }
}

private fun currentLocationText(settings: AppSettings): String {
    val preset = parseLocationPreset(settings.locationPreset)
    return when {
        preset != null -> "${preset.label()} • ${settings.countryMode.label()}"
        settings.countryMode == CountryMode.AUTO -> "تلقائي GPS"
        else -> settings.countryMode.label()
    }
}

private fun isNightMode(): Boolean = true
