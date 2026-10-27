package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import com.example.waterreminder.util.Units
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as WaterApp
        val kind = intent.getStringExtra(EXTRA_KIND)
        val pending = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = app.settingsRepository.current()
                val total = app.waterRepository.totalForDay(LocalDate.now())
                val remaining = settings.dailyTargetMl - total

                // Auto reminders stay quiet once the daily goal is reached; custom ones always fire.
                val show = kind != KIND_AUTO || remaining > 0
                if (show) {
                    val text = when {
                        total == 0 -> "You haven't logged anything yet today. Time for a glass!"
                        remaining > 0 -> "${Units.formatVolume(remaining, settings.units)} left to reach today's goal."
                        else -> "Goal reached. Stay hydrated!"
                    }
                    NotificationHelper.show(context, settings.reminderMode, text)
                }
                app.scheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_KIND = "kind"
        const val EXTRA_ID = "id"
        const val KIND_AUTO = "auto"
        const val KIND_CUSTOM = "custom"
    }
}
