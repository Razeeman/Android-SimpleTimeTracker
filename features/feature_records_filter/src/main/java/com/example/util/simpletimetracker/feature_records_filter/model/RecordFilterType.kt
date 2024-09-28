package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

sealed interface RecordFilterType : FilterViewData.Type {
    object Untracked : RecordFilterType
    object Multitask : RecordFilterType
    object Activity : RecordFilterType
    object Category : RecordFilterType
    object Comment : RecordFilterType
    object Date : RecordFilterType
    object SelectedTags : RecordFilterType
    object FilteredTags : RecordFilterType
    object ManuallyFiltered : RecordFilterType
    object DaysOfWeek : RecordFilterType
    object TimeOfDay : RecordFilterType
    object Duration : RecordFilterType
}