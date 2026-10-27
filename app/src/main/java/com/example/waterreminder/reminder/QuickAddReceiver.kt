package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.DrinkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Drank 200 ml" button on the reminder notification. */
class QuickAddReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val amount = intent.getIntExtra(NotificationHelper.EXTRA_AMOUNT, NotificationHelper.QUICK_ADD_ML)
        val app = context.applicationContext as WaterApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.waterRepository.add(DrinkType.WATER, null, amount, System.currentTimeMillis())
                NotificationHelper.cancel(context)
            } finally {
                pending.finish()
            }
        }
    }
}
