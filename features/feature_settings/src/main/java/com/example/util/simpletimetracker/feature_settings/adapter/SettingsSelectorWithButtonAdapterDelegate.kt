package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSelectorWithButtonBinding as Binding

fun createSettingsSelectorWithButtonAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        selectorAdapterBindDelegate(
            item = item.data,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            value = tvItemSettingsSelectorValue,
            space = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            group = groupItemSettingsSelector,
            onClick = onClick,
        )

        btnItemSettingsSelector.visible = item.isButtonVisible
        tvItemSettingsSelectorButton.text = item.buttonText
        btnItemSettingsSelector.setOnClick { onClick(item.buttonBlock) }
    }
}

data class SettingsSelectorWithButtonViewData(
    val data: SettingsSelectorViewData,
    val buttonBlock: SettingsBlock,
    val isButtonVisible: Boolean,
    val buttonText: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}