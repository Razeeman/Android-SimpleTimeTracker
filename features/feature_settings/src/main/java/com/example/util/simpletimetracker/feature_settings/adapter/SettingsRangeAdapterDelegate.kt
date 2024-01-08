package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsRangeBinding as Binding

fun createSettingsRangeAdapterDelegate(
    onStartClick: (SettingsBlock) -> Unit,
    onEndClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTitle.text = item.title
        tvItemSettingsStart.text = item.start
        tvItemSettingsEnd.text = item.end

        viewItemSettingsDivider.visible = item.dividerIsVisible

        tvItemSettingsStart.setOnClick { onStartClick(item.block) }
        tvItemSettingsEnd.setOnClick { onEndClick(item.block) }
    }
}

data class SettingsRangeViewData(
    val block: SettingsBlock,
    val title: String,
    val start: String,
    val end: String,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}