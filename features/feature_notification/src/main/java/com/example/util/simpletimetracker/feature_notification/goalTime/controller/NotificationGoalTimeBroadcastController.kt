package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    fun onGoalTimeReminder(typeId: Long, goalRange: RecordTypeGoal.Range) {
        notificationGoalTimeInteractor.show(typeId, goalRange)
    }

    fun onRangeEndReminder() {
        // TODO reschedule only from this range (day, week, etc)
        reschedule()
    }

    fun onBootCompleted() {
        reschedule()
    }

    fun onExactAlarmPermissionStateChanged() {
        reschedule()
    }

    private fun reschedule() {
        GlobalScope.launch {
            notificationGoalTimeInteractor.checkAndReschedule()
        }
    }
}