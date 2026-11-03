package com.example.util.simpletimetracker.provider

import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.calendar.model.CalendarType
import javax.inject.Inject

class WearCalendarTypeProvider @Inject constructor() : CalendarTypeProvider {

    override fun get(): CalendarType {
        return CalendarType.GREGORIAN
    }
}
