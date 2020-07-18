package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsRangeViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>,
        recordTypesFiltered: List<Long>,
        showDuration: Boolean
    ): List<ViewHolderType> {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()

        val sumDuration = statistics
            .filterNot { it.typeId in recordTypesFiltered }
            .map(Statistics::duration)
            .sum()

        return statistics
            .filterNot { it.typeId in recordTypesFiltered }
            .mapNotNull { statistic ->
                (map(statistic, sumDuration, recordTypesMap[statistic.typeId], showDuration)
                    ?: return@mapNotNull null) to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    fun mapToChart(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>,
        recordTypesFiltered: List<Long>
    ): ViewHolderType {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()

        return StatisticsChartViewData(
            statistics
                .filterNot { it.typeId in recordTypesFiltered }
                .mapNotNull { statistic ->
                    (mapToChart(statistic, recordTypesMap[statistic.typeId])
                        ?: return@mapNotNull null) to statistic.duration
                }
                .sortedByDescending { (_, duration) -> duration }
                .map { (statistics, _) -> statistics }
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.statistics_empty.let(resourceRepo::getString)
        )
    }

    fun mapToRanges(): List<StatisticsRangeViewData> {
        return listOf(
            RangeLength.ALL,
            RangeLength.MONTH,
            RangeLength.WEEK,
            RangeLength.DAY
        ).map {
            StatisticsRangeViewData(
                rangeLength = it,
                name = mapToRangeName(it)
            )
        }
    }

    private fun map(
        statistics: Statistics,
        sumDuration: Long,
        recordType: RecordType?,
        showDuration: Boolean
    ): StatisticsViewData? {
        val durationPercent = (statistics.duration * 100 / sumDuration)

        when {
            statistics.typeId == -1L -> {
                return StatisticsViewData(
                    typeId = statistics.typeId,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    duration = statistics.duration
                        .let(timeMapper::formatInterval),
                    percent = "$durationPercent%",
                    iconId = R.drawable.unknown,
                    color = R.color.untracked_time_color
                        .let(resourceRepo::getColor)
                )
            }
            recordType != null -> {
                return StatisticsViewData(
                    typeId = statistics.typeId,
                    name = recordType.name,
                    duration = if (showDuration) {
                        statistics.duration.let(timeMapper::formatInterval)
                    } else {
                        ""
                    },
                    percent = "$durationPercent%",
                    iconId = recordType.icon
                        .let(iconMapper::mapToDrawableResId),
                    color = recordType.color
                        .let(colorMapper::mapToColorResId)
                        .let(resourceRepo::getColor)
                )
            }
            else -> {
                return null
            }
        }
    }

    private fun mapToChart(
        statistics: Statistics,
        recordType: RecordType?
    ): PiePortion? {
        return when {
            statistics.typeId == -1L -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = R.color.untracked_time_color
                        .let(resourceRepo::getColor),
                    iconId = R.drawable.unknown
                )
            }
            recordType != null -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = recordType.color
                        .let(colorMapper::mapToColorResId)
                        .let(resourceRepo::getColor),
                    iconId = recordType.icon
                        .let(iconMapper::mapToDrawableResId)
                )
            }
            else -> {
                null
            }
        }
    }

    private fun mapToRangeName(rangeLength: RangeLength): String {
        return when (rangeLength) {
            RangeLength.DAY -> R.string.title_today
            RangeLength.WEEK -> R.string.title_this_week
            RangeLength.MONTH -> R.string.title_this_month
            RangeLength.ALL -> R.string.title_overall
        }.let(resourceRepo::getString)
    }
}