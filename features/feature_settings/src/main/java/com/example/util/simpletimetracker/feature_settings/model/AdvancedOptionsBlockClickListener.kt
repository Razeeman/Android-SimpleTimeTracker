package com.example.util.simpletimetracker.feature_settings.model

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock

interface AdvancedOptionsBlockClickListener {
    fun getAdvancedContent(): LiveData<List<ViewHolderType>>
    fun onAdvancedOptionsBlockClicked(block: SettingsBlock)
    fun onAdvancedOptionsSpinnerPositionSelected(block: SettingsBlock, position: Int)
}