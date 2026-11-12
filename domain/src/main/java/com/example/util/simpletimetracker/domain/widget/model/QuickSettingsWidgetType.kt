package com.example.util.simpletimetracker.domain.widget.model

sealed interface QuickSettingsWidgetType {
    object AllowMultitasking : QuickSettingsWidgetType
    object ShowRecordTagSelection : QuickSettingsWidgetType
    object ShowTimerNotifications : QuickSettingsWidgetType
    object RetroactiveTracking : QuickSettingsWidgetType
    object ShowCommentInput : QuickSettingsWidgetType
}