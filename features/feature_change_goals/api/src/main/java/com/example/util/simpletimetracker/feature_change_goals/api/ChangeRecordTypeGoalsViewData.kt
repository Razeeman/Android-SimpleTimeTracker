package com.example.util.simpletimetracker.feature_change_goals.api

import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class ChangeRecordTypeGoalsViewData(
    val selectedCount: Int,
    val viewData: List<ViewHolderType>,
) {

    data class GoalViewData(
        val key: Long,
        val rangeItems: List<CustomSpinner.CustomSpinnerItem>,
        val rangeSelectedPosition: Int,
        val typeItems: List<CustomSpinner.CustomSpinnerItem>,
        val typeSelectedPosition: Int,
        val type: Type,
        val subtypeItems: List<ButtonsRowViewData>,
        val value: String,
        val daysOfWeek: List<ViewHolderType>,
        val requestScroll: Boolean,
    ) : ViewHolderType {

        override fun getUniqueId(): Long = key

        override fun isValidType(other: ViewHolderType): Boolean = other is GoalViewData
    }

    sealed interface Type {
        data object Duration : Type
        data object Count : Type
    }
}