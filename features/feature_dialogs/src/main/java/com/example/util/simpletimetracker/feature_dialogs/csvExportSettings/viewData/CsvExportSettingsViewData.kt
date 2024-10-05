package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class CsvExportSettingsViewData(
    val rangeStartString: String,
    val rangeEndString: String,
    @ColorInt val textColor: Int,
    val filters: List<ViewHolderType>,
)