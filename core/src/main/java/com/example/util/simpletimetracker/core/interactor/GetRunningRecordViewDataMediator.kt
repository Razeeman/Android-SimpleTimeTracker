package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class GetRunningRecordViewDataMediator @Inject constructor(
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
) {

    suspend fun execute(
        type: RecordType,
        tags: List<RecordTag>,
        goals: List<RecordTypeGoal>,
        record: RunningRecord,
        nowIconVisible: Boolean,
        goalsVisible: Boolean,
        totalDurationVisible: Boolean,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        pomodoroEnabled: Boolean = false, // TODO POM remove?
        pomodoroRunning: Boolean = false,
    ): RunningRecordViewData {
        val dailyCurrent = if ((goals.getDaily() != null && goalsVisible) || totalDurationVisible) {
            getCurrentRecordsDurationInteractor.getDailyCurrent(record)
        } else {
            null
        }

        return runningRecordViewDataMapper.map(
            runningRecord = record,
            dailyCurrent = dailyCurrent,
            recordType = type,
            recordTags = tags,
            goals = goals,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            showSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
            nowIconVisible = nowIconVisible,
            goalsVisible = goalsVisible,
            totalDurationVisible = totalDurationVisible,
            pomodoroEnabled = pomodoroEnabled,
            pomodoroRunning = pomodoroRunning,
        )
    }
}