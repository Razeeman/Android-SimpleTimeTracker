package com.example.util.simpletimetracker.domain.scheduledReminder.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.ScheduledReminderNotificationInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.scheduledReminder.repo.ScheduledReminderRepo
import javax.inject.Inject

class ScheduledReminderInteractor @Inject constructor(
    private val repo: ScheduledReminderRepo,
    private val notificationInteractor: ScheduledReminderNotificationInteractor,
) {

    suspend fun getAll(): List<ScheduledReminder> {
        return repo.getAll()
    }

    suspend fun get(id: Long): ScheduledReminder? {
        return repo.get(id)
    }

    suspend fun save(data: ScheduledReminder): Long {
        data.id.takeIf { it != 0L }?.let(notificationInteractor::cancel)
        val saved = repo.add(data)
        if (data.enabled) notificationInteractor.schedule(saved)
        return saved
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        notificationInteractor.cancel(id)
        repo.setEnabled(id, enabled)
        if (enabled) notificationInteractor.schedule(id)
    }

    suspend fun remove(id: Long) {
        notificationInteractor.cancel(id)
        repo.remove(id)
    }

    suspend fun clear() {
        repo.getAll().forEach { notificationInteractor.cancel(it.id) }
        repo.clear()
    }
}
