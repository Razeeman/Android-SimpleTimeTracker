package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.CsvExportSettingsParams

interface CsvExportSettingsDialogListener {

    fun onCsvExportSettingsSelected(data: CsvExportSettingsParams)
}