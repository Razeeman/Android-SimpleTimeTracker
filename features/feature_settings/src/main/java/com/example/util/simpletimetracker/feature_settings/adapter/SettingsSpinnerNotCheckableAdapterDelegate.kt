package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerNotCheckableViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSpinnerNotCheckableBinding as Binding

fun createSettingsSpinnerNotCheckableAdapterDelegate(
    onPositionSelected: (block: SettingsBlock, position: Int) -> Unit,
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
    }
}

data class SettingsSpinnerNotCheckableViewData(
    val data: SettingsSpinnerViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}