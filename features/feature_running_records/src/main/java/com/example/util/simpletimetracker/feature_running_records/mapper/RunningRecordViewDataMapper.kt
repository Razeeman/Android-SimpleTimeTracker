package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.GoalTimeMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val goalTimeMapper: GoalTimeMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
) {

    fun map(
        runningRecord: RunningRecord,
        dailyCurrent: Long,
        weeklyCurrent: Long,
        monthlyCurrent: Long,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): RunningRecordViewData {
        val currentDuration = System.currentTimeMillis() - runningRecord.timeStarted
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            tagName = recordTags
                .getFullName(),
            timeStarted = runningRecord.timeStarted
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            timer = currentDuration
                .let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = true,
                        useProportionalMinutes = false,
                    )
                },
            goalTime = goalTimeMapper.map(
                goalTime = recordType.goalTime,
                current = currentDuration,
                type = GoalTimeType.Session
            ),
            goalTime2 = goalTimeMapper.map(
                goalTime = recordType.dailyGoalTime,
                current = dailyCurrent,
                type = GoalTimeType.Day,
            ),
            goalTime3 = goalTimeMapper.map(
                goalTime = recordType.weeklyGoalTime,
                current = weeklyCurrent,
                type = GoalTimeType.Week,
            ),
            goalTime4 = goalTimeMapper.map(
                goalTime = recordType.monthlyGoalTime,
                current = monthlyCurrent,
                type = GoalTimeType.Month,
            ),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            comment = runningRecord.comment
        )
    }

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean,
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
        isDarkTheme: Boolean,
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