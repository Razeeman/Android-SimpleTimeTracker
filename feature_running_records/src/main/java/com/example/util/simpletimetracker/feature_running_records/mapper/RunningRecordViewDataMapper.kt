package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor() {

    fun map(runningRecord: RunningRecord): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = runningRecord.name,
            timeString = runningRecord.timeStarted.let(::formatInterval)
        )
    }

    private fun formatInterval(timeStarted: Long): String {
        val interval = System.currentTimeMillis() - timeStarted

        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        return String.format("%02d:%02d:%02d", hr, min, sec)
    }
}