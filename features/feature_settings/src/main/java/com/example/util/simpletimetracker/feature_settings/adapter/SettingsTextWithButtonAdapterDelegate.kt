package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsTextWithButtonBinding as Binding

fun createSettingsTextWithButtonAdapterDelegate(
    onButtonClick: (block: SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        textAdapterBindDelegate(
            item = item.data,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
        )

        btnItemSettingsText.setOnClick { onButtonClick(item.data.block) }
    }
}

data class SettingsTextWithButtonViewData(
    val data: SettingsTextViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}