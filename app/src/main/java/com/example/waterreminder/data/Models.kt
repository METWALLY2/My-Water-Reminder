package com.example.waterreminder.data

import com.example.waterreminder.util.TargetCalculator
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

enum class DrinkType(val label: String, val emoji: String) {
    WATER("Water", "💧"),
    COFFEE("Coffee", "☕"),
    TEA("Tea", "🍵"),
    JUICE("Juice", "🧃"),
    MILK("Milk", "🥛"),
    SODA("Soda", "🥤"),
    BEER("Beer", "🍺"),
    CUSTOM("Custom", "🍹");

    companion object {
        fun fromName(name: String): DrinkType = entries.firstOrNull { it.name == name } ?: WATER
        val selectable: List<DrinkType> get() = entries.filter { it != CUSTOM }
    }
}

enum class Gender { MALE, FEMALE }

enum class UnitSystem { METRIC, IMPERIAL }

enum class ReminderMode(val label: String) {
    SOUND_VIBRATE("Sound and vibrate"),
    SOUND("Sound only"),
    VIBRATE("Vibrate only"),
    SILENT("Silent")
}

data class UserSettings(
    val gender: Gender = Gender.MALE,
    val weightKg: Int = 65,
    val dailyTargetMl: Int = TargetCalculator.calculate(Gender.MALE, 65),
    val units: UnitSystem = UnitSystem.METRIC,
    val weekStart: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
    val autoReminders: Boolean = true,
    val wakeMinutes: Int = 7 * 60,
    val bedMinutes: Int = 23 * 60,
    val intervalMinutes: Int = 90,
    val reminderMode: ReminderMode = ReminderMode.SOUND_VIBRATE
)
