package com.example.waterreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Insert
    suspend fun insert(entry: DrinkEntry): Long

    @Delete
    suspend fun delete(entry: DrinkEntry)

    @Query("SELECT * FROM drink_entries WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp DESC")
    fun observeRange(from: Long, to: Long): Flow<List<DrinkEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_entries WHERE timestamp >= :from AND timestamp < :to")
    suspend fun sumRange(from: Long, to: Long): Int

    @Query("SELECT * FROM drink_entries ORDER BY timestamp DESC")
    suspend fun getAll(): List<DrinkEntry>

    @Query(
        "SELECT drinkType, customName, amountMl, MAX(timestamp) AS lastUsed " +
            "FROM drink_entries GROUP BY drinkType, customName, amountMl " +
            "ORDER BY lastUsed DESC LIMIT :limit"
    )
    fun observeRecents(limit: Int): Flow<List<RecentDrink>>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM custom_reminders ORDER BY hour, minute")
    fun observeAll(): Flow<List<CustomReminder>>

    @Query("SELECT * FROM custom_reminders")
    suspend fun getAll(): List<CustomReminder>

    @Insert
    suspend fun insert(reminder: CustomReminder): Long

    @Update
    suspend fun update(reminder: CustomReminder)

    @Delete
    suspend fun delete(reminder: CustomReminder)
}
