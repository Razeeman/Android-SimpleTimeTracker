package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.IsQuickSettingEnabledInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import javax.inject.Inject

class IsSettingShortcutEnabledInteractor @Inject constructor(
    private val isQuickSettingEnabledInteractor: IsQuickSettingEnabledInteractor,
) {

    suspend fun execute(
        shortcut: RecordShortcut.Target.Setting,
    ): Boolean {
        return when (val action = shortcut.action) {
            is RecordShortcut.SettingAction.Toggle -> {
                isQuickSettingEnabledInteractor.execute(action.type)
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