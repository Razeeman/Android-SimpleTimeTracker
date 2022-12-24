package com.example.util.simpletimetracker.feature_change_running_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.GoalTimeMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val goalTimeMapper: GoalTimeMapper,
) {

    fun map(
        runningRecord: RunningRecord,
        dailyCurrent: Long,
        weeklyCurrent: Long,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): ChangeRunningRecordViewData {
        val currentDuration = System.currentTimeMillis() - runningRecord.timeStarted
        return ChangeRunningRecordViewData(
            name = recordType?.name.orEmpty(),
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
            dateTimeStarted = runningRecord.timeStarted
                .let {
                    timeMapper.formatDateTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            duration = currentDuration
                .let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = true,
                        useProportionalMinutes = false,
                    )
                },
            goalTime = goalTimeMapper.map(
                goalTime = recordType?.goalTime.orZero(),
                current = currentDuration,
                type = GoalTimeType.Session
            ),
            goalTime2 = goalTimeMapper.map(
                goalTime = recordType?.dailyGoalTime.orZero(),
                current = dailyCurrent + currentDuration,
                type = GoalTimeType.Day,
            ),
            goalTime3 = goalTimeMapper.map(
                goalTime = recordType?.weeklyGoalTime.orZero(),
                current = weeklyCurrent + currentDuration,
                type = GoalTimeType.Week,
            ),
            iconId = recordType?.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = recordType?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: ColorMapper.getAvailableColors().random().let(resourceRepo::getColor),
            comment = runningRecord.comment
        )
    }
}