package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
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
            name = recordType?.name
                ?: resourceRepo.getString(R.string.untracked_time_name),
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
            dateTimeStarted = timeMapper.getFormattedDateTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            dateTimeFinished = timeMapper.getFormattedDateTime(
                time = record.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            duration = timeMapper.formatInterval(
                interval = recordViewDataMapper.mapDuration(
                    record = record,
                    showSeconds = showSeconds,
                ),
                forceSeconds = showSeconds,
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

    fun mapSimple(
        preview: ChangeRecordViewData,
        showTimeEnded: Boolean,
        timeStartedChanged: Boolean,
        timeEndedChanged: Boolean,
    ): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.name,
            timeStarted = preview.timeStarted,
            timeEnded = if (showTimeEnded) {
                preview.timeFinished
            } else {
                ""
            },
            timeStartedChanged = timeStartedChanged,
            timeEndedChanged = timeEndedChanged,
            duration = preview.duration,
            iconId = preview.iconId,
            color = preview.color,
        )
    }
}