package com.example.waterreminder.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<CustomReminder>,
    val settings: UserSettings
)

class RemindersViewModel(private val app: WaterApp) : ViewModel() {

    val state: StateFlow<RemindersUiState?> =
        combine(app.reminderDao.observeAll(), app.settingsRepository.settings) { list, s ->
            RemindersUiState(list, s)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun canScheduleExact(): Boolean = app.scheduler.canScheduleExact()

    fun addReminder(minutesOfDay: Int) {
        viewModelScope.launch {
            app.reminderDao.insert(CustomReminder(hour = minutesOfDay / 60, minute = minutesOfDay % 60))
            app.scheduler.rescheduleAll()
        }
    }

    fun updateReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            app.reminderDao.update(reminder)
            app.scheduler.rescheduleAll()
        }
    }

    fun deleteReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            app.scheduler.cancelCustom(reminder.id)
            app.reminderDao.delete(reminder)
        }
    }

    /** Any change to auto-reminder settings re-arms the alarms. */
    fun updateSettings(block: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            app.settingsRepository.update(block)
            app.scheduler.rescheduleAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { RemindersViewModel(waterApp()) }
        }
    }
}
