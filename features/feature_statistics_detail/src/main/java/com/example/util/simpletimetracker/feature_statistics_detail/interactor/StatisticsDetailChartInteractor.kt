package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.extension.getMonthlyDuration
import com.example.util.simpletimetracker.domain.extension.getWeeklyDuration
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataRange
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

class StatisticsDetailChartInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailGetGoalFromFilterInteractor: StatisticsDetailGetGoalFromFilterInteractor,
) {

    fun getEmptyChartViewData(): StatisticsDetailChartCompositeViewData {
        return statisticsDetailViewDataMapper.mapToEmptyChartViewData()
    }

    suspend fun getChartViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        filter: List<RecordsFilter>,
        compare: List<RecordsFilter>,
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailChartCompositeViewData = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val useMonthDayTimeFormat = prefsInteractor.getUseMonthDayTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val (ranges, compositeData) = getRanges(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
        )
        val data = getChartData(
            allRecords = records,
            ranges = ranges,
        )
        val prevData = if (rangeLength != RangeLength.All) {
            val (prevRanges, _) = getRanges(
                currentChartGrouping = currentChartGrouping,
                currentChartLength = currentChartLength,
                rangeLength = rangeLength,
                rangePosition = rangePosition - 1,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                useMonthDayTimeFormat = useMonthDayTimeFormat,
            )
            getChartData(
                allRecords = records,
                ranges = prevRanges,
            )
        } else {
            emptyList()
        }
        val compareData = getChartData(
            allRecords = compareRecords,
            ranges = ranges,
        )

        return@withContext statisticsDetailViewDataMapper.mapToChartViewData(
            data = data,
            prevData = prevData,
            goalValue = getGoalValue(filter, compositeData.appliedChartGrouping),
            compareData = compareData,
            compareGoalValue = getGoalValue(compare, compositeData.appliedChartGrouping),
            showComparison = compare.isNotEmpty(),
            rangeLength = rangeLength,
            availableChartGroupings = compositeData.availableChartGroupings,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            availableChartLengths = compositeData.availableChartLengths,
            appliedChartLength = compositeData.appliedChartLength,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
    }

    private suspend fun getGoalValue(
        filter: List<RecordsFilter>,
        appliedChartGrouping: ChartGrouping,
    ): Long {
        val goals = statisticsDetailGetGoalFromFilterInteractor.execute(filter)

        return when (appliedChartGrouping) {
            ChartGrouping.DAILY -> goals.getDailyDuration().value
            ChartGrouping.WEEKLY -> goals.getWeeklyDuration().value
            ChartGrouping.MONTHLY -> goals.getMonthlyDuration().value
            ChartGrouping.YEARLY -> 0
        } * 1000
    }

    private fun getChartData(
        allRecords: List<RecordBase>,
        ranges: List<ChartBarDataRange>,
    ): List<ChartBarDataDuration> {
        fun mapEmpty(): List<ChartBarDataDuration> {
            return ranges.map { ChartBarDataDuration(legend = it.legend, duration = 0L) }
        }

        val records = rangeMapper.getRecordsFromRange(
            records = allRecords,
            range = Range(
                timeStarted = ranges.first().rangeStart,
                timeEnded = ranges.last().rangeEnd,
            ),
        )

        if (records.isEmpty()) return mapEmpty()

        return ranges
            .map { data ->
                val range = Range(data.rangeStart, data.rangeEnd)
                val duration = rangeMapper.getRecordsFromRange(records, range)
                    .map { record -> rangeMapper.clampToRange(record, range) }
                    .let(rangeMapper::mapToDuration)

                ChartBarDataDuration(
                    legend = data.legend,
                    duration = duration,
                )
            }
    }

    private fun getRanges(
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        useMonthDayTimeFormat: Boolean,
    ): Pair<List<ChartBarDataRange>, CompositeChartData> {
        var customRangeGroupings: List<Pair<ChartGrouping, Int>> = emptyList()

        val availableChartGroupings: List<ChartGrouping> = when (rangeLength) {
            is RangeLength.Day,
            is RangeLength.Week,
            is RangeLength.Last,
            -> listOf(
                ChartGrouping.DAILY,
            )
            is RangeLength.Month,
            -> listOf(
                ChartGrouping.DAILY,
                ChartGrouping.WEEKLY,
            )
            is RangeLength.Year -> listOf(
                ChartGrouping.DAILY,
                ChartGrouping.WEEKLY,
                ChartGrouping.MONTHLY,
            )
            is RangeLength.All -> listOf(
                ChartGrouping.DAILY,
                ChartGrouping.WEEKLY,
                ChartGrouping.MONTHLY,
                ChartGrouping.YEARLY,
            )
            is RangeLength.Custom -> {
                customRangeGroupings = calculateCustomRangeGropings(rangeLength, firstDayOfWeek)
                customRangeGroupings.map { (grouping, _) -> grouping }
            }
        }
        val appliedChartGrouping: ChartGrouping = currentChartGrouping
            .takeIf { it in availableChartGroupings }
            ?: availableChartGroupings.first()
        val availableChartLengths = when (rangeLength) {
            is RangeLength.All -> listOf(ChartLength.TEN, ChartLength.FIFTY, ChartLength.HUNDRED)
            else -> emptyList()
        }

        val startDate = when (rangeLength) {
            is RangeLength.Day,
            is RangeLength.Week,
            is RangeLength.Month,
            is RangeLength.Year,
            is RangeLength.Last,
            -> timeMapper.getRangeStartAndEnd(
                rangeLength, rangePosition, firstDayOfWeek, 0,
            ).timeEnded - 1
            is RangeLength.All -> System.currentTimeMillis()
            is RangeLength.Custom -> rangeLength.range.timeEnded - 1
        }

        val numberOfGroups: Int = when (rangeLength) {
            is RangeLength.Day -> 1
            is RangeLength.Week -> 7
            is RangeLength.Month -> when (appliedChartGrouping) {
                ChartGrouping.DAILY ->
                    timeMapper.getActualMaximum(startDate, Calendar.DAY_OF_MONTH, firstDayOfWeek)
                else ->
                    timeMapper.getActualMaximum(startDate, Calendar.WEEK_OF_MONTH, firstDayOfWeek)
            }
            is RangeLength.Year -> when (appliedChartGrouping) {
                ChartGrouping.DAILY ->
                    timeMapper.getActualMaximum(startDate, Calendar.DAY_OF_YEAR, firstDayOfWeek)
                ChartGrouping.WEEKLY ->
                    timeMapper.getActualMaximum(startDate, Calendar.WEEK_OF_YEAR, firstDayOfWeek)
                else -> 12
            }
            is RangeLength.All -> when (currentChartLength) {
                ChartLength.TEN -> 10
                ChartLength.FIFTY -> 50
                ChartLength.HUNDRED -> 100
            }
            is RangeLength.Custom -> {
                customRangeGroupings.first { it.first == appliedChartGrouping }.second
            }
            is RangeLength.Last -> rangeLength.DAYS
        }

        return when (appliedChartGrouping) {
            ChartGrouping.DAILY -> getDailyGrouping(startDate, numberOfGroups, startOfDayShift, useMonthDayTimeFormat)
            ChartGrouping.WEEKLY -> getWeeklyGrouping(startDate, numberOfGroups, firstDayOfWeek, startOfDayShift)
            ChartGrouping.MONTHLY -> getMonthlyGrouping(startDate, numberOfGroups, startOfDayShift)
            ChartGrouping.YEARLY -> getYearlyGrouping(startDate, numberOfGroups, startOfDayShift)
        } to CompositeChartData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
            availableChartLengths = availableChartLengths,
            appliedChartLength = currentChartLength,
        )
    }

    private fun getDailyGrouping(
        startDate: Long,
        numberOfDays: Int,
        startOfDayShift: Long,
        useMonthDayTimeFormat: Boolean,
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfDays - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = startDate
                setToStartOfDay()
            }
            calendar.add(Calendar.DATE, -shift)

            val legend = timeMapper.formatShortDay(calendar.timeInMillis, useMonthDayTimeFormat)
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart + startOfDayShift,
                rangeEnd = rangeEnd + startOfDayShift,
            )
        }
    }

    private fun getWeeklyGrouping(
        startDate: Long,
        numberOfWeeks: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = timeMapper.toCalendarDayOfWeek(firstDayOfWeek)

        return (numberOfWeeks - 1 downTo 0).map { shift ->
            calendar.apply {
                this.firstDayOfWeek = dayOfWeek
                timeInMillis = startDate
                setToStartOfDay()
            }
            calendar.setWeekToFirstDay()
            calendar.add(Calendar.DATE, -shift * 7)

            val legend = timeMapper.formatShortMonth(calendar.timeInMillis)
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart + startOfDayShift,
                rangeEnd = rangeEnd + startOfDayShift,
            )
        }
    }

    private fun getMonthlyGrouping(
        startDate: Long,
        numberOfMonths: Int,
        startOfDayShift: Long,
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfMonths - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = startDate
                setToStartOfDay()
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -shift)

            val legend = timeMapper.formatShortMonth(calendar.timeInMillis)
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart + startOfDayShift,
                rangeEnd = rangeEnd + startOfDayShift,
            )
        }
    }

    private fun getYearlyGrouping(
        startDate: Long,
        numberOfYears: Int,
        startOfDayShift: Long,
    ): List<ChartBarDataRange> {
        val calendar = Calendar.getInstance()

        return (numberOfYears - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = startDate
                setToStartOfDay()
            }
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.add(Calendar.YEAR, -shift)

            val legend = timeMapper.formatShortYear(calendar.timeInMillis)
            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis

            ChartBarDataRange(
                legend = legend,
                rangeStart = rangeStart + startOfDayShift,
                rangeEnd = rangeEnd + startOfDayShift,
            )
        }
    }

    private fun calculateCustomRangeGropings(
        rangeLength: RangeLength.Custom,
        firstDayOfWeek: DayOfWeek,
    ): List<Pair<ChartGrouping, Int>> {
        val range = rangeLength.range
        val result = mutableListOf<Pair<ChartGrouping, Int>>()
        val allChartGroupings = listOf(
            ChartGrouping.DAILY,
            ChartGrouping.WEEKLY,
            ChartGrouping.MONTHLY,
            ChartGrouping.YEARLY,
        )

        allChartGroupings.forEach { chartGrouping ->
            val currentRangeLength: RangeLength = when (chartGrouping) {
                ChartGrouping.DAILY -> RangeLength.Day
                ChartGrouping.WEEKLY -> RangeLength.Week
                ChartGrouping.MONTHLY -> RangeLength.Month
                ChartGrouping.YEARLY -> RangeLength.Year
            }

            val shift = timeMapper.toTimestampShift(
                fromTime = range.timeStarted,
                toTime = range.timeEnded - 1, // end of day is beginning on next one, shift back.
                range = currentRangeLength,
                firstDayOfWeek = firstDayOfWeek,
            )

            when {
                shift != 0L -> result.add(chartGrouping to abs(shift).toInt() + 1) // compensate one shift.
                chartGrouping == ChartGrouping.DAILY -> result.add(chartGrouping to 1)
            }
        }

        return result
    }

    private data class CompositeChartData(
        val availableChartGroupings: List<ChartGrouping>,
        val appliedChartGrouping: ChartGrouping,
        val availableChartLengths: List<ChartLength>,
        val appliedChartLength: ChartLength,
    )
}