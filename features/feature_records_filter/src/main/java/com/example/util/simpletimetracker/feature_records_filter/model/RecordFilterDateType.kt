package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

data class RecordFilterDateType(
    val rangeLength: RangeLength,
) : FilterViewData.Type
