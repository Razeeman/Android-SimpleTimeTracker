package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class RecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        record: Record,
        recordType: RecordType,
        rangeStart: Long = 0L,
        rangeEnd: Long = 0L
    ): RecordViewData {
        val timeStarted = if (rangeStart != 0L) {
            max(record.timeStarted, rangeStart)
        } else {
            record.timeStarted
        }
        val timeEnded = if (rangeEnd != 0L) {
            min(record.timeEnded, rangeEnd)
        } else {
            record.timeEnded
        }

        return RecordViewData(
            id = record.id,
            name = recordType.name,
            timeStarted = timeStarted
                .let(timeMapper::formatTime),
            timeFinished = timeEnded
                .let(timeMapper::formatTime),
            duration = (timeEnded - timeStarted)
                .let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}