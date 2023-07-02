package com.example.util.simpletimetracker.core.interactor

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import java.util.Locale
import javax.inject.Inject

class LanguageInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) {
            resourceRepo.getString(R.string.settings_dark_mode_system)
        } else {
            val locale = locales[0] ?: return ""
            locale.getDisplayLanguage(locale).capitalize(locale)
        }
    }

    // TODO replace with string resources from translators to avoid confusion for chinese language.
    fun getDisplayName(tag: String): String {
        val locale = Locale.forLanguageTag(tag)
        return locale.getDisplayName(locale).capitalize(locale)
    }

    fun setLanguage(languageTag: String) {
        val locale = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(locale)
    }

    private fun String.capitalize(locale: Locale): String {
        return replaceFirstChar { it.titlecase(locale) }
    }
}