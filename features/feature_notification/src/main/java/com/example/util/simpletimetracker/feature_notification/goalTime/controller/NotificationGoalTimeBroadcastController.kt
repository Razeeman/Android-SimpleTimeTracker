package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor
) {

    fun onGoalTimeReminder(typeId: Long, goalTimeType: GoalTimeType) {
        notificationGoalTimeInteractor.show(typeId, goalTimeType)
    }

    fun onBootCompleted() {
        // TODO reschedule goal time reminder
    }
}