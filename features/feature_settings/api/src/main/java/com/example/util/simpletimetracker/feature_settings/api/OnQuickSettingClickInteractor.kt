package com.example.util.simpletimetracker.feature_settings.api

import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType

interface OnQuickSettingClickInteractor {
    suspend fun execute(type: QuickSettingsWidgetType)
}
