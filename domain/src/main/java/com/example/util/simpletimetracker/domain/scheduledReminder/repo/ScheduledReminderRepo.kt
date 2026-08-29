package com.example.util.simpletimetracker.domain.scheduledReminder.repo

import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder

interface ScheduledReminderRepo {

    suspend fun getAll(): List<ScheduledReminder>

    suspend fun get(id: Long): ScheduledReminder?

    suspend fun add(data: ScheduledReminder): Long

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun remove(id: Long)

    suspend fun clear()
}
