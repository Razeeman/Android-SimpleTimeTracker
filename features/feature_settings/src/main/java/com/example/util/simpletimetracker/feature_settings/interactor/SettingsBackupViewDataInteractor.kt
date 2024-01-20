package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsTextColor
import javax.inject.Inject

class SettingsBackupViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsCommonInteractor: SettingsCommonInteractor,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.BackupTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.BackupCollapse,
            title = resourceRepo.getString(R.string.settings_backup_title),
            opened = !isCollapsed,
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            result += SettingsTextViewData(
                block = SettingsBlock.BackupSave,
                title = resourceRepo.getString(R.string.settings_save_backup),
                subtitle = resourceRepo.getString(R.string.settings_save_description),
            )

            val automaticBackupLastSaveTime = loadAutomaticBackupLastSaveTime()
            val automaticBackupLastSaveTimeVisible = automaticBackupLastSaveTime.isNotEmpty()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.BackupAutomatic,
                title = resourceRepo.getString(R.string.settings_automatic_backup),
                subtitle = resourceRepo.getString(R.string.settings_automatic_description),
                isChecked = loadAutomaticBackupEnabled(),
                bottomSpaceIsVisible = !automaticBackupLastSaveTimeVisible,
                dividerIsVisible = !automaticBackupLastSaveTimeVisible,
                forceBind = true,
            )
            if (automaticBackupLastSaveTimeVisible) {
                result += SettingsHintViewData(
                    block = SettingsBlock.BackupAutomaticHint,
                    text = automaticBackupLastSaveTime,
                    textColor = SettingsTextColor.Success,
                    topSpaceIsVisible = false,
                )
            }

            result += SettingsTextViewData(
                block = SettingsBlock.BackupRestore,
                title = resourceRepo.getString(R.string.settings_restore_backup),
                subtitle = resourceRepo.getString(R.string.settings_restore_description),
                subtitleColor = SettingsTextColor.Attention,
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.BackupBottom,
        )

        return result
    }

    private suspend fun loadAutomaticBackupEnabled(): Boolean {
        return prefsInteractor.getAutomaticBackupUri().isNotEmpty()
    }

    private suspend fun loadAutomaticBackupLastSaveTime(): String {
        return if (loadAutomaticBackupEnabled()) {
            settingsCommonInteractor.getLastSaveString(
                prefsInteractor.getAutomaticBackupLastSaveTime()
            )
        } else {
            ""
        }
    }
}