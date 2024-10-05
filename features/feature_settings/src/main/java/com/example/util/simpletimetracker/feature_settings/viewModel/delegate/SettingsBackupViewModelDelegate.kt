package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsBackupViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.BackupOptionsParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsBackupViewModelDelegate @Inject constructor(
    private val router: Router,
    private val settingsBackupViewDataInteractor: SettingsBackupViewDataInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
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
            SettingsBlock.BackupRestore ->
                settingsFileWorkDelegate.onRestoreClick(
                    tag = BACKUP_RESTORE_DIALOG_TAG,
                    params = BackupOptionsData.Restore.Standard,
                )
            SettingsBlock.BackupCustomized ->
                onCustomizeClick()
            // TODO move to export delegate
            SettingsBlock.ExportSpreadsheet ->
                settingsFileWorkDelegate.onExportCsvClick(CSV_EXPORT_DIALOG_TAG)
            SettingsBlock.ExportSpreadsheetAutomatic ->
                settingsFileWorkDelegate.onAutomaticExportClick()
            SettingsBlock.ExportSpreadsheetImport ->
                settingsFileWorkDelegate.onImportCsvClick(CSV_IMPORT_ALERT_DIALOG_TAG)
            SettingsBlock.ExportSpreadsheetImportHint ->
                settingsFileWorkDelegate.onImportCsvHelpClick()
            SettingsBlock.ExportIcs ->
                settingsFileWorkDelegate.onExportIcsClick(ICS_EXPORT_DIALOG_TAG)
            else -> {
                // Do nothing
            }
        }
    }

    fun onPositiveClick(tag: String?) {
        when (tag) {
            BACKUP_RESTORE_DIALOG_TAG -> settingsFileWorkDelegate.onRestoreConfirmed()
            CSV_IMPORT_ALERT_DIALOG_TAG -> settingsFileWorkDelegate.onCsvImportConfirmed()
        }
    }

    fun onDataExportSettingsSelected(data: DataExportSettingsResult) = delegateScope.launch {
        val rangeLength = data.range.toModel()
        val range = if (rangeLength !is RangeLength.All) {
            timeMapper.getRangeStartAndEnd(
                rangeLength = data.range.toModel(),
                shift = 0,
                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                startOfDayShift = prefsInteractor.getStartOfDayShift(),
            )
        } else {
            null
        }
        when (data.tag) {
            CSV_EXPORT_DIALOG_TAG -> settingsFileWorkDelegate.onCsvExport(range)
            ICS_EXPORT_DIALOG_TAG -> settingsFileWorkDelegate.onIcsExport(range)
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