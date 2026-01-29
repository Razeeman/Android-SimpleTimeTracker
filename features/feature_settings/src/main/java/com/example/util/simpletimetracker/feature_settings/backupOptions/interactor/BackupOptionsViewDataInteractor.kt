package com.example.util.simpletimetracker.feature_settings.backupOptions.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextColor
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextViewData
import javax.inject.Inject

class BackupOptionsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTextViewData(
            block = SettingsBlock.BackupCustomizedPartialSave,
            title = resourceRepo.getString(R.string.backup_options_save_without_records),
            subtitle = resourceRepo.getString(R.string.backup_options_save_without_records_hint),
        )

        result += SettingsTextViewData(
            block = SettingsBlock.BackupCustomizedFullRestore,
            title = resourceRepo.getString(R.string.backup_options_full_restore),
            subtitle = resourceRepo.getString(R.string.backup_options_full_restore_hint),
            hint = resourceRepo.getString(R.string.settings_restore_description),
            hintColor = SettingsTextColor.Attention,
        )

        result += SettingsTextViewData(
            block = SettingsBlock.BackupCustomizedPartialRestore,
            title = resourceRepo.getString(R.string.backup_options_partial_restore),
            subtitle = resourceRepo.getString(R.string.backup_options_partial_restore_hint),
        )

        if (loadAutomaticBackupEnabled()) {
            result += SettingsTextViewData(
                block = SettingsBlock.BackupCustomizedTriggerAutoBackup,
                title = resourceRepo.getString(R.string.backup_options_trigger_auto_backup),
                subtitle = "",
            )
        }

        return result
    }

    private suspend fun loadAutomaticBackupEnabled(): Boolean {
        return prefsInteractor.getAutomaticBackupUri().isNotEmpty()
    }
}