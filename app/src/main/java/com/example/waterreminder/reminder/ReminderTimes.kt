package com.example.waterreminder.reminder

import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.data.hasDay
import java.time.ZonedDateTime

/** Pure time math, kept separate from Android APIs so it is easy to reason about and test. */
object ReminderTimes {

    /**
     * Minutes since midnight for each auto reminder of one "schedule day".
     * If bedtime is earlier than wake-up (e.g. wake 12:30, bed 01:00) it belongs to the next
     * calendar day, so values may exceed 1440.
     */
    fun autoMinutes(s: UserSettings): List<Int> {
        val wake = s.wakeMinutes
        val bed = if (s.bedMinutes <= wake) s.bedMinutes + 1440 else s.bedMinutes
        val step = s.intervalMinutes.coerceAtLeast(15)
        val out = ArrayList<Int>()
        var t = wake
        while (t <= bed) {
            out.add(t)
            t += step
        }
        return out
    }

    fun nextAuto(s: UserSettings, now: ZonedDateTime): ZonedDateTime? {
        val minutes = autoMinutes(s)
        val today = now.toLocalDate()
        var best: ZonedDateTime? = null
        // Yesterday's schedule can spill past midnight, tomorrow's covers late evenings.
        for (offset in -1L..1L) {
            val base = today.plusDays(offset).atStartOfDay()
            for (m in minutes) {
                val candidate = base.plusMinutes(m.toLong()).atZone(now.zone)
                if (candidate.isAfter(now) && (best == null || candidate.isBefore(best))) {
                    best = candidate
                }
            }
        }
        return best
    }

    fun nextCustom(r: CustomReminder, now: ZonedDateTime): ZonedDateTime? {
        val today = now.toLocalDate()
        for (offset in 0L..7L) {
            val date = today.plusDays(offset)
            if (!r.hasDay(date.dayOfWeek)) continue
            val candidate = date.atTime(r.hour, r.minute).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        return null
    }
}
