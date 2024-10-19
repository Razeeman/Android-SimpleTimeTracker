package com.example.util.simpletimetracker.feature_notification.goalTime.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class NotificationGoalTimeBroadcastController @Inject constructor(
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    fun onGoalTimeReminder(
        idData: RecordTypeGoal.IdData,
        goalRange: RecordTypeGoal.Range,
    ) {
        GlobalScope.launch {
            notificationGoalTimeInteractor.show(idData, goalRange)
            if (idData is RecordTypeGoal.IdData.Type) {
                externalViewsInteractor.onGoalTimeReached(idData.value)
            }
        }
    }

    fun onRangeEndReminder() {
        GlobalScope.launch {
            reschedule()
            externalViewsInteractor.onGoalRangeEnd()
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