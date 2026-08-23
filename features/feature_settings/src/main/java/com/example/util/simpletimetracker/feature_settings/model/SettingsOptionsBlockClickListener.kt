package com.example.util.simpletimetracker.feature_settings.model

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock

interface SettingsOptionsBlockClickListener {
    fun getOptionsContent(): LiveData<List<ViewHolderType>>
    fun onOptionsBlockClicked(block: SettingsBlock)
    fun onOptionsSpinnerPositionSelected(block: SettingsBlock, position: Int)
    fun onOptionsDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData)
}