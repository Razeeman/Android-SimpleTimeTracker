package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsUpdateInteractor @Inject constructor() {

    val dateTimeChanged: SharedFlow<Unit> get() = _dateTimeChanged.asSharedFlow()
    private val _dateTimeChanged = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val shareClicked: SharedFlow<Unit> get() = _shareClicked.asSharedFlow()
    private val _shareClicked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val filterClicked: SharedFlow<Unit> get() = _filterClicked.asSharedFlow()
    private val _filterClicked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val rangeChanged: SharedFlow<RangeLength> get() = _rangeChanged.asSharedFlow()
    private val _rangeChanged = MutableSharedFlow<RangeLength>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val optionsVisible: SharedFlow<Boolean> get() = _optionsVisible.asSharedFlow()
    private val _optionsVisible = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun sendDateTimeChanged() {
        _dateTimeChanged.emit(Unit)
    }

    suspend fun sendShareClicked() {
        _shareClicked.emit(Unit)
    }

    suspend fun sendFilterClicked() {
        _filterClicked.emit(Unit)
    }

    suspend fun sendRangeChanged(rangeLength: RangeLength) {
        _rangeChanged.emit(rangeLength)
    }

    suspend fun sendOptionsVisible(isVisible: Boolean) {
        _optionsVisible.emit(isVisible)
    }
}