package com.example.waterreminder.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.ui.components.ChoiceDialog
import com.example.waterreminder.ui.components.TextInputDialog
import com.example.waterreminder.ui.components.ValueRow
import com.example.waterreminder.util.Units
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private enum class SettingsDialog { NONE, TARGET, GENDER, WEIGHT, UNITS, WEEK }

@Composable
fun SettingsScreen(
    onOpenReminders: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(SettingsDialog.NONE) }
    val close = { dialog = SettingsDialog.NONE }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )

        ValueRow(
            title = "Reminders",
            icon = Icons.Filled.Notifications,
            showChevron = true,
            onClick = onOpenReminders
        )
        ValueRow(
            title = "Daily target",
            icon = Icons.Filled.GpsFixed,
            value = Units.formatVolume(s.dailyTargetMl, s.units),
            onClick = { dialog = SettingsDialog.TARGET }
        )
        ValueRow(
            title = "Gender",
            icon = Icons.Filled.Wc,
            value = if (s.gender == Gender.MALE) "Male" else "Female",
            onClick = { dialog = SettingsDialog.GENDER }
        )
        ValueRow(
            title = "Weight",
            icon = Icons.Filled.MonitorWeight,
            value = Units.formatWeight(s.weightKg, s.units),
            onClick = { dialog = SettingsDialog.WEIGHT }
        )
        ValueRow(
            title = "Units",
            icon = Icons.Filled.Straighten,
            value = if (s.units == UnitSystem.METRIC) "kg, ml" else "lb, fl oz",
            onClick = { dialog = SettingsDialog.UNITS }
        )
        ValueRow(
            title = "Start week on",
            icon = Icons.Filled.CalendarViewWeek,
            value = s.weekStart.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            onClick = { dialog = SettingsDialog.WEEK }
        )

        Text(
            text = "Your daily target is calculated from your gender and weight. You can also set it manually.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }

    when (dialog) {
        SettingsDialog.NONE -> Unit

        SettingsDialog.TARGET -> TextInputDialog(
            title = "Daily target (${Units.volumeUnit(s.units)})",
            initial = Units.volumeValue(s.dailyTargetMl, s.units).toString(),
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.let { vm.setDailyTarget(Units.toMl(it, s.units).coerceIn(500, 8000)) }
                close()
            },
            onDismiss = close
        )

        SettingsDialog.GENDER -> ChoiceDialog(
            title = "Gender",
            options = listOf(Gender.MALE to "Male", Gender.FEMALE to "Female"),
            selected = s.gender,
            onSelect = { vm.setGender(it); close() },
            onDismiss = close
        )

        SettingsDialog.WEIGHT -> TextInputDialog(
            title = "Weight (${Units.weightUnit(s.units)})",
            initial = Units.weightValue(s.weightKg, s.units).toString(),
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.let { vm.setWeightKg(Units.toKg(it, s.units).coerceIn(20, 300)) }
                close()
            },
            onDismiss = close
        )

        SettingsDialog.UNITS -> ChoiceDialog(
            title = "Units",
            options = listOf(UnitSystem.METRIC to "kg, ml", UnitSystem.IMPERIAL to "lb, fl oz"),
            selected = s.units,
            onSelect = { vm.setUnits(it); close() },
            onDismiss = close
        )

        SettingsDialog.WEEK -> ChoiceDialog(
            title = "Start week on",
            options = (1..7).map {
                DayOfWeek.of(it) to DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            },
            selected = s.weekStart,
            onSelect = { vm.setWeekStart(it); close() },
            onDismiss = close
        )
    }
}
