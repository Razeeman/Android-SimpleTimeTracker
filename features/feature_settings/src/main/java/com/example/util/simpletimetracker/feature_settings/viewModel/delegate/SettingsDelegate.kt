package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.model.OptionsContent
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface SettingsDelegate {
    fun init(parent: SettingsParent) = Unit
    suspend fun getViewData(): ViewData
    suspend fun getSheetViewData(content: OptionsContent): List<ViewHolderType>? = null
    fun onHidden() = Unit
    fun onBlockClicked(block: SettingsBlock) = Unit
    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) = Unit
    fun onDurationSet(tag: String?, duration: Long) = Unit
    fun onDurationDisabled(tag: String?) = Unit
    fun onDateTimeSet(timestamp: Long, tag: String?) = Unit
    fun onDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData) = Unit
    fun collapse() = Unit
    fun onTypesSelected(typeIds: List<Long>, tag: String) = Unit
    fun onOptionsItemClick(id: OptionsListParams.Item.Id) = Unit
    fun onPositiveClick(tag: String?) = Unit
    fun onDataExportSettingsSelected(data: DataExportSettingsResult) = Unit

    interface Key

    data class ViewData(
        val key: Key,
        val data: List<ViewHolderType>,
    )
}