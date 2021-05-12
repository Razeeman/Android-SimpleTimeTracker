package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        record: Record?,
        recordType: RecordType?,
        recordTag: RecordTag?,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): ChangeRecordViewData {
        return ChangeRecordViewData(
            name = recordType?.name.orEmpty(),
            tagName = recordTag?.name.orEmpty(),
            timeStarted = record?.timeStarted
                ?.let { timeMapper.formatTime(it, useMilitaryTime) }
                .orEmpty(),
            timeFinished = record?.timeEnded
                ?.let { timeMapper.formatTime(it, useMilitaryTime) }
                .orEmpty(),
            dateTimeStarted = record?.timeStarted
                ?.let { timeMapper.formatDateTime(it, useMilitaryTime) }
                .orEmpty(),
            dateTimeFinished = record?.timeEnded
                ?.let { timeMapper.formatDateTime(it, useMilitaryTime) }
                .orEmpty(),
            duration = record
                ?.let { it.timeEnded - it.timeStarted }
                ?.let(timeMapper::formatInterval)
                .orEmpty(),
            iconId = recordType?.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = recordType?.color
                ?.let { colorMapper.mapToColorResId(it, isDarkTheme) }
                ?.let(resourceRepo::getColor)
                ?: colorMapper.toUntrackedColor(isDarkTheme),
            comment = record?.comment
                .orEmpty()
        )
    }
}