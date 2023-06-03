package com.example.util.simpletimetracker.feature_settings.viewData

sealed interface SettingsUntrackedRangeViewData {

    object Disabled : SettingsUntrackedRangeViewData
    data class Enabled(
        val rangeStart: String,
        val rangeEnd: String,
    ) : SettingsUntrackedRangeViewData
}