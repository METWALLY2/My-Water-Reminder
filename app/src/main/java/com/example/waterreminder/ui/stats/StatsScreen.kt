package com.example.waterreminder.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.util.Fmt
import com.example.waterreminder.util.Units
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val s = state
    val weekly = s?.mode != StatsMode.MONTHLY

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Stats",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
        TabRow(selectedTabIndex = if (weekly) 0 else 1) {
            Tab(selected = weekly, onClick = { vm.setMode(StatsMode.WEEKLY) }, text = { Text("Weekly") })
            Tab(selected = !weekly, onClick = { vm.setMode(StatsMode.MONTHLY) }, text = { Text("Monthly") })
        }
        if (s != null) {
            StatsContent(s, onPrevious = vm::previous, onNext = vm::next)
        }
    }
}

@Composable
private fun StatsContent(s: StatsUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    val weekly = s.mode == StatsMode.WEEKLY
    val rangeLabel = if (weekly) {
        "${Fmt.shortDate(s.start)} - ${Fmt.shortDate(s.endInclusive)}"
    } else {
        Fmt.monthTitle(s.start)
    }

    Column(Modifier.padding(16.dp)) {
        Text(
            text = Units.formatVolume(s.totalMl, s.units, grouped = true),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "of ${Units.formatVolume(s.targetMl, s.units, grouped = true)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Text(rangeLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNext, enabled = s.canGoNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
        }

        Spacer(Modifier.height(16.dp))
        BarChart(days = s.days, dailyTarget = s.dailyTargetMl, weekly = weekly)
    }
}

/** Bars show each day as a percentage of the daily target (capped at 100%). */
@Composable
private fun BarChart(days: List<DayStat>, dailyTarget: Int, weekly: Boolean) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gap = if (weekly) 8.dp else 2.dp
    val labelFont = if (weekly) 12.sp else 9.sp

    Row(Modifier.fillMaxWidth().height(280.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Canvas(Modifier.fillMaxSize()) {
                    for (i in 0..5) {
                        val y = size.height * (1f - i / 5f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { d ->
                        val fraction =
                            if (dailyTarget > 0) (d.ml.toFloat() / dailyTarget).coerceIn(0f, 1f) else 0f
                        if (fraction > 0f) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEach { d ->
                    val label = when {
                        weekly -> d.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        d.date.dayOfMonth == 1 || d.date.dayOfMonth % 5 == 0 -> d.date.dayOfMonth.toString()
                        else -> ""
                    }
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = labelFont,
                        maxLines = 1,
                        softWrap = false,
                        color = labelColor
                    )
                }
            }
        }

        // Y axis labels (percent of daily target)
        Column(Modifier.fillMaxHeight().padding(start = 6.dp).width(40.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "80%", "60%", "40%", "20%", "0").forEach {
                    Text(it, fontSize = 10.sp, color = labelColor)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
