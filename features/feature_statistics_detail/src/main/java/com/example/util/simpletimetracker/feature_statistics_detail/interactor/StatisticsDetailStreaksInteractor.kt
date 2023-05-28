package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesView
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksTypeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            showData = false,
            data = emptyList(),
            showComparison = false,
            compareData = emptyList(),
        )
    }

    suspend fun getStreaksViewData(
        records: List<Record>,
        compareRecords: List<Record>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
        streaksType: StreaksType,
    ): StatisticsDetailStreaksViewData = withContext(Dispatchers.Default) {
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
            streaksType = streaksType,
        )
        val (compareMaxStreak, compareCurrentStreak, compareData) = if (showComparison) {
            mapStatsData(
                range = range,
                records = compareRecords,
                rangeLength = rangeLength,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                streaksType = streaksType,
            )
        } else {
            Triple(null, null, emptyList())
        }

        fun processMaxStreak(value: Long): String {
            // No point count streak of one day.
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

        return@withContext StatisticsDetailStreaksViewData(
            streaks = streaks,
            showData = rangeLength !is RangeLength.Day, // No point count streak of one day.
            data = data,
            showComparison = showComparison,
            compareData = compareData,
        )
    }

    fun mapToStreaksTypeViewData(
        streaksType: StreaksType,
    ): List<ViewHolderType> {
        val types = listOf(
            StreaksType.LONGEST,
            StreaksType.LATEST,
        )

        return types.map {
            StatisticsDetailStreaksTypeViewData(
                type = it,
                name = mapToStreakTypeName(it),
                isSelected = it == streaksType
            )
        }
    }

    private fun mapToStreakTypeName(streaksType: StreaksType): String {
        return when (streaksType) {
            StreaksType.LONGEST -> R.string.statistics_detail_streaks_longest
            StreaksType.LATEST -> R.string.statistics_detail_streaks_latest
        }.let(resourceRepo::getString)
    }

    private fun mapStatsData(
        range: Range,
        records: List<Record>,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        streaksType: StreaksType,
    ): Triple<Long, Long, List<SeriesView.ViewData>> {
        val (maxStreak, rangeCurrentStreak, rangeCurrentData) = calculate(
            range = range,
            records = records,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            streaksType = streaksType,
        )

        val currentStreak = if (rangeLength is RangeLength.All) {
            rangeCurrentStreak
        } else {
            calculate(
                range = Range(timeStarted = 0, timeEnded = 0),
                records = records,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                streaksType = streaksType,
            ).second
        }

        return Triple(maxStreak, currentStreak, rangeCurrentData)
    }

    private fun calculate(
        range: Range,
        records: List<Record>,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        streaksType: StreaksType,
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

        val data: MutableList<Triple<Long, Long, Long>> = mutableListOf()
        var maxStreak: Long = 0
        var counter: Long = 0
        var streakStart: Long = 0
        var streakEnd: Long = 0
        durations.forEachIndexed { index, duration ->
            val isLast = index == durations.size - 1
            if (duration.second > 0) {
                counter++
                if (streakStart == 0L) streakStart = duration.first
                streakEnd = duration.first
            }
            if (duration.second <= 0 || isLast) {
                // Series of one day makes no sense.
                if (counter > 1) {
                    Triple(
                        counter,
                        streakStart,
                        streakEnd,
                    ).let(data::add)
                }
                if (counter > maxStreak) maxStreak = counter
            }
            if (duration.second <= 0) {
                counter = 0
                streakStart = 0
                streakEnd = 0
            }
        }
        when (streaksType) {
            StreaksType.LONGEST -> data.sortByDescending { it.first }
            StreaksType.LATEST -> data.sortByDescending { it.third }
        }

        return Triple(
            // Series of one day makes no sense.
            maxStreak.takeIf { it > 1 }.orZero(),
            counter.takeIf { it > 1 }.orZero(),
            data.take(MAX_STREAKS_IN_CHART).map {
                SeriesView.ViewData(
                    value = it.first,
                    legendStart = timeMapper.formatDateYear(it.second),
                    legendEnd = timeMapper.formatDateYear(it.third),
                )
            }
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
                valueChange = StatisticsDetailCardViewData.ValueChange.None,
                secondValue = compareMaxStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_longest)
            ),
            StatisticsDetailCardViewData(
                value = currentStreak,
                valueChange = StatisticsDetailCardViewData.ValueChange.None,
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