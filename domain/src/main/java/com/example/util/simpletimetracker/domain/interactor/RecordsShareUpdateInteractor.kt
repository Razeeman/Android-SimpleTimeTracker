package com.example.util.simpletimetracker.domain.interactor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsShareUpdateInteractor @Inject constructor() {

    val shareClicked: SharedFlow<Unit> get() = _shareClicked.asSharedFlow()
    private val _shareClicked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun sendShareClicked() {
        _shareClicked.emit(Unit)
    }
}