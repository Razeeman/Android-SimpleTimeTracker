package com.example.util.simpletimetracker.core.manager

import android.app.Activity
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ThemeManager @Inject constructor(
    private val prefsInteractor: PrefsInteractor
) {

    fun setTheme(activity: Activity) {
        val nightMode = runBlocking { prefsInteractor.getDarkMode() }

        if (nightMode) {
            activity.setTheme(R.style.AppThemeDark)
        } else {
            activity.setTheme(R.style.AppTheme)
        }
    }
}