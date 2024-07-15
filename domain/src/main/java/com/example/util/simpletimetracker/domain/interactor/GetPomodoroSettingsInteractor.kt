package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.PomodoroCycleSettings
import javax.inject.Inject

class GetPomodoroSettingsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(): PomodoroCycleSettings {
        return PomodoroCycleSettings(
            focusTimeMs = prefsInteractor.getPomodoroFocusTime() * 1000L,
            breakTimeMs = prefsInteractor.getPomodoroBreakTime() * 1000L,
            longBreakTimeMs = prefsInteractor.getPomodoroLongBreakTime() * 1000L,
            periodsUntilLongBreak = prefsInteractor.getPomodoroPeriodsUntilLongBreak(),
        )
    }
}