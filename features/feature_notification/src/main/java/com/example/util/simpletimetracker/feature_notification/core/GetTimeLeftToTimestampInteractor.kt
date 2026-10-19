package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetTimeLeftToTimestampInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    // Interval should divide a day evenly,
    // so trigger times are repeated at the same time every day.
    fun execute(
        dayTimestamp: Long,
        interval: Long,
    ): Long {
        val dayDuration = TimeUnit.DAYS.toMillis(1)

        return (0 until dayDuration / interval)
            .map { (dayTimestamp + it * interval) % dayDuration }
            .minOf(::getTimeLeft)
    }

    private fun getTimeLeft(
        dayTimestamp: Long,
    ): Long {
        val calendar = Calendar.getInstance()
        val current = currentTimestampProvider.get()

        val triggerTime = calendar.shiftTimeStamp(
            timestamp = timeMapper.getStartOfDayTimeStamp(current),
            shift = dayTimestamp,
        ).let {
            if (it > current) {
                it
            } else {
                calendar.timeInMillis = it
                calendar.apply { add(Calendar.DATE, 1) }
                calendar.timeInMillis
            }
        }

        return triggerTime - current
    }
}