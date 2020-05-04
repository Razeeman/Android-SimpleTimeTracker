package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor() {

    fun map(runningRecord: RunningRecord): RunningRecordViewData {
        return RunningRecordViewData(
            name = runningRecord.name,
            timeStarted = runningRecord.timeStarted
        )
    }
}