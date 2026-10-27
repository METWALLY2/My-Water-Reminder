package com.example.waterreminder.util

import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object TargetCalculator {
    /** ~33 ml per kg for men, 30 ml per kg for women (65 kg male -> 2166 ml). */
    fun calculate(gender: Gender, weightKg: Int): Int {
        val perKg = if (gender == Gender.MALE) 100.0 / 3.0 else 30.0
        return (weightKg * perKg).toInt()
    }
}

/** All data is stored in ml / kg; these helpers only convert for display and input. */
object Units {
    private const val ML_PER_OZ = 29.5735
    private const val LB_PER_KG = 2.20462

    fun volumeUnit(u: UnitSystem): String = if (u == UnitSystem.METRIC) "ml" else "fl oz"
    fun weightUnit(u: UnitSystem): String = if (u == UnitSystem.METRIC) "kg" else "lb"

    fun volumeValue(ml: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) ml else (ml / ML_PER_OZ).roundToInt()

    fun toMl(value: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) value else (value * ML_PER_OZ).roundToInt()

    fun weightValue(kg: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) kg else (kg * LB_PER_KG).roundToInt()

    fun toKg(value: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) value else (value / LB_PER_KG).roundToInt()

    fun formatVolume(ml: Int, u: UnitSystem, grouped: Boolean = false): String {
        val v = volumeValue(ml, u)
        val number = if (grouped) String.format(Locale.US, "%,d", v) else v.toString()
        return "$number ${volumeUnit(u)}"
    }

    fun formatWeight(kg: Int, u: UnitSystem): String = "${weightValue(kg, u)} ${weightUnit(u)}"

    fun formatTotal(ml: Long, u: UnitSystem): String =
        if (u == UnitSystem.METRIC) String.format(Locale.US, "%.1f L", ml / 1000.0)
        else String.format(Locale.US, "%,d fl oz", (ml / ML_PER_OZ).roundToInt())
}

/** Locale-pinned formatters so digits and names stay consistent with the English UI. */
object Fmt {
    private val time = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val dayTitle = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
    private val shortDate = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val monthTitle = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    fun clock(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(time)

    fun minutes(totalMinutes: Int): String =
        String.format(Locale.US, "%02d:%02d", (totalMinutes / 60) % 24, totalMinutes % 60)

    fun dayTitle(d: LocalDate): String = d.format(dayTitle)
    fun shortDate(d: LocalDate): String = d.format(shortDate)
    fun monthTitle(d: LocalDate): String = d.format(monthTitle)
}
