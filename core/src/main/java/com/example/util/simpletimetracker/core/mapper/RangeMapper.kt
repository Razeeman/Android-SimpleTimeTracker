package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.RangeLength
import java.util.Calendar
import javax.inject.Inject

class RangeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun mapToRanges(currentRange: RangeLength): RangesViewData {
        val selectDateButton = mapToSelectDateName(currentRange)
            ?.let(::listOf) ?: emptyList()
        val data = selectDateButton + ranges.map(::mapToRangeName)
        val selectedPosition = data.indexOfFirst {
            (it as? RangeViewData)?.range == currentRange
        }.takeUnless { it == -1 }.orZero()

        return RangesViewData(
            items = data,
            selectedPosition = selectedPosition
        )
    }

    fun mapToTitle(rangeLength: RangeLength, position: Int): String {
        return when (rangeLength) {
            RangeLength.DAY -> timeMapper.toDayTitle(position)
            RangeLength.WEEK -> timeMapper.toWeekTitle(position)
            RangeLength.MONTH -> timeMapper.toMonthTitle(position)
            RangeLength.YEAR -> timeMapper.toYearTitle(position)
            RangeLength.ALL -> resourceRepo.getString(R.string.title_overall)
        }
    }

    fun getRange(rangeLength: RangeLength?, shift: Int): Pair<Long, Long> {
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (rangeLength) {
            RangeLength.DAY -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }
            RangeLength.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }
            RangeLength.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            }
            RangeLength.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.YEAR, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis
            }
            RangeLength.ALL -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
            else -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
        }

        return rangeStart to rangeEnd
    }

    private fun mapToRangeName(rangeLength: RangeLength): RangeViewData {
        val text =  when (rangeLength) {
            RangeLength.DAY -> R.string.title_today
            RangeLength.WEEK -> R.string.title_this_week
            RangeLength.MONTH -> R.string.title_this_month
            RangeLength.YEAR -> R.string.title_this_year
            RangeLength.ALL -> R.string.title_overall
        }.let(resourceRepo::getString)

        return RangeViewData(
            range = rangeLength,
            text = text
        )
    }

    private fun mapToSelectDateName(rangeLength: RangeLength): SelectDateViewData? {
        return when (rangeLength) {
            RangeLength.DAY -> R.string.title_select_day
            RangeLength.WEEK -> R.string.title_select_week
            RangeLength.MONTH -> R.string.title_select_month
            RangeLength.YEAR -> R.string.title_select_year
            else -> null
        }
            ?.let(resourceRepo::getString)
            ?.let(::SelectDateViewData)
    }

    companion object {
        private val ranges: List<RangeLength> = listOf(
            RangeLength.ALL,
            RangeLength.YEAR,
            RangeLength.MONTH,
            RangeLength.WEEK,
            RangeLength.DAY
        )
    }
}