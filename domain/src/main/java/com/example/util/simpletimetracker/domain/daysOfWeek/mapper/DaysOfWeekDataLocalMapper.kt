package com.example.util.simpletimetracker.domain.daysOfWeek.mapper

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import javax.inject.Inject

class DaysOfWeekDataLocalMapper @Inject constructor() {

    fun mapDaysOfWeek(data: String): Set<DayOfWeek> {
        return daysOfWeek.mapIndexedNotNull { index, dayOfWeek ->
            when (data.getOrNull(index)) {
                // Selected days are marked with 1, days that are not selected - with 0,
                // if string is empty - assume day is selected to support old app versions.
                '1' -> dayOfWeek
                null -> dayOfWeek
                '0' -> null
                else -> null
            }
        }.toSet()
    }

    fun mapDaysOfWeek(domain: Set<DayOfWeek>): String {
        return daysOfWeek.map { dayOfWeek ->
            if (dayOfWeek in domain) '1' else '0'
        }.joinToString(separator = "")
    }

    companion object {
        // Do not change order, these values are saved in the database and preferences accordingly.
        private val daysOfWeek = listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
        )
    }
}