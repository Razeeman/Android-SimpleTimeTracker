package com.example.util.simpletimetracker.feature_settings.adapter

import android.view.View
import android.widget.Space
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSelectorBinding as Binding

fun createSettingsSelectorAdapterDelegate(
    onClick: (SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        selectorAdapterBindDelegate(
            item = item,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            value = tvItemSettingsSelectorValue,
            space = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            group = groupItemSettingsSelector,
            onClick = onClick,
        )
    }
}

fun selectorAdapterBindDelegate(
    item: SettingsSelectorViewData,
    title: AppCompatTextView,
    subtitle: AppCompatTextView,
    value: AppCompatTextView,
    space: Space,
    divider: View,
    group: LinearLayoutCompat,
    onClick: (SettingsBlock) -> Unit,
) {
    title.text = item.title
    if (item.subtitle.isEmpty()) {
        subtitle.visible = false
    } else {
        subtitle.text = item.subtitle
        subtitle.visible = true
    }
    value.text = item.selectedValue
    space.visible = item.bottomSpaceIsVisible
    divider.visible = item.dividerIsVisible
    group.setOnClick { onClick(item.block) }
}

data class SettingsSelectorViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val selectedValue: String,
    val bottomSpaceIsVisible: Boolean = true,
    val dividerIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}