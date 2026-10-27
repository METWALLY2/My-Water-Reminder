package com.example.waterreminder

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.waterreminder.data.AppDatabase
import com.example.waterreminder.data.ReminderDao
import com.example.waterreminder.data.SettingsRepository
import com.example.waterreminder.data.WaterRepository
import com.example.waterreminder.reminder.NotificationHelper
import com.example.waterreminder.reminder.ReminderScheduler

/** Tiny manual DI container. Everything is lazy so cold start does no unnecessary work. */
class WaterApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val waterRepository: WaterRepository by lazy { WaterRepository(database.drinkDao()) }
    val reminderDao: ReminderDao by lazy { database.reminderDao() }
    val scheduler: ReminderScheduler by lazy { ReminderScheduler(this, settingsRepository, reminderDao) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}

fun CreationExtras.waterApp(): WaterApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WaterApp
