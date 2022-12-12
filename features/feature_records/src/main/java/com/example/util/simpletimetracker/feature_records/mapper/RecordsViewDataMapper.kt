package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.R
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class RecordsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        val (timeStarted, timeEnded) = clampToRange(record, rangeStart, rangeEnd)

        return recordViewDataMapper.map(
            record = record,
            recordType = recordType,
            recordTags = recordTags,
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    fun mapToUntracked(
        record: Record,
        rangeStart: Long,
        rangeEnd: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RecordViewData.Untracked {
        val (timeStarted, timeEnded) = clampToRange(record, rangeStart, rangeEnd)

        return recordViewDataMapper.mapToUntracked(
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
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
        rangeEnd: Long,
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