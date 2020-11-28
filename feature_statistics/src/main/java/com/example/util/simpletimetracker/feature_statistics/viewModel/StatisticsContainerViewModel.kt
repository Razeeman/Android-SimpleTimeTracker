package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import javax.inject.Inject

class StatisticsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val statisticsViewDataMapper: StatisticsViewDataMapper
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData(loadTitle())
    }

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val buttons: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadButtons())
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

    fun onNewRange(newRangeLength: RangeLength) {
        rangeLength = newRangeLength
        updatePosition(0)
    }

    fun onSelectDateClick() {
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

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
        (buttons as MutableLiveData).value = loadButtons()
    }

    private fun loadTitle(): String {
        val position = position.value.orZero()
        return when (rangeLength) {
            RangeLength.DAY -> timeMapper.toDayTitle(position)
            RangeLength.WEEK -> timeMapper.toWeekTitle(position)
            RangeLength.MONTH -> timeMapper.toMonthTitle(position)
            RangeLength.ALL -> resourceRepo.getString(R.string.title_overall)
        }
    }

    private fun loadButtons(): List<ViewHolderType> {
        return statisticsViewDataMapper.mapToButtons(rangeLength)
    }

    private fun getMapperRange(): TimeMapper.Range? {
        return when (rangeLength) {
            RangeLength.DAY -> TimeMapper.Range.DAY
            RangeLength.WEEK -> TimeMapper.Range.WEEK
            RangeLength.MONTH -> TimeMapper.Range.MONTH
            else -> null
        }
    }

    companion object {
        private const val DATE_TAG = "statistics_date_tag"
    }
}
