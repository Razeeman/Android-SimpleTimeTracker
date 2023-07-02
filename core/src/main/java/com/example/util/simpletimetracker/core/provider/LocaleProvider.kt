package com.example.util.simpletimetracker.core.provider

import android.os.Build
import java.util.Locale
import javax.inject.Inject

class LocaleProvider @Inject constructor(
    private val contextProvider: ContextProvider,
) {

    fun get(): Locale {
        val configuration = contextProvider.get().resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0] ?: Locale.getDefault()
        } else {
            configuration.locale
        }
    }
}