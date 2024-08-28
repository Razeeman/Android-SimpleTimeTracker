package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsCheckboxWithButtonBinding as Binding

fun createSettingsCheckboxWithButtonAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        checkboxAdapterBindDelegate(
            item = item.data,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            checkbox = checkboxItemSettings,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            onClick = onClick,
        )

        btnItemSettings.visible = item.isButtonVisible
        btnItemSettings.setOnClick { onClick(item.buttonBlock) }
    }
}

data class SettingsCheckboxWithButtonViewData(
    val data: SettingsCheckboxViewData,
    val buttonBlock: SettingsBlock,
    val isButtonVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = buttonBlock.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    override fun areContentsTheSame(other: ViewHolderType): Boolean =
        super.areContentsTheSame(other) && !data.forceBind
}