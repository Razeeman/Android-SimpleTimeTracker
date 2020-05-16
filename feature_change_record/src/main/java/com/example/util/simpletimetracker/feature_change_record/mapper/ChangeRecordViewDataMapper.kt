package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(record: Record, recordType: RecordType): ChangeRecordViewData {
        return ChangeRecordViewData(
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

    fun mapToEmpty(): ChangeRecordViewData {
        return ChangeRecordViewData(
            name = "",
            timeStarted = "",
            timeFinished = "",
            duration = "",
            iconId = R.drawable.ic_unknown,
            color = ColorMapper.availableColors
                .random()
                .let(resourceRepo::getColor)
        )
    }
}