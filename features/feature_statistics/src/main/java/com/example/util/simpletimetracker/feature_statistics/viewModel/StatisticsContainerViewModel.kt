package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadTitle() }
            initial
        }
    }

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData<RangesViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRanges() }
            initial
        }
    }

    val navButtonsVisibility: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch { initial.value = loadNavButtonsVisibility() }
            initial
        }
    }

    private var rangeLength: RangeLength? = null

    fun onVisible() {
        updateTitle()
    }

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(position.value.orZero() + 1)
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updateRanges()
            }
            is SelectRangeViewData -> {
                onSelectRangeClick()
                updateRanges()
            }
        }
    }

    fun onRangeUpdated(newRange: RangeLength) {
        if (newRange != rangeLength) {
            rangeLength = newRange
            updateNavButtonsVisibility()
            updatePosition(0)
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper.toTimestampShift(
                    toTime = timestamp,
                    range = prefsInteractor.getStatisticsRange(),
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
                ).toInt().let(::updatePosition)
            }
        }
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = position.value.orZero(),
            range = prefsInteractor.getStatisticsRange()
        )

        router.navigate(
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = current,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek
            )
        )
    }

    private fun onSelectRangeClick() = viewModelScope.launch {
        val currentCustomRange = (prefsInteractor.getStatisticsRange() as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    private suspend fun getRangeLength(): RangeLength {
        return rangeLength ?: prefsInteractor.getStatisticsRange()
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        updateTitle()
        updateRanges()
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeViewDataMapper.mapToTitle(
            rangeLength = getRangeLength(),
            position = position.value.orZero(),
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek
        )
    }

    private fun updateRanges() = viewModelScope.launch {
        rangeItems.set(loadRanges())
    }

    private suspend fun loadRanges(): RangesViewData {
        return rangeViewDataMapper.mapToRanges(getRangeLength())
    }

    private fun updateNavButtonsVisibility() = viewModelScope.launch {
        navButtonsVisibility.set(loadNavButtonsVisibility())
    }

    private suspend fun loadNavButtonsVisibility(): Boolean {
        return when (getRangeLength()) {
            is RangeLength.All, is RangeLength.Custom, is RangeLength.Last -> false
            else -> true
        }
    }

    companion object {
        private const val DATE_TAG = "statistics_date_tag"
    }
}
