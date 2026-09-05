package com.example.util.simpletimetracker.domain.activityReminder.interactor

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import javax.inject.Inject

class ActivityReminderOverrideInteractor @Inject constructor(
    private val repo: ActivityReminderOverrideRepo,
) {

    suspend fun getAll(): List<ActivityReminderOverride> {
        return repo.getAll()
    }

    suspend fun get(activityId: Long): ActivityReminderOverride? {
        return repo.get(activityId)
    }

    suspend fun save(data: ActivityReminderOverride) {
        repo.save(data)
    }

    suspend fun remove(activityId: Long) {
        repo.remove(activityId)
    }
}
