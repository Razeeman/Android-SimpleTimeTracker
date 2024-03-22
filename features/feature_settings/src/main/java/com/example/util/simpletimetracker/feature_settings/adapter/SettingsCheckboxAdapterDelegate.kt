package com.example.util.simpletimetracker.feature_settings.adapter

import android.view.View
import android.widget.Space
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
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

        checkboxAdapterBindDelegate(
            item = item,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            checkbox = checkboxItemSettings,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
            onClick = onClick,
        )
    }
}

fun checkboxAdapterBindDelegate(
    item: ViewData,
    title: AppCompatTextView,
    subtitle: AppCompatTextView,
    checkbox: AppCompatCheckBox,
    spaceTop: Space,
    spaceBottom: Space,
    divider: View,
    onClick: (block: SettingsBlock) -> Unit,
) {
    title.text = item.title

    if (item.subtitle.isEmpty()) {
        subtitle.visible = false
    } else {
        subtitle.text = item.subtitle
        subtitle.visible = true
    }

    if (checkbox.isChecked != item.isChecked) {
        checkbox.isChecked = item.isChecked
    }

    spaceTop.visible = item.topSpaceIsVisible
    spaceBottom.visible = item.bottomSpaceIsVisible
    divider.visible = item.dividerIsVisible

    checkbox.setOnClick { onClick(item.block) }
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