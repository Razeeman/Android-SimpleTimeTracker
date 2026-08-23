package com.example.util.simpletimetracker.feature_settings.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_settings.views.SettingsTranslatorViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsTranslatorBinding as Binding

fun createSettingsTranslatorAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTranslators.text = item.translator
        tvItemSettingsTranslatorsLanguage.setTextOptional(item.language)
    }
}

data class SettingsTranslatorViewData(
    val translator: String,
    val language: String? = null,
) : ViewHolderType {

    override fun getUniqueId(): Long = translator.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}