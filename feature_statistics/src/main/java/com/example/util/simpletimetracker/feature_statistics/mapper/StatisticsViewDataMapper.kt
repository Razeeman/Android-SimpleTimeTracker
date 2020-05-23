package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
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
            .sortedByDescending { it.duration }
            .mapNotNull {
                map(
                    statistics = it,
                    sumDuration = sumDuration,
                    recordType = recordTypesMap[it.typeId] ?: return@mapNotNull null
                )
            }
    }

    fun mapToChart(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>
    ): ViewHolderType {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()

        return StatisticsChartViewData(
            statistics
                .sortedByDescending { it.duration }
                .mapNotNull {
                PiePortion(
                    value = it.duration,
                    colorInt = recordTypesMap[it.typeId]?.color
                        ?.let(colorMapper::mapToColorResId)
                        ?.let(resourceRepo::getColor)
                        ?: return@mapNotNull null,
                    iconId = recordTypesMap[it.typeId]?.icon
                        ?.let(iconMapper::mapToDrawableResId)
                )
            }
        )
    }

    private fun map(
        statistics: Statistics,
        sumDuration: Long,
        recordType: RecordType
    ): StatisticsViewData {
        val durationPercent = (statistics.duration * 100 / sumDuration)
        return StatisticsViewData(
            name = recordType.name,
            duration = statistics.duration.let(timeMapper::formatInterval),
            percent = "$durationPercent%",
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}