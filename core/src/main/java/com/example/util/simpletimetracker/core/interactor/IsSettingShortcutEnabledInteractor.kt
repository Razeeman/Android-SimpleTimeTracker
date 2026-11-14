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
            is RecordShortcut.SettingAction.Toggle,
            -> isQuickSettingEnabledInteractor.execute(action.type)
            is RecordShortcut.SettingAction.Navigation,
            is RecordShortcut.SettingAction.SortActivities,
            -> false
        }
    }
}