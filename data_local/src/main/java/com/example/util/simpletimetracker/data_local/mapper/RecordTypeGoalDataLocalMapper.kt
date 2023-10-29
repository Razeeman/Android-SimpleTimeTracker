package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTypeGoalDBO
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import javax.inject.Inject

class RecordTypeGoalDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeGoalDBO): RecordTypeGoal {
        return RecordTypeGoal(
            id = dbo.id,
            typeId = dbo.typeId,
            categoryId = dbo.categoryId,
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
        )
    }

    fun map(domain: RecordTypeGoal): RecordTypeGoalDBO {
        return RecordTypeGoalDBO(
            id = domain.id,
            typeId = domain.typeId,
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
            categoryId = domain.categoryId,
        )
    }
}