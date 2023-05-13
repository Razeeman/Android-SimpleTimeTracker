package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.count
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val recordsViewDataMapper: RecordsViewDataMapper,
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

    fun onRecordAddClick() {
        val params = ChangeRecordParams.New(daysFromToday = position.value.orZero())
        router.navigate(ChangeRecordFromMainParams(params))
    }

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
            val shift = position.value.orZero()
            val actualShift = if (prefsInteractor.getShowRecordsCalendar()) {
                shift * prefsInteractor.getDaysInCalendar().count
            } else {
                shift
            }
            val current = timeMapper.toTimestampShifted(
                rangesFromToday = actualShift,
                range = RangeLength.Day
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
    }

    fun onTodayLongClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(position.value.orZero() + 1)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper
                    .toTimestampShift(
                        toTime = timestamp,
                        range = RangeLength.Day,
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
                    )
                    .toInt()
                    .let { shift ->
                        if (prefsInteractor.getShowRecordsCalendar()) {
                            shift / prefsInteractor.getDaysInCalendar().count
                        } else {
                            shift
                        }
                    }
                    .let(::updatePosition)
            }
        }
    }

    private fun updatePosition(newPosition: Int) = viewModelScope.launch {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
    }

    private suspend fun loadTitle(): String {
        val shift = position.value.orZero()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val calendarDayCount = prefsInteractor.getDaysInCalendar()

        return recordsViewDataMapper.mapTitle(
            shift = shift,
            startOfDayShift = startOfDayShift,
            isCalendarView = isCalendarView,
            calendarDayCount = calendarDayCount.count
        )
    }

    companion object {
        private const val DATE_TAG = "records_date_tag"
    }
}
