package com.example.util.simpletimetracker.feature_settings.views

import android.view.View
import android.widget.Space
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsCheckboxBinding as Binding

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
            background = backgroundItemSettings,
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
    background: CardView,
    onClick: (block: SettingsBlock) -> Unit,
) {
    title.text = item.title
    subtitle.setTextOptional(item.subtitle)
    if (checkbox.isChecked != item.isChecked) checkbox.isChecked = item.isChecked
    spaceTop.visible = item.topSpaceIsVisible
    spaceBottom.visible = item.bottomSpaceIsVisible
    divider.visible = item.dividerIsVisible
    background.visible = item.backgroundIsVisible

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
    val backgroundIsVisible: Boolean = true,
    val forceBind: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    override fun areContentsTheSame(other: ViewHolderType): Boolean =
        super.areContentsTheSame(other) && !forceBind
}