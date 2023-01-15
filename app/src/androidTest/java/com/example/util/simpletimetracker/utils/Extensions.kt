package com.example.util.simpletimetracker.utils

import java.util.Calendar

fun Calendar.getMillis(hour: Int, minute: Int, millis: Int = 0): Long {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.MILLISECOND, millis)
    return timeInMillis
}
