package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class ChangeRecordTypeGoalsViewData(
    val session: GoalViewData,
    val daily: GoalViewData,
    val weekly: GoalViewData,
    val monthly: GoalViewData,
) {

    data class GoalViewData(
        val title: String,
        val typeItems: List<CustomSpinner.CustomSpinnerItem>,
        val typeSelectedPosition: Int,
        val type: Type,
        val value: String,
    )

    sealed interface Type {
        object Duration : Type
        object Count : Type
    }
}