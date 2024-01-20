package com.example.util.simpletimetracker.feature_settings.adapter

import android.view.View
import android.widget.Space
import androidx.appcompat.widget.AppCompatTextView
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsTextColor
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

        textAdapterBindDelegate(
            item = item,
            title = tvItemSettingsTitle,
            subtitle = tvItemSettingsSubtitle,
            spaceTop = spaceItemSettingsTop,
            spaceBottom = spaceItemSettingsBottom,
            divider = viewItemSettingsDivider,
        )

        layoutItemSettingsText.setOnClick { onClick(item.block) }
    }
}

fun textAdapterBindDelegate(
    item: SettingsTextViewData,
    title: AppCompatTextView,
    subtitle: AppCompatTextView,
    spaceTop: Space,
    spaceBottom: Space,
    divider: View,
) {
    title.text = item.title
    if (item.subtitle.isEmpty()) {
        subtitle.visible = false
    } else {
        subtitle.text = item.subtitle
        item.subtitleColor.getColor(subtitle.context).let(subtitle::setTextColor)
        subtitle.visible = true
    }
    spaceTop.visible = item.topSpaceIsVisible
    spaceBottom.visible = item.bottomSpaceIsVisible
    divider.visible = item.dividerIsVisible
}

data class SettingsTextViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val subtitleColor: SettingsTextColor = SettingsTextColor.Default,
    val topSpaceIsVisible: Boolean = true,
    val dividerIsVisible: Boolean = true,
    val bottomSpaceIsVisible: Boolean = true,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}