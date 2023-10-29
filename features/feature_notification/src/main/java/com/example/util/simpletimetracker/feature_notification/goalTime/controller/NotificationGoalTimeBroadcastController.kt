package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.WidgetType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
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
        GlobalScope.launch {
            reschedule()
            widgetInteractor.updateWidgets(listOf(WidgetType.RECORD_TYPE))
        }
    }

    fun onBootCompleted() {
        GlobalScope.launch {
            reschedule()
        }
    }

    fun onExactAlarmPermissionStateChanged() {
        GlobalScope.launch {
            reschedule()
        }
    }

    private suspend fun reschedule() {
        notificationGoalTimeInteractor.checkAndReschedule()
    }
}