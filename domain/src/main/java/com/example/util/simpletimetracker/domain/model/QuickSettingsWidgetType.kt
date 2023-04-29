package com.example.util.simpletimetracker.domain.model

sealed interface QuickSettingsWidgetType {
    object AllowMultitasking: QuickSettingsWidgetType
    object ShowRecordTagSelection: QuickSettingsWidgetType
}