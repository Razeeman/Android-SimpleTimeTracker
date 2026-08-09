package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordType.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthlyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getWeeklyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.conts.TAG_VALUE_PRECISION
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataRange
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.domain.statistics.model.ChartValueMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToLong

class StatisticsDetailChartInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val colorMapper: ColorMapper,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailGetGoalFromFilterInteractor: StatisticsDetailGetGoalFromFilterInteractor,
    private val statisticsDetailPreviewInteractor: StatisticsDetailPreviewInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
) {

    // Shouldn't be suspend to avoid blocks jumping on screen open.
    fun getEmptyChartViewData(
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailChartCompositeViewData<*> {
        val compositeData = getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )
        val ranges = getRanges(
            compositeData = compositeData,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = DayOfWeek.MONDAY,
            startOfDayShift = 0,
            useMonthDayTimeFormat = false,
        )
        return statisticsDetailViewDataMapper.mapToEmptyChartViewData(
            ranges = ranges,
            availableChartGroupings = compositeData.availableChartGroupings,
            availableChartLengths = compositeData.availableChartLengths,
        )
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
        splitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
    ): StatisticsDetailChartCompositeViewData<*> = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val durationFormat = prefsInteractor.getDurationFormat()
        val useMonthDayTimeFormat = prefsInteractor.getUseMonthDayTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val typesOrder = types.map(RecordType::id)
        val canSplitByActivity = canSplitByActivity(filter)
        val canComparisonSplitByActivity = canSplitByActivity(compare)
        val chartMode = ChartMode.DURATIONS
        val chartValueMode = ChartValueMode.TOTAL

        val compositeData = getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
        )
        val ranges = getRanges(
            compositeData = compositeData,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
        )
        val data = getChartData(
            allRecords = records,
            ranges = ranges,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = chartValueMode,
            multiplyDuration = false,
            splitByActivity = splitByActivity && canSplitByActivity,
            splitSortMode = splitSortMode,
        )
        val prevData = getPrevData(
            rangeLength = rangeLength,
            compositeData = compositeData,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
            records = records,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = chartValueMode,
            multiplyDuration = false,
            splitSortMode = splitSortMode,
        )
        val compareData = getChartData(
            allRecords = compareRecords,
            ranges = ranges,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = chartValueMode,
            multiplyDuration = false,
            splitByActivity = splitByActivity && canComparisonSplitByActivity,
            splitSortMode = splitSortMode,
        )

        return@withContext statisticsDetailViewDataMapper.mapToChartViewData(
            data = data,
            prevData = prevData,
            splitByActivity = splitByActivity,
            canSplitByActivity = canSplitByActivity,
            canComparisonSplitByActivity = canComparisonSplitByActivity,
            splitSortMode = splitSortMode,
            goalValue = getGoalValue(
                goals = statisticsDetailGetGoalFromFilterInteractor.execute(filter),
                appliedChartGrouping = compositeData.appliedChartGrouping,
            ),
            compareData = compareData,
            compareGoalValue = getGoalValue(
                goals = statisticsDetailGetGoalFromFilterInteractor.execute(compare),
                appliedChartGrouping = compositeData.appliedChartGrouping,
            ),
            showComparison = compare.isNotEmpty(),
            rangeLength = rangeLength,
            availableChartGroupings = compositeData.availableChartGroupings,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            availableChartLengths = compositeData.availableChartLengths,
            appliedChartLength = compositeData.appliedChartLength,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
    }

    fun getChartData(
        allRecords: List<RecordBase>,
        ranges: List<ChartBarDataRange>,
        typesOrder: List<Long>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        chartMode: ChartMode,
        chartValueMode: ChartValueMode,
        multiplyDuration: Boolean,
        splitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
    ): List<ChartBarDataDuration> {
        fun mapEmpty(): List<ChartBarDataDuration> {
            return ranges.map {
                ChartBarDataDuration(
                    rangeStart = it.rangeStart,
                    legend = it.legend,
                    durations = emptyList(),
                )
            }
        }

        fun multiplyDuration(tagValue: Double, record: RecordBase): Double {
            return if (multiplyDuration) {
                val hours: Double = record.duration.toDouble() / TimeUnit.HOURS.toMillis(1)
                tagValue.times(hours)
            } else {
                tagValue
            }
        }

        fun mapRangesToValue(list: List<RecordBase>, range: Range): Long? {
            return when (chartMode) {
                is ChartMode.DURATIONS -> {
                    list.map { record ->
                        rangeMapper.clampToRange(record, range)
                    }.let(rangeMapper::mapToDuration)
                }
                is ChartMode.COUNTS -> {
                    list.size.toLong()
                }
                is ChartMode.TAG_VALUE -> {
                    val tagsValues = list.mapNotNull { record ->
                        // Only one valued tag should be available.
                        record.tags
                            .firstOrNull { it.tagId == chartMode.tagId }
                            ?.numericValue
                            ?.times(TAG_VALUE_PRECISION)
                            ?.let { multiplyDuration(it, record) }
                    }.takeUnless { it.isEmpty() }
                    when (chartValueMode) {
                        ChartValueMode.TOTAL -> tagsValues?.sum()?.roundToLong()
                        ChartValueMode.AVERAGE -> tagsValues?.sum()?.div(tagsValues.size)?.roundToLong()
                    }
                }
            }
        }

        val unknownColor = colorMapper.toUntrackedColor(isDarkTheme)

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
                val durations = if (!splitByActivity) {
                    val clampedRecords = rangeMapper.getRecordsFromRange(records, range)
                    val value = mapRangesToValue(clampedRecords, range)
                    if (value == null) emptyList() else listOf(value to 0)
                } else {
                    rangeMapper.getRecordsFromRange(records, range)
                        .groupBy { it.typeIds.firstOrNull().orZero() }
                        .toList()
                        .mapNotNull { (id, records) ->
                            val value = mapRangesToValue(records, range) ?: return@mapNotNull null
                            value to id
                        }
                        .run {
                            when (splitSortMode) {
                                ChartSplitSortMode.DURATION -> sortedByDescending { (duration, _) ->
                                    duration
                                }
                                ChartSplitSortMode.ACTIVITY_ORDER -> sortedBy { (_, typeId) ->
                                    typesOrder.indexOf(typeId).toLong()
                                }
                            }
                        }.map { (duration, typeId) ->
                            val color = typesMap[typeId]?.color
                                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                                ?: unknownColor
                            duration to color
                        }
                }

                ChartBarDataDuration(
                    rangeStart = data.rangeStart,
                    legend = data.legend,
                    durations = durations,
                )
            }
    }

    fun getChartRangeSelectionData(
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        firstDayOfWeek: DayOfWeek,
    ): CompositeChartData {
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
            ?: availableChartGroupings.firstOrNull()
            ?: currentChartGrouping // Just in case.
        val availableChartLengths = when (rangeLength) {
            is RangeLength.All -> listOf(ChartLength.TEN, ChartLength.FIFTY, ChartLength.HUNDRED)
            else -> emptyList()
        }

        return CompositeChartData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
            availableChartLengths = availableChartLengths,
            appliedChartLength = currentChartLength,
            customRangeGroupings = customRangeGroupings,
        )
    }

    fun getRanges(
        compositeData: CompositeChartData,
        rangeLength: RangeLength,
        rangePosition: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        useMonthDayTimeFormat: Boolean,
    ): List<ChartBarDataRange> {
        val customRangeGroupings = compositeData.customRangeGroupings
        val appliedChartGrouping = compositeData.appliedChartGrouping
        val currentChartLength = compositeData.appliedChartLength

        val startDate = when (rangeLength) {
            is RangeLength.Day,
            is RangeLength.Week,
            is RangeLength.Month,
            is RangeLength.Year,
            is RangeLength.Last,
            is RangeLength.Custom,
            -> timeMapper.getRangeStartAndEnd(
                rangeLength, rangePosition, firstDayOfWeek, 0,
            ).timeEnded - 1
            is RangeLength.All -> System.currentTimeMillis()
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
            is RangeLength.Last -> rangeLength.days
        }

        return when (appliedChartGrouping) {
            ChartGrouping.DAILY -> getDailyGrouping(startDate, numberOfGroups, startOfDayShift, useMonthDayTimeFormat)
            ChartGrouping.WEEKLY -> getWeeklyGrouping(startDate, numberOfGroups, firstDayOfWeek, startOfDayShift)
            ChartGrouping.MONTHLY -> getMonthlyGrouping(startDate, numberOfGroups, startOfDayShift)
            ChartGrouping.YEARLY -> getYearlyGrouping(startDate, numberOfGroups, startOfDayShift)
        }
    }

    fun getPrevData(
        rangeLength: RangeLength,
        compositeData: CompositeChartData,
        rangePosition: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        useMonthDayTimeFormat: Boolean,
        records: List<RecordBase>,
        typesOrder: List<Long>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        chartMode: ChartMode,
        chartValueMode: ChartValueMode,
        multiplyDuration: Boolean,
        splitSortMode: ChartSplitSortMode,
    ): List<ChartBarDataDuration> {
        return if (rangeLength != RangeLength.All) {
            val prevRanges = getRanges(
                compositeData = compositeData,
                rangeLength = rangeLength,
                rangePosition = rangePosition - 1,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
                useMonthDayTimeFormat = useMonthDayTimeFormat,
            )
            getChartData(
                allRecords = records,
                ranges = prevRanges,
                typesOrder = typesOrder,
                typesMap = typesMap,
                isDarkTheme = isDarkTheme,
                splitByActivity = false,
                chartMode = chartMode,
                chartValueMode = chartValueMode,
                multiplyDuration = multiplyDuration,
                splitSortMode = splitSortMode,
            )
        } else {
            emptyList()
        }
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

    // Should return at least one.
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

    private fun canSplitByActivity(
        filter: List<RecordsFilter>,
    ): Boolean {
        val previewType = statisticsDetailPreviewInteractor.getPreviewType(filter)
        return previewType is StatisticsDetailPreviewInteractor.PreviewType.Activities &&
            filter.getTypeIds().size > 1
    }

    private fun getGoalValue(
        goals: List<RecordTypeGoal>,
        appliedChartGrouping: ChartGrouping,
    ): Long {
        // Currently only duration goals are on chart.
        return getGoal(
            goals = goals,
            appliedChartGrouping = appliedChartGrouping,
        ).value * 1000
    }

    private fun getGoal(
        goals: List<RecordTypeGoal>,
        appliedChartGrouping: ChartGrouping,
    ): RecordTypeGoal? {
        return when (appliedChartGrouping) {
            ChartGrouping.DAILY -> goals.getDailyDuration()
            ChartGrouping.WEEKLY -> goals.getWeeklyDuration()
            ChartGrouping.MONTHLY -> goals.getMonthlyDuration()
            ChartGrouping.YEARLY -> null
        }
    }

    data class CompositeChartData(
        val availableChartGroupings: List<ChartGrouping>,
        val appliedChartGrouping: ChartGrouping,
        val availableChartLengths: List<ChartLength>,
        val appliedChartLength: ChartLength,
        val customRangeGroupings: List<Pair<ChartGrouping, Int>>,
    )
}