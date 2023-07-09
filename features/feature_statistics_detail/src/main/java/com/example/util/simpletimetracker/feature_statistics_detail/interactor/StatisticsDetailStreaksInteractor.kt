package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.shift
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
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
                longestStreak = "",
                compareLongestStreak = "",
                currentStreak = "",
                compareCurrentStreak = "",
            ),
            showData = false,
            data = emptyList(),
            showComparison = false,
            compareData = emptyList(),
            showCalendar = false,
            calendarData = emptyList(),
            showComparisonCalendar = false,
            compareCalendarData = emptyList(),
        )
    }

    suspend fun getStreaksViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
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
            startOfDayShift = 0, // ignore start of day shift, add later.
        )

        val statsData = mapStatsData(
            range = range,
            records = records,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            streaksType = streaksType,
        )
        val compareStatsData = if (showComparison) {
            mapStatsData(
                range = range,
                records = compareRecords,
                rangeLength = rangeLength,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                streaksType = streaksType,
            )
        } else {
            null
        }

        fun processLongestStreak(value: Long): String {
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
            longestStreak = statsData.longestStreak
                .let(::processLongestStreak),
            compareLongestStreak = compareStatsData?.longestStreak
                ?.let(::processLongestStreak)
                .let(::processComparisonString),
            currentStreak = statsData.currentStreak
                .toString(),
            compareCurrentStreak = compareStatsData?.currentStreak
                ?.toString()
                .let(::processComparisonString),
        )

        val streaksCalendarCanBeShown = when (rangeLength) {
            is RangeLength.Month,
            is RangeLength.Year,
            -> true

            is RangeLength.All -> {
                statsData.calendarData.size > RANGE_ALL_STREAKS_CALENDAR_CUTOFF ||
                    compareStatsData?.calendarData?.size.orZero() > RANGE_ALL_STREAKS_CALENDAR_CUTOFF
            }

            else -> false
        }
        val streaksCanBeShown = rangeLength !is RangeLength.Day // No point count streak of one day.
        val hasDataToShow = streaksCanBeShown &&
            statsData.rangeCurrentData.size > 1 // one data point would be the same as Longest streak card.
        val hasComparisonDataToShow = streaksCanBeShown &&
            compareStatsData?.rangeCurrentData.orEmpty().size > 1
        val hasData = hasDataToShow || hasComparisonDataToShow

        return@withContext StatisticsDetailStreaksViewData(
            streaks = streaks,
            showData = hasData,
            data = statsData.rangeCurrentData,
            showComparison = showComparison && hasData,
            compareData = compareStatsData?.rangeCurrentData.orEmpty(),
            showCalendar = streaksCalendarCanBeShown,
            calendarData = statsData.calendarData,
            showComparisonCalendar = showComparison && streaksCalendarCanBeShown,
            compareCalendarData = compareStatsData?.calendarData.orEmpty(),
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
                isSelected = it == streaksType,
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
        records: List<RecordBase>,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        streaksType: StreaksType,
    ): IntermediateData {
        val stats = calculate(
            range = range,
            records = records,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            streaksType = streaksType,
        )

        // If range is not all data - calculate current streak on all data.
        return if (rangeLength is RangeLength.All) {
            stats
        } else {
            val currentStreak = calculate(
                range = Range(timeStarted = 0, timeEnded = 0),
                records = records,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                streaksType = streaksType,
            ).currentStreak
            stats.copy(currentStreak = currentStreak)
        }
    }

    private fun calculate(
        range: Range,
        records: List<RecordBase>,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        streaksType: StreaksType,
    ): IntermediateData {
        val calendar = Calendar.getInstance()
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
                range = Range(day.timeStarted, day.timeEnded),
            ).map {
                rangeMapper.clampToRange(
                    record = it,
                    range = Range(
                        timeStarted = day.timeStarted,
                        timeEnded = day.timeEnded,
                    ),
                )
            }.sumOf(Range::duration)
        }

        // Format: days, range start, range end.
        val data: MutableList<Triple<Long, Long, Long>> = mutableListOf()
        var longestStreak: Long = 0
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
                if (counter > longestStreak) longestStreak = counter
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

        return IntermediateData(
            longestStreak = longestStreak.takeIf { it > 1 }.orZero(), // Series of one day makes no sense.
            currentStreak = counter.takeIf { it > 1 }.orZero(),
            rangeCurrentData = data.take(MAX_STREAKS_IN_CHART).map {
                SeriesView.ViewData(
                    value = it.first,
                    legendStart = calendar.shiftTimeStamp(it.second, -startOfDayShift)
                        .let(timeMapper::formatDateYear),
                    legendEnd = calendar.shiftTimeStamp(it.third, -startOfDayShift)
                        .let(timeMapper::formatDateYear),
                )
            },
            calendarData = mapDurationsToCalendarData(
                data = durations,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            ),
        )
    }

    private fun mapToStatsViewData(
        longestStreak: String,
        compareLongestStreak: String,
        currentStreak: String,
        compareCurrentStreak: String,
    ): List<StatisticsDetailCardViewData> {
        return listOf(
            StatisticsDetailCardViewData(
                value = longestStreak,
                valueChange = StatisticsDetailCardViewData.ValueChange.None,
                secondValue = compareLongestStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_longest),
            ),
            StatisticsDetailCardViewData(
                value = currentStreak,
                valueChange = StatisticsDetailCardViewData.ValueChange.None,
                secondValue = compareCurrentStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_current),
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
            firstDayOfWeek = firstDayOfWeek,
        )

        return getDailyGroupings(
            startDate = end,
            numberOfDays = numberOfDays,
            startOfDayShift = startOfDayShift,
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
            firstDayOfWeek = firstDayOfWeek,
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
                timeStarted = calendar.shiftTimeStamp(rangeStart, startOfDayShift),
                timeEnded = calendar.shiftTimeStamp(rangeEnd, startOfDayShift),
            )
        }
    }

    // Data is in format timeStarted to duration.
    private fun mapDurationsToCalendarData(
        data: List<Pair<Long, Long>>,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<SeriesCalendarView.ViewData> {
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        ).let { list ->
            list.indexOf(firstDayOfWeek)
                .takeUnless { it == -1 }.orZero()
                .let(list::rotateLeft)
        }.reversed()

        val calendar = Calendar.getInstance()
        // Take last because data is from range start to range end.
        val endDayOfWeek: DayOfWeek? = data.lastOrNull()?.first?.let {
            calendar
                .apply {
                    timeInMillis = it
                    shift(-startOfDayShift)
                }
                .get(Calendar.DAY_OF_WEEK)
                .let(timeMapper::toDayOfWeek)
        }
        // If for example today is wednesday - need to add 4 dummy days to show correct position on view.
        val daysToAdd = days.indexOfFirst { it == endDayOfWeek }.takeUnless { it == -1 }.orZero()
        val dummyDays = List(daysToAdd) { SeriesCalendarView.ViewData.Dummy }

        return dummyDays + data
            .map {
                val rangeStart = calendar.shiftTimeStamp(it.first, -startOfDayShift)
                val monthLegend = timeMapper.formatShortMonth(rangeStart)
                if (it.second > 0) {
                    SeriesCalendarView.ViewData.Present(rangeStart, monthLegend)
                } else {
                    SeriesCalendarView.ViewData.NotPresent(rangeStart, monthLegend)
                }
            }
            .reversed()
    }

    private data class IntermediateData(
        val longestStreak: Long,
        val currentStreak: Long,
        val rangeCurrentData: List<SeriesView.ViewData>,
        val calendarData: List<SeriesCalendarView.ViewData>,
    )

    companion object {
        private const val MAX_STREAKS_IN_CHART = 10
        private const val RANGE_ALL_STREAKS_CALENDAR_CUTOFF = 21
    }
}