package com.example.util.simpletimetracker.feature_settings.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTranslatorViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsTranslatorBinding as Binding

fun createSettingsTranslatorAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTranslators.text = item.translator
        tvItemSettingsTranslatorsLanguage.isVisible = !item.language.isNullOrBlank()
        tvItemSettingsTranslatorsLanguage.text = item.language
    }
}

data class SettingsTranslatorViewData(
    val translator: String,
    val language: String? = null,
) : ViewHolderType {

    override fun getUniqueId(): Long = translator.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}