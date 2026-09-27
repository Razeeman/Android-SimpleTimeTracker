package com.example.util.simpletimetracker.feature_base_adapter.runningRecord

data class GoalTimeViewData(
    val text: String,
    val state: Subtype,
) {

    // TODO GOAL replace with domain model
    sealed interface Subtype {
        data object Hidden : Subtype
        data object Goal : Subtype
        data object Limit : Subtype
    }
}