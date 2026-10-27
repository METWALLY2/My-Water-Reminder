package com.example.waterreminder.ui.reminders

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.ReminderMode
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.data.hasDay
import com.example.waterreminder.data.toggleDay
import com.example.waterreminder.reminder.ReminderTimes
import com.example.waterreminder.ui.components.ChoiceDialog
import com.example.waterreminder.ui.components.TimePickerDialog
import com.example.waterreminder.ui.components.ValueRow
import com.example.waterreminder.util.Fmt
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RemindersScreen(vm: RemindersViewModel = viewModel(factory = RemindersViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Re-check when returning from the system "Alarms & reminders" screen.
    var canExact by remember { mutableStateOf(vm.canScheduleExact()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { canExact = vm.canScheduleExact() }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Reminders") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Settings") })
        }

        if (!canExact) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow \"Alarms & reminders\" so reminders arrive exactly on time.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= 31) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }) { Text("Allow") }
                }
            }
        }

        val s = state
        Box(Modifier.weight(1f)) {
            if (s != null) {
                if (tab == 0) CustomTab(s, vm) else AutoTab(s.settings, vm)
            }
        }
    }
}

// ---------- Custom reminders ----------

@Composable
private fun CustomTab(state: RemindersUiState, vm: RemindersViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomReminder?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            if (state.reminders.isEmpty()) {
                item {
                    Text(
                        text = "No custom reminders yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.reminders, key = { it.id }) { r ->
                ReminderCard(
                    reminder = r,
                    weekStart = state.settings.weekStart,
                    onToggle = { vm.updateReminder(r.copy(enabled = it)) },
                    onDayClick = { vm.updateReminder(r.toggleDay(it)) },
                    onEdit = { editing = r },
                    onDelete = { vm.deleteReminder(r) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add reminder")
        }
    }

    if (showAdd) {
        TimePickerDialog(
            initialMinutes = 9 * 60,
            onConfirm = { vm.addReminder(it); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
    editing?.let { r ->
        TimePickerDialog(
            initialMinutes = r.hour * 60 + r.minute,
            onConfirm = { m ->
                vm.updateReminder(r.copy(hour = m / 60, minute = m % 60))
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: CustomReminder,
    weekStart: DayOfWeek,
    onToggle: (Boolean) -> Unit,
    onDayClick: (DayOfWeek) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Fmt.minutes(reminder.hour * 60 + reminder.minute),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onEdit)
                )
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit time") },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
                Switch(checked = reminder.enabled, onCheckedChange = onToggle)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (0..6).map { weekStart.plus(it.toLong()) }.forEach { day ->
                    DayChip(
                        label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        selected = reminder.hasDay(day),
                        onClick = { onDayClick(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, color = fg, maxLines = 1)
    }
}

// ---------- Auto reminders ----------

private enum class AutoDialog { NONE, WAKE, BED, INTERVAL, MODE }

@Composable
private fun AutoTab(settings: UserSettings, vm: RemindersViewModel) {
    var dialog by remember { mutableStateOf(AutoDialog.NONE) }
    val close = { dialog = AutoDialog.NONE }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settings.autoReminders) "Auto reminders enabled" else "Auto reminders disabled",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.autoReminders,
                    onCheckedChange = { on -> vm.updateSettings { it.copy(autoReminders = on) } }
                )
            }
        }

        ValueRow(
            title = "Wake up time",
            icon = Icons.Filled.Alarm,
            value = Fmt.minutes(settings.wakeMinutes),
            onClick = { dialog = AutoDialog.WAKE }
        )
        ValueRow(
            title = "Bedtime",
            icon = Icons.Filled.Bedtime,
            value = Fmt.minutes(settings.bedMinutes),
            onClick = { dialog = AutoDialog.BED }
        )
        ValueRow(
            title = "Reminder interval",
            value = "${settings.intervalMinutes} min",
            onClick = { dialog = AutoDialog.INTERVAL }
        )
        ValueRow(
            title = "Reminder mode",
            value = settings.reminderMode.label,
            onClick = { dialog = AutoDialog.MODE }
        )

        if (settings.autoReminders) {
            Text(
                text = "Daily schedule: " +
                    ReminderTimes.autoMinutes(settings).joinToString("  ·  ") { Fmt.minutes(it) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }

    when (dialog) {
        AutoDialog.NONE -> Unit

        AutoDialog.WAKE -> TimePickerDialog(
            initialMinutes = settings.wakeMinutes,
            onConfirm = { m -> vm.updateSettings { it.copy(wakeMinutes = m) }; close() },
            onDismiss = close
        )

        AutoDialog.BED -> TimePickerDialog(
            initialMinutes = settings.bedMinutes,
            onConfirm = { m -> vm.updateSettings { it.copy(bedMinutes = m) }; close() },
            onDismiss = close
        )

        AutoDialog.INTERVAL -> ChoiceDialog(
            title = "Reminder interval",
            options = listOf(30, 45, 60, 90, 120, 150, 180).map { it to "$it min" },
            selected = settings.intervalMinutes,
            onSelect = { v -> vm.updateSettings { it.copy(intervalMinutes = v) }; close() },
            onDismiss = close
        )

        AutoDialog.MODE -> ChoiceDialog(
            title = "Reminder mode",
            options = ReminderMode.entries.map { it to it.label },
            selected = settings.reminderMode,
            onSelect = { v -> vm.updateSettings { it.copy(reminderMode = v) }; close() },
            onDismiss = close
        )
    }
}
