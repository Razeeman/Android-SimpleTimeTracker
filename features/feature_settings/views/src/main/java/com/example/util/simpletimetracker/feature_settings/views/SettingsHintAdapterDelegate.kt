package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.views.SettingsHintViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsHintBinding as Binding

fun createSettingsHintAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsHint.text = item.text
        item.textColor.getColor(tvItemSettingsHint.context)
            .let(tvItemSettingsHint::setTextColor)
        spaceItemSettingsTop.visible = item.topSpaceIsVisible
        spaceItemSettingsBottom.visible = item.bottomSpaceIsVisible
        viewItemSettingsDivider.visible = item.dividerIsVisible
    }
}

data class SettingsHintViewData(
    val block: SettingsBlock,
    val text: String,
    val textColor: SettingsTextColor = SettingsTextColor.Default,
    val topSpaceIsVisible: Boolean = true,
    val dividerIsVisible: Boolean = true,
    val bottomSpaceIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}