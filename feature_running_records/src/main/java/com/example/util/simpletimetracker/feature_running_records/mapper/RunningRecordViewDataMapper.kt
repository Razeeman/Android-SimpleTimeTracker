package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        runningRecord: RunningRecord,
        recordType: RecordType
    ): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            timeStarted = runningRecord.timeStarted
                .let(::formatTime),
            timer = runningRecord.timeStarted
                .let(::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
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