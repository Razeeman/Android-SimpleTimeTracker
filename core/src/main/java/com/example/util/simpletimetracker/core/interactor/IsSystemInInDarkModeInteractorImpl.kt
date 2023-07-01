package com.example.util.simpletimetracker.core.interactor

import android.content.Context
import android.content.res.Configuration
import com.example.util.simpletimetracker.domain.interactor.IsSystemInDarkModeInteractor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IsSystemInInDarkModeInteractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : IsSystemInDarkModeInteractor {

    override fun execute(): Boolean {
        val configuration = context.resources.configuration

        return when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }
}