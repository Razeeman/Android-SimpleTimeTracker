package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsViewData
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        records: List<Record>,
        recordTypes: List<RecordType>
    ): List<StatisticsViewData> {
        val recordTypesMap = recordTypes
            .map { it.id to it }
            .toMap()
        val recordsMap = records
            .groupBy { it.typeId }

        return recordsMap
            .mapNotNull { entry ->
                map(
                    records = entry.value,
                    recordType = recordTypesMap[entry.key] ?: return@mapNotNull null
                )
            }
            .sortedByDescending { it.duration }
    }

    private fun map(
        records: List<Record>,
        recordType: RecordType
    ): StatisticsViewData {
        return StatisticsViewData(
            name = recordType.name,
            duration = mapToDuration(records),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }

    private fun mapToDuration(records: List<Record>): String {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
            .let(timeMapper::formatInterval)
    }
}