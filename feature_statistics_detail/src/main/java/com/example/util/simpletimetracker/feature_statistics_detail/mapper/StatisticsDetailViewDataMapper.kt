package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.DailyChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        records: List<Record>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): StatisticsDetailViewData {
        val recordsSorted = records.sortedBy { it.timeStarted }
        val durations = records.map(::mapToDuration)
        val totalDuration = durations.sum()
        val timesTracked = records.size
        val shortest = durations.min().orZero()
        val average = if (records.isNotEmpty()) durations.sum() / durations.size else 0
        val longest = durations.max().orZero()
        val first = recordsSorted.firstOrNull()?.timeStarted
        val last = recordsSorted.lastOrNull()?.timeEnded

        val recordsAllIcon = StatisticsDetailCardViewData.Icon(
            iconDrawable = R.drawable.statistics_detail_records_all,
            iconColor = if (isDarkTheme) {
                R.color.colorInactiveDark
            } else {
                R.color.colorInactive
            }.let(resourceRepo::getColor)
        )

        return mapToViewData(
            totalDuration = totalDuration
                .let(timeMapper::formatInterval),
            timesTracked = timesTracked,
            timesTrackedIcon = recordsAllIcon,
            shortestRecord = shortest
                .let(timeMapper::formatInterval),
            averageRecord = average
                .let(timeMapper::formatInterval),
            longestRecord = longest
                .let(timeMapper::formatInterval),
            firstRecord = first
                ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
                .orEmpty(),
            lastRecord = last
                ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
                .orEmpty()
        )
    }

    fun mapToEmptyViewData(): StatisticsDetailViewData {
        return mapToViewData(
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
        name: String?,
        iconName: String?,
        colorId: Int?,
        isDarkTheme: Boolean
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            name = name
                .orEmpty(),
            iconId = iconName
                ?.let(iconMapper::mapToDrawableResId),
            color = colorId
                ?.let { colorMapper.mapToColorResId(it, isDarkTheme) }
                ?.let(resourceRepo::getColor)
                ?: colorMapper.toUntrackedColor(isDarkTheme)
        )
    }

    fun mapToPreviewUntracked(
        isDarkTheme: Boolean
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = R.drawable.unknown,
            color = colorMapper.toUntrackedColor(isDarkTheme)
        )
    }

    fun mapToChartViewData(
        data: List<ChartBarDataDuration>
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
            data = data.map {
                BarChartView.ViewData(
                    value = formatInterval(it.duration, isMinutes),
                    legend = it.legend
                )
            },
            legendSuffix = legendSuffix
        )
    }

    fun mapToDailyChartViewData(
        data: Map<DailyChartGrouping, Long>
    ): StatisticsDetailChartViewData {
        val dayLegends = mapOf(
            DailyChartGrouping.MONDAY to R.string.statistics_detail_chart_monday,
            DailyChartGrouping.TUESDAY to R.string.statistics_detail_chart_tuesday,
            DailyChartGrouping.WEDNESDAY to R.string.statistics_detail_chart_wednesday,
            DailyChartGrouping.THURSDAY to R.string.statistics_detail_chart_thursday,
            DailyChartGrouping.FRIDAY to R.string.statistics_detail_chart_friday,
            DailyChartGrouping.SATURDAY to R.string.statistics_detail_chart_saturday,
            DailyChartGrouping.SUNDAY to R.string.statistics_detail_chart_sunday
        )

        val viewData = dayLegends
            .map { (day, legendResId) ->
                BarChartView.ViewData(
                    value = data[day].orZero().toFloat(),
                    legend = legendResId.let(resourceRepo::getString)
                )
            }

        return StatisticsDetailChartViewData(
            data = viewData,
            legendSuffix = "%"
        )
    }

    fun mapToChartGroupingViewData(chartGrouping: ChartGrouping): List<ViewHolderType> {
        return listOf(
            ChartGrouping.DAILY,
            ChartGrouping.WEEKLY,
            ChartGrouping.MONTHLY,
            ChartGrouping.YEARLY
        ).map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                isSelected = it == chartGrouping
            )
        }
    }

    fun mapToChartLengthViewData(chartLength: ChartLength): List<ViewHolderType> {
        return listOf(
            ChartLength.TEN,
            ChartLength.FIFTY,
            ChartLength.HUNDRED
        ).map {
            StatisticsDetailChartLengthViewData(
                chartLength = it,
                name = mapToLengthName(it),
                isSelected = it == chartLength
            )
        }
    }

    private fun mapToViewData(
        totalDuration: String,
        timesTracked: Int?,
        timesTrackedIcon: StatisticsDetailCardViewData.Icon?,
        shortestRecord: String,
        averageRecord: String,
        longestRecord: String,
        firstRecord: String,
        lastRecord: String
    ): StatisticsDetailViewData {
        return StatisticsDetailViewData(
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
}