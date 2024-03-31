package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun map(
        record: Record,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        return RecordViewData.Tracked(
            id = record.id,
            timeStartedTimestamp = record.timeStarted,
            timeEndedTimestamp = record.timeEnded,
            name = recordType.name,
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
            duration = timeMapper.formatIntervalAdjusted(
                timeStarted = record.timeStarted,
                timeEnded = record.timeEnded,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            ),
            iconId = iconMapper.mapIcon(recordType.icon),
            color = colorMapper.mapToColorInt(
                color = recordType.color,
                isDarkTheme = isDarkTheme,
            ),
            comment = record.comment,
        )
    }

    fun mapToUntracked(
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Untracked {
        return RecordViewData.Untracked(
            name = resourceRepo.getString(R.string.untracked_time_name),
            timeStarted = timeMapper.formatTime(
                time = timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeStartedTimestamp = timeStarted,
            timeFinished = timeMapper.formatTime(
                time = timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeEndedTimestamp = timeEnded,
            duration = timeMapper.formatIntervalAdjusted(
                timeStarted = timeStarted,
                timeEnded = timeEnded,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            ),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.no_data.let(resourceRepo::getString),
        )
    }

    fun mapToNoRecords(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.no_records_exist),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }
}