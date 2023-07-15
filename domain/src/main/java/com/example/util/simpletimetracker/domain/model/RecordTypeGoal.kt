package com.example.util.simpletimetracker.domain.model

data class RecordTypeGoal(
    val id: Long = 0,
    val typeId: Long,
    val range: Range,
    val type: Type,
) {

    sealed interface Range {
        object Session : Range
        object Daily : Range
        object Weekly : Range
        object Monthly : Range
    }

    sealed interface Type {
        data class Duration(val seconds: Long) : Type
        data class Count(val times: Long) : Type
    }
}