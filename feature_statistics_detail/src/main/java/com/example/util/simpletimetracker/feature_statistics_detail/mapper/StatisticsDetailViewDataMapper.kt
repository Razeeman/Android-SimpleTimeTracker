package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun mapStatsData(
        records: List<Record>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): StatisticsDetailStatsViewData {
        val recordsSorted = records.sortedBy { it.timeStarted }
        val durations = records.map(::mapToDuration)
        val totalDuration = durations.sum()
        val timesTracked = records.size
        val shortest = durations.min()
        val average = if (durations.isNotEmpty()) durations.sum() / durations.size else null
        val longest = durations.max()
        val first = recordsSorted.firstOrNull()?.timeStarted
        val last = recordsSorted.lastOrNull()?.timeEnded
        val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

        val recordsAllIcon = StatisticsDetailCardViewData.Icon(
            iconDrawable = R.drawable.statistics_detail_records_all,
            iconColor = if (isDarkTheme) {
                R.color.colorInactiveDark
            } else {
                R.color.colorInactive
            }.let(resourceRepo::getColor)
        )

        return mapToStatsViewData(
            totalDuration = totalDuration
                .let(timeMapper::formatInterval),
            timesTracked = timesTracked,
            timesTrackedIcon = recordsAllIcon,
            shortestRecord = shortest
                ?.let(timeMapper::formatInterval)
                ?: emptyValue,
            averageRecord = average
                ?.let(timeMapper::formatInterval)
                ?: emptyValue,
            longestRecord = longest
                ?.let(timeMapper::formatInterval)
                ?: emptyValue,
            firstRecord = first
                ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
                ?: emptyValue,
            lastRecord = last
                ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
                ?: emptyValue
        )
    }

    fun mapToEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return mapToStatsViewData(
            totalDuration = "",
            timesTracked = null,
            timesTrackedIcon = null,
            shortestRecord = "",
            averageRecord = "",
            longestRecord = "",
            firstRecord = "",
            lastRecord = ""
        )
    }

    fun mapToPreview(
        recordType: RecordType,
        isDarkTheme: Boolean,
        isFirst: Boolean
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = recordType.id,
            name = recordType.name.takeIf { isFirst }.orEmpty(),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    fun mapToPreview(
        category: Category,
        isDarkTheme: Boolean
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = category.id,
            name = category.name,
            iconId = null,
            color = category.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    fun mapToPreviewEmpty(
        isDarkTheme: Boolean
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = 0,
            name = "",
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme)
        )
    }

    fun mapToChartViewData(
        data: List<ChartBarDataDuration>,
        rangeLength: RangeLength
    ): StatisticsDetailChartViewData {
        val isMinutes = data.map(ChartBarDataDuration::duration)
            .max().orZero()
            .let(TimeUnit.MILLISECONDS::toHours) == 0L

        val legendSuffix = if (isMinutes) {
            R.string.statistics_detail_legend_minute_suffix
        } else {
            R.string.statistics_detail_legend_hour_suffix
        }.let(resourceRepo::getString)

        return StatisticsDetailChartViewData(
            visible = rangeLength != RangeLength.DAY,
            data = data.map {
                BarChartView.ViewData(
                    value = formatInterval(it.duration, isMinutes),
                    legend = it.legend
                )
            },
            legendSuffix = legendSuffix,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = when (rangeLength) {
                RangeLength.DAY -> false
                RangeLength.WEEK -> true
                RangeLength.MONTH -> false
                RangeLength.YEAR -> data.size <= 12
                RangeLength.ALL -> data.size <= 10
            }
        )
    }

    fun mapToDailyChartViewData(
        data: Map<Int, Float>,
        firstDayOfWeek: DayOfWeek
    ): StatisticsDetailChartViewData {
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        ).let { list ->
            list.indexOf(firstDayOfWeek)
                .takeUnless { it == -1 }.orZero()
                .let(list::rotateLeft)
        }

        val viewData = days.map { day ->
            val calendarDay = timeMapper.toCalendarDayOfWeek(day)
            BarChartView.ViewData(
                value = data[calendarDay].orZero(),
                legend = timeMapper.toShortDayOfWeekName(day)
            )
        }

        return StatisticsDetailChartViewData(
            visible = true,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true
        )
    }

    fun mapToHourlyChartViewData(
        data: Map<Int, Float>
    ): StatisticsDetailChartViewData {
        val hourLegends = (0 until 24).map {
            it to it.toString()
        }

        val viewData = hourLegends
            .map { (hour, legend) ->
                BarChartView.ViewData(
                    value = data[hour].orZero(),
                    legend = legend
                )
            }

        return StatisticsDetailChartViewData(
            visible = true,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true
        )
    }

    fun mapToChartGroupingViewData(
        rangeLength: RangeLength,
        chartGrouping: ChartGrouping
    ): List<ViewHolderType> {
        val groupings = when (rangeLength) {
            RangeLength.YEAR -> listOf(
                ChartGrouping.DAILY,
                ChartGrouping.WEEKLY,
                ChartGrouping.MONTHLY
            )
            RangeLength.ALL -> listOf(
                ChartGrouping.DAILY,
                ChartGrouping.WEEKLY,
                ChartGrouping.MONTHLY,
                ChartGrouping.YEARLY
            )
            else -> emptyList()
        }

        return groupings.map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                isSelected = it == chartGrouping
            )
        }
    }

    fun mapToSplitChartGroupingViewData(
        rangeLength: RangeLength,
        splitChartGrouping: SplitChartGrouping
    ): List<ViewHolderType> {
        val groupings = when (rangeLength) {
            RangeLength.DAY -> emptyList()
            else -> listOf(
                SplitChartGrouping.HOURLY,
                SplitChartGrouping.DAILY
            )
        }

        return groupings.map {
            StatisticsDetailSplitGroupingViewData(
                splitChartGrouping = it,
                name = mapToSplitGroupingName(it),
                isSelected = it == splitChartGrouping
            )
        }
    }

    fun mapToChartLengthViewData(
        rangeLength: RangeLength,
        chartLength: ChartLength
    ): List<ViewHolderType> {
        val lengths = when (rangeLength) {
            RangeLength.ALL -> listOf(
                ChartLength.TEN,
                ChartLength.FIFTY,
                ChartLength.HUNDRED
            )
            else -> emptyList()
        }

        return lengths.map {
            StatisticsDetailChartLengthViewData(
                chartLength = it,
                name = mapToLengthName(it),
                isSelected = it == chartLength
            )
        }
    }

    private fun mapToStatsViewData(
        totalDuration: String,
        timesTracked: Int?,
        timesTrackedIcon: StatisticsDetailCardViewData.Icon?,
        shortestRecord: String,
        averageRecord: String,
        longestRecord: String,
        firstRecord: String,
        lastRecord: String
    ): StatisticsDetailStatsViewData {
        return StatisticsDetailStatsViewData(
            totalDuration = listOf(
                StatisticsDetailCardViewData(
                    title = totalDuration,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_total_duration)
                )
            ),
            timesTracked = listOf(
                StatisticsDetailCardViewData(
                    title = timesTracked?.toString() ?: "",
                    subtitle = resourceRepo.getQuantityString(
                        R.plurals.statistics_detail_times_tracked, timesTracked.orZero()
                    ),
                    icon = timesTrackedIcon
                )
            ),
            averageRecord = listOf(
                StatisticsDetailCardViewData(
                    title = shortestRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_shortest_record)
                ),
                StatisticsDetailCardViewData(
                    title = averageRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_average_record)
                ),
                StatisticsDetailCardViewData(
                    title = longestRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_longest_record)
                )
            ),
            datesTracked = listOf(
                StatisticsDetailCardViewData(
                    title = firstRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_first_record)
                ),
                StatisticsDetailCardViewData(
                    title = lastRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_last_record)
                )
            )
        )
    }

    private fun formatInterval(interval: Long, isMinutes: Boolean): Float {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        return if (isMinutes) {
            min + sec / 60f
        } else {
            hr + min / 60f
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

    private fun mapToDuration(record: Record): Long {
        return record.let { it.timeEnded - it.timeStarted }
    }

    companion object {
        private const val SPLIT_CHART_LEGEND = "%"
    }
}