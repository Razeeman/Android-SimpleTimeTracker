package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records.adapter.RecordViewData
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        record: Record,
        recordType: RecordType
    ): RecordViewData {
        return RecordViewData(
            id = record.id,
            name = recordType.name,
            timeStarted = record.timeStarted
                .let(::formatTime),
            timeFinished = record.timeEnded
                .let(::formatTime),
            duration = (record.timeEnded - record.timeStarted)
                .let(::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
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

        // TODO remove 0s if empty
        return String.format("%2dh %2dm %2ds", hr, min, sec)
    }
}