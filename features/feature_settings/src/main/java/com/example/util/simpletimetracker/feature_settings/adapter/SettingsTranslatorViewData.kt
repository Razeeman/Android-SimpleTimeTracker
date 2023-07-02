package com.example.util.simpletimetracker.feature_settings.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class SettingsTranslatorViewData(
    val translator: String,
    val language: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = translator.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is SettingsTranslatorViewData
}