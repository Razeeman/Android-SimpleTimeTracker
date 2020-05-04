package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_running_records.adapter.RunningRecordViewData
import javax.inject.Inject

class RunningRecordMapper @Inject constructor() {

    fun map(record: Record): RunningRecordViewData {
        return RunningRecordViewData(
            id = record.id,
            name = record.name,
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded
        )
    }
}