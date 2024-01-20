package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCollapseViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsCollapseBinding as Binding

fun createSettingsCollapseAdapterDelegate(
    onClick: (block: SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsCollapseTitle.text = item.title
        viewItemSettingsDivider.visible = item.dividerIsVisible
        arrowItemSettingsCollapse.apply { if (item.opened) rotateDown() else rotateUp() }
        layoutItemSettingsCollapseTitle.setOnClick { onClick(item.block) }
    }
}

data class SettingsCollapseViewData(
    val block: SettingsBlock,
    val title: String,
    val opened: Boolean,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}