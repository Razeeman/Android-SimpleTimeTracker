package com.example.util.simpletimetracker.core.mapper

import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeMapper @Inject constructor() {

    fun formatTime(time: Long): String {
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
}