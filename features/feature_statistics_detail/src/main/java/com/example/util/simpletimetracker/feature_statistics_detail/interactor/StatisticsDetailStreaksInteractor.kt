package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

class StatisticsDetailStreaksInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val resourceRepo: ResourceRepo,
) {

    private val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

    suspend fun getStreaksViewData(
        records: List<Record>,
        compareRecords: List<Record>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<StatisticsDetailCardViewData> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        ).let {
            Range(timeStarted = it.first, timeEnded = it.second)
        }

        val (maxStreak, currentStreak) = mapStatsData(
            range = range,
            records = records,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )
        val (compareMaxStreak, compareCurrentStreak) = if (showComparison) {
            mapStatsData(
                range = range,
                records = compareRecords,
                rangeLength = rangeLength,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift
            )
        } else {
            null to null
        }

        return mapToStatsViewData(
            rangeLength = rangeLength,
            maxStreak = maxStreak,
            compareMaxStreak = compareMaxStreak,
            currentStreak = currentStreak,
            compareCurrentStreak = compareCurrentStreak,
        )
    }

    private fun mapStatsData(
        range: Range,
        records: List<Record>,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): Pair<Long, Long> {
        val (maxStreak, rangeCurrentStreak) = calculate(
            range = range,
            records = records,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        val currentStreak = if (rangeLength is RangeLength.All) {
            rangeCurrentStreak
        } else {
            calculate(
                range = Range(timeStarted = 0, timeEnded = 0),
                records = records,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            ).second
        }

        return maxStreak to currentStreak
    }

    private fun calculate(
        range: Range,
        records: List<Record>,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): Pair<Long, Long> {
        var maxStreak: Long = 0

        val durations = getRanges(
            range = if (range.timeStarted == 0L && range.timeEnded == 0L) {
                Range(
                    timeStarted = records.minByOrNull { it.timeStarted }?.timeStarted ?: 0,
                    timeEnded = System.currentTimeMillis(),
                )
            } else {
                range
            },
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        ).map { day ->
            rangeMapper.getRecordsFromRange(
                records = records,
                rangeStart = day.timeStarted,
                rangeEnd = day.timeEnded,
            ).map {
                rangeMapper.clampToRange(
                    record = it,
                    rangeStart = day.timeStarted,
                    rangeEnd = day.timeEnded,
                )
            }.sumOf {
                it.duration
            }
        }

        var counter: Long = 0
        durations.forEach { duration ->
            if (duration > 0) {
                counter++
            } else {
                if (counter > maxStreak) maxStreak = counter
                counter = 0
            }
        }
        if (counter > maxStreak) maxStreak = counter

        return maxStreak to counter
    }

    private fun mapToStatsViewData(
        rangeLength: RangeLength,
        maxStreak: Long,
        compareMaxStreak: Long?,
        currentStreak: Long,
        compareCurrentStreak: Long?,
    ): List<StatisticsDetailCardViewData> {
        fun processComparisonString(value: String?): String {
            return value
                ?.let { "($it)" }
                .orEmpty()
        }

        fun processMaxStreak(value: Long): String {
            return value.takeUnless { rangeLength is RangeLength.Day }
                ?.toString()
                ?: emptyValue
        }

        return listOf(
            StatisticsDetailCardViewData(
                // No point count streak of one day.
                value = maxStreak.let(::processMaxStreak),
                secondValue = compareMaxStreak
                    ?.let(::processMaxStreak)
                    .let(::processComparisonString),
                description = resourceRepo.getString(R.string.statistics_detail_streaks_longest)
            ),
            StatisticsDetailCardViewData(
                value = currentStreak.toString(),
                secondValue = compareCurrentStreak
                    ?.toString()
                    .let(::processComparisonString),
                description = resourceRepo.getString(R.string.statistics_detail_streaks_current)
            ),
        )
    }

    private fun getRanges(
        range: Range,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<Range> {
        val start = range.timeStarted
        val end = range.timeEnded - 1 // end of day is beginning on next one, shift back.

        val numberOfDays = calculateCustomRangeGropings(
            rangeStart = start,
            rangeEnd = end,
            firstDayOfWeek = firstDayOfWeek
        )

        return getDailyGroupings(
            startDate = end,
            numberOfDays = numberOfDays,
            startOfDayShift = startOfDayShift
        )
    }

    private fun calculateCustomRangeGropings(
        rangeStart: Long,
        rangeEnd: Long,
        firstDayOfWeek: DayOfWeek,
    ): Int {
        val shift = timeMapper.toTimestampShift(
            fromTime = rangeStart,
            toTime = rangeEnd,
            range = RangeLength.Day,
            firstDayOfWeek = firstDayOfWeek
        )

        return when {
            shift != 0L -> abs(shift).toInt() + 1 // compensate one shift.
            else -> 1
        }
    }

    private fun getDailyGroupings(
        startDate: Long,
        numberOfDays: Int,
        startOfDayShift: Long,
    ): List<Range> {
        val calendar = Calendar.getInstance()

        return (numberOfDays - 1 downTo 0).map { shift ->
            calendar.apply {
                timeInMillis = startDate
                setToStartOfDay()
            }
            calendar.add(Calendar.DATE, -shift)

            val rangeStart = calendar.timeInMillis
            val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis

            Range(
                timeStarted = rangeStart + startOfDayShift,
                timeEnded = rangeEnd + startOfDayShift
            )
        }
    }
}