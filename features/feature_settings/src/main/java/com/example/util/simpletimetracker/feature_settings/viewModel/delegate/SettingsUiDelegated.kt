package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface SettingsUiDelegated {
    fun onHidden()
    fun onBlockClicked(block: SettingsBlock)
    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int)
    fun onPositiveClick(tag: String?)
    fun onDurationSet(tag: String?, duration: Long)
    fun onDurationDisabled(tag: String?)
    fun onDateTimeSet(timestamp: Long, tag: String?)
    fun onDataExportSettingsSelected(data: DataExportSettingsResult)
    fun onTypesSelected(typeIds: List<Long>, tag: String)
    fun onOptionsItemClick(id: OptionsListParams.Item.Id)
    fun onDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData)
}