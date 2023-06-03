package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
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
        record: RunningRecord,
        nowIconVisible: Boolean,
        goalsVisible: Boolean,
        totalDurationVisible: Boolean,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): RunningRecordViewData {
        val dailyCurrent = if (goalsVisible) {
            getCurrentRecordsDurationInteractor.getDailyCurrent(record)
        } else {
            null
        }
        val weeklyCurrent = if (type.weeklyGoalTime > 0L && goalsVisible) {
            getCurrentRecordsDurationInteractor.getWeeklyCurrent(record).duration
        } else {
            0L
        }
        val monthlyCurrent = if (type.monthlyGoalTime > 0L && goalsVisible) {
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