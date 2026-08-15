package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import android.annotation.SuppressLint
import android.graphics.Color
import com.example.util.simpletimetracker.core.extension.removeTrailingZeroes
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.base.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.base.OneShotValue
import com.example.util.simpletimetracker.domain.base.STATISTICS_TOTAL_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.conts.TAG_VALUE_PRECISION
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataRange
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailChartViewModelDelegate
import com.example.util.simpletimetracker.feature_views.barChart.BarChartView
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
        showName: Boolean,
        isForComparison: Boolean,
        isFiltered: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = recordType.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            dataType = StatisticsDetailPreviewViewData.DataType.ACTIVITY,
            name = recordType.name.takeIf { showName }.orEmpty(),
            iconId = iconMapper.mapIcon(recordType.icon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            isFiltered = isFiltered,
        ).let {
            mapFilteredState(
                state = it,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    fun mapToCategorizedPreview(
        category: Category,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
        isFiltered: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = category.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            dataType = StatisticsDetailPreviewViewData.DataType.CATEGORY,
            name = category.name,
            iconId = null,
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.mapToColorInt(category.color, isDarkTheme),
            isFiltered = isFiltered,
        ).let {
            mapFilteredState(
                state = it,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    fun mapToUncategorizedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
        isFiltered: Boolean,
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
            dataType = StatisticsDetailPreviewViewData.DataType.CATEGORY,
            name = item.name,
            iconId = RecordTypeIcon.Image(R.drawable.untagged),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = item.color,
            isFiltered = isFiltered,
        ).let {
            mapFilteredState(
                state = it,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    fun mapToTaggedPreview(
        tag: RecordTag,
        types: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
        isFiltered: Boolean,
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
            dataType = StatisticsDetailPreviewViewData.DataType.TAG,
            name = tag.name,
            iconId = icon?.let(iconMapper::mapIcon),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.mapToColorInt(color, isDarkTheme),
            isFiltered = isFiltered,
        ).let {
            mapFilteredState(
                state = it,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    fun mapToUntaggedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
        isFiltered: Boolean,
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
            dataType = StatisticsDetailPreviewViewData.DataType.TAG,
            name = item.name,
            iconId = item.icon,
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = item.color,
            isFiltered = isFiltered,
        ).let {
            mapFilteredState(
                state = it,
                isDarkTheme = isDarkTheme,
            )
        }
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
            dataType = StatisticsDetailPreviewViewData.DataType.OTHER,
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            isFiltered = false,
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
            dataType = StatisticsDetailPreviewViewData.DataType.OTHER,
            name = resourceRepo.getString(R.string.multitask_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.multitask),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            isFiltered = false,
        )
    }

    fun mapToPreviewEmpty(
        isDarkTheme: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = 0,
            type = StatisticsDetailPreviewViewData.Type.FILTER,
            dataType = StatisticsDetailPreviewViewData.DataType.OTHER,
            name = "",
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            isFiltered = false,
        )
    }

    fun mapToTotalPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = STATISTICS_TOTAL_ITEM_ID,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            dataType = StatisticsDetailPreviewViewData.DataType.OTHER,
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            iconId = null,
            iconColor = colorMapper.toIconColor(isDarkTheme),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            isFiltered = false,
        )
    }

    fun mapToChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        splitByActivity: Boolean,
        canSplitByActivity: Boolean,
        canComparisonSplitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
        goalValue: Long,
        compareData: List<ChartBarDataDuration>,
        compareGoalValue: Long,
        showComparison: Boolean,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): StatisticsDetailChartCompositeViewData {
        val chartIsSplitByActivity = splitByActivity && canSplitByActivity
        val chartComparisonIsSplitByActivity = splitByActivity && canComparisonSplitByActivity

        val chartData = mapChartData(
            data = data,
            goal = goalValue,
            rangeLength = rangeLength,
            chartMode = chartMode,
            yAxisZoomed = false,
            showSelectedBarOnStart = true,
            useSingleColor = !chartIsSplitByActivity,
            drawRoundCaps = !chartIsSplitByActivity,
        )?.let {
            StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.ChartData,
                singleColor = null,
                marginTopDp = 16,
                data = it,
            )
        }?.mapItem(forComparison = false)
        val compareChartData = if (showComparison && chartData != null) {
            mapChartData(
                data = compareData,
                goal = compareGoalValue,
                rangeLength = rangeLength,
                chartMode = chartMode,
                yAxisZoomed = false,
                showSelectedBarOnStart = false,
                useSingleColor = !chartComparisonIsSplitByActivity,
                drawRoundCaps = !chartComparisonIsSplitByActivity,
            )
        } else {
            null
        }?.let {
            StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.ChartDataComparison,
                singleColor = null,
                marginTopDp = 16,
                data = it,
            )
        }?.mapItem(forComparison = true)
        val rangeAverages = getRangeAverages(
            data = data,
            prevData = prevData,
            compareData = compareData,
            showComparison = showComparison,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        ).takeIf {
            it.second.isNotEmpty()
        }?.let { (title, data) ->
            StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.RangeAverages,
                title = title,
                marginTopDp = 0,
                data = data,
            )
        }.let(::listOfNotNull)
        val buttons = mutableListOf<ViewHolderType>()
        val chartGroupingViewData = mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        ).takeIf {
            it.size > 1
        }?.let {
            ButtonsRowItemViewData(
                block = StatisticsDetailBlock.ChartGrouping,
                marginTopDp = 4,
                data = it,
            )
        }
        val chartLengthViewData = mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        ).takeIf {
            it.isNotEmpty()
        }?.let {
            ButtonsRowItemViewData(
                block = StatisticsDetailBlock.ChartLength,
                marginTopDp = -10,
                data = it,
            )
        }
        val splitByActivityItems = if (
            chartData != null &&
            (canSplitByActivity || canComparisonSplitByActivity)
        ) {
            mapSplitByActivityItems(
                splitByActivity = splitByActivity,
                splitSortMode = splitSortMode,
                isDarkTheme = isDarkTheme,
            )
        } else {
            emptyList()
        }

        buttons += chartGroupingViewData
        buttons += chartLengthViewData
        // Update margin top depending on if it has buttons before.
        val hasButtonsBefore = buttons.lastOrNull() is ButtonsRowItemViewData
        val newMarginTopDp = if (hasButtonsBefore) -10 else 4
        buttons += splitByActivityItems.map {
            (it as? StatisticsDetailButtonViewData)?.copy(marginTopDp = newMarginTopDp) ?: it
        }

        val viewDataItems = listOfNotNull(chartData) +
            listOfNotNull(compareChartData) +
            buttons.mapItems() +
            rangeAverages.mapItems()

        return StatisticsDetailChartCompositeViewData(
            data = StatisticsDetailChartViewModelDelegate.mapToViewData(viewDataItems),
            appliedChartGrouping = appliedChartGrouping,
            appliedChartLength = appliedChartLength,
        )
    }

    fun mapToEmptyChartViewData(
        ranges: List<ChartBarDataRange>,
        availableChartGroupings: List<ChartGrouping>,
        availableChartLengths: List<ChartLength>,
    ): StatisticsDetailChartCompositeViewData {
        val emptyChart = StatisticsDetailChartViewData(
            data = emptyList(),
            legendSuffix = "",
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = false,
            showSelectedBarOnStart = false,
            selectedBarPosition = null,
            goalValue = 0f,
            yAxisZoomed = false,
            drawRoundCaps = true,
            useSingleColor = true,
            animate = OneShotValue(true),
        ).takeIf {
            ranges.size > 1
        }?.let {
            // TODO remove duplication
            StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.ChartData,
                singleColor = null,
                marginTopDp = 16,
                data = it,
            )
        }
        val chartGroupingViewData = if (availableChartGroupings.size > 1) {
            // TODO remove duplication
            ButtonsRowItemViewData(
                block = StatisticsDetailBlock.ChartGrouping,
                marginTopDp = 4,
                data = emptyList(),
            )
        } else {
            null
        }
        val chartLengthViewData = if (availableChartLengths.isNotEmpty()) {
            // TODO remove duplication
            ButtonsRowItemViewData(
                block = StatisticsDetailBlock.ChartLength,
                marginTopDp = -10,
                data = emptyList(),
            )
        } else {
            null
        }
        val rangeAverages = if (ranges.size < 2) {
            emptyList()
        } else {
            mapToEmptyRangeAverages()
        }.takeIf {
            it.isNotEmpty()
        }?.let {
            // TODO remove duplication
            StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.RangeAverages,
                title = " ",
                marginTopDp = 0,
                data = it,
            )
        }
        val viewDataItems = listOfNotNull(
            emptyChart?.mapItem(forComparison = false),
            chartGroupingViewData?.mapItem(),
            chartLengthViewData?.mapItem(),
            rangeAverages?.mapItem(),
        )

        return StatisticsDetailChartCompositeViewData(
            data = StatisticsDetailChartViewModelDelegate.mapToViewData(viewDataItems),
            appliedChartGrouping = ChartGrouping.DAILY,
            appliedChartLength = ChartLength.TEN,
        )
    }

    fun mapToDailyChartViewData(
        data: Map<Int, Float>,
        firstDayOfWeek: DayOfWeek,
    ): StatisticsDetailChartViewData {
        val days = timeMapper.getWeekOrder(firstDayOfWeek)

        val viewData = days.map { day ->
            val calendarDay = timeMapper.toCalendarDayOfWeek(day)
            BarChartView.ViewData(
                id = 0,
                value = listOf(data[calendarDay].orZero() to Color.TRANSPARENT),
                legend = timeMapper.toShortDayOfWeekName(day),
            )
        }

        return StatisticsDetailChartViewData(
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            selectedBarPosition = null,
            goalValue = 0f,
            yAxisZoomed = false,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    fun mapToHourlyChartViewData(
        data: Map<Int, Float>,
    ): StatisticsDetailChartViewData {
        val hourLegends = (0 until 24).map {
            it to it.toString()
        }

        val viewData = hourLegends
            .map { (hour, legend) ->
                BarChartView.ViewData(
                    id = 0,
                    value = listOf(data[hour].orZero() to Color.TRANSPARENT),
                    legend = legend,
                )
            }

        return StatisticsDetailChartViewData(
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            selectedBarPosition = null,
            goalValue = 0f,
            yAxisZoomed = false,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
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
        }.takeIf {
            it.isNotEmpty()
        }?.let {
            ButtonsRowItemViewData(
                block = StatisticsDetailBlock.SplitChartGrouping,
                marginTopDp = 4,
                data = it,
            )
        }.let(::listOfNotNull)
    }

    fun mapToDurationsSlipChartViewData(
        data: Map<Range, Float>,
    ): StatisticsDetailChartViewData {
        val viewData = data
            .map { (range, percent) ->
                val started = timeMapper.formatDuration(range.timeStarted / 1000)
                val ended = timeMapper.formatDuration(range.timeEnded / 1000)
                range to BarChartView.ViewData(
                    id = 0,
                    value = listOf(percent to Color.TRANSPARENT),
                    legend = ended,
                    selectedBarLegend = "$started - $ended",
                )
            }.sortedBy { (range, _) ->
                range.timeStarted
            }.map { (_, data) ->
                data
            }

        return StatisticsDetailChartViewData(
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            selectedBarPosition = null,
            goalValue = 0f,
            yAxisZoomed = false,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    @SuppressLint("DefaultLocale")
    fun processPercentageString(value: Float): String {
        val text = when {
            value >= 10 -> value.toLong()
            (value * 10).roundToLong() % 10L == 0L -> value.toLong()
            else -> String.format("%.1f", value)
        }
        return "$text%"
    }

    fun getRangeAverages(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        compareData: List<ChartBarDataDuration>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        chartGrouping: ChartGrouping,
        chartMode: ChartMode,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): Pair<String, List<StatisticsDetailCardInternalViewData>> {
        // No reason to show average of one value.
        if (data.size < 2 && compareData.size < 2) return "" to emptyList()

        fun getAverage(data: List<ChartBarDataDuration>): Float {
            if (data.isEmpty()) return 0f
            val sum = data.sumOf { point -> point.durations.sumOf { it.first } }.toFloat()
            return sum / data.size
        }

        @SuppressLint("DefaultLocale")
        fun formatDecimalValue(value: Float): String {
            val abs = abs(value)
            return when {
                abs >= 1000f -> value.toLong().toString()
                abs >= 100f -> String.format("%.1f", value)
                abs >= 10f -> String.format("%.2f", value)
                else -> String.format("%.3f", value)
            }.removeTrailingZeroes()
        }

        fun formatInterval(interval: Float): String {
            return when (chartMode) {
                is ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval.toLong(),
                    forceSeconds = showSeconds,
                    durationFormat = durationFormat,
                )
                is ChartMode.COUNTS -> formatDecimalValue(interval)
                is ChartMode.TAG_VALUE -> formatDecimalValue(interval / TAG_VALUE_PRECISION)
            }
        }

        fun filterNonEmptyData(
            data: List<ChartBarDataDuration>,
        ): List<ChartBarDataDuration> {
            return when (chartMode) {
                is ChartMode.DURATIONS,
                is ChartMode.COUNTS,
                -> data.filter { it.totalDuration.orZero() != 0L }
                is ChartMode.TAG_VALUE,
                -> data.filter { it.durations.isNotEmpty() }
            }
        }

        val average = getAverage(data)
        val nonEmptyData = filterNonEmptyData(data)
        val averageByNonEmpty = getAverage(nonEmptyData)

        val comparisonAverage = getAverage(compareData)
        val comparisonNonEmptyData = filterNonEmptyData(compareData)
        val comparisonAverageByNonEmpty = getAverage(comparisonNonEmptyData)

        val prevAverage = getAverage(prevData)
        val prevNonEmptyData = filterNonEmptyData(prevData)
        val prevAverageByNonEmpty = getAverage(prevNonEmptyData)

        val title = resourceRepo.getString(
            R.string.statistics_detail_range_averages_title,
            mapToGroupingName(chartGrouping),
        )

        val rangeAverages = listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(average),
                valueChange = mapValueChange(
                    average = average,
                    prevAverage = prevAverage,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = formatInterval(comparisonAverage)
                    .let { "($it)" }
                    .takeIf { showComparison }
                    .orEmpty(),
                description = resourceRepo.getString(R.string.statistics_detail_range_averages),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(averageByNonEmpty),
                valueChange = mapValueChange(
                    average = averageByNonEmpty,
                    prevAverage = prevAverageByNonEmpty,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = formatInterval(comparisonAverageByNonEmpty)
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
        average: Float,
        prevAverage: Float,
        rangeLength: RangeLength,
        isDarkTheme: Boolean,
    ): StatisticsDetailCardInternalViewData.ValueChange {
        if (rangeLength == RangeLength.All) {
            return StatisticsDetailCardInternalViewData.ValueChange.None
        }

        val change: Float = when {
            prevAverage.orZero() == 0f && average.orZero() == 0f -> 0f
            prevAverage.orZero() == 0f && average.orZero() > 0f -> 100f
            prevAverage.orZero() == 0f && average.orZero() < 0f -> -100f
            prevAverage != 0f -> (average.orZero() - prevAverage) * 100f / abs(prevAverage)
            else -> 0f
        }

        // Lowest precision is one decimal.
        @SuppressLint("DefaultLocale")
        fun formatChange(value: Float): String {
            val abs = abs(value)
            val text = when {
                abs >= 1_000_000f -> "∞"
                abs >= 1_000f -> "${(abs / 1000).toLong()}K"
                abs >= 10 -> abs.toLong().toString()
                (abs * 10).roundToLong() % 10L == 0L -> abs.toLong().toString()
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

    fun mapChartData(
        data: List<ChartBarDataDuration>,
        goal: Long,
        rangeLength: RangeLength,
        chartMode: ChartMode,
        yAxisZoomed: Boolean,
        showSelectedBarOnStart: Boolean,
        useSingleColor: Boolean,
        drawRoundCaps: Boolean,
    ): StatisticsDetailChartViewData? {
        if (data.size <= 1) return null

        val (legendSuffix, isMinutes) = when (chartMode) {
            is ChartMode.DURATIONS -> mapLegendSuffix(data)
            is ChartMode.COUNTS -> "" to false
            is ChartMode.TAG_VALUE -> "" to false
        }

        fun formatInterval(interval: Long): Float {
            return when (chartMode) {
                is ChartMode.DURATIONS -> formatInterval(interval, isMinutes)
                is ChartMode.COUNTS -> interval.toFloat()
                is ChartMode.TAG_VALUE -> interval.toFloat() / TAG_VALUE_PRECISION
            }
        }

        return StatisticsDetailChartViewData(
            data = data.map {
                val value = it.durations.map { (duration, color) ->
                    formatInterval(duration) to color
                }
                BarChartView.ViewData(
                    id = 0,
                    value = value,
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
            selectedBarPosition = null,
            goalValue = formatInterval(goal),
            yAxisZoomed = yAxisZoomed,
            useSingleColor = useSingleColor,
            drawRoundCaps = drawRoundCaps,
            animate = OneShotValue(true),
        )
    }

    fun mapLegendSuffix(
        data: List<ChartBarDataDuration>,
    ): Pair<String, Boolean> {
        val isMinutes = data
            .maxOfOrNull { barPart -> abs(barPart.totalDuration.orZero()) }
            .orZero()
            .let(TimeUnit.MILLISECONDS::toHours) == 0L

        val legendSuffix = if (isMinutes) {
            R.string.statistics_detail_legend_minute_suffix
        } else {
            R.string.statistics_detail_legend_hour_suffix
        }.let(resourceRepo::getString)

        return legendSuffix to isMinutes
    }

    fun formatInterval(interval: Long, isMinutes: Boolean): Float {
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

    fun mapToChartMode(
        goal: RecordTypeGoal?,
    ): ChartMode {
        return when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> ChartMode.DURATIONS
            is RecordTypeGoal.Type.Count -> ChartMode.COUNTS
            null -> ChartMode.DURATIONS
        }
    }

    private fun mapSplitByActivityItems(
        splitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return StatisticsDetailButtonViewData(
            marginTopDp = 0, // Set later depending on previous items in list.
            data = StatisticsDetailButtonViewData.Button(
                block = StatisticsDetailBlock.ChartSplitByActivity,
                text = resourceRepo.getString(R.string.statistics_detail_chart_split),
                color = if (splitByActivity) {
                    resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme)
                } else {
                    resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme)
                },
            ),
            dataSecond = if (splitByActivity) {
                StatisticsDetailButtonViewData.Button(
                    block = StatisticsDetailBlock.ChartSplitByActivitySort,
                    text = when (splitSortMode) {
                        ChartSplitSortMode.ACTIVITY_ORDER ->
                            resourceRepo.getString(R.string.settings_sort_activity)
                        ChartSplitSortMode.DURATION ->
                            resourceRepo.getString(R.string.records_all_sort_duration)
                    },
                    color = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
                )
            } else {
                null
            },
        ).let(::listOf)
    }

    fun mapToChartGroupingViewData(
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
    ): List<ViewHolderType> {
        return availableChartGroupings.map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                isSelected = it == appliedChartGrouping,
                textSizeSp = if (availableChartGroupings.size >= 3) 12 else null,
            )
        }
    }

    fun mapToChartLengthViewData(
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

    private fun mapFilteredState(
        state: StatisticsDetailPreviewViewData,
        isDarkTheme: Boolean,
    ): StatisticsDetailPreviewViewData {
        val isFiltered = state.isFiltered
        return state.copy(
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                state.color
            },
            iconColor = colorMapper.toIconColor(isDarkTheme = isDarkTheme, isFiltered = isFiltered),
            iconAlpha = colorMapper.toIconAlpha(icon = state.iconId, isFiltered = isFiltered),
        )
    }

    companion object {
        private const val SPLIT_CHART_LEGEND = "%"
    }
}