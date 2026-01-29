package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.backup.model.BackupOptionsData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdvancedOptionsUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsBackupViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.BackupOptionsParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsBackupViewModelDelegate @Inject constructor(
    private val router: Router,
    private val settingsBackupViewDataInteractor: SettingsBackupViewDataInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val automaticBackupInteractor: AutomaticBackupInteractor,
    private val automaticExportInteractor: AutomaticExportInteractor,
    private val settingsAdvancedOptionsUpdateInteractor: SettingsAdvancedOptionsUpdateInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsBackupViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.BackupCollapse ->
                onCollapseClick()
            SettingsBlock.BackupSave ->
                settingsFileWorkDelegate.onSaveClick(
                    params = BackupOptionsData.Save.Standard,
                )
            SettingsBlock.BackupAutomatic ->
                settingsFileWorkDelegate.onAutomaticBackupClick()
            SettingsBlock.BackupAutomaticTime ->
                onAutoBackupTriggerTimeClicked()
            SettingsBlock.BackupRestore ->
                settingsFileWorkDelegate.onRestoreClick(
                    tag = BACKUP_RESTORE_DIALOG_TAG,
                    params = BackupOptionsData.Restore.Standard,
                )
            SettingsBlock.BackupCustomized ->
                onCustomizeClick()
            // TODO move to export delegate?
            SettingsBlock.ExportSpreadsheet ->
                settingsFileWorkDelegate.onExportCsvClick(CSV_EXPORT_DIALOG_TAG)
            SettingsBlock.ExportSpreadsheetAutomatic ->
                settingsFileWorkDelegate.onAutomaticExportClick()
            SettingsBlock.ExportSpreadsheetAutomaticTime ->
                onAutoExportTriggerTimeClicked()
            SettingsBlock.ExportSpreadsheetImport ->
                settingsFileWorkDelegate.onImportCsvClick(CSV_IMPORT_ALERT_DIALOG_TAG)
            SettingsBlock.ExportSpreadsheetImportHint ->
                settingsFileWorkDelegate.onImportCsvHelpClick()
            SettingsBlock.ExportIcs -> delegateScope.launch {
                settingsAdvancedOptionsUpdateInteractor.sendDismiss()
                delay(200)
                settingsFileWorkDelegate.onExportIcsClick(ICS_EXPORT_DIALOG_TAG)
            }
            else -> {
                // Do nothing
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        onDateTimeSetDelegate(timestamp, tag)
    }

    fun onPositiveClick(tag: String?) {
        when (tag) {
            BACKUP_RESTORE_DIALOG_TAG -> {
                settingsFileWorkDelegate.onRestoreConfirmed()
            }
            CSV_IMPORT_ALERT_DIALOG_TAG -> delegateScope.launch {
                settingsAdvancedOptionsUpdateInteractor.sendDismiss()
                settingsFileWorkDelegate.onCsvImportConfirmed()
            }
        }
    }

    fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        when (data.tag) {
            CSV_EXPORT_DIALOG_TAG -> settingsFileWorkDelegate.onCsvExport(data)
            ICS_EXPORT_DIALOG_TAG -> settingsFileWorkDelegate.onIcsExport(data)
        }
    }

    fun collapse() {
        isCollapsed = true
    }

    private fun onAutoBackupTriggerTimeClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.AUTO_BACKUP_TRIGGER_TIME_DIALOG_TAG,
                timestamp = prefsInteractor.getAutomaticBackupTriggerTime(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onAutoExportTriggerTimeClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.AUTO_EXPORT_TRIGGER_TIME_DIALOG_TAG,
                timestamp = prefsInteractor.getAutomaticExportTriggerTime(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onDateTimeSetDelegate(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.AUTO_BACKUP_TRIGGER_TIME_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setAutomaticBackupTriggerTime(newValue)
                automaticBackupInteractor.schedule()
                parent?.updateContent()
            }
            SettingsViewModel.AUTO_EXPORT_TRIGGER_TIME_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setAutomaticExportTriggerTime(newValue)
                automaticExportInteractor.schedule()
                parent?.updateContent()
            }
        }
    }

    private fun onCustomizeClick() {
        router.navigate(BackupOptionsParams)
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    companion object {
        private const val CSV_EXPORT_DIALOG_TAG = "csv_export_dialog_tag"
        private const val ICS_EXPORT_DIALOG_TAG = "ics_export_dialog_tag"
        private const val BACKUP_RESTORE_DIALOG_TAG = "backup_restore_dialog_tag"
        private const val CSV_IMPORT_ALERT_DIALOG_TAG = "csv_import_alert_dialog_tag"
    }
}