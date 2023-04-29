package com.example.util.simpletimetracker.feature_data_edit.model

sealed interface DataEditDeleteRecordsState {

    object Disabled : DataEditDeleteRecordsState
    object Enabled : DataEditDeleteRecordsState
}