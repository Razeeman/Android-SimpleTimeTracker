package com.example.util.simpletimetracker.feature_settings.adapter

import androidx.appcompat.widget.AppCompatTextView
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsSpinnerBinding as Binding

fun createSettingsSpinnerAdapterDelegate(
    onPositionSelected: (block: SettingsBlock, position: Int) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        spinnerAdapterBindDelegate(
            item = item,
            title = tvItemSettingsTitle,
            value = tvItemSettingsValue,
            spinner = spinnerItemSettings,
            onPositionSelected = onPositionSelected,
        )
    }
}

fun spinnerAdapterBindDelegate(
    item: ViewData,
    title: AppCompatTextView,
    value: AppCompatTextView,
    spinner: CustomSpinner,
    onPositionSelected: (block: SettingsBlock, position: Int) -> Unit,
) {
    title.text = item.title
    value.text = item.value
    spinner.setProcessSameItemSelection(item.processSameItemSelected)
    spinner.setData(item.items, item.selectedPosition)
    spinner.onPositionSelected = { onPositionSelected(item.block, it) }
}

data class SettingsSpinnerViewData(
    val block: SettingsBlock,
    val title: String,
    val value: String,
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int,
    val processSameItemSelected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}