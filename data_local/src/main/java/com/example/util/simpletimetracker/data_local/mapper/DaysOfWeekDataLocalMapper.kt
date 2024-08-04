package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.domain.model.DayOfWeek
import javax.inject.Inject

class DaysOfWeekDataLocalMapper @Inject constructor() {

    fun mapDaysOfWeek(dbo: String): List<DayOfWeek> {
        return daysOfWeek.mapIndexedNotNull { index, dayOfWeek ->
            when (dbo.getOrNull(index)) {
                // Selected days are marked with 1, days that are not selected - with 0,
                // if string is empty - assume day is selected to support old app versions.
                '1' -> dayOfWeek
                null -> dayOfWeek
                '0' -> null
                else -> null
            }
        }
    }

    fun mapDaysOfWeek(domain: List<DayOfWeek>): String {
        return daysOfWeek.map { dayOfWeek ->
            if (dayOfWeek in domain) '1' else '0'
        }.joinToString(separator = "")
    }

    companion object {
        // Do not change order, this values saved in database accordingly.
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