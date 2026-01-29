package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdvancedOptionsUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsExportViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ExportOptionsParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsExportViewModelDelegate @Inject constructor(
    private val router: Router,
    private val settingsExportViewDataInteractor: SettingsExportViewDataInteractor,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val settingsAdvancedOptionsUpdateInteractor: SettingsAdvancedOptionsUpdateInteractor,
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
        settingsAdvancedOptionsUpdateInteractor.sendDismiss()
        settingsFileWorkDelegate.onTriggerAutoExportClick()
    }

    private fun onCustomizeClick() {
        router.navigate(ExportOptionsParams)
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