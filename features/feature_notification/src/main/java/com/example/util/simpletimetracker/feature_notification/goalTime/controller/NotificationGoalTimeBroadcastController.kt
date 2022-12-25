package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    fun onGoalTimeReminder(typeId: Long, goalTimeType: GoalTimeType) {
        notificationGoalTimeInteractor.show(typeId, goalTimeType)
    }

    fun onRangeEndReminder() {
        GlobalScope.launch {
            notificationGoalTimeInteractor.checkAndReschedule()
        }
    }

    fun onBootCompleted() {
        // TODO reschedule goal time reminder
    }
}