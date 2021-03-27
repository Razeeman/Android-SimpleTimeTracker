package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.DailyChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import java.util.Calendar
import javax.inject.Inject

class StatisticsDetailDailyChartInteractor @Inject constructor(
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper
) {

    suspend fun getDailyChartViewData(
        id: Long,
        filter: ChartFilterType,
        rangeLength: RangeLength,
        rangePosition: Int
    ): StatisticsDetailChartViewData {
        // If untracked
        if (id == -1L) {
            return statisticsDetailViewDataMapper.mapToDailyChartViewData(emptyMap(), rangeLength)
        }

        val range = timeMapper.getRangeStartAndEnd(rangeLength, rangePosition)
        val typesIds = when (filter) {
            ChartFilterType.ACTIVITY -> {
                listOf(id)
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getTypes(categoryId = id)
            }
        }
        val data = getDailyDurations(typesIds, range)

        return statisticsDetailViewDataMapper.mapToDailyChartViewData(data, rangeLength)
    }

    private suspend fun getDailyDurations(
        typeIds: List<Long>,
        range: Pair<Long, Long>
    ): Map<DailyChartGrouping, Long> {
        val calendar = Calendar.getInstance()
        val dataDurations: MutableMap<DailyChartGrouping, Long> = mutableMapOf()
        val dataTimesTracked: MutableMap<DailyChartGrouping, Long> = mutableMapOf()

        val records = recordInteractor.getByType(typeIds)
        val ranges = mapToRanges(records, range)
        val totalTracked = ranges.let(rangeMapper::mapToDuration)

        processRecords(calendar, ranges).forEach {
            val day = mapToDailyGrouping(calendar, it)
            val duration = it.timeEnded - it.timeStarted
            dataDurations[day] = dataDurations[day].orZero() + duration
            dataTimesTracked[day] = dataTimesTracked[day].orZero() + 1
        }

        val daysTracked = dataTimesTracked.values.filter { it != 0L }.size

        return dataDurations.mapValues { (_, duration) ->
            when {
                totalTracked != 0L -> duration * 100 / totalTracked
                daysTracked != 0 -> 100L / daysTracked
                else -> 100L
            }
        }
    }

    private fun mapToRanges(records: List<Record>, range: Pair<Long, Long>): List<Range> {
        return if (range.first != 0L && range.second != 0L) {
            rangeMapper.getRecordsFromRange(records, range.first, range.second)
                .map { rangeMapper.clampToRange(it, range.first, range.second) }
        } else {
            records.map { Range(it.timeStarted, it.timeEnded) }
        }
    }

    private fun processRecords(calendar: Calendar, records: List<Range>): List<Range> {
        val processedRecords: MutableList<Range> = mutableListOf()

        records.forEach { record ->
            splitIntoRecords(calendar, record).forEach { processedRecords.add(it) }
        }

        return processedRecords
    }

    private fun mapToDailyGrouping(calendar: Calendar, record: Range): DailyChartGrouping {
        val day = calendar
            .apply { timeInMillis = record.timeStarted }
            .get(Calendar.DAY_OF_WEEK)

        return when (day) {
            Calendar.MONDAY -> DailyChartGrouping.MONDAY
            Calendar.TUESDAY -> DailyChartGrouping.TUESDAY
            Calendar.WEDNESDAY -> DailyChartGrouping.WEDNESDAY
            Calendar.THURSDAY -> DailyChartGrouping.THURSDAY
            Calendar.FRIDAY -> DailyChartGrouping.FRIDAY
            Calendar.SATURDAY -> DailyChartGrouping.SATURDAY
            else -> DailyChartGrouping.SUNDAY
        }
    }

    /**
     * If a record is on several days - split into several records each within separate day range.
     */
    private tailrec fun splitIntoRecords(
        calendar: Calendar,
        record: Range,
        splitRecords: MutableList<Range> = mutableListOf()
    ): List<Range> {
        if (timeMapper.sameDay(record.timeStarted, record.timeEnded)) {
            return splitRecords.also { it.add(record) }
        }

        val adjustedCalendar = calendar.apply {
            timeInMillis = record.timeStarted
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val rangeEnd = adjustedCalendar.apply { add(Calendar.DATE, 1) }.timeInMillis

        val firstRecord = record.copy(
            timeStarted = record.timeStarted,
            timeEnded = rangeEnd
        )
        val secondRecord = record.copy(
            timeStarted = rangeEnd,
            timeEnded = record.timeEnded
        )
        splitRecords.add(firstRecord)

        return splitIntoRecords(calendar, secondRecord, splitRecords)
    }
}