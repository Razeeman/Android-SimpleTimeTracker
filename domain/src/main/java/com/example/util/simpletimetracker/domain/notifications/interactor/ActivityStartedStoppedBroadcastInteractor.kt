package com.example.util.simpletimetracker.domain.notifications.interactor

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

interface ActivityStartedStoppedBroadcastInteractor {

    suspend fun onActionActivityStarted(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
        timeStarted: Long,
    )

    suspend fun onActivityStopped(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
        timeStarted: Long,
    )

    suspend fun onGoalReached(
        idData: RecordTypeGoal.IdData,
        goalType: RecordTypeGoal.Type?,
    )
}