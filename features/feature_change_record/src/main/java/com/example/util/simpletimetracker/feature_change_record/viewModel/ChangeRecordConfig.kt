package com.example.util.simpletimetracker.feature_change_record.viewModel

data class ChangeRecordConfig(
    val forceSecondsInDurationDialog: Boolean,
    val showTimeEndedOnSplitPreview: Boolean,
    val showTimeEndedOnAdjustPreview: Boolean,
    val adjustNextRecordAvailable: Boolean,
    val isTimeEndedAvailable: Boolean,
    val isAdditionalActionsAvailable: Boolean,
    val isDuplicateActionAvailable: Boolean,
    val isDeleteButtonVisible: Boolean,
    val isStatisticsButtonVisible: Boolean,
)