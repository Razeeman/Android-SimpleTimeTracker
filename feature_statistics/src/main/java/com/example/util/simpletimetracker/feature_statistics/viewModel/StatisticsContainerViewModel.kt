package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import javax.inject.Inject

class StatisticsContainerViewModel @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>(loadTitle())
    }

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val rangeLength: LiveData<RangeLength> by lazy {
        return@lazy MutableLiveData(RangeLength.DAY)
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

    fun onRangeClick(range: Int) {
        updateRangeLength(
            when (range) {
                1 -> RangeLength.DAY
                2 -> RangeLength.WEEK
                3 -> RangeLength.MONTH
                else -> RangeLength.ALL
            }
        )
    }

    private fun updateRangeLength(newRangeLength: RangeLength) {
        (rangeLength as MutableLiveData).value = newRangeLength
        updatePosition(0)
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
    }

    private fun loadTitle(): String {
        val position = position.value.orZero()
        return when (rangeLength.value ?: RangeLength.DAY) {
            RangeLength.DAY -> timeMapper.toDayTitle(position)
            RangeLength.WEEK -> timeMapper.toWeekTitle(position)
            RangeLength.MONTH -> timeMapper.toMonthTitle(position)
            RangeLength.ALL -> resourceRepo.getString(R.string.title_overall)
        }
    }
}
