package com.example.waterreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.Achievements
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.RecentDrink
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class HomeUiState(
    val day: LocalDate = LocalDate.now(),
    val entries: List<DrinkEntry> = emptyList(),
    val totalMl: Int = 0,
    val settings: UserSettings = UserSettings(),
    val recents: List<RecentDrink> = emptyList()
) {
    val isToday: Boolean get() = day == LocalDate.now()
}

class HomeViewModel(private val app: WaterApp) : ViewModel() {

    private val repo = app.waterRepository
    private val day = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState> = day
        .flatMapLatest { d ->
            combine(
                repo.entriesForDay(d),
                app.settingsRepository.settings,
                repo.recents(6)
            ) { entries, settings, recents ->
                HomeUiState(
                    day = d,
                    entries = entries,
                    totalMl = entries.sumOf { it.amountMl },
                    settings = settings,
                    recents = withDefaults(recents)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousDay() {
        day.value = day.value.minusDays(1)
    }

    fun nextDay() {
        if (day.value.isBefore(LocalDate.now())) day.value = day.value.plusDays(1)
    }

    /** Adds a drink to the selected day (current time of day on that date). */
    fun add(type: DrinkType, customName: String?, ml: Int) {
        val timestamp = LocalDateTime.of(day.value, LocalTime.now())
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch { repo.add(type, customName, ml, timestamp) }
    }

    fun delete(entry: DrinkEntry) {
        viewModelScope.launch { repo.delete(entry) }
    }

    suspend fun achievements(): Achievements = withContext(Dispatchers.Default) {
        Achievements.compute(
            entries = repo.all(),
            target = state.value.settings.dailyTargetMl,
            today = LocalDate.now(),
            zone = ZoneId.systemDefault()
        )
    }

    private fun withDefaults(recents: List<RecentDrink>): List<RecentDrink> {
        val defaults = listOf(
            RecentDrink(DrinkType.WATER.name, null, 200, 0L),
            RecentDrink(DrinkType.COFFEE.name, null, 100, 0L)
        )
        return (recents + defaults)
            .distinctBy { Triple(it.drinkType, it.customName, it.amountMl) }
            .take(4)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(waterApp()) }
        }
    }
}
