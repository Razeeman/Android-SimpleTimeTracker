package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class StopPomodoroInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun checkAndStop(typeId: Long) {
        if (!prefsInteractor.getEnablePomodoroMode()) return

        val currentRunningRecords = runningRecordInteractor.getAll().map { it.id }
        val typesWithAutoStart = prefsInteractor.getAutostartPomodoroActivities()

        // If was auto started and when stopped.
        if (typeId in typesWithAutoStart &&
            currentRunningRecords.none { it in typesWithAutoStart }
        ) {
            prefsInteractor.setPomodoroModeStartedTimestampMs(0)
        }
    }
}