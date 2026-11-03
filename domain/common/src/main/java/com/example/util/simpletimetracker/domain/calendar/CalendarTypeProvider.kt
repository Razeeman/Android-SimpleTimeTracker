package com.example.util.simpletimetracker.domain.calendar

import com.example.util.simpletimetracker.domain.calendar.model.CalendarType

interface CalendarTypeProvider {
    fun get(): CalendarType
}
