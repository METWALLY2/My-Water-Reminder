package com.example.waterreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.util.TargetCalculator
import com.example.waterreminder.waterApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class SettingsViewModel(private val app: WaterApp) : ViewModel() {

    val settings: StateFlow<UserSettings> = app.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    private fun update(block: (UserSettings) -> UserSettings) {
        viewModelScope.launch { app.settingsRepository.update(block) }
    }

    // Changing gender or weight recalculates the daily target; the target can also be set by hand.
    fun setGender(g: Gender) = update {
        it.copy(gender = g, dailyTargetMl = TargetCalculator.calculate(g, it.weightKg))
    }

    fun setWeightKg(kg: Int) = update {
        it.copy(weightKg = kg, dailyTargetMl = TargetCalculator.calculate(it.gender, kg))
    }

    fun setDailyTarget(ml: Int) = update { it.copy(dailyTargetMl = ml) }

    fun setUnits(u: UnitSystem) = update { it.copy(units = u) }

    fun setWeekStart(d: DayOfWeek) = update { it.copy(weekStart = d) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(waterApp()) }
        }
    }
}
