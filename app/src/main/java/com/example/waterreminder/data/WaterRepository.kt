package com.example.waterreminder.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class WaterRepository(private val dao: DrinkDao) {

    private fun startOf(day: LocalDate): Long =
        day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun entriesForDay(day: LocalDate): Flow<List<DrinkEntry>> =
        dao.observeRange(startOf(day), startOf(day.plusDays(1)))

    fun entriesBetween(start: LocalDate, endExclusive: LocalDate): Flow<List<DrinkEntry>> =
        dao.observeRange(startOf(start), startOf(endExclusive))

    fun recents(limit: Int): Flow<List<RecentDrink>> = dao.observeRecents(limit)

    suspend fun totalForDay(day: LocalDate): Int =
        dao.sumRange(startOf(day), startOf(day.plusDays(1)))

    suspend fun all(): List<DrinkEntry> = dao.getAll()

    suspend fun add(type: DrinkType, customName: String?, amountMl: Int, timestamp: Long) {
        dao.insert(
            DrinkEntry(
                drinkType = type.name,
                customName = customName?.trim()?.takeIf { it.isNotEmpty() },
                amountMl = amountMl,
                timestamp = timestamp
            )
        )
    }

    suspend fun delete(entry: DrinkEntry) = dao.delete(entry)
}
