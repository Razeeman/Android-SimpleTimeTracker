package com.example.util.simpletimetracker.domain.model

data class ComplexRule(
    val id: Long,
    val action: Action,
    val actionSetTagIds: Set<Long>,
    val conditionStartingTypeIds: Set<Long>,
    val conditionCurrentTypeIds: Set<Long>,
    val conditionDaysOfWeek: Set<DayOfWeek>,
) {

    sealed interface Action {
        object AllowMultitasking : Action
        object DisallowMultitasking : Action
        object SetTag : Action
    }
}