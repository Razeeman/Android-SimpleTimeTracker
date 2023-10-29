package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
) {

    fun onGoalTimeReminder(typeId: Long, goalRange: RecordTypeGoal.Range) {
        GlobalScope.launch {
            notificationGoalTimeInteractor.show(typeId, goalRange)
            widgetInteractor.updateSingleWidgets(typeIds = listOf(typeId))
        }
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