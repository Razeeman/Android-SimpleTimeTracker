/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.presentation.datePicker.WearDateSelectedInteractor
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.features.statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.features.statistics.screen.StatisticsListState
import com.example.util.simpletimetracker.presentation.datePicker.toStartOfDayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
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
class StatisticsViewModel @Inject constructor(
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val wearDataRepo: WearDataRepo,
    private val wearDateSelectedInteractor: WearDateSelectedInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    val state: StateFlow<StatisticsListState> get() = _state.asStateFlow()
    private val _state = MutableStateFlow<StatisticsListState>(StatisticsListState.Loading)

    val effects: SharedFlow<Effect> get() = _effects.asSharedFlow()
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false
    private var shift: Int = 0
    private var filterType: ChartFilterType = ChartFilterType.ACTIVITY
    private var rangeLength = RangeLength.Day
    private var settings: WearSettings? = null

    fun init() {
        if (isInitialized) return
        isInitialized = true
        subscribeToUpdates()
        viewModelScope.launch { loadData() }
    }

    fun onRefresh() = viewModelScope.launch {
        _state.value = StatisticsListState.Loading
        loadData()
    }

    fun onTitleClick() = viewModelScope.launch {
        val timestamp = timeMapper.toTimestampShifted(
            rangesFromToday = shift,
            range = rangeLength,
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
            range = rangeLength,
            firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.MONDAY,
        ).toInt().let(::changeShift)
    }

    private fun changeShift(newPosition: Int) = viewModelScope.launch {
        shift = newPosition
        _state.value = statisticsViewDataMapper.mapContentLoadingState(
            rangeLength = rangeLength,
            shift = shift,
            settings = settings,
        )
        loadData()
    }

    private suspend fun loadData() {
        val statistics = wearDataRepo.loadStatistics(
            forceReload = true,
            shift = shift,
            filterType = filterType,
        )
        val settingsResult = wearDataRepo.loadSettings(forceReload = false)

        when {
            statistics.isFailure || settingsResult.isFailure -> {
                showError()
            }
            statistics.getOrNull().isNullOrEmpty() -> {
                _state.value = statisticsViewDataMapper.mapEmptyState(
                    rangeLength = rangeLength,
                    shift = shift,
                    settings = settings,
                )
            }
            else -> {
                settings = settingsResult.getOrNull()
                _state.value = statisticsViewDataMapper.mapContentState(
                    statistics = statistics.getOrNull().orEmpty(),
                    rangeLength = rangeLength,
                    shift = shift,
                    settings = settings,
                )
            }
        }
    }

    private fun showError() {
        _state.value = statisticsViewDataMapper.mapErrorState()
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            wearDateSelectedInteractor.data.collect(::onDateSelected)
        }
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData() }
        }
    }

    sealed interface Effect {
        data class OnOpenDatePicker(val timestamp: Long) : Effect
    }
}