package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsViewData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
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
            .let(::formatInterval)
    }

    // TODO move to core mapper
    private fun formatInterval(interval: Long): String {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        // TODO remove 0s if empty
        return String.format("%2dh %2dm %2ds", hr, min, sec)
    }
}