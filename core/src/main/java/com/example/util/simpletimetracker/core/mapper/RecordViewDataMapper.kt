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
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        return RecordViewData.Tracked(
            id = record.id,
            timeStartedTimestamp = timeStarted,
            timeEndedTimestamp = timeEnded,
            name = recordType.name,
            tagName = recordTags
                .getFullName(),
            timeStarted = timeStarted
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            timeFinished = timeEnded
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            duration = (timeEnded - timeStarted)
                .let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    )
                },
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
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
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            timeStarted = timeStarted
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            timeStartedTimestamp = timeStarted,
            timeFinished = timeEnded
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            timeEndedTimestamp = timeEnded,
            duration = (timeEnded - timeStarted)
                .let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    )
                },
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

    fun mapToUntrackedTimeHint(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.change_record_untracked_time_hint),
            infoIconVisible = false,
            closeIconVisible = true,
        )
    }
}