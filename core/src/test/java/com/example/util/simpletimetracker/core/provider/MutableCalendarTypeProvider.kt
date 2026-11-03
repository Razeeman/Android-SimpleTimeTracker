package com.example.util.simpletimetracker.core.provider

import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.calendar.model.CalendarType

class MutableCalendarTypeProvider(
    var calendarType: CalendarType = CalendarType.GREGORIAN,
) : CalendarTypeProvider {

    override fun get(): CalendarType = calendarType
}
