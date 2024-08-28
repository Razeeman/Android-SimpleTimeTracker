package com.example.util.simpletimetracker.feature_change_goals.api

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class ChangeRecordTypeGoalsViewData(
    val selectedCount: Int,
    val session: GoalViewData,
    val daily: GoalViewData,
    val weekly: GoalViewData,
    val monthly: GoalViewData,
    val daysOfWeek: List<ViewHolderType>,
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