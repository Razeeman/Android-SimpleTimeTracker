package com.example.util.simpletimetracker.feature_dialogs.typesFilter.adapter

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R

fun createTypesFilterDividerAdapterDelegate() =
    createRecyclerAdapterDelegate<TypesFilterDividerViewData>(
        R.layout.item_types_filter_divider_layout
    ) { _, _, _ ->

        // Nothing to bind
    }

data class TypesFilterDividerViewData(
    val id: Long
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is TypesFilterDividerViewData
}