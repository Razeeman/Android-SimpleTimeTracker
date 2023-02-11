package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val goalTimeMapper: GoalTimeMapper,
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
}