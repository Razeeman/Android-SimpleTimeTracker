package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectLastDaysViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import javax.inject.Inject

class RangeViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
) {

    fun mapToRanges(
        currentRange: RangeLength,
        addSelection: Boolean,
        lastDaysCount: Int,
    ): RangesViewData {
        val selectDateButton = mapToSelectDateName(currentRange)
            ?.takeIf { addSelection }?.let(::listOf) ?: emptyList()
        val selectRangeButton = mapToSelectRange()
            .takeIf { addSelection }?.let(::listOf) ?: emptyList()
        val selectLastDaysButton = mapToSelectLastDays(lastDaysCount)
            .let(::listOf)

        val data = selectDateButton +
            selectRangeButton +
            selectLastDaysButton +
            ranges.mapNotNull(::mapToRangeName)

        val selectedPosition = when (currentRange) {
            is RangeLength.Custom -> data.indexOfFirst { it is SelectRangeViewData }
            is RangeLength.Last -> data.indexOfFirst { it is SelectLastDaysViewData }
            else -> data.indexOfFirst { (it as? RangeViewData)?.range == currentRange }
        }.takeUnless { it == -1 }.orZero()

        return RangesViewData(
            items = data,
            selectedPosition = selectedPosition,
        )
    }

    fun mapToTitle(
        rangeLength: RangeLength,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        return when (rangeLength) {
            is RangeLength.Day -> timeMapper.toDayTitle(position, startOfDayShift)
            is RangeLength.Week -> timeMapper.toWeekTitle(position, startOfDayShift, firstDayOfWeek)
            is RangeLength.Month -> timeMapper.toMonthTitle(position, startOfDayShift)
            is RangeLength.Year -> timeMapper.toYearTitle(position, startOfDayShift)
            is RangeLength.All -> resourceRepo.getString(R.string.range_overall)
            is RangeLength.Custom -> mapToSelectRangeName()
            is RangeLength.Last -> mapToSelectLastDaysName(rangeLength.days)
        }
    }

    fun mapToShareTitle(
        rangeLength: RangeLength,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        return when (rangeLength) {
            is RangeLength.Day -> timeMapper.toDayDateTitle(position, startOfDayShift)
            is RangeLength.Week -> timeMapper.toWeekDateTitle(position, startOfDayShift, firstDayOfWeek)
            is RangeLength.Month -> timeMapper.toMonthDateTitle(position, startOfDayShift)
            is RangeLength.Year -> timeMapper.toYearDateTitle(position, startOfDayShift)
            is RangeLength.All,
            is RangeLength.Custom,
            is RangeLength.Last,
            -> mapToTitle(rangeLength, position, startOfDayShift, firstDayOfWeek)
        }
    }

    private fun mapToRangeName(rangeLength: RangeLength): RangeViewData? {
        val text = when (rangeLength) {
            is RangeLength.Day -> R.string.range_day
            is RangeLength.Week -> R.string.range_week
            is RangeLength.Month -> R.string.range_month
            is RangeLength.Year -> R.string.range_year
            is RangeLength.All -> R.string.range_overall
            // These ranges mapped separately
            is RangeLength.Custom -> return null
            is RangeLength.Last -> return null
        }.let(resourceRepo::getString)

        return RangeViewData(
            range = rangeLength,
            text = text,
        )
    }

    private fun mapToSelectDateName(rangeLength: RangeLength): SelectDateViewData? {
        return when (rangeLength) {
            is RangeLength.Day -> R.string.range_select_day
            is RangeLength.Week -> R.string.range_select_week
            is RangeLength.Month -> R.string.range_select_month
            is RangeLength.Year -> R.string.range_select_year
            else -> null
        }
            ?.let(resourceRepo::getString)
            ?.let(::SelectDateViewData)
    }

    private fun mapToSelectRange(): SelectRangeViewData {
        val text = mapToSelectRangeName()
        return SelectRangeViewData(text)
    }

    private fun mapToSelectRangeName(): String {
        return resourceRepo.getString(R.string.range_custom)
    }

    private fun mapToSelectLastDays(days: Int): SelectLastDaysViewData {
        val text = mapToSelectLastDaysName(days)
        return SelectLastDaysViewData(text)
    }

    private fun mapToSelectLastDaysName(days: Int): String {
        return resourceRepo.getQuantityString(R.plurals.range_last, days, days)
    }

    companion object {
        private val ranges: List<RangeLength> = listOf(
            RangeLength.All,
            RangeLength.Year,
            RangeLength.Month,
            RangeLength.Week,
            RangeLength.Day,
        )
    }
}