package com.example.util.simpletimetracker.feature_records_filter.viewData

import com.example.util.simpletimetracker.feature_base_adapter.selectionButton.SelectionButtonViewData

data class RecordsFilterSelectionButtonType(
    val type: Type,
    val subtype: Subtype,
) : SelectionButtonViewData.Type {

    sealed interface Type {
        object Activities : Type
        object Categories : Type
        object Tags : Type
    }

    sealed interface Subtype {
        object SelectAll : Subtype
        object SelectNone : Subtype
    }
}
