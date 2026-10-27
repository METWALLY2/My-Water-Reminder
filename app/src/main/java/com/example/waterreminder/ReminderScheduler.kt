package com.example.waterreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.waterreminder.data.ReminderDao
import com.example.waterreminder.data.SettingsRepository
import java.time.ZonedDateTime

/**
 * Keeps exactly one pending alarm per reminder (one for the auto schedule, one per custom
 * reminder). When an alarm fires, [ReminderReceiver] shows the notification and calls
 * [rescheduleAll] to arm the next occurrence.
 */
class ReminderScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val reminderDao: ReminderDao
) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()

    suspend fun rescheduleAll() {
        val settings = settingsRepository.current()
        val now = ZonedDateTime.now()

        val nextAuto = if (settings.autoReminders) ReminderTimes.nextAuto(settings, now) else null
        if (nextAuto != null) {
            schedule(AUTO_CODE, ReminderReceiver.KIND_AUTO, -1L, nextAuto)
        } else {
            cancel(AUTO_CODE)
        }

        for (r in reminderDao.getAll()) {
            val code = CUSTOM_BASE + r.id.toInt()
            val next = if (r.enabled) ReminderTimes.nextCustom(r, now) else null
            if (next != null) schedule(code, ReminderReceiver.KIND_CUSTOM, r.id, next) else cancel(code)
        }
    }

    fun cancelCustom(id: Long) = cancel(CUSTOM_BASE + id.toInt())

    private fun schedule(code: Int, kind: String, id: Long, at: ZonedDateTime) {
        val pi = pendingIntent(code, kind, id)
        val millis = at.toInstant().toEpochMilli()
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    private fun cancel(code: Int) {
        val pi = pendingIntent(code, ReminderReceiver.KIND_CUSTOM, -1L)
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(code: Int, kind: String, id: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_KIND, kind)
            .putExtra(ReminderReceiver.EXTRA_ID, id)
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val AUTO_CODE = 1
        const val CUSTOM_BASE = 1000
    }
}
