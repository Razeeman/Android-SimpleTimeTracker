package com.example.util.simpletimetracker.feature_settings.views

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsSelectorWithButtonBinding as Binding

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
            background = backgroundItemSettings,
            onClick = onClick,
        )

        btnItemSettings.visible = item.buttonContent != null
        tvItemSettingsSelectorButton.visible = item.buttonContent is ViewData.Button.Text
        ivItemSettingsSelectorButton.visible = item.buttonContent is ViewData.Button.Icon
        when (val content = item.buttonContent) {
            is ViewData.Button.Text -> {
                tvItemSettingsSelectorButton.text = content.text
            }
            is ViewData.Button.Icon -> {
                ivItemSettingsSelectorButton.setImageResource(content.drawableResId)
            }
            null -> Unit
        }
        btnItemSettings.setOnClick { onClick(item.buttonBlock) }
    }
}

data class SettingsSelectorWithButtonViewData(
    val data: SettingsSelectorViewData,
    val buttonBlock: SettingsBlock,
    val buttonContent: Button?,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    sealed interface Button {
        data class Text(val text: String) : Button
        data class Icon(@DrawableRes val drawableResId: Int) : Button
    }
}