package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        record: Record,
        recordType: RecordType,
        recordTag: RecordTag?,
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): RecordViewData {
        return RecordViewData.Tracked(
            id = record.id,
            name = recordType.name,
            tagName = recordTag?.name.orEmpty(),
            timeStarted = timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeFinished = timeEnded
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            duration = (timeEnded - timeStarted)
                .let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor),
            comment = record.comment
        )
    }

    fun mapToUntracked(
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): RecordViewData {
        return RecordViewData.Untracked(
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            tagName = "",
            timeStarted = timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeStartedTimestamp = timeStarted,
            timeFinished = timeEnded
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeEndedTimestamp = timeEnded,
            duration = (timeEnded - timeStarted)
                .let(timeMapper::formatInterval),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            comment = ""
        )
    }
}