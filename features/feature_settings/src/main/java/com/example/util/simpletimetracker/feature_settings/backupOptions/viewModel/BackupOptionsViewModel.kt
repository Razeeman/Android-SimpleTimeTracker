package com.example.util.simpletimetracker.feature_settings.backupOptions.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.backup.model.BackupOptionsData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.backupOptions.interactor.BackupOptionsViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@HiltViewModel
class BackupOptionsViewModel @Inject constructor(
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
    private val backupOptionsViewDataInteractor: BackupOptionsViewDataInteractor,
    private val automaticBackupInteractor: AutomaticBackupInteractor,
) : BaseViewModel() {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val dismiss: LiveData<Unit> by lazy { SingleLiveEvent<Unit>() }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.BackupCustomizedPartialSave ->
                onPartialSaveClick()
            SettingsBlock.BackupCustomizedFullRestore ->
                onFullRestoreClick()
            SettingsBlock.BackupCustomizedPartialRestore ->
                onPartialRestoreClick()
            SettingsBlock.BackupCustomizedTriggerAutoBackup ->
                onTriggerAutoBackupClick()
            else -> {
                // Do nothing.
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        // Do nothing.
    }

    private fun onPartialSaveClick() {
        settingsFileWorkDelegate.onSaveClick(
            params = BackupOptionsData.Save.SaveWithoutRecords,
        )
        dismiss.set(Unit)
    }

    private fun onFullRestoreClick() {
        settingsFileWorkDelegate.onRestoreClick(
            tag = BACKUP_OPTIONS_RESTORE_DIALOG_TAG,
            params = BackupOptionsData.Restore.WithSettings,
        )
    }

    private fun onPartialRestoreClick() {
        settingsFileWorkDelegate.onPartialRestoreClick()
        dismiss.set(Unit)
    }

    private fun onTriggerAutoBackupClick() {
        MainScope().launch {
            val result = automaticBackupInteractor.backup()
            result?.message?.let(settingsFileWorkDelegate::showMessage)
        }
        dismiss.set(Unit)
    }

    fun onPositiveClick(tag: String?) {
        when (tag) {
            BACKUP_OPTIONS_RESTORE_DIALOG_TAG -> {
                settingsFileWorkDelegate.onRestoreConfirmed()
                dismiss.set(Unit)
            }
        }
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        return backupOptionsViewDataInteractor.execute()
    }

    companion object {
        private const val BACKUP_OPTIONS_RESTORE_DIALOG_TAG = "backup_options_restore_dialog_tag"
    }
}
