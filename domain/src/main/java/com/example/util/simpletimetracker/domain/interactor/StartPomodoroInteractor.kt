package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class StartPomodoroInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun checkAndStart(typeId: Long) {
        if (!prefsInteractor.getEnablePomodoroMode()) return

        if (prefsInteractor.getPomodoroModeStartedTimestampMs() == 0L &&
            typeId in prefsInteractor.getAutostartPomodoroActivities()
        ) {
            val current = System.currentTimeMillis()
            prefsInteractor.setPomodoroModeStartedTimestampMs(current)
        }
    }
}