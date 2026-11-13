package com.example.util.simpletimetracker.domain.prefs.interactor

import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import javax.inject.Inject

class IsQuickSettingEnabledInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(type: QuickSettingsWidgetType): Boolean {
        return when (type) {
            QuickSettingsWidgetType.AllowMultitasking -> prefsInteractor.getAllowMultitasking()
            QuickSettingsWidgetType.ShowRecordTagSelection -> prefsInteractor.getShowRecordTagSelection()
            QuickSettingsWidgetType.ShowTimerNotifications -> prefsInteractor.getShowNotifications()
            QuickSettingsWidgetType.RetroactiveTracking -> prefsInteractor.getRetroactiveTrackingMode()
            QuickSettingsWidgetType.ShowCommentInput -> prefsInteractor.getShowCommentInput()
        }
    }
}
