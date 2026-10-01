/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.records.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.WearRecord
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.features.records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.features.records.screen.RecordsListState
import com.example.util.simpletimetracker.presentation.datePicker.WearDateSelectedInteractor
import com.example.util.simpletimetracker.presentation.datePicker.toStartOfDayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val wearDataRepo: WearDataRepo,
    private val dateSelectedInteractor: WearDateSelectedInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    val state: StateFlow<RecordsListState> get() = _state.asStateFlow()
    private val _state = MutableStateFlow<RecordsListState>(RecordsListState.Loading)

    val effects: SharedFlow<Effect> get() = _effects.asSharedFlow()
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var initialized = false
    private var shift = 0
    private var settings: WearSettings? = null
    private var records: List<WearRecord> = emptyList()

    fun init() {
        if (initialized) return
        initialized = true
        subscribeToUpdates()
        viewModelScope.launch { loadData() }
        viewModelScope.launch { startUpdate() }
    }

    fun onRefresh() = viewModelScope.launch {
        _state.value = RecordsListState.Loading
        loadData()
    }

    fun onTitleClick() = viewModelScope.launch {
        val timestamp = timeMapper.toTimestampShifted(
            rangesFromToday = shift,
            range = RangeLength.Day,
            startOfDayShift = settings?.startOfDayShift.orZero(),
        )
        _effects.emit(Effect.OnOpenDatePicker(timestamp))
    }

    fun onTitleLongClick() {
        changeShift(0)
    }

    fun onPrevClick() {
        changeShift(shift - 1)
    }

    fun onNextClick() {
        changeShift(shift + 1)
    }

    private fun onDateSelected(date: LocalDate) {
        val startOfDayShift = settings?.startOfDayShift.orZero()
        timeMapper.toTimestampShift(
            fromTime = System.currentTimeMillis().shiftTimeStamp(-startOfDayShift),
            toTime = date.toStartOfDayTimestamp(),
            range = RangeLength.Day,
            firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.MONDAY,
        ).toInt().let(::changeShift)
    }

    private fun changeShift(newPosition: Int) = viewModelScope.launch {
        shift = newPosition
        _state.value = recordsViewDataMapper.mapContentLoadingState(
            shift = shift,
            settings = settings,
        )
        loadData()
    }

    private suspend fun loadData() {
        val recordsResult = wearDataRepo.loadRecords(
            forceReload = true,
            shift = shift,
        )
        val settingsResult = wearDataRepo.loadSettings(forceReload = false)
        when {
            settingsResult.isFailure || recordsResult.isFailure -> {
                showError()
            }
            else -> {
                settings = settingsResult.getOrNull()
                records = recordsResult.getOrNull().orEmpty()
                updateData()
            }
        }
    }

    private fun updateData() {
        _state.value = if (records.isEmpty()) {
            recordsViewDataMapper.mapEmptyState(
                shift = shift,
                settings = settings,
            )
        } else {
            recordsViewDataMapper.mapContentState(
                records = records,
                shift = shift,
                settings = settings,
                now = System.currentTimeMillis(),
            )
        }
    }

    private fun showError() {
        _state.value = recordsViewDataMapper.mapErrorState()
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            dateSelectedInteractor.data.collect(::onDateSelected)
        }
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData() }
        }
    }

    private suspend fun startUpdate() {
        while (true) {
            delay(1_000L)
            val needUpdate = records.any { it.type == WearRecord.Type.Running }
            if (needUpdate) updateData()
        }
    }

    sealed interface Effect {
        data class OnOpenDatePicker(val timestamp: Long) : Effect
    }
}
