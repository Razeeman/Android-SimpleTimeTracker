package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
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
        recordType: RecordType
    ): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            timeStarted = runningRecord.timeStarted
                .let(timeMapper::formatTime),
            timer = (System.currentTimeMillis() - runningRecord.timeStarted)
                .let(timeMapper::formatIntervalWithSeconds),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.map(recordType, numberOfCards).copy(
            color = if (isFiltered) {
                R.color.filtered_color
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString)
        )
    }

    fun mapToAddItem(
        numberOfCards: Int
    ): RunningRecordTypeAddViewData {
        return RunningRecordTypeAddViewData(
            name = R.string.running_records_add_type.let(resourceRepo::getString),
            iconId = R.drawable.add,
            color = R.color.blue_grey_200
                .let(resourceRepo::getColor),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}