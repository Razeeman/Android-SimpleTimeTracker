package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsTextWithButtonBinding as Binding

fun createSettingsTextWithButtonAdapterDelegate(
    onClick: (block: SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        textAdapterBindDelegate(
            item = item.data,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            hint = tvItemSettingsHint,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            layout = layoutItemSettingsText,
            onClick = onClick,
        )

        btnItemSettings.setOnClick { onClick(item.buttonBlock) }
    }
}

data class SettingsTextWithButtonViewData(
    val buttonBlock: SettingsBlock,
    val data: SettingsTextViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}