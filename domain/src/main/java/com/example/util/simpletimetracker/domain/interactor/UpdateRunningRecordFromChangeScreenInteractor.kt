package com.example.util.simpletimetracker.domain.interactor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRunningRecordFromChangeScreenInteractor @Inject constructor() {

    val dataUpdated: SharedFlow<Update> get() = _dataUpdated.asSharedFlow()

    private val _dataUpdated = MutableSharedFlow<Update>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun send(update: Update) {
        _dataUpdated.emit(update)
    }

    data class Update(
        val id: Long,
        val timer: String,
        val timerTotal: String,
        val goalText: String,
        val goalComplete: Boolean,
        val additionalData: AdditionalData?,
    )

    data class AdditionalData(
        val tagName: String,
        val timeStarted: String,
        val comment: String,
    )
}