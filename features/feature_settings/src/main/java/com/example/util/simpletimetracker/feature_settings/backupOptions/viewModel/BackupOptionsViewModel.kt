package com.example.util.simpletimetracker.feature_settings.backupOptions.viewModel

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BackupOptionsViewModel @Inject constructor(
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
) : BaseViewModel() {

    val dismiss: LiveData<Unit>
        by lazy { SingleLiveEvent<Unit>() }

    fun onPartialSaveClick() {
        settingsFileWorkDelegate.onSaveClick(
            params = BackupOptionsData.Save.SaveWithoutRecords,
        )
        dismiss.set(Unit)
    }

    fun onFullRestoreClick() {
        settingsFileWorkDelegate.onRestoreClick(
            tag = BACKUP_OPTIONS_RESTORE_DIALOG_TAG,
            params = BackupOptionsData.Restore.WithSettings,
        )
    }

    fun onPartialRestoreClick() {
        settingsFileWorkDelegate.onPartialRestoreClick()
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

    companion object {
        private const val BACKUP_OPTIONS_RESTORE_DIALOG_TAG = "backup_options_restore_dialog_tag"
    }
}
