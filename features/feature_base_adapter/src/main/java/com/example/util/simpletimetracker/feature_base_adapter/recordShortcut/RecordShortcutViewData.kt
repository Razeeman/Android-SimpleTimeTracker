package com.example.util.simpletimetracker.feature_base_adapter.recordShortcut

import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class RecordShortcutViewData(
    val id: Long,
    val target: RecordShortcut.Target,
    val hint: String,
    val data: CategoryViewData.Record.Tagged,
    val spinnerData: SpinnerData? = null,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordShortcutViewData

    data class SpinnerData(
        val block: Any,
        val items: List<CustomSpinner.CustomSpinnerItem>,
        val selectedPosition: Int,
        val processSameItemSelected: Boolean,
        val isButtonVisible: Boolean,
    )
}