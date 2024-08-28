package com.example.util.simpletimetracker.feature_settings_views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings_views.SettingsRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_settings_views.databinding.ItemSettingsRangeBinding as Binding

fun createSettingsRangeAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTitle.text = item.title
        tvItemSettingsStart.text = item.start
        tvItemSettingsEnd.text = item.end

        viewItemSettingsDivider.visible = item.dividerIsVisible

        tvItemSettingsStart.setOnClick { onClick(item.blockStart) }
        tvItemSettingsEnd.setOnClick { onClick(item.blockEnd) }
    }
}

data class SettingsRangeViewData(
    val blockStart: SettingsBlock,
    val blockEnd: SettingsBlock,
    val title: String,
    val start: String,
    val end: String,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = blockStart.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}