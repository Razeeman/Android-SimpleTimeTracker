package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
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
        recordTypes: List<RecordType>
    ): List<ViewHolderType> {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()

        val sumDuration = statistics.map(Statistics::duration).sum()

        return statistics
            .mapNotNull { statistic ->
                (map(statistic, sumDuration, recordTypesMap[statistic.typeId])
                    ?: return@mapNotNull null) to statistic.duration
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun mapToChart(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>,
        recordTypesFiltered: List<Long>
    ): ViewHolderType {
        val recordTypesMap = recordTypes
            .filterNot { it.id in recordTypesFiltered }
            .map { it.id to it }
            .toMap()

        return StatisticsChartViewData(
            statistics
                .mapNotNull { statistic ->
                    (mapToChart(statistic, recordTypesMap[statistic.typeId])
                        ?: return@mapNotNull null) to statistic.duration
                }
                .sortedByDescending { it.second }
                .map { it.first }
        )
    }

    private fun map(
        statistics: Statistics,
        sumDuration: Long,
        recordType: RecordType?
    ): StatisticsViewData? {
        val durationPercent = (statistics.duration * 100 / sumDuration)

        when {
            statistics.typeId == -1L -> {
                return StatisticsViewData(
                    name = "Untracked",
                    duration = statistics.duration
                        .let(timeMapper::formatInterval),
                    percent = "$durationPercent%",
                    iconId = R.drawable.ic_unknown,
                    color = R.color.blue_grey_200
                        .let(resourceRepo::getColor)
                )
            }
            recordType != null -> {
                return StatisticsViewData(
                    name = recordType.name,
                    duration = statistics.duration
                        .let(timeMapper::formatInterval),
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
                    colorInt = R.color.blue_grey_200
                        .let(resourceRepo::getColor),
                    iconId = R.drawable.ic_unknown
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
}