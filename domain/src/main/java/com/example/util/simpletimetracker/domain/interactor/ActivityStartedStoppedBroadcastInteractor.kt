package com.example.util.simpletimetracker.domain.interactor

interface ActivityStartedStoppedBroadcastInteractor {

    suspend fun onActionActivityStarted(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    )

    suspend fun onActivityStopped(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    )
}