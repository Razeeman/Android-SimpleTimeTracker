package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor() {

    fun map(runningRecord: RunningRecord): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = runningRecord.name,
            timeString = runningRecord.timeStarted.let(::formatTime)
        )
    }

    private fun formatTime(time: Long): String {
        return DateFormat.getDateTimeInstance().format(Date(time))
    }
}