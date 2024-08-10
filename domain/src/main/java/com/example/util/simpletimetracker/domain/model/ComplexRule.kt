package com.example.util.simpletimetracker.domain.model

data class ComplexRule(
    val id: Long = 0L,
    val disabled: Boolean,
    val action: Action,
    val actionAssignTagIds: Set<Long>,
    val conditionStartingTypeIds: Set<Long>,
    val conditionCurrentTypeIds: Set<Long>,
    val conditionDaysOfWeek: Set<DayOfWeek>,
) {

    sealed interface Action {
        object AllowMultitasking : Action
        object DisallowMultitasking : Action
        object AssignTag : Action
    }

    sealed interface Condition {
        object StartingType : Condition
        object CurrentType : Condition
        object DaysOfWeek : Condition
    }

    val conditions: List<Condition>
        get() = listOfNotNull(
            Condition.StartingType.takeIf { conditionStartingTypeIds.isNotEmpty() },
            Condition.CurrentType.takeIf { conditionCurrentTypeIds.isNotEmpty() },
            Condition.DaysOfWeek.takeIf { conditionDaysOfWeek.isNotEmpty() },
        )

    val hasConditions: Boolean get() = conditions.isNotEmpty()
}