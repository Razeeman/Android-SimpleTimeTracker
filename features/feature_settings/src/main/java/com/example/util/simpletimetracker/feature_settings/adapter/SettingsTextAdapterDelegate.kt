package com.example.util.simpletimetracker.feature_settings.adapter

import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsTextBinding as Binding

fun createSettingsTextAdapterDelegate(
    onClick: (block: SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTitle.text = item.title

        if (item.subtitle.isEmpty()) {
            tvItemSettingsSubtitle.visible = false
            tvItemSettingsTitle.updatePadding(bottom = 12.dpToPx())
        } else {
            tvItemSettingsSubtitle.text = item.subtitle
            tvItemSettingsSubtitle.visible = true
            tvItemSettingsTitle.updatePadding(bottom = 0.dpToPx())
        }

        viewItemSettingsDivider.visible = item.dividerIsVisible

        layoutItemSettingsText.setOnClick { onClick(item.block) }
    }
}

data class SettingsTextViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}