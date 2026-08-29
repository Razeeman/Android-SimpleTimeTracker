package com.example.util.simpletimetracker.domain.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.scheduledReminder.repo.ScheduledReminderRepo
import javax.inject.Inject

class ScheduledReminderInteractor @Inject constructor(
    private val repo: ScheduledReminderRepo,
) {

    suspend fun getAll(): List<ScheduledReminder> {
        return repo.getAll()
    }

    suspend fun get(id: Long): ScheduledReminder? {
        return repo.get(id)
    }

    suspend fun save(data: ScheduledReminder): Long {
        return repo.add(data)
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        repo.setEnabled(id, enabled)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }

    suspend fun clear() {
        repo.clear()
    }
}
