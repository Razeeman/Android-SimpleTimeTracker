package com.example.util.simpletimetracker.core.interactor

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AppLanguage
import java.util.Locale
import javax.inject.Inject

class LanguageInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) {
            getDisplayName(AppLanguage.System)
        } else {
            val locale = locales[0] ?: return ""
            locale.getDisplayLanguage(locale).capitalize(locale)
        }
    }

    fun getDisplayName(language: AppLanguage): String {
        return when (language) {
            is AppLanguage.System -> R.string.settings_dark_mode_system
            is AppLanguage.English -> R.string.settings_english_language
            is AppLanguage.Arabic -> R.string.settings_arabic_language
            is AppLanguage.Catalan -> R.string.settings_catalan_language
            is AppLanguage.German -> R.string.settings_german_language
            is AppLanguage.Spanish -> R.string.settings_spanish_language
            is AppLanguage.Farsi -> R.string.settings_farsi_language
            is AppLanguage.French -> R.string.settings_french_language
            is AppLanguage.Hindi -> R.string.settings_hindi_language
            is AppLanguage.Indonesian -> R.string.settings_indonesian_language
            is AppLanguage.Italian -> R.string.settings_italian_language
            is AppLanguage.Japanese -> R.string.settings_japanese_language
            is AppLanguage.Korean -> R.string.settings_korean_language
            is AppLanguage.Dutch -> R.string.settings_dutch_language
            is AppLanguage.Portuguese -> R.string.settings_portuguese_language
            is AppLanguage.PortuguesePortugal -> R.string.settings_portuguese_portugal_language
            is AppLanguage.Russian -> R.string.settings_russian_language
            is AppLanguage.Swedish -> R.string.settings_swedish_language
            is AppLanguage.Vietnamese -> R.string.settings_vietnamese_language
            is AppLanguage.Turkish -> R.string.settings_turkish_language
            is AppLanguage.Ukrainian -> R.string.settings_ukrainian_language
            is AppLanguage.ChineseSimplified -> R.string.settings_chinese_simplified_language
            is AppLanguage.ChineseTraditional -> R.string.settings_chinese_traditional_language
        }.let(resourceRepo::getString)
    }

    fun getTag(language: AppLanguage): String {
        return when (language) {
            is AppLanguage.System -> return ""
            is AppLanguage.English -> R.string.settings_english_tag
            is AppLanguage.Arabic -> R.string.settings_arabic_tag
            is AppLanguage.Catalan -> R.string.settings_catalan_tag
            is AppLanguage.German -> R.string.settings_german_tag
            is AppLanguage.Spanish -> R.string.settings_spanish_tag
            is AppLanguage.Farsi -> R.string.settings_farsi_tag
            is AppLanguage.French -> R.string.settings_french_tag
            is AppLanguage.Hindi -> R.string.settings_hindi_tag
            is AppLanguage.Indonesian -> R.string.settings_indonesian_tag
            is AppLanguage.Italian -> R.string.settings_italian_tag
            is AppLanguage.Japanese -> R.string.settings_japanese_tag
            is AppLanguage.Korean -> R.string.settings_korean_tag
            is AppLanguage.Dutch -> R.string.settings_dutch_tag
            is AppLanguage.Portuguese -> R.string.settings_portuguese_tag
            is AppLanguage.PortuguesePortugal -> R.string.settings_portuguese_portugal_tag
            is AppLanguage.Russian -> R.string.settings_russian_tag
            is AppLanguage.Swedish -> R.string.settings_swedish_tag
            is AppLanguage.Vietnamese -> R.string.settings_vietnamese_tag
            is AppLanguage.Turkish -> R.string.settings_turkish_tag
            is AppLanguage.Ukrainian -> R.string.settings_ukrainian_tag
            is AppLanguage.ChineseSimplified -> R.string.settings_chinese_simplified_tag
            is AppLanguage.ChineseTraditional -> R.string.settings_chinese_traditional_tag
        }.let(resourceRepo::getString)
    }

    fun getTranslators(language: AppLanguage): String {
        return when (language) {
            is AppLanguage.System,
            is AppLanguage.English,
            -> return ""

            is AppLanguage.Arabic -> R.string.settings_arabic_translators
            is AppLanguage.Catalan -> R.string.settings_catalan_translators
            is AppLanguage.German -> R.string.settings_german_translators
            is AppLanguage.Spanish -> R.string.settings_spanish_translators
            is AppLanguage.Farsi -> R.string.settings_farsi_translators
            is AppLanguage.French -> R.string.settings_french_translators
            is AppLanguage.Hindi -> R.string.settings_hindi_translators
            is AppLanguage.Indonesian -> R.string.settings_indonesian_translators
            is AppLanguage.Italian -> R.string.settings_italian_translators
            is AppLanguage.Japanese -> R.string.settings_japanese_translators
            is AppLanguage.Korean -> R.string.settings_korean_translators
            is AppLanguage.Dutch -> R.string.settings_dutch_translators
            is AppLanguage.Portuguese -> R.string.settings_portuguese_translators
            is AppLanguage.PortuguesePortugal -> R.string.settings_portuguese_portugal_translators
            is AppLanguage.Russian -> R.string.settings_russian_translators
            is AppLanguage.Swedish -> R.string.settings_swedish_translators
            is AppLanguage.Vietnamese -> R.string.settings_vietnamese_translators
            is AppLanguage.Turkish -> R.string.settings_turkish_translators
            is AppLanguage.Ukrainian -> R.string.settings_ukrainian_translators
            is AppLanguage.ChineseSimplified -> R.string.settings_chinese_simplified_translators
            is AppLanguage.ChineseTraditional -> R.string.settings_chinese_traditional_translators
        }.let(resourceRepo::getString)
    }

    fun setLanguage(languageTag: String) {
        val locale = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(locale)
    }

    private fun String.capitalize(locale: Locale): String {
        return replaceFirstChar { it.titlecase(locale) }
    }

    companion object {
        val languageList: List<AppLanguage> = listOf(
            AppLanguage.System,
            AppLanguage.English,
            AppLanguage.Arabic,
            AppLanguage.Catalan,
            AppLanguage.German,
            AppLanguage.Spanish,
            AppLanguage.Farsi,
            AppLanguage.French,
            AppLanguage.Hindi,
            AppLanguage.Indonesian,
            AppLanguage.Italian,
            AppLanguage.Japanese,
            AppLanguage.Korean,
            AppLanguage.Dutch,
            AppLanguage.Portuguese,
            AppLanguage.PortuguesePortugal,
            AppLanguage.Russian,
            AppLanguage.Swedish,
            AppLanguage.Vietnamese,
            AppLanguage.Turkish,
            AppLanguage.Ukrainian,
            AppLanguage.ChineseSimplified,
            AppLanguage.ChineseTraditional,
        )
    }
}