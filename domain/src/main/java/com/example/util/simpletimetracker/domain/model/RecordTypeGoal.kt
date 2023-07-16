package com.example.util.simpletimetracker.domain.model

data class RecordTypeGoal(
    val id: Long = 0,
    val typeId: Long,
    val range: Range,
    val type: Type,
) {

    // TODO switch to GoalTimeType
    sealed interface Range {
        object Session : Range
        object Daily : Range
        object Weekly : Range
        object Monthly : Range
    }

    sealed interface Type {
        val value: Long

        data class Duration(override val value: Long) : Type
        data class Count(override val value: Long) : Type
    }
}