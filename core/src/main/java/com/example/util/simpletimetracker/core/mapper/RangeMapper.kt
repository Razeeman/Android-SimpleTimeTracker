package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

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
            RangeLength.ALL -> resourceRepo.getString(R.string.range_overall)
        }
    }

    fun getRecordsFromRange(
        records: List<Record>,
        rangeStart: Long,
        rangeEnd: Long
    ): List<Record> {
        return records.filter { it.timeStarted < rangeEnd && it.timeEnded > rangeStart }
    }

    fun clampToRange(
        record: Record,
        rangeStart: Long,
        rangeEnd: Long
    ): Range {
        return Range(
            timeStarted = max(record.timeStarted, rangeStart),
            timeEnded = min(record.timeEnded, rangeEnd)
        )
    }

    fun mapToDuration(ranges: List<Range>): Long {
        return ranges.map { it.timeEnded - it.timeStarted }.sum()
    }

    private fun mapToRangeName(rangeLength: RangeLength): RangeViewData {
        val text = when (rangeLength) {
            RangeLength.DAY -> R.string.range_day
            RangeLength.WEEK -> R.string.range_week
            RangeLength.MONTH -> R.string.range_month
            RangeLength.YEAR -> R.string.range_year
            RangeLength.ALL -> R.string.range_overall
        }.let(resourceRepo::getString)

        return RangeViewData(
            range = rangeLength,
            text = text
        )
    }

    private fun mapToSelectDateName(rangeLength: RangeLength): SelectDateViewData? {
        return when (rangeLength) {
            RangeLength.DAY -> R.string.range_select_day
            RangeLength.WEEK -> R.string.range_select_week
            RangeLength.MONTH -> R.string.range_select_month
            RangeLength.YEAR -> R.string.range_select_year
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