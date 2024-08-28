package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsBottomViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsBottomBinding as Binding

fun createSettingsBottomAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { _, _, _ ->
    // Nothing to bind
}

data class SettingsBottomViewData(
    val block: SettingsBlock,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}