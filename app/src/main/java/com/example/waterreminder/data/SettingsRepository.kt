package com.example.waterreminder.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val GENDER = stringPreferencesKey("gender")
private val WEIGHT = intPreferencesKey("weight_kg")
private val TARGET = intPreferencesKey("daily_target_ml")
private val UNITS = stringPreferencesKey("units")
private val WEEK_START = intPreferencesKey("week_start")
private val AUTO = booleanPreferencesKey("auto_reminders")
private val WAKE = intPreferencesKey("wake_minutes")
private val BED = intPreferencesKey("bed_minutes")
private val INTERVAL = intPreferencesKey("interval_minutes")
private val MODE = stringPreferencesKey("reminder_mode")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsDataStore

    val settings: Flow<UserSettings> = store.data.map { it.toSettings() }

    suspend fun current(): UserSettings = settings.first()

    /** Atomic read-modify-write of the whole settings object. */
    suspend fun update(transform: (UserSettings) -> UserSettings) {
        store.edit { p ->
            val n = transform(p.toSettings())
            p[GENDER] = n.gender.name
            p[WEIGHT] = n.weightKg
            p[TARGET] = n.dailyTargetMl
            p[UNITS] = n.units.name
            p[WEEK_START] = n.weekStart.value
            p[AUTO] = n.autoReminders
            p[WAKE] = n.wakeMinutes
            p[BED] = n.bedMinutes
            p[INTERVAL] = n.intervalMinutes
            p[MODE] = n.reminderMode.name
        }
    }

    private fun Preferences.toSettings(): UserSettings {
        val d = UserSettings()
        return UserSettings(
            gender = this[GENDER]?.let { runCatching { Gender.valueOf(it) }.getOrNull() } ?: d.gender,
            weightKg = this[WEIGHT] ?: d.weightKg,
            dailyTargetMl = this[TARGET] ?: d.dailyTargetMl,
            units = this[UNITS]?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() } ?: d.units,
            weekStart = this[WEEK_START]?.let { runCatching { DayOfWeek.of(it) }.getOrNull() } ?: d.weekStart,
            autoReminders = this[AUTO] ?: d.autoReminders,
            wakeMinutes = this[WAKE] ?: d.wakeMinutes,
            bedMinutes = this[BED] ?: d.bedMinutes,
            intervalMinutes = this[INTERVAL] ?: d.intervalMinutes,
            reminderMode = this[MODE]?.let { runCatching { ReminderMode.valueOf(it) }.getOrNull() } ?: d.reminderMode
        )
    }
}
