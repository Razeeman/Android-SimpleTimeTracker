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

    fun map(record: Record?, recordType: RecordType?): ChangeRecordViewData {
        return ChangeRecordViewData(
            name = recordType?.name.orEmpty(),
            timeStarted = record?.timeStarted
                ?.let(timeMapper::formatTime)
                .orEmpty(),
            timeFinished = record?.timeEnded
                ?.let(timeMapper::formatTime)
                .orEmpty(),
            dateTimeStarted = record?.timeStarted
                ?.let(timeMapper::formatDateTime)
                .orEmpty(),
            dateTimeFinished = record?.timeEnded
                ?.let(timeMapper::formatDateTime)
                .orEmpty(),
            duration = record
                ?.let { it.timeEnded - it.timeStarted }
                ?.let(timeMapper::formatInterval)
                .orEmpty(),
            iconId = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown,
            color = (recordType?.color
                ?.let(colorMapper::mapToColorResId)
                ?: ColorMapper.availableColors.random())
                .let(resourceRepo::getColor)
        )
    }
}