package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerWithButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSpinnerWithButtonBinding as Binding

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

        btnItemSettingsSpinner.visible = item.isButtonVisible
        btnItemSettingsSpinner.setOnClick { onButtonClicked(item.data.block) }
    }
}

data class SettingsSpinnerWithButtonViewData(
    val data: SettingsSpinnerViewData,
    val isButtonVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}