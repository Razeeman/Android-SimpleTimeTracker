package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.domain.extension.hasMonthlyDuration
import com.example.util.simpletimetracker.domain.extension.hasWeeklyDuration
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
    ): RunningRecordViewData {
        val dailyCurrent = if (goalsVisible || totalDurationVisible) {
            getCurrentRecordsDurationInteractor.getDailyCurrent(record)
        } else {
            null
        }
        val weeklyCurrent = if (goals.hasWeeklyDuration() && goalsVisible) {
            getCurrentRecordsDurationInteractor.getWeeklyCurrent(record).duration
        } else {
            0L
        }
        val monthlyCurrent = if (goals.hasMonthlyDuration() && goalsVisible) {
            getCurrentRecordsDurationInteractor.getMonthlyCurrent(record).duration
        } else {
            0L
        }

        return runningRecordViewDataMapper.map(
            runningRecord = record,
            dailyCurrent = dailyCurrent,
            weeklyCurrent = weeklyCurrent,
            monthlyCurrent = monthlyCurrent,
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
        )
    }
}