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
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesView
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
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

    fun getEmptyStreaksViewData(): StatisticsDetailStreaksViewData {
        return StatisticsDetailStreaksViewData(
            streaks = mapToStatsViewData(
                maxStreak = "",
                compareMaxStreak = "",
                currentStreak = "",
                compareCurrentStreak = "",
            ),
            data = emptyList(),
        )
    }

    suspend fun getStreaksViewData(
        records: List<Record>,
        compareRecords: List<Record>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailStreaksViewData {
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

        val (maxStreak, currentStreak, data) = mapStatsData(
            range = range,
            records = records,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val (compareMaxStreak, compareCurrentStreak) = if (showComparison) {
            mapStatsData(
                range = range,
                records = compareRecords,
                rangeLength = rangeLength,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            )
        } else {
            Triple(null, null, emptyList())
        }

        fun processMaxStreak(value: Long): String {
            return value.takeUnless { rangeLength is RangeLength.Day }
                ?.toString()
                ?: emptyValue
        }

        fun processComparisonString(value: String?): String {
            return value
                ?.let { "($it)" }
                .orEmpty()
        }

        val streaks = mapToStatsViewData(
            // No point count streak of one day.
            maxStreak = maxStreak
                .let(::processMaxStreak),
            compareMaxStreak = compareMaxStreak
                ?.let(::processMaxStreak)
                .let(::processComparisonString),
            currentStreak = currentStreak
                .toString(),
            compareCurrentStreak = compareCurrentStreak
                ?.toString()
                .let(::processComparisonString),
        )

        return StatisticsDetailStreaksViewData(
            streaks = streaks,
            data = data,
        )
    }

    private fun mapStatsData(
        range: Range,
        records: List<Record>,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): Triple<Long, Long, List<SeriesView.ViewData>> {
        val (maxStreak, rangeCurrentStreak, rangeCurrentData) = calculate(
            range = range,
            records = records,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        val (currentStreak, currentData) = if (rangeLength is RangeLength.All) {
            rangeCurrentStreak to rangeCurrentData
        } else {
            calculate(
                range = Range(timeStarted = 0, timeEnded = 0),
                records = records,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            ).let {
                it.second to it.third
            }
        }

        return Triple(maxStreak, currentStreak, currentData)
    }

    private fun calculate(
        range: Range,
        records: List<Record>,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): Triple<Long, Long, List<SeriesView.ViewData>> {
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
            day.timeStarted to rangeMapper.getRecordsFromRange(
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

        val data: MutableList<SeriesView.ViewData> = mutableListOf()
        var maxStreak: Long = 0
        var counter: Long = 0
        var streakStart: Long = 0
        var streakEnd: Long = 0
        durations.forEachIndexed { index, duration ->
            val isLast = index == durations.size - 1
            if (duration.second > 0 && !isLast) {
                counter++
                if (streakStart == 0L) streakStart = duration.first
                streakEnd = duration.first
            } else {
                if (counter > 0) {
                    SeriesView.ViewData(
                        value = counter,
                        legendStart = timeMapper.formatDateYear(streakStart),
                        legendEnd = timeMapper.formatDateYear(streakEnd)
                    ).let(data::add)
                }
                if (counter > maxStreak) maxStreak = counter
                counter = 0
                streakStart = 0
                streakEnd = 0
            }
        }

        return Triple(
            maxStreak,
            counter,
            // TODO add comparison?
            // TODO add stat detail dividers?
            // TODO sort also by time ended to show latest streaks
            data.sortedByDescending { it.value }.take(MAX_STREAKS_IN_CHART)
        )
    }

    private fun mapToStatsViewData(
        maxStreak: String,
        compareMaxStreak: String,
        currentStreak: String,
        compareCurrentStreak: String,
    ): List<StatisticsDetailCardViewData> {
        return listOf(
            StatisticsDetailCardViewData(
                value = maxStreak,
                secondValue = compareMaxStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_longest)
            ),
            StatisticsDetailCardViewData(
                value = currentStreak,
                secondValue = compareCurrentStreak,
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

    companion object {
        private const val MAX_STREAKS_IN_CHART = 10
    }
}