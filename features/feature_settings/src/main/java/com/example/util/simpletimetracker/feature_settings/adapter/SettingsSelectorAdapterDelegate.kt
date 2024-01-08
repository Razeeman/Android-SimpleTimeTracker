package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSelectorBinding as Binding

fun createSettingsSelectorAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTitle.text = item.title

        if (item.subtitle.isEmpty()) {
            tvItemSettingsSubtitle.visible = false
        } else {
            tvItemSettingsSubtitle.text = item.subtitle
            tvItemSettingsSubtitle.visible = true
        }

        tvItemSettingsSelectorValue.text = item.selectedValue

        spaceItemSettingsBottom.visible = item.bottomSpaceIsVisible

        viewItemSettingsDivider.visible = item.dividerIsVisible

        groupItemSettingsSelector.setOnClick { onClick(item.block) }
    }
}

data class SettingsSelectorViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val selectedValue: String,
    val bottomSpaceIsVisible: Boolean = true,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}