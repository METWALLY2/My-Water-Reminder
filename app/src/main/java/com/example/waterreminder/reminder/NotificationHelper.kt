package com.example.waterreminder.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.waterreminder.MainActivity
import com.example.waterreminder.R
import com.example.waterreminder.data.ReminderMode

object NotificationHelper {
    private const val CH_SOUND_VIBRATE = "reminders_sound_vibrate"
    private const val CH_SOUND = "reminders_sound"
    private const val CH_VIBRATE = "reminders_vibrate"
    private const val CH_SILENT = "reminders_silent"
    private const val NOTIFICATION_ID = 2001

    const val ACTION_QUICK_ADD = "com.example.waterreminder.ACTION_QUICK_ADD"
    const val EXTRA_AMOUNT = "amount_ml"
    const val QUICK_ADD_ML = 200

    /**
     * Sound/vibration are fixed per channel once created, so each reminder mode gets its own
     * channel and [show] picks the right one.
     */
    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)

        fun channel(id: String, name: String, importance: Int, vibrate: Boolean, sound: Boolean) =
            NotificationChannel(id, name, importance).apply {
                description = "Drink water reminders"
                enableVibration(vibrate)
                if (!sound) setSound(null, null)
            }

        nm.createNotificationChannels(
            listOf(
                channel(CH_SOUND_VIBRATE, "Reminders (sound + vibrate)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = true, sound = true),
                channel(CH_SOUND, "Reminders (sound only)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = false, sound = true),
                channel(CH_VIBRATE, "Reminders (vibrate only)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = true, sound = false),
                channel(CH_SILENT, "Reminders (silent)", NotificationManager.IMPORTANCE_LOW, vibrate = false, sound = false)
            )
        )
    }

    private fun channelFor(mode: ReminderMode): String = when (mode) {
        ReminderMode.SOUND_VIBRATE -> CH_SOUND_VIBRATE
        ReminderMode.SOUND -> CH_SOUND
        ReminderMode.VIBRATE -> CH_VIBRATE
        ReminderMode.SILENT -> CH_SILENT
    }

    fun show(context: Context, mode: ReminderMode, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            flags
        )
        val quickAdd = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, QuickAddReceiver::class.java)
                .setAction(ACTION_QUICK_ADD)
                .putExtra(EXTRA_AMOUNT, QUICK_ADD_ML),
            flags
        )

        val notification = NotificationCompat.Builder(context, channelFor(mode))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to drink water 💧")
            .setContentText(text)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, "Drank $QUICK_ADD_ML ml", quickAdd)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }
}
