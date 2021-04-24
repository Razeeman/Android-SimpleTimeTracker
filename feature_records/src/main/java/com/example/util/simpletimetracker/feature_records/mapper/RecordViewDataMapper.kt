package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records.R
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
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): ViewHolderType {
        val (timeStarted, timeEnded) = clampToRange(record, rangeStart, rangeEnd)

        return RecordViewData.Tracked(
            id = record.id,
            name = recordType.name,
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
        record: Record,
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): RecordViewData {
        val (timeStarted, timeEnded) = clampToRange(record, rangeStart, rangeEnd)

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
                .let(timeMapper::formatInterval),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            comment = ""
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.records_empty.let(resourceRepo::getString)
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.records_hint.let(resourceRepo::getString)
        )
    }

    private fun clampToRange(
        record: Record,
        rangeStart: Long,
        rangeEnd: Long
    ): Pair<Long, Long> {
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

        return timeStarted to timeEnded
    }
}