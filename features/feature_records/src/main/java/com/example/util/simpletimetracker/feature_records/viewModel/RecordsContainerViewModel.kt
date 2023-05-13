package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
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
            viewModelScope.launch { initial.value = loadTitle(0) }
            initial
        }
    }
    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    fun onVisible() {
        updateTitle(position.value.orZero())
    }

    fun onRecordAddClick() {
        viewModelScope.launch {
            val shift = position.value.orZero()
            val actualShift = if (prefsInteractor.getShowRecordsCalendar()) {
                shift * prefsInteractor.getDaysInCalendar().count
            } else {
                shift
            }
            val params = ChangeRecordParams.New(actualShift)
            router.navigate(ChangeRecordFromMainParams(params))
        }
    }

    fun onPreviousClick() {
        updateData(position.value.orZero() - 1)
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
        updateData(0)
    }

    fun onNextClick() {
        updateData(position.value.orZero() + 1)
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
                    .let(::updateData)
            }
        }
    }

    private fun updateData(newPosition: Int) {
        updatePosition(newPosition)
        updateTitle(newPosition)
    }

    private fun updatePosition(shift: Int) = viewModelScope.launch {
        position.set(shift)
    }

    private fun updateTitle(newPosition: Int) = viewModelScope.launch {
        val data = loadTitle(newPosition)
        title.set(data)
    }

    private suspend fun loadTitle(shift: Int): String {
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
