package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-arms alarms after reboot, app update, clock change or time zone change. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as WaterApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                app.scheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
