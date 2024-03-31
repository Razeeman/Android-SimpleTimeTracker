package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): ChangeRecordViewData {
        return ChangeRecordViewData(
            name = recordType?.name.orEmpty(),
            tagName = recordTags
                .getFullName(),
            timeStarted = timeMapper.formatTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeFinished = timeMapper.formatTime(
                time = record.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            dateTimeStarted = timeMapper.formatDateTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            dateTimeFinished = timeMapper.formatDateTime(
                time = record.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            duration = timeMapper.formatIntervalAdjusted(
                timeStarted = record.timeStarted,
                timeEnded = record.timeEnded,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            ),
            iconId = recordType?.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = recordType?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: colorMapper.toUntrackedColor(isDarkTheme),
            comment = record.comment,
        )
    }
}