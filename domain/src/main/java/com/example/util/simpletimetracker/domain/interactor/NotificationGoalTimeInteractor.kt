package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

interface NotificationGoalTimeInteractor {

    suspend fun checkAndReschedule()

    suspend fun checkAndReschedule(typeIds: List<Long>)

    suspend fun checkAndRescheduleCategory(categoryId: Long)

    fun cancel(typeId: Long)

    suspend fun show(
        idData: RecordTypeGoal.IdData,
        goalRange: RecordTypeGoal.Range,
    )
}