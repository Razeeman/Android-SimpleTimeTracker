package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRunningRecordsInteractor @Inject constructor() {

    val dataUpdated: SharedFlow<Update> get() = _dataUpdated.asSharedFlow()
    private val _dataUpdated = MutableSharedFlow<Update>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val fullUpdate: SharedFlow<Unit> get() = _fullUpdate.asSharedFlow()
    private val _fullUpdate = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun send(update: Update) {
        _dataUpdated.emit(update)
    }

    suspend fun sendFullUpdate() {
        _fullUpdate.emit(Unit)
    }

    data class Update(
        val id: Long,
        val timer: String,
        val timerTotal: String,
        val goalTimes: List<GoalTime>,
        val additionalData: AdditionalData?,
    )

    data class GoalTime(
        val text: String,
        val state: RecordTypeGoal.Subtype?,
    )

    data class AdditionalData(
        val tagName: String,
        val timeStarted: String,
        val comment: String,
    )
}