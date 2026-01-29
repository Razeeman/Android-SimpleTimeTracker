package com.example.util.simpletimetracker.feature_settings.model

import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock

interface AdvancedOptionsBlockClickListener {

    fun onAdvancedOptionsBlockClicked(block: SettingsBlock)
    fun onAdvancedOptionsSpinnerPositionSelected(block: SettingsBlock, position: Int)
}