package com.example.waterreminder.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.Achievements
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.displayLabel
import com.example.waterreminder.data.drink
import com.example.waterreminder.ui.components.SectionLabel
import com.example.waterreminder.util.Fmt
import com.example.waterreminder.util.Units
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var achievements by remember { mutableStateOf<Achievements?>(null) }

    val settings = state.settings
    val units = settings.units
    val progress =
        if (settings.dailyTargetMl > 0) state.totalMl.toFloat() / settings.dailyTargetMl else 0f

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Title + achievements
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.size(48.dp))
                    Text(
                        text = if (state.isToday) "Today" else Fmt.dayTitle(state.day),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = { scope.launch { achievements = vm.achievements() } }) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress circle with day navigation
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = vm::previousDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
                    }
                    WaterCircle(
                        progress = progress,
                        valueText = Units.volumeValue(state.totalMl, units).toString(),
                        targetText = "of ${Units.formatVolume(settings.dailyTargetMl, units)}",
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isToday) {
                        Spacer(Modifier.size(48.dp))
                    } else {
                        IconButton(onClick = vm::nextDay) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
                        }
                    }
                }
            }

            // Recents quick-add chips
            item { SectionLabel("Recents") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recents) { r ->
                        OutlinedButton(
                            onClick = { vm.add(DrinkType.fromName(r.drinkType), r.customName, r.amountMl) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("+ ${Units.volumeValue(r.amountMl, units)}")
                            Spacer(Modifier.width(8.dp))
                            Text(DrinkType.fromName(r.drinkType).emoji)
                        }
                    }
                }
            }

            // Activity timeline
            item { SectionLabel("Activity") }
            if (state.entries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No activity for ${if (state.isToday) "Today" else "this day"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add a record",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.entries, key = { it.id }) { entry ->
                    ActivityRow(entry, units, onDelete = { vm.delete(entry) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add drink")
        }
    }

    if (showSheet) {
        AddDrinkSheet(
            units = units,
            onDismiss = { showSheet = false },
            onAdd = { type, name, ml -> vm.add(type, name, ml) }
        )
    }

    achievements?.let { a ->
        AlertDialog(
            onDismissRequest = { achievements = null },
            title = { Text("Achievements") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔥 Current streak: ${a.currentStreak} days")
                    Text("🏆 Best streak: ${a.bestStreak} days")
                    Text("🎯 Goal reached: ${a.goalDays} days")
                    Text("💧 Total logged: ${Units.formatTotal(a.totalMl, units)}")
                }
            },
            confirmButton = { TextButton(onClick = { achievements = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun ActivityRow(entry: DrinkEntry, units: UnitSystem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(entry.drink.emoji, fontSize = 28.sp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${Units.formatVolume(entry.amountMl, units)} of ${entry.displayLabel()}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = Fmt.clock(entry.timestamp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
