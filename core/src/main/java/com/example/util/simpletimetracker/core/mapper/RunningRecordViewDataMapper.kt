package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.extension.getSessionDuration
import com.example.util.simpletimetracker.domain.extension.hasDailyDuration
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val goalTimeMapper: GoalTimeMapper,
) {

    fun map(
        runningRecord: RunningRecord,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        goals: List<RecordTypeGoal>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        useProportionalMinutes: Boolean,
        nowIconVisible: Boolean,
        goalsVisible: Boolean,
        totalDurationVisible: Boolean,
    ): RunningRecordViewData {
        val currentDuration = System.currentTimeMillis() - runningRecord.timeStarted

        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            tagName = recordTags
                .getFullName(),
            timeStarted = timeMapper.formatTime(
                time = runningRecord.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timer = timeMapper.formatInterval(
                interval = currentDuration,
                forceSeconds = true,
                useProportionalMinutes = false,
            ),
            timerTotal = mapTotalDuration(
                dailyCurrent = dailyCurrent,
                totalDurationVisible = totalDurationVisible,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            ),
            goalTime = mapGoalTime(
                currentDuration = currentDuration,
                goals = goals,
                dailyCurrent = dailyCurrent,
                goalsVisible = goalsVisible,
            ),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            comment = runningRecord.comment,
            nowIconVisible = nowIconVisible,
        )
    }

    private fun mapTotalDuration(
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        totalDurationVisible: Boolean,
        showSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        if (!totalDurationVisible) return ""
        if (dailyCurrent == null) return ""
        if (!dailyCurrent.durationDiffersFromCurrent) return ""

        val hint = resourceRepo.getString(R.string.title_today).lowercase()
        val duration = timeMapper.formatInterval(
            interval = dailyCurrent.duration,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )

        return "$hint $duration"
    }

    private fun mapGoalTime(
        currentDuration: Long,
        goals: List<RecordTypeGoal>,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        goalsVisible: Boolean,
    ): GoalTimeViewData {
        fun getSessionGoal() = goalTimeMapper.map(
            goalTime = goals.getSessionDuration().value,
            current = currentDuration,
            type = GoalTimeType.Session,
            goalsVisible = goalsVisible,
        )
        fun getDailyGoal() = goalTimeMapper.map(
            goalTime = goals.getDailyDuration().value,
            current = dailyCurrent?.duration.orZero(),
            type = GoalTimeType.Day,
            goalsVisible = goalsVisible,
        )

        return when {
            goals.hasDailyDuration() -> getDailyGoal()
            else -> getSessionGoal()
        }
    }
}