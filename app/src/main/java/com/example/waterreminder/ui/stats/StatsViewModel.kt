package com.example.waterreminder.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class StatsMode { WEEKLY, MONTHLY }

data class DayStat(val date: LocalDate, val ml: Int)

data class StatsUiState(
    val mode: StatsMode,
    val start: LocalDate,
    val endInclusive: LocalDate,
    val days: List<DayStat>,
    val totalMl: Int,
    val targetMl: Int,
    val dailyTargetMl: Int,
    val units: UnitSystem,
    val canGoNext: Boolean
)

class StatsViewModel(private val app: WaterApp) : ViewModel() {

    private val mode = MutableStateFlow(StatsMode.WEEKLY)
    private val offset = MutableStateFlow(0) // 0 = current period, -1 = previous, ...

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState?> =
        combine(mode, offset, app.settingsRepository.settings) { m, o, s -> Triple(m, o, s) }
            .flatMapLatest { (m, o, s) ->
                val (start, endExclusive) = period(m, o, s.weekStart, LocalDate.now())
                app.waterRepository.entriesBetween(start, endExclusive)
                    .map { entries -> build(m, o, s, start, endExclusive, entries) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setMode(m: StatsMode) {
        mode.value = m
        offset.value = 0
    }

    fun previous() {
        offset.value -= 1
    }

    fun next() {
        if (offset.value < 0) offset.value += 1
    }

    private fun period(
        m: StatsMode,
        offset: Int,
        weekStart: DayOfWeek,
        today: LocalDate
    ): Pair<LocalDate, LocalDate> =
        if (m == StatsMode.WEEKLY) {
            val start = today.with(TemporalAdjusters.previousOrSame(weekStart)).plusWeeks(offset.toLong())
            start to start.plusDays(7)
        } else {
            val start = today.withDayOfMonth(1).plusMonths(offset.toLong())
            start to start.plusMonths(1)
        }

    private fun build(
        m: StatsMode,
        o: Int,
        s: UserSettings,
        start: LocalDate,
        endExclusive: LocalDate,
        entries: List<DrinkEntry>
    ): StatsUiState {
        val zone = ZoneId.systemDefault()
        val byDay = entries
            .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }

        val days = generateSequence(start) { it.plusDays(1) }
            .takeWhile { it.isBefore(endExclusive) }
            .map { DayStat(it, byDay[it] ?: 0) }
            .toList()

        return StatsUiState(
            mode = m,
            start = start,
            endInclusive = endExclusive.minusDays(1),
            days = days,
            totalMl = days.sumOf { it.ml },
            targetMl = s.dailyTargetMl * days.size,
            dailyTargetMl = s.dailyTargetMl,
            units = s.units,
            canGoNext = o < 0
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { StatsViewModel(waterApp()) }
        }
    }
}
