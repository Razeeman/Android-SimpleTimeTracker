package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord.RunningRecordViewData
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper
) {

    fun map(
        runningRecord: RunningRecord,
        recordType: RecordType
    ): RunningRecordViewData {
        return RunningRecordViewData(
            name = runningRecord.name,
            timeStarted = runningRecord.timeStarted.let(::formatTime),
            timer = runningRecord.timeStarted.let(::formatInterval),
            iconId = recordType.icon.let(iconMapper::mapToDrawableId),
            color = recordType.color
        )
    }

    private fun formatTime(timeStarted: Long): String {
        return DateFormat.getDateTimeInstance().format(Date(timeStarted))
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