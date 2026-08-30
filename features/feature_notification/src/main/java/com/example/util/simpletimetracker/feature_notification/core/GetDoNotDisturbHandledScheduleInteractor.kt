package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.isValidTimeOfDay
import java.util.Calendar
import javax.inject.Inject

class GetDoNotDisturbHandledScheduleInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    // TODO fix DST offest, use new date api.
    fun execute(
        reminderDurationSeconds: Long,
        dndStart: Long,
        dndEnd: Long,
        activeDaysOfWeek: Set<DayOfWeek>,
        nowTimestamp: Long,
    ): Long? {
        if (reminderDurationSeconds <= 0L) return null
        if (activeDaysOfWeek.isEmpty()) return null
        if (!dndStart.isValidTimeOfDay() || !dndEnd.isValidTimeOfDay()) return null

        val timestamp = reminderDurationSeconds * 1000L + nowTimestamp

        val dndHandledTimestamp = applyDoNotDisturb(timestamp, dndStart, dndEnd)
        val candidateDayOfWeek = Calendar.getInstance()
            .apply { timeInMillis = dndHandledTimestamp }
            .getDayOfWeek()

        if (candidateDayOfWeek in activeDaysOfWeek) return dndHandledTimestamp

        val nextSelectedDay = Calendar.getInstance().apply {
            timeInMillis = dndHandledTimestamp
            setToStartOfDay()
        }
        repeat(7) {
            nextSelectedDay.add(Calendar.DATE, 1)
            if (nextSelectedDay.getDayOfWeek() in activeDaysOfWeek) {
                return applyDoNotDisturb(nextSelectedDay.timeInMillis, dndStart, dndEnd)
            }
        }

        return null
    }

    private fun applyDoNotDisturb(
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

    private fun Calendar.getDayOfWeek(): DayOfWeek {
        return timeMapper.toDayOfWeek(this.get(Calendar.DAY_OF_WEEK))
    }
}