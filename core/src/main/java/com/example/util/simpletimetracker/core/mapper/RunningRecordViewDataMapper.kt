package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordType.extension.getLongest
import com.example.util.simpletimetracker.domain.recordType.extension.getSession
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val goalViewDataMapper: GoalViewDataMapper,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
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
        durationFormat: DurationFormat,
        nowIconVisible: Boolean,
        goalsVisible: Boolean,
        totalDurationVisible: Boolean,
    ): RunningRecordViewData {
        val currentDuration = System.currentTimeMillis() - runningRecord.timeStarted
        val tagIds = runningRecord.tags.map(RecordBase.Tag::tagId)

        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            tagName = recordTagFullNameMapper.getFullName(
                tags = recordTags.filter { it.id in tagIds },
                tagData = runningRecord.tags,
            ),
            timeStarted = timeMapper.formatTime(
                time = runningRecord.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeStartedTimestamp = runningRecord.timeStarted,
            timer = timeMapper.formatInterval(
                interval = currentDuration,
                forceSeconds = true,
                durationFormat = durationFormat,
            ),
            timerTotal = mapTotalDuration(
                dailyCurrent = dailyCurrent,
                totalDurationVisible = totalDurationVisible,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ),
            goalTime = mapGoalTime(
                currentDuration = currentDuration,
                goals = goals,
                dailyCurrent = dailyCurrent,
                goalsVisible = goalsVisible,
                durationFormat = durationFormat,
            ),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            comment = runningRecord.comment,
            nowIconVisible = nowIconVisible,
        )
    }

    fun mapFiltered(
        viewData: RunningRecordViewData,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): RunningRecordViewData {
        return when {
            isFiltered -> {
                viewData.copy(color = colorMapper.toFilteredColor(isDarkTheme))
            }
            else -> viewData
        }
    }

    private fun mapTotalDuration(
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        totalDurationVisible: Boolean,
        showSeconds: Boolean,
        durationFormat: DurationFormat,
    ): String {
        if (!totalDurationVisible) return ""
        if (dailyCurrent == null) return ""
        if (!dailyCurrent.durationDiffersFromCurrent) return ""

        val hint = resourceRepo.getString(R.string.title_today).lowercase()
        val duration = timeMapper.formatInterval(
            interval = dailyCurrent.duration,
            forceSeconds = showSeconds,
            durationFormat = durationFormat,
        )

        return "$hint $duration"
    }

    private fun mapGoalTime(
        currentDuration: Long,
        goals: List<RecordTypeGoal>,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result?,
        goalsVisible: Boolean,
        durationFormat: DurationFormat,
    ): GoalTimeViewData {
        // TODO GOAL show several goals
        val goal = goals.getDaily().ifEmpty { goals.getSession() }.getLongest()

        return goalViewDataMapper.mapForTimer(
            goal = goal,
            currentDuration = currentDuration,
            dailyCurrent = dailyCurrent,
            goalsVisible = goalsVisible,
            durationFormat = durationFormat,
        )
    }
}