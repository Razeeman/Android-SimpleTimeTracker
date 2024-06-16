package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToLong

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    fun mapToPreview(
        recordType: RecordType,
        isDarkTheme: Boolean,
        isFirst: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = recordType.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = recordType.name.takeIf { isFirst }.orEmpty(),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToCategorizedPreview(
        category: Category,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = category.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = category.name,
            iconId = null,
            color = category.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToUncategorizedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val item = categoryViewDataMapper.mapToUncategorizedItem(
            isDarkTheme = isDarkTheme,
            isFiltered = false,
        )

        return StatisticsDetailPreviewViewData(
            id = item.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = item.name,
            iconId = RecordTypeIcon.Image(R.drawable.untagged),
            color = item.color,
        )
    }

    fun mapToTaggedPreview(
        tag: RecordTag,
        types: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val icon = recordTagViewDataMapper.mapIcon(tag, types)
        val color = recordTagViewDataMapper.mapColor(tag, types)

        return StatisticsDetailPreviewViewData(
            id = tag.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = tag.name,
            iconId = icon?.let(iconMapper::mapIcon),
            color = color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToUntaggedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val item = categoryViewDataMapper.mapToUntaggedItem(
            isDarkTheme = isDarkTheme,
            isFiltered = false,
        )

        return StatisticsDetailPreviewViewData(
            id = item.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = item.name,
            iconId = item.icon,
            color = item.color,
        )
    }

    fun mapUntrackedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = UNTRACKED_ITEM_ID,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapMultitaskPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = MULTITASK_ITEM_ID,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = resourceRepo.getString(R.string.multitask_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.multitask),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapToPreviewEmpty(
        isDarkTheme: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = 0,
            type = StatisticsDetailPreviewViewData.Type.FILTER,
            name = "",
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapToChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        goalValue: Long,
        compareData: List<ChartBarDataDuration>,
        compareGoalValue: Long,
        showComparison: Boolean,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): StatisticsDetailChartCompositeViewData {
        val chartData = mapChartData(
            data = data,
            goal = goalValue,
            rangeLength = rangeLength,
            showSelectedBarOnStart = true
        )
        val compareChartData = mapChartData(
            data = compareData,
            goal = compareGoalValue,
            rangeLength = rangeLength,
            showSelectedBarOnStart = false
        )
        val (title, rangeAverages) = getRangeAverages(
            data = data,
            prevData = prevData,
            compareData = compareData,
            showComparison = showComparison,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )

        return StatisticsDetailChartCompositeViewData(
            chartData = chartData,
            compareChartData = compareChartData,
            showComparison = showComparison,
            rangeAveragesTitle = title,
            rangeAverages = rangeAverages,
            appliedChartGrouping = appliedChartGrouping,
            chartGroupingViewData = chartGroupingViewData,
            chartGroupingVisible = chartGroupingViewData.size > 1,
            appliedChartLength = appliedChartLength,
            chartLengthViewData = chartLengthViewData,
            chartLengthVisible = chartLengthViewData.isNotEmpty(),
        )
    }

    fun mapToEmptyChartViewData(): StatisticsDetailChartCompositeViewData {
        return StatisticsDetailChartCompositeViewData(
            chartData = StatisticsDetailChartViewData(
                visible = true,
                data = emptyList(),
                legendSuffix = "",
                addLegendToSelectedBar = false,
                shouldDrawHorizontalLegends = false,
                showSelectedBarOnStart = false,
                goalValue = 0f,
            ),
            compareChartData = StatisticsDetailChartViewData(
                visible = false,
                data = emptyList(),
                legendSuffix = "",
                addLegendToSelectedBar = false,
                shouldDrawHorizontalLegends = false,
                showSelectedBarOnStart = false,
                goalValue = 0f,
            ),
            showComparison = false,
            rangeAveragesTitle = " ",
            rangeAverages = mapToEmptyRangeAverages(),
            appliedChartGrouping = ChartGrouping.DAILY,
            chartGroupingViewData = emptyList(),
            chartGroupingVisible = true,
            appliedChartLength = ChartLength.TEN,
            chartLengthViewData = emptyList(),
            chartLengthVisible = true,
        )
    }

    fun mapToDailyChartViewData(
        data: Map<Int, Float>,
        firstDayOfWeek: DayOfWeek,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
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
        }

        val viewData = days.map { day ->
            val calendarDay = timeMapper.toCalendarDayOfWeek(day)
            BarChartView.ViewData(
                value = data[calendarDay].orZero(),
                legend = timeMapper.toShortDayOfWeekName(day),
            )
        }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
        )
    }

    fun mapToHourlyChartViewData(
        data: Map<Int, Float>,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
        val hourLegends = (0 until 24).map {
            it to it.toString()
        }

        val viewData = hourLegends
            .map { (hour, legend) ->
                BarChartView.ViewData(
                    value = data[hour].orZero(),
                    legend = legend,
                )
            }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
        )
    }

    fun mapToSplitChartGroupingViewData(
        rangeLength: RangeLength,
        splitChartGrouping: SplitChartGrouping,
    ): List<ViewHolderType> {
        val groupings = when (rangeLength) {
            is RangeLength.Day -> emptyList()
            else -> listOf(
                SplitChartGrouping.HOURLY,
                SplitChartGrouping.DAILY,
            )
        }

        return groupings.map {
            StatisticsDetailSplitGroupingViewData(
                splitChartGrouping = it,
                name = mapToSplitGroupingName(it),
                isSelected = it == splitChartGrouping,
            )
        }
    }

    fun mapToDurationsSlipChartViewData(
        data: Map<Range, Float>,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
        val viewData = data
            .map { (range, percent) ->
                val started = timeMapper.formatDuration(range.timeStarted / 1000)
                val ended = timeMapper.formatDuration(range.timeEnded / 1000)
                range to BarChartView.ViewData(
                    value = percent,
                    legend = ended,
                    selectedBarLegend = "$started - $ended",
                )
            }.sortedBy { (range, _) ->
                range.timeStarted
            }.map { (_, data) ->
                data
            }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
        )
    }

    private fun getRangeAverages(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        compareData: List<ChartBarDataDuration>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        chartGrouping: ChartGrouping,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): Pair<String, List<StatisticsDetailCardInternalViewData>> {
        // No reason to show average of one value.
        if (data.size < 2 && compareData.size < 2) return "" to emptyList()

        fun getAverage(data: List<ChartBarDataDuration>): Long {
            if (data.isEmpty()) return 0L
            return data.sumOf { it.duration } / data.size
        }

        val average = getAverage(data)
        val nonEmptyData = data.filter { it.duration > 0 }
        val averageByNonEmpty = getAverage(nonEmptyData)

        val comparisonAverage = getAverage(compareData)
        val comparisonNonEmptyData = compareData.filter { it.duration > 0 }
        val comparisonAverageByNonEmpty = getAverage(comparisonNonEmptyData)

        val prevAverage = getAverage(prevData)
        val prevNonEmptyData = prevData.filter { it.duration > 0 }
        val prevAverageByNonEmpty = getAverage(prevNonEmptyData)

        val title = resourceRepo.getString(
            R.string.statistics_detail_range_averages_title,
            mapToGroupingName(chartGrouping),
        )

        val rangeAverages = listOf(
            StatisticsDetailCardInternalViewData(
                value = average.let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    )
                },
                valueChange = mapValueChange(
                    average = average,
                    prevAverage = prevAverage,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = comparisonAverage
                    .let {
                        timeMapper.formatInterval(
                            interval = it,
                            forceSeconds = showSeconds,
                            useProportionalMinutes = useProportionalMinutes,
                        )
                    }
                    .let { "($it)" }
                    .takeIf { showComparison }
                    .orEmpty(),
                description = resourceRepo.getString(R.string.statistics_detail_range_averages),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = averageByNonEmpty.let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    )
                },
                valueChange = mapValueChange(
                    average = averageByNonEmpty,
                    prevAverage = prevAverageByNonEmpty,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = comparisonAverageByNonEmpty
                    .let {
                        timeMapper.formatInterval(
                            interval = it,
                            forceSeconds = showSeconds,
                            useProportionalMinutes = useProportionalMinutes,
                        )
                    }
                    .let { "($it)" }
                    .takeIf { showComparison }
                    .orEmpty(),
                description = resourceRepo.getString(R.string.statistics_detail_range_averages_non_empty),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )

        return title to rangeAverages
    }

    private fun mapToEmptyRangeAverages(): List<StatisticsDetailCardInternalViewData> {
        val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_range_averages),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_range_averages_non_empty),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }

    private fun mapValueChange(
        average: Long,
        prevAverage: Long,
        rangeLength: RangeLength,
        isDarkTheme: Boolean,
    ): StatisticsDetailCardInternalViewData.ValueChange {
        if (rangeLength == RangeLength.All) {
            return StatisticsDetailCardInternalViewData.ValueChange.None
        }

        val change: Float = when {
            prevAverage.orZero() == 0L && average.orZero() == 0L -> 0f
            prevAverage.orZero() == 0L && average.orZero() > 0L -> 100f
            prevAverage.orZero() > 0L && average.orZero() == 0L -> -100f
            prevAverage > 0 -> {
                (average.orZero() - prevAverage) * 100f / prevAverage
            }
            else -> 0f
        }

        fun formatChange(value: Float): String {
            val abs = abs(value)
            val text = when {
                abs >= 1_000_000f -> "∞"
                abs >= 1_000f -> "${(abs / 1000).toLong()}K"
                abs >= 10 -> abs.toLong()
                (abs * 10).roundToLong() % 10L == 0L -> abs.toLong()
                else -> String.format("%.1f", abs)
            }
            return if (value >= 0) "+$text%" else "-$text%"
        }

        return StatisticsDetailCardInternalViewData.ValueChange.Present(
            text = formatChange(change),
            color = if (change >= 0f) {
                colorMapper.toPositiveColor(isDarkTheme)
            } else {
                colorMapper.toNegativeColor(isDarkTheme)
            },
        )
    }

    private fun mapChartData(
        data: List<ChartBarDataDuration>,
        goal: Long,
        rangeLength: RangeLength,
        showSelectedBarOnStart: Boolean,
    ): StatisticsDetailChartViewData {
        val isMinutes = data.maxOfOrNull(ChartBarDataDuration::duration)
            .orZero()
            .let(TimeUnit.MILLISECONDS::toHours) == 0L

        val legendSuffix = if (isMinutes) {
            R.string.statistics_detail_legend_minute_suffix
        } else {
            R.string.statistics_detail_legend_hour_suffix
        }.let(resourceRepo::getString)

        return StatisticsDetailChartViewData(
            visible = data.size > 1,
            data = data.map {
                BarChartView.ViewData(
                    value = formatInterval(it.duration, isMinutes),
                    legend = it.legend,
                )
            },
            legendSuffix = legendSuffix,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = when (rangeLength) {
                is RangeLength.Day -> false
                is RangeLength.Week -> true
                is RangeLength.Month -> false
                is RangeLength.Year -> data.size <= 12
                is RangeLength.All,
                is RangeLength.Custom,
                is RangeLength.Last,
                -> data.size <= 10
            },
            showSelectedBarOnStart = showSelectedBarOnStart,
            goalValue = formatInterval(goal, isMinutes = isMinutes),
        )
    }

    private fun formatInterval(interval: Long, isMinutes: Boolean): Float {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )

        return if (isMinutes) {
            hr * 60f + min + sec / 60f
        } else {
            hr + min / 60f
        }
    }

    private fun mapToChartGroupingViewData(
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
    ): List<ViewHolderType> {
        return availableChartGroupings.map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                isSelected = it == appliedChartGrouping,
            )
        }
    }

    private fun mapToChartLengthViewData(
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
    ): List<ViewHolderType> {
        return availableChartLengths.map {
            StatisticsDetailChartLengthViewData(
                chartLength = it,
                name = mapToLengthName(it),
                isSelected = it == appliedChartLength,
            )
        }
    }

    private fun mapToGroupingName(chartGrouping: ChartGrouping): String {
        return when (chartGrouping) {
            ChartGrouping.DAILY -> R.string.statistics_detail_chart_daily
            ChartGrouping.WEEKLY -> R.string.statistics_detail_chart_weekly
            ChartGrouping.MONTHLY -> R.string.statistics_detail_chart_monthly
            ChartGrouping.YEARLY -> R.string.statistics_detail_chart_yearly
        }.let(resourceRepo::getString)
    }

    private fun mapToSplitGroupingName(splitChartGrouping: SplitChartGrouping): String {
        return when (splitChartGrouping) {
            SplitChartGrouping.HOURLY -> R.string.statistics_detail_chart_hourly
            SplitChartGrouping.DAILY -> R.string.statistics_detail_chart_daily
        }.let(resourceRepo::getString)
    }

    private fun mapToLengthName(chartLength: ChartLength): String {
        return when (chartLength) {
            ChartLength.TEN -> R.string.statistics_detail_length_ten
            ChartLength.FIFTY -> R.string.statistics_detail_length_fifty
            ChartLength.HUNDRED -> R.string.statistics_detail_length_hundred
        }.let(resourceRepo::getString)
    }

    companion object {
        private const val SPLIT_CHART_LEGEND = "%"
    }
}