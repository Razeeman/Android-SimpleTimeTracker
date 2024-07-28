package com.example.util.simpletimetracker.feature_change_complex_rule.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_complex_rule.adapter.ChangeComplexRuleActionViewData as ViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.databinding.ChangeComplexRuleActionItemBinding as Binding

fun createComplexRuleActionAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        btnChangeRecordButtonItem.text = item.text
        btnChangeRecordButtonItem.setOnClickWith(item, onClick)
    }
}

data class ChangeComplexRuleActionViewData(
    val type: Type,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    enum class Type {
        AllowMultitasking,
        DisallowMultitasking,
        SetTag,
    }
}