package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsCheckboxBinding as Binding

fun createSettingsCheckboxAdapterDelegate(
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

        if (checkboxItemSettings.isChecked != item.isChecked) {
            checkboxItemSettings.isChecked = item.isChecked
        }

        spaceItemSettingsTop.visible = item.topSpaceIsVisible
        spaceItemSettingsBottom.visible = item.bottomSpaceIsVisible
        viewItemSettingsDivider.visible = item.dividerIsVisible

        checkboxItemSettings.setOnClick { onClick(item.block) }
    }
}

data class SettingsCheckboxViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean,
    val topSpaceIsVisible: Boolean = true,
    val bottomSpaceIsVisible: Boolean = true,
    val dividerIsVisible: Boolean = true,
    val forceBind: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    override fun areContentsTheSame(other: ViewHolderType): Boolean =
        super.areContentsTheSame(other) && !forceBind
}