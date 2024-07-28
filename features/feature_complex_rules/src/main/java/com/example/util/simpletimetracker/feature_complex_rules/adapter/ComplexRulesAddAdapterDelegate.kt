package com.example.util.simpletimetracker.feature_complex_rules.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleAddViewData as ViewData
import com.example.util.simpletimetracker.feature_complex_rules.databinding.ItemComplexRuleAddLayoutBinding as Binding

fun createComplexRuleAddAdapterDelegate(
    onItemClick: (() -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerComplexRuleItem.setCardBackgroundColor(item.color)
        tvComplexRuleItemAddTitle.text = item.name

        containerComplexRuleItem.setOnClick(onItemClick)
    }
}

class ComplexRuleAddViewData(
    val name: String,
    @ColorInt val color: Int,
) : ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}