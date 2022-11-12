package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult

interface DataExportSettingsDialogListener {

    fun onDataExportSettingsSelected(data: DataExportSettingsResult)
}