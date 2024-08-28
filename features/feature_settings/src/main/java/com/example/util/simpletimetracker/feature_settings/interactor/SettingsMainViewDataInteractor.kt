package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.feature_settings_views.SettingsSpinnerNotCheckableViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTopViewData
import javax.inject.Inject

class SettingsMainViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
    private val languageInteractor: LanguageInteractor,
) {

    suspend fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.MainTop,
        )

        result += SettingsCheckboxViewData(
            block = SettingsBlock.AllowMultitasking,
            title = resourceRepo.getString(R.string.settings_allow_multitasking),
            subtitle = resourceRepo.getString(R.string.settings_allow_multitasking_hint),
            isChecked = prefsInteractor.getAllowMultitasking(),
        )

        val darkModeViewData = loadDarkModeViewData()
        result += SettingsSpinnerViewData(
            block = SettingsBlock.DarkMode,
            title = resourceRepo.getString(R.string.settings_dark_mode),
            value = darkModeViewData.items
                .getOrNull(darkModeViewData.selectedPosition)?.text.orEmpty(),
            items = darkModeViewData.items,
            selectedPosition = darkModeViewData.selectedPosition,
            processSameItemSelected = false,
        )

        val languageViewData = loadLanguageViewData()
        result += SettingsSpinnerViewData(
            block = SettingsBlock.Language,
            title = resourceRepo.getString(R.string.settings_language),
            value = languageViewData.currentLanguageName,
            items = languageViewData.items,
            selectedPosition = -1,
            processSameItemSelected = true,
        ).let(::SettingsSpinnerNotCheckableViewData)

        result += SettingsTextViewData(
            block = SettingsBlock.Categories,
            title = resourceRepo.getString(R.string.settings_edit_categories),
            subtitle = resourceRepo.getString(R.string.settings_edit_categories_hint),
        )

        result += SettingsTextViewData(
            block = SettingsBlock.Archive,
            title = resourceRepo.getString(R.string.settings_archive),
            subtitle = "",
            dividerIsVisible = false,
        )

        result += SettingsBottomViewData(
            block = SettingsBlock.MainBottom,
        )

        return result
    }

    private suspend fun loadDarkModeViewData(): DarkModeViewData {
        return prefsInteractor.getSelectedDarkMode()
            .let(settingsMapper::toDarkModeViewData)
    }

    private fun loadLanguageViewData(): LanguageViewData {
        return languageInteractor.getCurrentLanguage()
            .let(settingsMapper::toLanguageViewData)
    }
}