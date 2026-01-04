package com.example.util.simpletimetracker.feature_settings.views

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxWithIconViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsCheckboxWithIconBinding as Binding

fun createSettingsCheckboxWithIconAdapterDelegate(
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
            background = backgroundItemSettings,
            onClick = onClick,
        )

        item.iconResId?.let(binding.ivItemSettingsIcon::setImageResource)
            ?: binding.ivItemSettingsIcon.setImageDrawable(null)
        binding.cardItemSettingsIcon.setCardBackgroundColor(item.iconColor)
    }
}

data class SettingsCheckboxWithIconViewData(
    val data: SettingsCheckboxViewData,
    @DrawableRes val iconResId: Int?,
    @ColorInt val iconColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    override fun areContentsTheSame(other: ViewHolderType): Boolean =
        super.areContentsTheSame(other) && !data.forceBind
}