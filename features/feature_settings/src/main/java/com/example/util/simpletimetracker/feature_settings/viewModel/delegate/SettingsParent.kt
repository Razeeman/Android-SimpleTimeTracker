package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.feature_settings.model.OptionsContent

interface SettingsParent {
    fun openOptions(content: OptionsContent)
    suspend fun updateContent()
}