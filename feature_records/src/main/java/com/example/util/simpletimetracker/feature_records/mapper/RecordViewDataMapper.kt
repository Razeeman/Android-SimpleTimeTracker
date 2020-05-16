package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        record: Record,
        recordType: RecordType
    ): RecordViewData {
        return RecordViewData(
            id = record.id,
            name = recordType.name,
            timeStarted = record.timeStarted
                .let(timeMapper::formatTime),
            timeFinished = record.timeEnded
                .let(timeMapper::formatTime),
            duration = (record.timeEnded - record.timeStarted)
                .let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}