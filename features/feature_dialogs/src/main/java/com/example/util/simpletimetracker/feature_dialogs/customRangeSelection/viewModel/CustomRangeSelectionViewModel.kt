package com.example.util.simpletimetracker.feature_dialogs.customRangeSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.feature_dialogs.customRangeSelection.viewData.CustomRangeSelectionViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CustomRangeSelectionViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    lateinit var extra: CustomRangeSelectionParams

    val viewData: LiveData<CustomRangeSelectionViewData> by lazy {
        return@lazy MutableLiveData<CustomRangeSelectionViewData>().let { initial ->
            viewModelScope.launch {
                initializeViewData()
                initial.value = loadViewData()
            }
            initial
        }
    }
    val customRangeSelectionParams: LiveData<Range> = MutableLiveData()

    private var rangeStart: Long = 0
    private var rangeEnd: Long = 0

    fun onRangeStartClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = rangeStart,
                    type = DateTimeDialogType.DATE,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek,
                ),
            )
        }
    }

    fun onRangeEndClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = rangeEnd,
                    type = DateTimeDialogType.DATE,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek,
                ),
            )
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != rangeStart) {
                        rangeStart = timestamp
                        if (timestamp > rangeEnd) rangeEnd = timestamp
                        updateViewData()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != rangeEnd) {
                        rangeEnd = timestamp
                        if (timestamp < rangeStart) rangeStart = timestamp
                        updateViewData()
                    }
                }
            }
        }
    }

    fun onRangeSelected() {
        val calendar = Calendar.getInstance()
        // Time started is the beginning of selected day.
        val timeStarted = calendar.apply {
            timeInMillis = rangeStart
            setToStartOfDay()
        }.timeInMillis
        // Time ended is the end of selected day, meaning the beginning on the next day.
        val timeEnded = calendar.apply {
            timeInMillis = rangeEnd
            setToStartOfDay()
            add(Calendar.DATE, 1)
        }.timeInMillis

        Range(timeStarted = timeStarted, timeEnded = timeEnded)
            .let(customRangeSelectionParams::set)
    }

    private fun updateViewData() {
        viewData.set(loadViewData())
    }

    private fun initializeViewData() {
        rangeEnd = extra.rangeEnd?.minus(1)
            ?: System.currentTimeMillis()
        rangeStart = extra.rangeStart
            ?: System.currentTimeMillis()
    }

    private fun loadViewData(): CustomRangeSelectionViewData {
        return CustomRangeSelectionViewData(
            rangeStartString = rangeStart
                .let(timeMapper::formatDateYear),
            rangeEndString = rangeEnd
                .let(timeMapper::formatDateYear),
        )
    }

    companion object {
        private const val TIME_STARTED_TAG = "custom_range_selection_time_started_tag"
        private const val TIME_ENDED_TAG = "custom_range_selection_time_ended_tag"
    }
}
