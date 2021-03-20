package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import javax.inject.Inject

class StatisticsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData(loadTitle())
    }

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData(loadRanges())
    }

    private var rangeLength: RangeLength = RangeLength.DAY

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(position.value.orZero() + 1)
    }

    fun onRangeClick(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updatePosition(0)
            }
            is RangeViewData -> {
                rangeLength = item.range
                updatePosition(0)
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        when (tag) {
            DATE_TAG -> {
                timestamp
                    .let { timeMapper.toTimestampShift(it, getMapperRange() ?: return) }
                    .toInt()
                    .let(::updatePosition)
            }
        }
    }

    private fun onSelectDateClick() {
        val current = timeMapper.toTimestampShifted(
            position.value.orZero(),
            getMapperRange() ?: return
        )

        router.navigate(
            Screen.DATE_TIME_DIALOG,
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = current
            )
        )
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
        (rangeItems as MutableLiveData).value = loadRanges()
    }

    private fun loadTitle(): String {
        return rangeMapper.mapToTitle(rangeLength, position.value.orZero())
    }

    private fun loadRanges(): RangesViewData {
        return rangeMapper.mapToRanges(rangeLength)
    }

    private fun getMapperRange(): TimeMapper.Range? {
        return when (rangeLength) {
            RangeLength.DAY -> TimeMapper.Range.DAY
            RangeLength.WEEK -> TimeMapper.Range.WEEK
            RangeLength.MONTH -> TimeMapper.Range.MONTH
            RangeLength.YEAR -> TimeMapper.Range.YEAR
            else -> null
        }
    }

    companion object {
        private const val DATE_TAG = "statistics_date_tag"
    }
}
