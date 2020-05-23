package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun formatTime(time: Long): String {
        return DateFormat.getTimeInstance().format(Date(time))
    }

    fun formatDateTime(time: Long): String {
        return DateFormat.getDateTimeInstance().format(Date(time))
    }

    fun formatInterval(interval: Long): String {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr)
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
        )

        var res = ""
        if (hr != 0L) res += "${hr}h "
        if (hr != 0L || min != 0L) res += "${min}m "
        res += "${sec}s"

        return res
    }

    fun toTimestampShifted(daysFromToday: Int): Long {
        return if (daysFromToday != 0) {
            Calendar.getInstance()
                .apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DATE, daysFromToday)
                }
                .timeInMillis
        } else {
            return System.currentTimeMillis()
        }
    }

    fun toDayTitle(daysFromToday: Int): String {
        return when (daysFromToday) {
            -1 -> resourceRepo.getString(R.string.title_yesterday)
            0 -> resourceRepo.getString(R.string.title_today)
            1 -> resourceRepo.getString(R.string.title_tomorrow)
            else -> toDayDateTitle(daysFromToday)
        }
    }

    fun toWeekTitle(weeksFromToday: Int): String {
        return when (weeksFromToday) {
            -1 -> resourceRepo.getString(R.string.title_prev_week)
            0 -> resourceRepo.getString(R.string.title_this_week)
            1 -> resourceRepo.getString(R.string.title_next_week)
            else -> toWeekDateTitle(weeksFromToday)
        }
    }

    fun toMonthTitle(monthsFromToday: Int): String {
        return when (monthsFromToday) {
            -1 -> resourceRepo.getString(R.string.title_prev_month)
            0 -> resourceRepo.getString(R.string.title_this_month)
            1 -> resourceRepo.getString(R.string.title_next_month)
            else -> toMonthDateTitle(monthsFromToday)
        }
    }

    private fun toDayDateTitle(daysFromToday: Int): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, daysFromToday)
        }

        return SimpleDateFormat("E, MMMM d", Locale.US)
            .format(Date(calendar.timeInMillis))
    }

    private fun toWeekDateTitle(weeksFromToday: Int): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.DATE, weeksFromToday * 7)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis

        return SimpleDateFormat("MMM d", Locale.US).format(Date(rangeStart)) +
                " - " + SimpleDateFormat("MMM d", Locale.US).format(Date(rangeEnd))

    }

    private fun toMonthDateTitle(monthsFromToday: Int): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MONTH, monthsFromToday)
        }

        return SimpleDateFormat("MMMM", Locale.US)
            .format(Date(calendar.timeInMillis))
    }
}