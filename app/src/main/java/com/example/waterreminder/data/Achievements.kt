package com.example.waterreminder.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Achievements(
    val currentStreak: Int,
    val bestStreak: Int,
    val goalDays: Int,
    val totalMl: Long
) {
    companion object {
        fun compute(entries: List<DrinkEntry>, target: Int, today: LocalDate, zone: ZoneId): Achievements {
            val byDay: Map<LocalDate, Int> = entries
                .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
                .mapValues { (_, list) -> list.sumOf { it.amountMl } }

            val goalList = byDay.filterValues { it >= target }.keys.sorted()
            val goalSet = goalList.toHashSet()

            var best = 0
            var run = 0
            var prev: LocalDate? = null
            for (d in goalList) {
                run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
                if (run > best) best = run
                prev = d
            }

            var current = 0
            var cursor = if (today in goalSet) today else today.minusDays(1)
            while (cursor in goalSet) {
                current++
                cursor = cursor.minusDays(1)
            }

            return Achievements(
                currentStreak = current,
                bestStreak = best,
                goalDays = goalList.size,
                totalMl = entries.sumOf { it.amountMl.toLong() }
            )
        }
    }
}
