package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import javax.inject.Inject

class IsSettingShortcutEnabledInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(
        shortcut: RecordShortcut.Target.Setting,
    ): Boolean {
        return when (shortcut.action) {
            RecordShortcut.SettingAction.Multitasking -> {
                prefsInteractor.getAllowMultitasking()
            }
            RecordShortcut.SettingAction.RetroactiveMode -> {
                prefsInteractor.getRetroactiveTrackingMode()
            }
            RecordShortcut.SettingAction.Categories,
            RecordShortcut.SettingAction.Archive,
            RecordShortcut.SettingAction.DataEdit,
            RecordShortcut.SettingAction.SortActivities,
            RecordShortcut.SettingAction.Shortcuts,
            RecordShortcut.SettingAction.Reminders,
            -> false
        }
    }
}