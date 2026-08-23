package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsOptionsUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsExportViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsExportViewModelDelegate @Inject constructor(
    private val settingsExportViewDataInteractor: SettingsExportViewDataInteractor,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val settingsOptionsUpdateInteractor: SettingsOptionsUpdateInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsExportViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    suspend fun getAdvancedViewData(): List<ViewHolderType> {
        return settingsExportViewDataInteractor.executeAdvanced()
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.ExportCustomized -> onCustomizeClick()
            SettingsBlock.ExportCollapse -> onCollapseClick()
            SettingsBlock.ExportTriggerAutoBackup -> onTriggerAutoExportClick()
            else -> {
                // Do nothing
            }
        }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        when (block) {
            SettingsBlock.ExportSpreadsheetDateTimeFormat -> onDateTimeFormatSelected(position)
            else -> {
                // Do nothing
            }
        }
    }

    fun collapse() {
        isCollapsed = true
    }

    private fun onTriggerAutoExportClick() = delegateScope.launch {
        settingsOptionsUpdateInteractor.sendDismiss()
        settingsFileWorkDelegate.onTriggerAutoExportClick()
    }

    private fun onCustomizeClick() = delegateScope.launch {
        parent?.openExportOptions()
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    private fun onDateTimeFormatSelected(position: Int) {
        delegateScope.launch {
            val newData = settingsMapper.toCsvExportDateTimeFormat(position)
            prefsInteractor.setCsvExportDateTimeFormat(newData)
            parent?.updateContent()
        }
    }
}