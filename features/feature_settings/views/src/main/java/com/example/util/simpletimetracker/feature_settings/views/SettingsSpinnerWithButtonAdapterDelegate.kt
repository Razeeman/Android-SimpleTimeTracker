package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.views.SettingsSpinnerWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsSpinnerWithButtonBinding as Binding

fun createSettingsSpinnerWithButtonAdapterDelegate(
    onPositionSelected: (block: SettingsBlock, position: Int) -> Unit,
    onButtonClicked: (block: SettingsBlock) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        spinnerAdapterBindDelegate(
            item = item.data,
            title = tvItemSettingsTitle,
            value = tvItemSettingsValue,
            spinner = spinnerItemSettings,
            onPositionSelected = onPositionSelected,
        )

        btnItemSettings.visible = item.isButtonVisible
        btnItemSettings.setOnClick { onButtonClicked(item.data.block) }
    }
}

data class SettingsSpinnerWithButtonViewData(
    val data: SettingsSpinnerViewData,
    val isButtonVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}