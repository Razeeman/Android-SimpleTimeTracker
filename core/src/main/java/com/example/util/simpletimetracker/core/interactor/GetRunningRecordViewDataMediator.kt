package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
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
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RunningRecordViewData {
        val dailyCurrent = if ((goals.getDaily() != null && goalsVisible) || totalDurationVisible) {
            getCurrentRecordsDurationInteractor.getDailyCurrent(typeId = record.id, runningRecord = record)
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
            durationFormat = durationFormat,
            nowIconVisible = nowIconVisible,
            goalsVisible = goalsVisible,
            totalDurationVisible = totalDurationVisible,
        )
    }
}