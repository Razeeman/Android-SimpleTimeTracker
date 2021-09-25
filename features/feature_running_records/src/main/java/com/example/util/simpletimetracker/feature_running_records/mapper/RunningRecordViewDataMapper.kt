package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper
) {

    fun map(
        runningRecord: RunningRecord,
        recordType: RecordType,
        recordTag: RecordTag?,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            tagName = recordTag?.name.orEmpty(),
            timeStarted = runningRecord.timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timer = (System.currentTimeMillis() - runningRecord.timeStarted)
                .let(timeMapper::formatIntervalWithForcedSeconds),
            goalTime = recordType.goalTime
                .takeIf { it > 0 }
                ?.let(timeMapper::formatDuration)
                ?.let { resourceRepo.getString(R.string.running_record_goal_time, it) }
                .orEmpty(),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor),
            comment = runningRecord.comment
        )
    }

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.mapFiltered(
            recordType,
            numberOfCards,
            isDarkTheme,
            isFiltered
        )
    }

    fun mapToTypesEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_types_empty.let(resourceRepo::getString)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString)
        )
    }

    fun mapToAddItem(
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RunningRecordTypeAddViewData {
        return RunningRecordTypeAddViewData(
            name = R.string.running_records_add_type.let(resourceRepo::getString),
            iconId = RecordTypeIcon.Image(R.drawable.add),
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}