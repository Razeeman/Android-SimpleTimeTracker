package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.CsvExportSettingsParams

interface CsvExportSettingsDialogListener {

    fun onCsvExportSettingsSelected(data: CsvExportSettingsParams)
}