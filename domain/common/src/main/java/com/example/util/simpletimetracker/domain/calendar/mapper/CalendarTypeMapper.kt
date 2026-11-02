package com.example.util.simpletimetracker.domain.calendar.mapper

import com.example.util.simpletimetracker.domain.calendar.model.CalendarType
import javax.inject.Inject

class CalendarTypeMapper @Inject constructor() {

    fun map(value: String?): CalendarType {
        return when (value) {
            STORED_GREGORIAN -> CalendarType.GREGORIAN
            STORED_PERSIAN -> CalendarType.PERSIAN
            else -> CalendarType.GREGORIAN
        }
    }

    fun map(value: CalendarType): String {
        return when (value) {
            CalendarType.GREGORIAN -> STORED_GREGORIAN
            CalendarType.PERSIAN -> STORED_PERSIAN
        }
    }

    companion object {
        const val STORED_GREGORIAN = "gregorian"
        const val STORED_PERSIAN = "persian"
    }
}
