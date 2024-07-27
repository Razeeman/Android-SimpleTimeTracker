package com.example.util.simpletimetracker.domain.interactor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsContainerUpdateInteractor @Inject constructor() {

    val showCalendarSwitchUpdated: SharedFlow<Unit> get() = _showCalendarSwitchUpdated.asSharedFlow()
    private val _showCalendarSwitchUpdated = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val showCalendarUpdated: SharedFlow<Unit> get() = _showCalendarUpdated.asSharedFlow()
    private val _showCalendarUpdated = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val calendarDaysUpdated: SharedFlow<Unit> get() = _calendarDaysUpdated.asSharedFlow()
    private val _calendarDaysUpdated = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun sendShowCalendarSwitchUpdated() {
        _showCalendarSwitchUpdated.emit(Unit)
    }

    suspend fun sendShowCalendarUpdated() {
        _showCalendarUpdated.emit(Unit)
    }

    suspend fun sendCalendarDaysUpdated() {
        _calendarDaysUpdated.emit(Unit)
    }
}