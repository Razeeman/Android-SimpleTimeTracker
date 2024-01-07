package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsTopBinding as Binding

fun createSettingsTopAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { _, _, _ ->
    // Nothing to bind
}

data class SettingsTopViewData(
    val block: SettingsBlock,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}