package com.example.util.simpletimetracker.feature_settings_views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings_views.SettingsCheckboxWithRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_settings_views.databinding.ItemSettingsCheckboxWithRangeBinding as Binding

fun createSettingsCheckboxWithRangeAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsCheckboxWithRangeTitle.text = item.title

        tvItemSettingsCheckboxWithRangeSubtitle.text = item.subtitle

        if (checkboxItemSettings.isChecked != item.isChecked) {
            checkboxItemSettings.isChecked = item.isChecked
        }

        groupItemSettingsCheckboxWithRange.visible =
            item.range is ViewData.RangeViewData.Enabled

        if (item.range is ViewData.RangeViewData.Enabled) {
            tvItemSettingsStart.text = item.range.rangeStart
            tvItemSettingsEnd.text = item.range.rangeEnd
        }

        checkboxItemSettings.setOnClick { onClick(item.blockCheckbox) }
        tvItemSettingsStart.setOnClick { onClick(item.blockStart) }
        tvItemSettingsEnd.setOnClick { onClick(item.blockEnd) }
    }
}

data class SettingsCheckboxWithRangeViewData(
    val blockCheckbox: SettingsBlock,
    val blockStart: SettingsBlock,
    val blockEnd: SettingsBlock,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean,
    val range: RangeViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = blockCheckbox.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    sealed interface RangeViewData {
        object Disabled : RangeViewData
        data class Enabled(
            val rangeStart: String,
            val rangeEnd: String,
        ) : RangeViewData
    }
}