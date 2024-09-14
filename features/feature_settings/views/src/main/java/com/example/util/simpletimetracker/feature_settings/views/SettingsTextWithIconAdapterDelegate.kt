package com.example.util.simpletimetracker.feature_settings.views

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextWithIconViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsTextWithIconBinding as Binding

fun createSettingsTextWithIconAdapterDelegate(
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
            hint = null,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            layout = layoutItemSettingsText,
            onClick = onClick,
        )

        binding.ivItemSettingsIcon.setImageResource(item.iconResId)
        binding.cardItemSettingsIcon.setCardBackgroundColor(item.iconColor)
    }
}

data class SettingsTextWithIconViewData(
    val data: SettingsTextViewData,
    @DrawableRes val iconResId: Int,
    @ColorInt val iconColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}