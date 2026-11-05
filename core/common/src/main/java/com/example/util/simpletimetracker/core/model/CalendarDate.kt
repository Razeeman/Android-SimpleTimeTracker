package com.example.util.simpletimetracker.core.model

/**
 * A civil date with a one-based month (1–12).
 * Validity of the year, month, and day depends on the selected calendar.
 */
data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
)
