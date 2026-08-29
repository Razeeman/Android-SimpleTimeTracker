package com.example.util.simpletimetracker.data_local.scheduledReminder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScheduledReminderDao {

    @Query("SELECT * FROM scheduledReminders")
    suspend fun getAll(): List<ScheduledReminderDBO>

    @Query("SELECT * FROM scheduledReminders WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ScheduledReminderDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ScheduledReminderDBO): Long

    @Query("UPDATE scheduledReminders SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM scheduledReminders WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM scheduledReminders")
    suspend fun clear()
}
