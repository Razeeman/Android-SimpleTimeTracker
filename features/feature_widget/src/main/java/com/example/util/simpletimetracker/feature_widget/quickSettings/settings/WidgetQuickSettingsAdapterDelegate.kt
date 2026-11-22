package com.example.util.simpletimetracker.feature_widget.quickSettings.settings

import androidx.annotation.StringRes
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_widget.databinding.ItemWidgetQuickSettingsBinding as Binding
import com.example.util.simpletimetracker.feature_widget.quickSettings.settings.WidgetQuickSettingsViewData as ViewData

fun createWidgetQuickSettingsAdapterDelegate(
    onItemClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as ViewData

        tvItemWidgetQuickSettingsTitle.setText(item.title)
        tvItemWidgetQuickSettingsSubtitle.setText(item.subtitle)
        layoutItemWidgetQuickSettings.setOnClickWith(item, onItemClick)
    }
}

data class WidgetQuickSettingsViewData(
    val type: QuickSettingsWidgetType,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = title.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
