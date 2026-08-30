package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.isValidTimeOfDay
import com.example.util.simpletimetracker.domain.extension.toDomainDayOfWeek
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import java.time.LocalDate
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetDoNotDisturbHandledScheduleInteractor @Inject constructor(
    private val localDateMapper: LocalDateMapper,
) {

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

        val timeZone = TimeZone.getDefault()
        val timestamp = reminderDurationSeconds * 1000L + nowTimestamp

        val dndHandledTimestamp = applyDoNotDisturb(timestamp, dndStart, dndEnd, timeZone)
            ?: return null
        val dndHandledDate = dndHandledTimestamp.toLocalDateTime(timeZone).toLocalDate()
        val candidateDayOfWeek = dndHandledDate.getDomainDayOfWeek()

        if (candidateDayOfWeek in activeDaysOfWeek) return dndHandledTimestamp

        var nextSelectedDay = dndHandledDate
        repeat(7) {
            nextSelectedDay = nextSelectedDay.plusDays(1)
            if (nextSelectedDay.getDomainDayOfWeek() in activeDaysOfWeek) {
                val startOfDay = localDateMapper.resolveDateTime(
                    date = nextSelectedDay,
                    timeOfDayMillis = 0L,
                    timeZone = TimeZone.getDefault(),
                ) ?: return null
                return applyDoNotDisturb(startOfDay, dndStart, dndEnd, timeZone)
            }
        }

        return null
    }

    private fun applyDoNotDisturb(
        timestamp: Long,
        dndStart: Long,
        dndEnd: Long,
        timeZone: TimeZone,
    ): Long? {
        val dateTime = timestamp.toLocalDateTime(TimeZone.getDefault())
        val normalizedTimestamp = TimeUnit.NANOSECONDS.toMillis(dateTime.toLocalTime().toNanoOfDay())

        if (dndStart <= dndEnd) {
            // If ex. dnd is between 01:00 and 09:00 on the current day - set to 09:00
            if (normalizedTimestamp in dndStart..dndEnd) {
                return localDateMapper.resolveDateTime(dateTime.toLocalDate(), dndEnd, timeZone)
            }
        } else {
            // If ex. dnd is between 22:00 and 06:00:

            // Between 00:00 and 06:00 - set to 06:00.
            if (normalizedTimestamp <= dndEnd) {
                return localDateMapper.resolveDateTime(dateTime.toLocalDate(), dndEnd, timeZone)
            }
            // Between 22:00 and 24:00 - set to 06:00 next day.
            if (normalizedTimestamp >= dndStart) {
                val nextDay = dateTime.toLocalDate().plusDays(1)
                return localDateMapper.resolveDateTime(nextDay, dndEnd, timeZone)
            }
        }

        return timestamp
    }

    private fun LocalDate.getDomainDayOfWeek(): DayOfWeek {
        return this.dayOfWeek.toDomainDayOfWeek()
    }
}