package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

interface NotificationGoalTimeInteractor {

    suspend fun checkAndReschedule(typeIds: List<Long> = emptyList())

    suspend fun checkAndRescheduleTags(tagIds: List<Long> = emptyList())

    suspend fun cancel(idData: RecordTypeGoal.IdData)

    fun cancel(goalIds: List<Long>)

    // TODO move to notification module, also check other interactors.
    suspend fun show(goal: RecordTypeGoal)
}