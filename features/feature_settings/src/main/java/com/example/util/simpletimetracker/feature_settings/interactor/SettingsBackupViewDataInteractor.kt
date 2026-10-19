package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.AutomaticSaveIntervalViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextColor
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTopViewData
import javax.inject.Inject

class SettingsBackupViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val settingsCommonInteractor: SettingsCommonInteractor,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.BackupTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.BackupCollapse,
            title = resourceRepo.getString(R.string.settings_backup_title),
            opened = !isCollapsed,
            iconResId = R.drawable.save,
            iconColor = (if (isDarkTheme) R.color.green_300 else R.color.green_200)
                .let(resourceRepo::getColor),
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            result += SettingsTextViewData(
                block = SettingsBlock.BackupSave,
                title = resourceRepo.getString(R.string.settings_save_backup),
                subtitle = resourceRepo.getString(R.string.settings_save_description),
            )

            result += SettingsTextViewData(
                block = SettingsBlock.BackupRestore,
                title = resourceRepo.getString(R.string.settings_restore_backup),
                subtitle = resourceRepo.getString(R.string.settings_restore_description),
                subtitleColor = SettingsTextColor.Attention,
            )

            val automaticBackupEnabled = loadAutomaticBackupEnabled()
            val automaticBackupLastSaveTime = loadAutomaticBackupLastSaveTime()
            val automaticBackupLastSaveTimeVisible = automaticBackupLastSaveTime.isNotEmpty()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.BackupAutomatic,
                title = resourceRepo.getString(R.string.settings_automatic_backup),
                subtitle = resourceRepo.getString(R.string.settings_automatic_description),
                isChecked = automaticBackupEnabled,
                bottomSpaceIsVisible = !automaticBackupEnabled,
                dividerIsVisible = !automaticBackupEnabled,
                forceBind = true,
            )
            if (automaticBackupLastSaveTimeVisible) {
                result += SettingsHintViewData(
                    block = SettingsBlock.BackupAutomaticHint,
                    text = automaticBackupLastSaveTime,
                    textColor = SettingsTextColor.Success,
                    topSpaceIsVisible = false,
                    dividerIsVisible = false,
                    bottomSpaceIsVisible = false,
                )
            }
            if (automaticBackupEnabled) {
                result += SettingsSelectorViewData(
                    block = SettingsBlock.BackupAutomaticTime,
                    title = resourceRepo.getString(R.string.settings_automatic_save_time),
                    subtitle = "",
                    selectedValue = loadAutomaticBackupTriggerTime(),
                    bottomSpaceIsVisible = true,
                    dividerIsVisible = false,
                )
                val automaticBackupIntervalViewData = loadAutomaticBackupIntervalViewData()
                result += SettingsSpinnerViewData(
                    block = SettingsBlock.BackupAutomaticInterval,
                    title = resourceRepo.getString(R.string.settings_automatic_save_interval),
                    value = automaticBackupIntervalViewData.items
                        .getOrNull(automaticBackupIntervalViewData.selectedPosition)?.text.orEmpty(),
                    items = automaticBackupIntervalViewData.items,
                    selectedPosition = automaticBackupIntervalViewData.selectedPosition,
                    processSameItemSelected = false,
                    topSpaceIsVisible = false,
                )
            }

            result += SettingsTextViewData(
                block = SettingsBlock.BackupCustomized,
                title = resourceRepo.getString(R.string.settings_backup_options),
                subtitle = "",
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
                prefsInteractor.getAutomaticBackupLastSaveTime(),
            )
        } else {
            ""
        }
    }

    private suspend fun loadAutomaticBackupTriggerTime(): String {
        return settingsMapper.toStartOfDayText(
            startOfDayShift = prefsInteractor.getAutomaticBackupTriggerTime(),
            useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
        )
    }

    private suspend fun loadAutomaticBackupIntervalViewData(): AutomaticSaveIntervalViewData {
        return prefsInteractor.getAutomaticBackupInterval()
            .let(settingsMapper::toAutomaticSaveIntervalViewData)
    }
}