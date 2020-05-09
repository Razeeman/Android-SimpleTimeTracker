package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper
) {

    fun map(
        record: Record,
        recordType: RecordType
    ): RecordViewData {
        return RecordViewData(
            id = record.id,
            name = recordType.name,
            timeStarted = record.timeStarted.let(::formatTime),
            timeFinished = record.timeEnded.let(::formatTime),
            duration = (record.timeEnded - record.timeStarted).let(::formatInterval),
            iconId = recordType.icon.let(iconMapper::mapToDrawableId),
            color = recordType.color
        )
    }

    // TODO move to core mapper
    private fun formatTime(timeStarted: Long): String {
        return DateFormat.getDateTimeInstance().format(Date(timeStarted))
    }

    private fun formatInterval(interval: Long): String {
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