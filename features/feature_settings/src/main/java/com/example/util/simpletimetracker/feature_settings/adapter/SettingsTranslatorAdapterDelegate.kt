package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTranslatorViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.databinding.ItemSettingsTranslatorBinding as Binding

fun createSettingsTranslatorAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemSettingsTranslators.text = item.translator
        tvItemSettingsTranslatorsLanguage.text = item.language
    }
}