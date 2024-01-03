package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTypeGoalDBO
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import javax.inject.Inject

class RecordTypeGoalDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeGoalDBO): RecordTypeGoal {
        return RecordTypeGoal(
            id = dbo.id,
            idData = if (dbo.typeId != 0L) {
                RecordTypeGoal.IdData.Type(dbo.typeId)
            } else {
                RecordTypeGoal.IdData.Category(dbo.categoryId)
            },
            range = when (dbo.range) {
                0L -> RecordTypeGoal.Range.Session
                1L -> RecordTypeGoal.Range.Daily
                2L -> RecordTypeGoal.Range.Weekly
                3L -> RecordTypeGoal.Range.Monthly
                else -> RecordTypeGoal.Range.Session
            },
            type = when (dbo.type) {
                0L -> RecordTypeGoal.Type.Duration(dbo.value)
                1L -> RecordTypeGoal.Type.Count(dbo.value)
                else -> RecordTypeGoal.Type.Duration(dbo.value)
            },
            daysOfWeek = mapDaysOfWeek(dbo.daysOfWeek),
        )
    }

    fun map(domain: RecordTypeGoal): RecordTypeGoalDBO {
        return RecordTypeGoalDBO(
            id = domain.id,
            typeId = (domain.idData as? RecordTypeGoal.IdData.Type)?.value.orZero(),
            range = when (domain.range) {
                is RecordTypeGoal.Range.Session -> 0L
                is RecordTypeGoal.Range.Daily -> 1L
                is RecordTypeGoal.Range.Weekly -> 2L
                is RecordTypeGoal.Range.Monthly -> 3L
            },
            type = when (domain.type) {
                is RecordTypeGoal.Type.Duration -> 0L
                is RecordTypeGoal.Type.Count -> 1L
            },
            value = domain.type.value,
            categoryId = (domain.idData as? RecordTypeGoal.IdData.Category)?.value.orZero(),
            daysOfWeek = mapDaysOfWeek(domain.daysOfWeek),
        )
    }

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