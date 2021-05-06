package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData(loadTitle())
    }
    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    fun onRecordAddClick() {
        router.navigate(
            Screen.CHANGE_RECORD_FROM_MAIN,
            ChangeRecordParams.New(daysFromToday = position.value.orZero())
        )
    }

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
            val current = timeMapper.toTimestampShifted(
                rangesFromToday = position.value.orZero(),
                range = RangeLength.DAY
            )

            router.navigate(
                Screen.DATE_TIME_DIALOG,
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

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        when (tag) {
            DATE_TAG -> {
                timestamp
                    .let{ timeMapper.toTimestampShift(it, RangeLength.DAY) }
                    .toInt()
                    .let(::updatePosition)
            }
        }
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
    }

    private fun loadTitle(): String {
        return timeMapper.toDayTitle(position.value.orZero())
    }

    companion object {
        private const val DATE_TAG = "records_date_tag"
    }
}
