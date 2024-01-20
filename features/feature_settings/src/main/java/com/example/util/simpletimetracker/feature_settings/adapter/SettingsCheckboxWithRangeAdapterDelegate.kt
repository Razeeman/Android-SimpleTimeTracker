package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithRangeViewData
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsCheckboxWithRangeBinding as Binding

fun createSettingsCheckboxWithRangeAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
    onStartClick: (SettingsBlock) -> Unit,
    onEndClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsCheckboxWithRangeTitle.text = item.title

        tvItemSettingsCheckboxWithRangeSubtitle.text = item.subtitle

        if (checkboxItemSettingsCheckboxWithRange.isChecked != item.isChecked) {
            checkboxItemSettingsCheckboxWithRange.isChecked = item.isChecked
        }

        groupItemSettingsCheckboxWithRange.visible =
            item.range is SettingsCheckboxWithRangeViewData.RangeViewData.Enabled

        if (item.range is SettingsCheckboxWithRangeViewData.RangeViewData.Enabled) {
            tvItemSettingsCheckboxWithRangeStart.text = item.range.rangeStart
            tvItemSettingsCheckboxWithRangeEnd.text = item.range.rangeEnd
        }

        checkboxItemSettingsCheckboxWithRange.setOnClick { onClick(item.block) }
        tvItemSettingsCheckboxWithRangeStart.setOnClick { onStartClick(item.block) }
        tvItemSettingsCheckboxWithRangeEnd.setOnClick { onEndClick(item.block) }
    }
}

data class SettingsCheckboxWithRangeViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean,
    val range: RangeViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    sealed interface RangeViewData {
        object Disabled : RangeViewData
        data class Enabled(
            val rangeStart: String,
            val rangeEnd: String,
        ) : RangeViewData
    }
}