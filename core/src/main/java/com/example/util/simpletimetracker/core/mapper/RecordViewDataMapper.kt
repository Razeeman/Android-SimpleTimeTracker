package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
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
    ): RecordViewData.Tracked {
        return RecordViewData.Tracked(
            id = record.id,
            timeStartedTimestamp = timeStarted,
            timeEndedTimestamp = timeEnded,
            name = recordType.name,
            tagName = recordTags
                .getFullName(),
            timeStarted = timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeFinished = timeEnded
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            duration = (timeEnded - timeStarted)
                .let { timeMapper.formatInterval(it, useProportionalMinutes) },
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            comment = record.comment
        )
    }

    fun mapToUntracked(
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
    ): RecordViewData.Untracked {
        return RecordViewData.Untracked(
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            timeStarted = timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeStartedTimestamp = timeStarted,
            timeFinished = timeEnded
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeEndedTimestamp = timeEnded,
            duration = (timeEnded - timeStarted)
                .let { timeMapper.formatInterval(it, useProportionalMinutes) },
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }
}