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
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesView
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksGoal
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksGoalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksTypeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToLong

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
            calendarRowsCount = 1, // Doesn't matter, calendar is hidden.
            calendarData = emptyList(),
            showComparisonCalendar = false,
            compareCalendarData = emptyList(),
            completion = emptyList(),
        )
    }

    suspend fun getStreaksViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
        streaksType: StreaksType,
        streaksGoal: StreaksGoal,
        goalType: RecordTypeGoal.Type?,
        compareGoalType: RecordTypeGoal.Type?,
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
            streaksGoal = streaksGoal,
            goalType = goalType,
        )
        val compareStatsData = if (showComparison) {
            mapStatsData(
                range = range,
                records = compareRecords,
                rangeLength = rangeLength,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                streaksType = streaksType,
                streaksGoal = streaksGoal,
                goalType = compareGoalType,
            )
        } else {
            null
        }

        val streaksCalendarCanBeShown = statsData.calendarData.size > 1
        val isCalendarShownInOneRow = isCalendarShownInOneRow(
            dataSize = statsData.calendarData.size,
            rangeLength = rangeLength,
        )
        val calendarRowsCount = if (isCalendarShownInOneRow) 1 else 7

        val streaksCanBeShown = rangeLength !is RangeLength.Day // No point count streak of one day.

        val hasDataToShow = streaksCanBeShown &&
            statsData.rangeCurrentData.size > 1 // one data point would be the same as Longest streak card.

        val hasComparisonDataToShow = streaksCanBeShown &&
            compareStatsData?.rangeCurrentData.orEmpty().size > 1

        val hasData = hasDataToShow || hasComparisonDataToShow

        val streaks = mapToStreaks(
            statsData = statsData,
            compareStatsData = compareStatsData,
            rangeLength = rangeLength,
        )

        val completion = mapCalendarCompletionPercentage(
            statsData = statsData,
            compareStatsData = compareStatsData,
        ).takeIf { streaksCalendarCanBeShown }.orEmpty()

        return@withContext StatisticsDetailStreaksViewData(
            streaks = streaks,
            showData = hasData,
            data = statsData.rangeCurrentData,
            showComparison = showComparison && hasData,
            compareData = compareStatsData?.rangeCurrentData.orEmpty(),
            showCalendar = streaksCalendarCanBeShown,
            calendarRowsCount = calendarRowsCount,
            calendarData = statsData.calendarData,
            showComparisonCalendar = showComparison && streaksCalendarCanBeShown,
            compareCalendarData = compareStatsData?.calendarData.orEmpty(),
            completion = completion,
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

    fun mapToStreaksGoalViewData(
        streaksGoal: StreaksGoal,
        dailyGoal: RecordTypeGoal.Type?,
        compareGoalType: RecordTypeGoal.Type?,
        rangeLength: RangeLength,
    ): List<ViewHolderType> {
        if (dailyGoal == null && compareGoalType == null) {
            return emptyList()
        }
        if (rangeLength is RangeLength.Day) {
            return emptyList()
        }

        val types = listOf(
            StreaksGoal.ANY,
            StreaksGoal.GOAL,
        )

        return types.map {
            StatisticsDetailStreaksGoalViewData(
                type = it,
                name = mapToStreakGoalName(it),
                isSelected = it == streaksGoal,
            )
        }
    }

    private fun mapToStreakTypeName(streaksType: StreaksType): String {
        return when (streaksType) {
            StreaksType.LONGEST -> R.string.statistics_detail_streaks_longest
            StreaksType.LATEST -> R.string.statistics_detail_streaks_latest
        }.let(resourceRepo::getString)
    }

    private fun mapToStreakGoalName(streaksGoal: StreaksGoal): String {
        return when (streaksGoal) {
            StreaksGoal.ANY -> R.string.statistics_detail_streaks_any
            StreaksGoal.GOAL -> R.string.statistics_detail_streaks_goal
        }.let(resourceRepo::getString)
    }

    private fun mapStatsData(
        range: Range,
        records: List<RecordBase>,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        streaksType: StreaksType,
        streaksGoal: StreaksGoal,
        goalType: RecordTypeGoal.Type?,
    ): IntermediateData {
        val stats = calculate(
            range = range,
            records = records,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            streaksType = streaksType,
            streaksGoal = streaksGoal,
            goalType = goalType,
            rangeLength = rangeLength,
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
                streaksGoal = streaksGoal,
                goalType = goalType,
                rangeLength = rangeLength,
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
        streaksGoal: StreaksGoal,
        goalType: RecordTypeGoal.Type?,
        rangeLength: RangeLength,
    ): IntermediateData {
        val defaultGoal = RecordTypeGoal.Type.Duration(1)
        val goal = if (streaksGoal == StreaksGoal.GOAL) {
            goalType ?: defaultGoal
        } else {
            defaultGoal
        }
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
            }.run {
                when (goal) {
                    is RecordTypeGoal.Type.Count -> count().toLong()
                    is RecordTypeGoal.Type.Duration -> sumOf(Range::duration)
                }
            }
        }
        val goalValue = when (goal) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
        }

        // Format: days, range start, range end.
        val data: MutableList<Triple<Long, Long, Long>> = mutableListOf()
        var longestStreak: Long = 0
        var counter: Long = 0
        var streakStart: Long = 0
        var streakEnd: Long = 0
        durations.forEachIndexed { index, duration ->
            val isLast = index == durations.size - 1
            if (duration.second >= goalValue) {
                counter++
                if (streakStart == 0L) streakStart = duration.first
                streakEnd = duration.first
            }
            if (duration.second < goalValue || isLast) {
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
            if (duration.second < goalValue) {
                counter = 0
                streakStart = 0
                streakEnd = 0
            }
        }
        when (streaksType) {
            StreaksType.LONGEST -> data.sortByDescending { it.first }
            StreaksType.LATEST -> data.sortByDescending { it.third }
        }
        val rangeCurrentData = data.take(MAX_STREAKS_IN_CHART).map {
            SeriesView.ViewData(
                value = it.first,
                legendStart = calendar.shiftTimeStamp(it.second, -startOfDayShift)
                    .let(timeMapper::formatDateYear),
                legendEnd = calendar.shiftTimeStamp(it.third, -startOfDayShift)
                    .let(timeMapper::formatDateYear),
            )
        }
        val calendarData = mapDurationsToCalendarData(
            data = durations,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            goalValue = goalValue,
            rangeLength = rangeLength,
        )

        return IntermediateData(
            longestStreak = longestStreak.takeIf { it > 1 }.orZero(), // Series of one day makes no sense.
            currentStreak = counter.takeIf { it > 1 }.orZero(),
            rangeCurrentData = rangeCurrentData,
            calendarData = calendarData,
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
        goalValue: Long,
        rangeLength: RangeLength,
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
        val isCalendarShownInOneRow = isCalendarShownInOneRow(data.size, rangeLength)
        val daysToAdd = if (!isCalendarShownInOneRow) {
            days.indexOfFirst { it == endDayOfWeek }.takeUnless { it == -1 }.orZero()
        } else {
            0
        }
        val dummyDays = List(daysToAdd) { SeriesCalendarView.ViewData.Dummy }

        return dummyDays + data
            .map {
                val rangeStart = calendar.shiftTimeStamp(it.first, -startOfDayShift)
                val monthLegend = if (!isCalendarShownInOneRow) {
                    timeMapper.formatShortMonth(rangeStart)
                } else {
                    ""
                }
                if (it.second >= goalValue) {
                    SeriesCalendarView.ViewData.Present(rangeStart, monthLegend)
                } else {
                    SeriesCalendarView.ViewData.NotPresent(rangeStart, monthLegend)
                }
            }
            .reversed()
    }

    private fun mapToStreaks(
        statsData: IntermediateData,
        compareStatsData: IntermediateData?,
        rangeLength: RangeLength,
    ): List<StatisticsDetailCardInternalViewData> {
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

        return mapToStatsViewData(
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
    }

    private fun mapToStatsViewData(
        longestStreak: String,
        compareLongestStreak: String,
        currentStreak: String,
        compareCurrentStreak: String,
    ): List<StatisticsDetailCardInternalViewData> {
        return listOf(
            StatisticsDetailCardInternalViewData(
                value = longestStreak,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareLongestStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_longest),
            ),
            StatisticsDetailCardInternalViewData(
                value = currentStreak,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareCurrentStreak,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_current),
            ),
        )
    }

    private fun mapCalendarCompletionPercentage(
        statsData: IntermediateData,
        compareStatsData: IntermediateData?,
    ): List<StatisticsDetailCardInternalViewData> {
        fun processComparisonString(value: String?): String {
            return value
                ?.let { "($it)" }
                .orEmpty()
        }

        fun processPercentageString(value: Float): String {
            val text = when {
                value >= 10 -> value.toLong()
                (value * 10).roundToLong() % 10L == 0L -> value.toLong()
                else -> String.format("%.1f", value)
            }
            return "$text%"
        }

        fun getPercentage(
            data: List<SeriesCalendarView.ViewData>,
        ): String {
            val total = data.filter { it !is SeriesCalendarView.ViewData.Dummy }.size
            val completed = data.filterIsInstance<SeriesCalendarView.ViewData.Present>().size

            return if (total != 0) {
                processPercentageString(completed * 100f / total)
            } else {
                emptyValue
            }
        }

        fun getTotal(
            data: List<SeriesCalendarView.ViewData>,
        ): String {
            val total = data.filter { it !is SeriesCalendarView.ViewData.Dummy }.size
            val completed = data.filterIsInstance<SeriesCalendarView.ViewData.Present>().size
            return "$completed/$total"
        }

        return mapToCalendarCompletionViewData(
            completionPercentage = getPercentage(statsData.calendarData),
            compareCompletionPercentage = compareStatsData?.calendarData
                ?.let(::getPercentage)
                .let(::processComparisonString),
            completionCount = getTotal(statsData.calendarData),
            compareCompletionCount = compareStatsData?.calendarData
                ?.let(::getTotal)
                .let(::processComparisonString),
        )
    }

    private fun mapToCalendarCompletionViewData(
        completionPercentage: String,
        compareCompletionPercentage: String,
        completionCount: String,
        compareCompletionCount: String,
    ): List<StatisticsDetailCardInternalViewData> {
        return listOf(
            StatisticsDetailCardInternalViewData(
                value = completionPercentage,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareCompletionPercentage,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_completion_percentage),
            ),
            StatisticsDetailCardInternalViewData(
                value = completionCount,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareCompletionCount,
                description = resourceRepo.getString(R.string.statistics_detail_streaks_completion_count),
            ),
        )
    }

    private fun isCalendarShownInOneRow(
        dataSize: Int,
        rangeLength: RangeLength,
    ): Boolean {
        return when (rangeLength) {
            is RangeLength.Day,
            is RangeLength.Week,
            -> true
            is RangeLength.Month,
            is RangeLength.Year,
            -> false
            is RangeLength.All,
            is RangeLength.Last,
            is RangeLength.Custom,
            -> dataSize <= RANGE_ALL_STREAKS_CALENDAR_CUTOFF
        }
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