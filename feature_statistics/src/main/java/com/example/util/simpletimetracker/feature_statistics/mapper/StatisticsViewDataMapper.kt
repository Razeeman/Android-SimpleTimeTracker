package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsViewData
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
    ): List<StatisticsViewData> {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()

        return statistics
            .sortedByDescending { it.duration }
            .mapNotNull {
                map(
                    statistics = it,
                    recordType = recordTypesMap[it.typeId] ?: return@mapNotNull null
                )
            }
    }

    private fun map(
        statistics: Statistics,
        recordType: RecordType
    ): StatisticsViewData {
        return StatisticsViewData(
            name = recordType.name,
            duration = statistics.duration.let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}