package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.shift
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsDetailSplitChartInteractor @Inject constructor(
    private val mapper: StatisticsDetailViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
) {

    suspend fun getSplitChartViewData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        isForComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
        splitChartGrouping: SplitChartGrouping,
    ): StatisticsDetailChartViewData = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )
        val data = if (isForComparison && filter.isEmpty()) {
            emptyMap()
        } else {
            getDurations(records, range, splitChartGrouping, startOfDayShift)
        }
        val isVisible = (isForComparison && filter.isNotEmpty()) || !isForComparison

        return@withContext when (splitChartGrouping) {
            SplitChartGrouping.HOURLY ->
                mapper.mapToHourlyChartViewData(data, isVisible)

            SplitChartGrouping.DAILY ->
                mapper.mapToDailyChartViewData(data, firstDayOfWeek, isVisible)
        }
    }

    suspend fun getDurationSplitViewData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        isForComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailChartViewData = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )

        val ranges = if (range.timeStarted != 0L && range.timeEnded != 0L) {
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampToRange(it, range) }
        } else {
            records.map { Range(it.timeStarted, it.timeEnded) }
        }
        val isVisible = (isForComparison && filter.isNotEmpty()) || !isForComparison

        val minDuration = ranges
            .minByOrNull { it.duration }?.duration.orZero()
        val maxDuration = ranges
            .maxByOrNull { it.duration }?.duration.orZero()
        val durationSpread: Long = (maxDuration - minDuration)
        val total: Long = ranges.size.toLong()
        // Roughly 10 buckets.
        val roughStep: Float = durationSpread / 10f
        val steps = listOf<Long>(1, 2, 3, 4, 5, 10, 15, 20, 30)
        val availableSteps = listOf(
            steps.map { TimeUnit.SECONDS.toMillis(it) },
            steps.map { TimeUnit.MINUTES.toMillis(it) },
            steps.map { TimeUnit.HOURS.toMillis(it) },
        ).flatten()
        // Get next lower step.
        val step: Long = availableSteps
            .reversed()
            .firstOrNull { it < roughStep }
            ?: availableSteps.last()

        if (total == 0L || durationSpread == 0L || step == 0L) {
            return@withContext mapper.mapToDurationsSlipChartViewData(emptyMap(), isVisible)
        }

        val buckets: MutableMap<Range, Long> = mutableMapOf()
        val minBucket = nearestDivider(
            isUpper = false, divider = step, value = minDuration
        )
        val maxBucket = nearestDivider(
            isUpper = true, divider = step, value = maxDuration
        )
        (minBucket until maxBucket step step).forEach {
            buckets[Range(it, it + step)] = 0
        }

        var bucketTimeStarted: Long
        var bucketTimeEnded: Long
        var bucketRange: Range
        var nearestLower: Long
        var nearestUpper: Long
        var duration: Long
        ranges.forEach { it ->
            duration = it.duration
            nearestLower = nearestDivider(isUpper = false, divider = step, value = duration)
            nearestUpper = nearestDivider(isUpper = true, divider = step, value = duration)
            bucketTimeStarted = nearestLower
            bucketTimeEnded = nearestUpper
                .takeUnless { it == duration }
                ?: (nearestUpper + step)
            bucketRange = Range(bucketTimeStarted, bucketTimeEnded)
            buckets[bucketRange] = buckets[bucketRange].orZero() + 1
        }
        val data = buckets.mapValues { (_, count) ->
            count * 100f / total
        }

        return@withContext mapper.mapToDurationsSlipChartViewData(data, isVisible)
    }

    private fun getDurations(
        records: List<RecordBase>,
        range: Range,
        splitChartGrouping: SplitChartGrouping,
        startOfDayShift: Long,
    ): Map<Int, Float> {
        val calendar = Calendar.getInstance()
        val dataDurations: MutableMap<Int, Long> = mutableMapOf()
        val dataTimesTracked: MutableMap<Int, Long> = mutableMapOf()

        val ranges = mapToRanges(records, range)
        val totalTracked = ranges.let(rangeMapper::mapToDuration)

        processRecords(calendar, ranges, splitChartGrouping, startOfDayShift).forEach {
            val grouping = mapToGrouping(calendar, it, splitChartGrouping, startOfDayShift)
            dataDurations[grouping] = dataDurations[grouping].orZero() + it.duration
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

    private fun mapToRanges(records: List<RecordBase>, range: Range): List<Range> {
        return if (range.timeStarted != 0L && range.timeEnded != 0L) {
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampToRange(it, range) }
        } else {
            records.map { Range(it.timeStarted, it.timeEnded) }
        }
    }

    private fun processRecords(
        calendar: Calendar,
        records: List<Range>,
        splitChartGrouping: SplitChartGrouping,
        startOfDayShift: Long,
    ): List<Range> {
        val processedRecords: MutableList<Range> = mutableListOf()

        records.forEach { record ->
            splitIntoRecords(calendar, record, splitChartGrouping, startOfDayShift).forEach(processedRecords::add)
        }

        return processedRecords
    }

    private fun mapToGrouping(
        calendar: Calendar,
        record: Range,
        splitChartGrouping: SplitChartGrouping,
        startOfDayShift: Long,
    ): Int {
        return calendar.run {
            timeInMillis = record.timeStarted
            when (splitChartGrouping) {
                SplitChartGrouping.HOURLY -> {
                    get(Calendar.HOUR_OF_DAY)
                }

                SplitChartGrouping.DAILY -> {
                    shift(-startOfDayShift)
                    get(Calendar.DAY_OF_WEEK)
                }
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
        startOfDayShift: Long,
        splitRecords: MutableList<Range> = mutableListOf(),
    ): List<Range> {
        val rangeCheck = when (splitChartGrouping) {
            SplitChartGrouping.HOURLY -> timeMapper.sameHour(
                date1 = record.timeStarted,
                date2 = record.timeEnded,
                calendar = calendar
            )

            SplitChartGrouping.DAILY -> timeMapper.sameDay(
                date1 = calendar.shiftTimeStamp(record.timeStarted, -startOfDayShift),
                date2 = calendar.shiftTimeStamp(record.timeEnded, -startOfDayShift),
                calendar = calendar
            )
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
            if (splitChartGrouping == SplitChartGrouping.DAILY) shift(-startOfDayShift)
            if (splitChartGrouping == SplitChartGrouping.DAILY) set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (splitChartGrouping == SplitChartGrouping.DAILY) shift(startOfDayShift)
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

        return splitIntoRecords(
            calendar = calendar,
            record = secondRecord,
            splitChartGrouping = splitChartGrouping,
            startOfDayShift = startOfDayShift,
            splitRecords = splitRecords
        )
    }

    /**
     * Finds next multiple of divider bigger or lower than value.
     * Ex. isUpper = true, value = 31, divider = 5, result 35.
     * Ex. isUpper = false, value = 31, divider = 5, result 30.
     */
    private fun nearestDivider(
        divider: Long,
        value: Long,
        isUpper: Boolean,
    ): Long {
        val abs = abs(value / divider.toFloat())
        return divider * (if (isUpper) ceil(abs) else floor(abs)).toLong()
    }
}