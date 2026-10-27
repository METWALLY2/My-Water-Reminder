package com.example.waterreminder.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "drink_entries", indices = [Index("timestamp")])
data class DrinkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drinkType: String,
    val customName: String? = null,
    val amountMl: Int,
    val timestamp: Long
)

val DrinkEntry.drink: DrinkType get() = DrinkType.fromName(drinkType)

fun DrinkEntry.displayLabel(): String = customName?.takeIf { it.isNotBlank() } ?: drink.label

/** Result row for the "Recents" quick-add chips. */
data class RecentDrink(
    val drinkType: String,
    val customName: String?,
    val amountMl: Int,
    val lastUsed: Long
)

/** daysMask: bit 0 = Monday ... bit 6 = Sunday (DayOfWeek.value - 1). */
@Entity(tableName = "custom_reminders")
data class CustomReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysMask: Int = 127,
    val enabled: Boolean = true
)

fun CustomReminder.hasDay(day: DayOfWeek): Boolean = (daysMask and (1 shl (day.value - 1))) != 0

fun CustomReminder.toggleDay(day: DayOfWeek): CustomReminder =
    copy(daysMask = daysMask xor (1 shl (day.value - 1)))
