package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.domain.interactor.AppLanguage
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings_views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTranslatorViewData
import javax.inject.Inject

class SettingsTranslatorsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val languageInteractor: LanguageInteractor,
) {

    fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.TranslatorsTop,
        )

        result += SettingsTextViewData(
            block = SettingsBlock.TranslatorsTitle,
            title = resourceRepo.getString(R.string.settings_translators),
            subtitle = "",
            layoutIsClickable = false,
        )

        result += loadTranslatorsViewData()

        result += SettingsBottomViewData(
            block = SettingsBlock.TranslatorsBottom,
        )

        return result
    }

    private fun loadTranslatorsViewData(): List<SettingsTranslatorViewData> {
        val nonTranslatable = listOf(AppLanguage.System, AppLanguage.English)

        return LanguageInteractor.languageList
            .filter { it !in nonTranslatable }
            .map {
                SettingsTranslatorViewData(
                    translator = languageInteractor.getTranslators(it),
                    language = languageInteractor.getDisplayName(it),
                )
            }
    }
}