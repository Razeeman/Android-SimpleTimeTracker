package com.example.util.simpletimetracker.core.manager

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ThemeManager @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    fun setTheme(activity: ComponentActivity) {
        val nightMode = runBlocking { prefsInteractor.getDarkMode() }

        if (nightMode) {
            activity.setTheme(R.style.AppThemeDark)
        } else {
            activity.setTheme(R.style.AppTheme)
        }

        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { nightMode },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = DefaultLightScrim,
                darkScrim = DefaultDarkScrim,
                detectDarkMode = { nightMode },
            ),
        )
    }

    companion object {
        // Copy from enableEdgeToEdge() implementation.
        private val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        private val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}