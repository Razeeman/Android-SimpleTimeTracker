package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun onActionActivityStart(name: String) {
        val typeId = getTypeIdByName(name) ?: return
        val runningRecord = runningRecordInteractor.get(typeId)

        if (runningRecord != null) return // Already running.

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            tagIds = emptyList()
        )
    }

    suspend fun onActionActivityStop(name: String) {
        val typeId = getTypeIdByName(name) ?: return
        val runningRecord = runningRecordInteractor.get(typeId)
            ?: return // Not running.

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord
        )
    }

    private suspend fun getTypeIdByName(name: String): Long? {
        return recordTypeInteractor.getAll().firstOrNull { it.name == name }?.id
    }
}