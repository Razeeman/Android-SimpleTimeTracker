package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import java.util.Calendar
import javax.inject.Inject

class GetDoNotDisturbHandledScheduleInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    fun execute(
        timestamp: Long,
        dndStart: Long,
        dndEnd: Long,
    ): Long {
        val startOfDay = timeMapper.getStartOfDayTimeStamp(timestamp)
        val startOfNextDay = Calendar.getInstance().apply {
            timeInMillis = startOfDay
            add(Calendar.DATE, 1)
        }.timeInMillis

        val normalizedTimestamp = timestamp - startOfDay
        val dayEnd = startOfNextDay - startOfDay

        if (dndStart <= dndEnd) {
            // If ex. dnd is between 01:00 and 09:00 on the current day - set to 09:00
            if (normalizedTimestamp in dndStart..dndEnd) return dndEnd + startOfDay
        } else {
            // If ex. dnd is between 22:00 and 06:00:

            // Between 00:00 and 06:00 - set to 06:00.
            if (normalizedTimestamp in 0..dndEnd) return dndEnd + startOfDay
            // Between 22:00 and 24:00 - set to 06:00 next day.
            if (normalizedTimestamp in dndStart..dayEnd) return dndEnd + startOfNextDay
        }

        return timestamp
    }
}