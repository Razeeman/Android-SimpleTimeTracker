package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

interface NotificationGoalRangeEndInteractor {

    suspend fun checkAndRescheduleDaily()

    suspend fun schedule(range: RecordTypeGoal.Range)

    fun cancel(range: RecordTypeGoal.Range)
}