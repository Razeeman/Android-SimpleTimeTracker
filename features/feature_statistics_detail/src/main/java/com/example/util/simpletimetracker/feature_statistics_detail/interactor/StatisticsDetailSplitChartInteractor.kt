package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.isNotFiltered
import com.example.util.simpletimetracker.core.interactor.TypesFilterInteractor
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import java.util.Calendar
import javax.inject.Inject

class StatisticsDetailSplitChartInteractor @Inject constructor(
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val typesFilterInteractor: TypesFilterInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper
) {

    suspend fun getSplitChartViewData(
        filter: TypesFilterParams,
        rangeLength: RangeLength,
        rangePosition: Int,
        splitChartGrouping: SplitChartGrouping
    ): StatisticsDetailChartViewData {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val range = timeMapper.getRangeStartAndEnd(rangeLength, rangePosition, firstDayOfWeek)
        val data = getDurations(filter, range, splitChartGrouping)

        return when (splitChartGrouping) {
            SplitChartGrouping.HOURLY ->
                statisticsDetailViewDataMapper.mapToHourlyChartViewData(data)
            SplitChartGrouping.DAILY ->
                statisticsDetailViewDataMapper.mapToDailyChartViewData(data, firstDayOfWeek)
        }
    }

    private suspend fun getDurations(
        filter: TypesFilterParams,
        range: Pair<Long, Long>,
        splitChartGrouping: SplitChartGrouping
    ): Map<Int, Float> {
        val calendar = Calendar.getInstance()
        val dataDurations: MutableMap<Int, Long> = mutableMapOf()
        val dataTimesTracked: MutableMap<Int, Long> = mutableMapOf()

        val typeIds = typesFilterInteractor.getTypeIds(filter)
        val records = recordInteractor.getByType(typeIds).filter { it.isNotFiltered(filter) }
        val ranges = mapToRanges(records, range)
        val totalTracked = ranges.let(rangeMapper::mapToDuration)

        processRecords(calendar, ranges, splitChartGrouping).forEach {
            val grouping = mapToGrouping(calendar, it, splitChartGrouping)
            val duration = it.timeEnded - it.timeStarted
            dataDurations[grouping] = dataDurations[grouping].orZero() + duration
            dataTimesTracked[grouping] = dataTimesTracked[grouping].orZero() + 1
        }

        val daysTracked = dataTimesTracked.values.filter { it != 0L }.size

        return dataDurations.mapValues { (_, duration) ->
            when {
                totalTracked != 0L -> duration * 100f / totalTracked
                daysTracked != 0 -> 100f / daysTracked
                else -> 100f
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

    private fun processRecords(
        calendar: Calendar,
        records: List<Range>,
        splitChartGrouping: SplitChartGrouping
    ): List<Range> {
        val processedRecords: MutableList<Range> = mutableListOf()

        records.forEach { record ->
            splitIntoRecords(calendar, record, splitChartGrouping).forEach { processedRecords.add(it) }
        }

        return processedRecords
    }

    private fun mapToGrouping(
        calendar: Calendar,
        record: Range,
        splitChartGrouping: SplitChartGrouping
    ): Int {
        return calendar
            .apply { timeInMillis = record.timeStarted }
            .let {
                when (splitChartGrouping) {
                    SplitChartGrouping.HOURLY -> it.get(Calendar.HOUR_OF_DAY)
                    SplitChartGrouping.DAILY -> it.get(Calendar.DAY_OF_WEEK)
                }
            }
    }

    // TODO splitting all records hourly probably is super expensive memory wise, find a better way?
    /**
     * If a record is on several days or hours - split into several records each within separate range.
     */
    private tailrec fun splitIntoRecords(
        calendar: Calendar,
        record: Range,
        splitChartGrouping: SplitChartGrouping,
        splitRecords: MutableList<Range> = mutableListOf()
    ): List<Range> {
        val rangeCheck = when (splitChartGrouping) {
            SplitChartGrouping.HOURLY -> timeMapper.sameHour(record.timeStarted, record.timeEnded, calendar)
            SplitChartGrouping.DAILY -> timeMapper.sameDay(record.timeStarted, record.timeEnded, calendar)
        }
        val rangeStep = when (splitChartGrouping) {
            SplitChartGrouping.HOURLY -> Calendar.HOUR_OF_DAY
            SplitChartGrouping.DAILY -> Calendar.DATE
        }

        if (rangeCheck) {
            return splitRecords.also { it.add(record) }
        }

        val adjustedCalendar = calendar.apply {
            timeInMillis = record.timeStarted
            if (splitChartGrouping == SplitChartGrouping.DAILY) set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val rangeEnd = adjustedCalendar.apply { add(rangeStep, 1) }.timeInMillis

        val firstRecord = record.copy(
            timeStarted = record.timeStarted,
            timeEnded = rangeEnd
        )
        val secondRecord = record.copy(
            timeStarted = rangeEnd,
            timeEnded = record.timeEnded
        )
        splitRecords.add(firstRecord)

        return splitIntoRecords(calendar, secondRecord, splitChartGrouping, splitRecords)
    }
}