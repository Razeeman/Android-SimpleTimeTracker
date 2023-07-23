package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

interface SettingsParent {

    fun openDateTimeDialog(
        tag: String,
        timestamp: Long,
        useMilitaryTime: Boolean,
    )

    suspend fun onUseMilitaryTimeClicked()
}