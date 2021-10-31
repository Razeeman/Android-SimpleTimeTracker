package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper,
) {

    fun mapItemsList(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        filteredIds: List<Long>,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
    ): List<StatisticsViewData> {
        val statisticsFiltered = statistics.filterNot { it.id in filteredIds }
        val sumDuration = statisticsFiltered.map(Statistics::duration).sum()
        val statisticsSize = statisticsFiltered.size

        return statisticsFiltered
            .mapNotNull { statistic ->
                (
                    mapItem(
                        statistics = statistic,
                        sumDuration = sumDuration,
                        dataHolder = data[statistic.id],
                        showDuration = showDuration,
                        isDarkTheme = isDarkTheme,
                        statisticsSize = statisticsSize,
                        useProportionalMinutes = useProportionalMinutes,
                    ) ?: return@mapNotNull null
                    ) to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    fun mapChart(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        recordTypesFiltered: List<Long>,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        return StatisticsChartViewData(
            statistics
                .filterNot { it.id in recordTypesFiltered }
                .mapNotNull { statistic ->
                    (
                        mapChart(
                            statistics = statistic,
                            dataHolder = data[statistic.id],
                            isDarkTheme = isDarkTheme
                        ) ?: return@mapNotNull null
                        ) to statistic.duration
                }
                .sortedByDescending { (_, duration) -> duration }
                .map { (statistics, _) -> statistics }
        )
    }

    fun mapStatisticsTotalTracked(
        statistics: List<Statistics>,
        filteredIds: List<Long>,
        useProportionalMinutes: Boolean,
    ): ViewHolderType {
        val statisticsFiltered = statistics
            .filterNot { it.id in filteredIds || it.id == -1L }
        val totalTracked = statisticsFiltered.map(Statistics::duration).sum()

        return mapTotalTracked(totalTracked, useProportionalMinutes)
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.statistics_empty.let(resourceRepo::getString)
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.statistics_hint.let(resourceRepo::getString)
        )
    }

    private fun mapItem(
        statistics: Statistics,
        sumDuration: Long,
        dataHolder: StatisticsDataHolder?,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean,
    ): StatisticsViewData? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.duration,
            statisticsSize = statisticsSize
        )

        when {
            statistics.id == -1L -> {
                return StatisticsViewData(
                    id = statistics.id,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    duration = statistics.duration
                        .let { timeMapper.formatInterval(it, useProportionalMinutes) },
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.unknown),
                    color = colorMapper.toUntrackedColor(isDarkTheme)
                )
            }
            dataHolder != null -> {
                return StatisticsViewData(
                    id = statistics.id,
                    name = dataHolder.name,
                    duration = if (showDuration) {
                        timeMapper.formatInterval(statistics.duration, useProportionalMinutes)
                    } else {
                        ""
                    },
                    percent = durationPercent,
                    icon = dataHolder.icon
                        ?.let(iconMapper::mapIcon),
                    color = dataHolder.color
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor)
                )
            }
            else -> {
                return null
            }
        }
    }

    private fun mapChart(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
    ): PiePortion? {
        return when {
            statistics.id == -1L -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = colorMapper.toUntrackedColor(isDarkTheme),
                    iconId = RecordTypeIcon.Image(R.drawable.unknown)
                )
            }
            dataHolder != null -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = dataHolder.color
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor),
                    iconId = dataHolder.icon
                        ?.let(iconMapper::mapIcon)
                )
            }
            else -> {
                null
            }
        }
    }

    private fun mapTotalTracked(totalTracked: Long, useProportionalMinutes: Boolean): ViewHolderType {
        return StatisticsInfoViewData(
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            text = timeMapper.formatInterval(totalTracked, useProportionalMinutes)
        )
    }
}