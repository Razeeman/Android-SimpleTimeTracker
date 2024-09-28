package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextColor
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTopViewData
import javax.inject.Inject

class SettingsExportViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsCommonInteractor: SettingsCommonInteractor,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.ExportTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.ExportCollapse,
            title = resourceRepo.getString(R.string.settings_export_title),
            opened = !isCollapsed,
            iconResId = R.drawable.import_export,
            iconColor = (if (isDarkTheme) R.color.green_300 else R.color.green_200)
                .let(resourceRepo::getColor),
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            result += SettingsTextViewData(
                block = SettingsBlock.ExportSpreadsheet,
                title = resourceRepo.getString(R.string.settings_export_csv),
                subtitle = resourceRepo.getString(R.string.settings_export_csv_description),
                hint = resourceRepo.getString(R.string.settings_export_warning),
                hintColor = SettingsTextColor.Attention,
            )

            result += SettingsTextWithButtonViewData(
                buttonBlock = SettingsBlock.ExportSpreadsheetImportHint,
                data = SettingsTextViewData(
                    block = SettingsBlock.ExportSpreadsheetImport,
                    title = resourceRepo.getString(R.string.settings_import_csv),
                    subtitle = resourceRepo.getString(R.string.settings_import_csv_description),
                    hint = resourceRepo.getString(R.string.data_edit_hint),
                    hintColor = SettingsTextColor.Attention,
                ),
            )

            val automaticExportLastSaveTime = loadAutomaticExportLastSaveTime()
            val automaticExportLastSaveTimeVisible = automaticExportLastSaveTime.isNotEmpty()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.ExportSpreadsheetAutomatic,
                title = resourceRepo.getString(R.string.settings_automatic_export),
                subtitle = resourceRepo.getString(R.string.settings_automatic_description),
                isChecked = loadAutomaticExportEnabled(),
                bottomSpaceIsVisible = !automaticExportLastSaveTimeVisible,
                dividerIsVisible = !automaticExportLastSaveTimeVisible,
                forceBind = true,
            )
            if (automaticExportLastSaveTimeVisible) {
                result += SettingsHintViewData(
                    block = SettingsBlock.ExportSpreadsheetAutomaticHint,
                    text = automaticExportLastSaveTime,
                    textColor = SettingsTextColor.Success,
                    topSpaceIsVisible = false,
                )
            }

            result += SettingsTextViewData(
                block = SettingsBlock.ExportIcs,
                title = resourceRepo.getString(R.string.settings_export_ics),
                subtitle = resourceRepo.getString(R.string.settings_export_warning),
                subtitleColor = SettingsTextColor.Attention,
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.ExportBottom,
        )

        return result
    }

    private suspend fun loadAutomaticExportEnabled(): Boolean {
        return prefsInteractor.getAutomaticExportUri().isNotEmpty()
    }

    private suspend fun loadAutomaticExportLastSaveTime(): String {
        return if (loadAutomaticExportEnabled()) {
            settingsCommonInteractor.getLastSaveString(
                prefsInteractor.getAutomaticExportLastSaveTime(),
            )
        } else {
            ""
        }
    }
}