package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.model

import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

data class CsvExportSettingsFilterType(
    val rangeLength: RangeLength,
) : FilterViewData.Type
